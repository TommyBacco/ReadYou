package me.ash.reader.ui.component.webview

import android.content.Context
import android.net.http.SslError
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import me.ash.reader.R
import me.ash.reader.ui.component.reader.getVideo
import me.ash.reader.ui.component.reader.isYoutubeEmbed
import me.ash.reader.ui.ext.isUrl
import java.io.DataInputStream
import java.net.HttpURLConnection
import java.net.URI

const val INJECTION_TOKEN = "/android_asset_font/"

class WebViewClient(
    private val context: Context,
    private val refererDomain: String?,
    private val userAgent: String? = null,
    private val onOpenLink: (url: String) -> Unit,
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? {
        val url = request?.url?.toString()
        if (url != null && request?.isForMainFrame == false) {
            val video = getVideo(url)
            if (video != null && !video.isYoutubeEmbed()) {
                fetchEmbed(url)?.let { return it }
                return WebResourceResponse(
                    "text/html",
                    "UTF-8",
                    WebViewHtml.videoFallback(
                            video,
                            context.getString(R.string.touch_to_play_video),
                        )
                        .byteInputStream(),
                )
            }
        }
        if (url != null && url.contains(INJECTION_TOKEN)) {
            try {
                val assetPath = url.substring(
                    url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length,
                    url.length
                )
                return WebResourceResponse(
                    "text/HTML",
                    "UTF-8",
                    context.assets.open(assetPath)
                )
            } catch (e: Exception) {
                Log.e("RLog", "WebView shouldInterceptRequest: $e")
            }
        } else if (url != null && url.isUrl()) {
            try {
                var connection = URI.create(url).toURL().openConnection() as HttpURLConnection
                if (connection.responseCode == 403) {
                    connection.disconnect()
                    connection = URI.create(url).toURL().openConnection() as HttpURLConnection
                    connection.setRequestProperty("Referer", refererDomain)
                    val inputStream = DataInputStream(connection.inputStream)
                    return WebResourceResponse(connection.contentType, "UTF-8", inputStream)
                }
            } catch (e: Exception) {
                Log.e("RLog", "shouldInterceptRequest url: $e")
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view!!.evaluateJavascript(OnImgClickScript, null)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (null == request?.url) return false
        val url = request.url.toString()
        if (url.isNotEmpty()) onOpenLink(url)
        return true
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        Log.e("RLog", "RYWebView onReceivedError: $error")
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        handler?.cancel()
    }

    private fun fetchEmbed(url: String): WebResourceResponse? = try {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 10000
        userAgent?.let { connection.setRequestProperty("User-Agent", it) }
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            null
        } else {
            val contentType = connection.contentType.orEmpty()
            val mimeType =
                contentType.substringBefore(';').trim().takeIf { it.isNotEmpty() } ?: "text/html"
            val charset =
                Regex("charset=([^;\\s]+)", RegexOption.IGNORE_CASE)
                    .find(contentType)
                    ?.groupValues
                    ?.get(1) ?: "UTF-8"
            WebResourceResponse(mimeType, charset, connection.inputStream)
        }
    } catch (e: Exception) {
        Log.e("RLog", "fetchEmbed: $e")
        null
    }

    companion object {
        private const val OnImgClickScript = """
            javascript:(function() {
                var imgs = document.getElementsByTagName("img");
                for(var i = 0; i < imgs.length; i++){
                    imgs[i].pos = i;
                    imgs[i].onclick = function(event) {
                        event.preventDefault();
                        window.${JavaScriptInterface.NAME}.onImgTagClick(this.src, this.alt);
                    }
                }
            })()
            """
    }
}
