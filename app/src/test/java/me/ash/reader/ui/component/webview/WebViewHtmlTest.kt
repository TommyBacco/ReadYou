package me.ash.reader.ui.component.webview

import me.ash.reader.ui.component.reader.Video
import org.junit.Assert
import org.junit.Test

class WebViewHtmlTest {

    @Test
    fun testResolveBaseUrl() {
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl(null))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl(""))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("   "))
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("https://example.com/article"),
        )
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("//example.com/article"),
        )
        Assert.assertEquals(
            "https://example.com/article",
            WebViewHtml.resolveBaseUrl("example.com/article"),
        )
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("https://example.com/a b"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("javascript:alert(1)"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("file:///sdcard/article.html"))
        Assert.assertEquals("", WebViewHtml.resolveBaseUrl("file:/etc/passwd"))
    }

    @Test
    fun testEscapeAttribute() {
        Assert.assertEquals(
            "https://example.com/?a=1&amp;b=&quot;2&quot;",
            WebViewHtml.escapeAttribute("https://example.com/?a=1&b=\"2\""),
        )
        Assert.assertEquals(
            "&lt;script&gt;",
            WebViewHtml.escapeAttribute("<script>"),
        )
    }

    @Test
    fun testVideoFallback() {
        val html =
            WebViewHtml.videoFallback(
                video =
                    Video(
                        src = "https://invidious.example/embed/cjxnVO9RpaQ",
                        imageUrl = "https://invidious.example/vi/cjxnVO9RpaQ/hqdefault.jpg",
                        link = "https://invidious.example/watch?v=cjxnVO9RpaQ",
                    ),
                playLabel = "Touch to play video",
            )
        Assert.assertTrue(html.contains("href=\"https://invidious.example/watch?v=cjxnVO9RpaQ\""))
        Assert.assertTrue(html.contains("target=\"_top\""))
        Assert.assertTrue(
            html.contains("src=\"https://invidious.example/vi/cjxnVO9RpaQ/hqdefault.jpg\"")
        )
        Assert.assertTrue(html.contains("Touch to play video"))
    }
}
