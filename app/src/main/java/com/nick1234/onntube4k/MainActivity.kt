package com.nick1234.onntube4k
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val executor = Executors.newSingleThreadExecutor()
    @Volatile private var playerOpen = false
    private val youtubeApiKey: String by lazy {
        String(android.util.Base64.decode(BuildConfig.YOUTUBE_API_KEY_B64, android.util.Base64.DEFAULT), Charsets.UTF_8)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.setBackgroundColor(android.graphics.Color.rgb(15, 15, 15))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = false
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(TubeBridge(), "AndroidTube")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class TubeBridge {
        @JavascriptInterface
        fun search(query: String) {
            if (youtubeApiKey.isBlank()) {
                sendError("Add your YOUTUBE_API_KEY in GitHub Actions Secrets before building.")
                return
            }
            executor.execute {
                try {
                    val q = URLEncoder.encode(query, "UTF-8")
                    val requestUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=20&q=" + q + "&key=" + URLEncoder.encode(youtubeApiKey, "UTF-8")
                    val connection = URL(requestUrl).openConnection() as HttpURLConnection
                    connection.connectTimeout = 12000
                    connection.readTimeout = 15000
                    connection.requestMethod = "GET"
                    val code = connection.responseCode
                    val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                    val body = stream.bufferedReader().use { it.readText() }
                    connection.disconnect()
                    if (code !in 200..299) throw IllegalStateException("YouTube API error (" + code + "): " + JSONObject(body).optJSONObject("error")?.optString("message", "Request failed"))
                    val items = JSONObject(body).optJSONArray("items") ?: JSONArray()
                    val videos = JSONArray()
                    for (i in 0 until items.length()) {
                        val item = items.optJSONObject(i) ?: continue
                        val id = item.optJSONObject("id")?.optString("videoId").orEmpty()
                        if (id.isBlank()) continue
                        val snippet = item.optJSONObject("snippet") ?: JSONObject()
                        val thumbs = snippet.optJSONObject("thumbnails") ?: JSONObject()
                        val image = thumbs.optJSONObject("high")?.optString("url")
                            ?: thumbs.optJSONObject("medium")?.optString("url")
                            ?: thumbs.optJSONObject("default")?.optString("url").orEmpty()
                        videos.put(JSONObject().put("id", id).put("title", snippet.optString("title"))
                            .put("channel", snippet.optString("channelTitle")).put("thumbnail", image))
                    }
                    runOnUiThread { web.evaluateJavascript("showResults(" + JSONObject.quote(videos.toString()) + ")", null) }
                } catch (e: Exception) {
                    sendError(e.message ?: "Search failed. Check your connection and API settings.")
                }
            }
        }
        @JavascriptInterface
        fun setPlayerOpen(open: Boolean) { playerOpen = open }
    }

    private fun sendError(message: String) {
        runOnUiThread { web.evaluateJavascript("showError(" + JSONObject.quote(message) + ")", null) }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (playerOpen) {
            web.evaluateJavascript("closePlayer()", null)
            playerOpen = false
        } else super.onBackPressed()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        web.destroy()
        super.onDestroy()
    }
}
