package io.github.nini22p.webgal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewClientCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var audioManager: AudioManager? = null
    private var saveLoadPath: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_REQUEST_CODE = 0
    private val FILECREATE_REQUEST_CODE = 1
    private var saveData: String? = null

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClientCompat() {
            //从本地加载游戏
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        webView.loadUrl("https://appassets.androidplatform.net/assets/webgal/index.html")

        //android R 以上全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                WindowInsets.Type.statusBars()
                        or WindowInsets.Type.navigationBars()
            )
        }

        //导出存档与选项
        webView.addJavascriptInterface(SaveInterface(), "Save")
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val script = "javascript: (() => {" +
                        "async function getBase64StringFromBlobUrl() {" +
                        "const xhr = new XMLHttpRequest();" +
                        "xhr.open('GET', '${url}', true);" +
                        "xhr.responseType = 'blob';" +
                        "xhr.onload = () => {" +
                        "if (xhr.status === 200) {" +
                        "const blobResponse = xhr.response;" +
                        "const fileReaderInstance = new FileReader();" +
                        "fileReaderInstance.readAsDataURL(blobResponse);" +
                        "fileReaderInstance.onloadend = () => {" +
                        "const base64data = fileReaderInstance.result;" +
                        "Save.getBase64Data(base64data);" +
                        "}" +
                        "}" +
                        "};" +
                        "xhr.send();" +
                        "}" +
                        "getBase64StringFromBlobUrl()" +
                        "}) ()"

                webView.evaluateJavascript(script, null)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "导出失败", Toast.LENGTH_LONG).show()
            }
        }

        //导入存档与选项
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                saveLoadPath = filePathCallback
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                startActivityForResult(intent, FILECHOOSER_REQUEST_CODE)
                return true
            }
        }
    }

    inner class SaveInterface {
        //存档解码
        @JavascriptInterface
        fun getBase64Data(string: String) {
            val byte = Base64.getDecoder().decode(string.replace("data:application/json;base64,",""))
            saveData = String(byte, charset("UTF-8"))
            createSave()
        }
    }

    //打开saf保存界面
    fun createSave() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "save.json")
        }
        startActivityForResult(intent, FILECREATE_REQUEST_CODE)
    }

    //导出存档
    private fun saveFile(intent: Intent?) {
        try {
            val uri = intent?.data ?: return
            val outputStream = contentResolver.openOutputStream(uri)
            outputStream?.use {
                it.write(saveData?.toByteArray())
                it.flush()
                it.close()
            }
            Toast.makeText(applicationContext, "导出成功", Toast.LENGTH_LONG).show()
        }catch(e:Exception) {
            Toast.makeText(applicationContext, "导出失败", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            intent ?: return
            if (requestCode == FILECREATE_REQUEST_CODE) saveFile(intent)
            if (requestCode == FILECHOOSER_REQUEST_CODE) {
                saveLoadPath!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(resultCode, intent)
                )
                saveLoadPath = null
            }
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == FILECREATE_REQUEST_CODE) return
            if (requestCode == FILECHOOSER_REQUEST_CODE) {
                saveLoadPath!!.onReceiveValue(null)
                saveLoadPath = null
                return
            }
        } else return
    }

    //游戏后台暂停
    override fun onPause() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager?.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        webView.run {
            pauseTimers()
            onPause()
        }
        super.onPause()
    }

    //游戏从后台恢复
    override fun onResume() {
        audioManager?.abandonAudioFocus(null)
        webView.run {
            resumeTimers()
            onResume()
        }
        super.onResume()
    }
}

