package io.github.nini22p.webgaldemo

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.WindowInsets
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewClientCompat


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var audioManager: AudioManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClientCompat() {
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
        webView.setDownloadListener { _, url, _, _, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setTitle(R.string.app_name.toString())
                request.setDescription("导出存档与选项")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, R.string.app_name.toString() + "-save.json"
                )
                val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(applicationContext, "导出成功", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "导出失败", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onPause() {

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager?.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)

        webView.run {
            pauseTimers()
            onPause()
        }

        super.onPause()
    }

    override fun onResume() {

        audioManager?.abandonAudioFocus(null)

        webView.run {
            resumeTimers()
            onResume()
        }

        super.onResume()
    }

}