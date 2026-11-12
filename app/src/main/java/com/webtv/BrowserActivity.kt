package com.webtv

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.MotionEvent
import android.os.SystemClock
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import java.util.HashMap

class BrowserActivity : Activity() {

    companion object {
        fun launch(activity: Activity, url: String, referer: String? = null, userAgent: String? = null, isEvent: Boolean = false) {
            activity.startActivity(Intent(activity, BrowserActivity::class.java).apply {
                putExtra("URL", url)
                putExtra("REFERER", referer)
                putExtra("USER_AGENT", userAgent)
                putExtra("IS_EVENT", isEvent)
            })
        }
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingBanner: TextView
    private var bannerVisible = false
    private val uiHandler = Handler(Looper.getMainLooper())
    private var startedAt = 0L
    private var bannerDismissed = false
    private var urlLoaded = ""
    private var isEvent = false
    inner class VideoBridge {
        @JavascriptInterface
        fun onVideoPlaying() {
            uiHandler.post { dismissBannerEarly() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        startedAt = SystemClock.uptimeMillis()

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        loadingBanner = findViewById(R.id.loadingBanner)

        isEvent = intent.getBooleanExtra("IS_EVENT", false)
        webView.addJavascriptInterface(VideoBridge(), "AndroidVideoBridge")
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        showBanner()
        setupWebView()
        loadUrl(intent)
    }

    private fun simulateUserTap() {
        try {
            val x = webView.width / 2f
            val y = webView.height / 2f
            val downTime = SystemClock.uptimeMillis()
            val eventDown = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
            val eventUp = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0)
            webView.dispatchTouchEvent(eventDown)
            webView.dispatchTouchEvent(eventUp)
            eventDown.recycle()
            eventUp.recycle()
        } catch (_: Exception) {}
    }

    private fun showBanner() {
        if (bannerVisible) return
        bannerVisible = true
        bannerDismissed = false
        loadingBanner.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        loadingBanner.text = if (isEvent) {
            "MC TV\n\nPreparando evento\n\nMC Producciones\nWhatsApp: 0983.868.174"
        } else {
            "MC TV\n\nMejorando tu experiencia de ver tv\n\nMC Producciones\nWhatsApp: 0983.868.174"
        }
    }

    private fun dismissBannerEarly() {
        if (bannerDismissed || !bannerVisible) return
        val elapsed = SystemClock.uptimeMillis() - startedAt
        if (elapsed >= 3000) hideBanner()
        else uiHandler.postDelayed({ hideBanner() }, 3000 - elapsed)
    }

    private fun hideBanner() {
        if (bannerDismissed) return
        bannerDismissed = true
        bannerVisible = false
        loadingBanner.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startedAt = SystemClock.uptimeMillis()
        bannerDismissed = false
        isEvent = intent.getBooleanExtra("IS_EVENT", false)
        webView.visibility = View.VISIBLE
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
        showBanner()
        loadUrl(intent)
    }

    private fun loadUrl(intent: Intent) {
        val url = intent.getStringExtra("URL") ?: return
        val referer = intent.getStringExtra("REFERER")
        val userAgent = intent.getStringExtra("USER_AGENT")
        urlLoaded = url

        if (userAgent != null) {
            webView.settings.userAgentString = userAgent
        }

        if (url.contains(".m3u8", ignoreCase = true)) {
            webView.loadDataWithBaseURL(url, hlsPage(url), "text/html", "UTF-8", null)
        } else if (referer != null) {
            webView.loadUrl(url, HashMap<String, String>().apply { put("Referer", referer) })
        } else {
            webView.loadUrl(url)
        }
    }

    private fun hlsPage(url: String): String = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
        <script src="https://cdn.jsdelivr.net/npm/hls.js@latest"></script>
        <style>
        *{margin:0;padding:0}
        body{background:#000;width:100vw;height:100vh}
        video{width:100%;height:100%}
        </style>
        </head>
        <body>
        <video id="v" controls playsinline></video>
        <script>
        (function(){
            var v=document.getElementById('v');
            v.addEventListener('playing',function(){AndroidVideoBridge.onVideoPlaying()});
            if(Hls.isSupported()){
                var h=new Hls();h.loadSource('$url');h.attachMedia(v);
                h.on(Hls.Events.MANIFEST_PARSED,function(){v.play()});
            } else if(v.canPlayType('application/vnd.apple.mpegurl')){
                v.src='$url';v.play();
            }
        })();
        </script>
        </body>
        </html>
    """.trimIndent()

    override fun onPause() {
        super.onPause(); webView.onPause()
    }

    override fun onResume() {
        super.onResume(); webView.onResume()
    }

    override fun onDestroy() {
        uiHandler.removeCallbacksAndMessages(null)
        // Limpieza profunda del WebView para evitar memory leaks
        webView.removeJavascriptInterface("AndroidVideoBridge")
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.clearCache(true)
        webView.onPause()
        (webView.parent as? android.view.ViewGroup)?.removeView(webView)
        webView.removeAllViews()
        webView.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun triggerTelefuturoPlayback() {
        // Solo Telefuturo: Video.js + IMA ads requieren unmute via API
        webView.evaluateJavascript("""
            (function(){
                try {
                    var p = videojs('my_video_1');
                    if(p) {
                        try { p.ima.initializeAdDisplayContainer(); } catch(e){}
                        p.muted(false);
                        if(p.paused()) p.play();
                    }
                } catch(e){}
            })();
        """.trimIndent(), null)
        uiHandler.postDelayed({ simulateUserTap() }, 1500)
        uiHandler.postDelayed({
            webView.evaluateJavascript("""
                (function(){
                    try {
                        var p = videojs('my_video_1');
                        if(p) {
                            p.muted(false);
                            if(p.paused()) p.play();
                        }
                    } catch(e){}
                })();
            """.trimIndent(), null)
        }, 3500)
    }

    private fun triggerDailymotionPlayback() {
        uiHandler.postDelayed({
            webView.evaluateJavascript("""
                (function(){
                    try {
                        var v = document.querySelector('video');
                        if (v) {
                            v.muted = true;
                            v.autoplay = true;
                            v.playsInline = true;
                            v.play();
                        }
                    } catch(e){}
                })();
            """.trimIndent(), null)
            simulateUserTap()
        }, 2500)
    }

    private fun setupWebView() {
        webView.apply {
            // Forzar capa hardware para mejorar video en Android 5-9 (en 10+ se hereda del manifest)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = false
                allowContentAccess = false
                mediaPlaybackRequiresUserGesture = false
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    showBanner()
                }
                override fun onPageFinished(view: WebView, url: String) {
                    val u = url.lowercase()
                    when {
                        u.contains("telefuturo") -> {
                            triggerTelefuturoPlayback()
                            uiHandler.postDelayed({ hideBanner() }, 3500)
                        }
                        u.contains("dailymotion") -> {
                            triggerDailymotionPlayback()
                            uiHandler.postDelayed({ hideBanner() }, 3500)
                        }
                        u.contains("desdepylabs") || u.contains("desdeparaguay") || u.contains("gentv.") -> {
                            // Sin inyeccion JS - la pagina maneja autoplay sola
                            uiHandler.postDelayed({ hideBanner() }, 3500)
                        }
                        u.endsWith(".m3u8") || urlLoaded.endsWith(".m3u8") -> {
                            uiHandler.postDelayed({ hideBanner() }, 5000)
                        }
                        else -> {
                            uiHandler.postDelayed({ hideBanner() }, 4000)
                        }
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progressBar.progress = newProgress
                }

                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    super.onShowCustomView(view, callback)
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                }
            }
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    finish()
                    true
                } else false
            }
        }
    }

}
