package io.github.nini22p.webgaldemo

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
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

//        android R 以上全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                WindowInsets.Type.statusBars()
                        or WindowInsets.Type.navigationBars()
            )
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