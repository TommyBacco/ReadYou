package me.ash.reader.ui.component.webview

import org.junit.Assert
import org.junit.Test

class WebViewContentTest {

    @Test
    fun testIlPostYoutubePlayer() {
        val html =
            """
            <p>Before</p>
            <div class="ilPostSocial"
                 data-component="ilPostSocial"
                 data-type="youtube"
                 data-url="https://www.youtube.com/embed/cjxnVO9RpaQ?feature=oembed"
                 data-params="v=cjxnVO9RpaQ"
                 data-thumbnail="https://i.ytimg.com/vi/cjxnVO9RpaQ/hqdefault.jpg"></div>
            <p>After</p>
            """
        val rewritten = WebViewContent.rewrite(html, "https://www.ilpost.it/article/")
        Assert.assertTrue(rewritten.contains("<iframe"))
        Assert.assertTrue(rewritten.contains("https://www.youtube.com/embed/cjxnVO9RpaQ"))
        Assert.assertTrue(rewritten.contains("allowfullscreen"))
        Assert.assertTrue(rewritten.contains("Before"))
        Assert.assertTrue(rewritten.contains("After"))
    }

    @Test
    fun testIlPostInvidiousPlayer() {
        val html =
            """
            <div class="ilPostSocial"
                 data-type="youtube"
                 data-url="https://invidious.example/embed/cjxnVO9RpaQ"
                 data-params="v=cjxnVO9RpaQ"
                 data-thumbnail="https://i.ytimg.com/vi/cjxnVO9RpaQ/hqdefault.jpg"></div>
            """
        val rewritten = WebViewContent.rewrite(html, "https://www.ilpost.it/article/")
        Assert.assertTrue(rewritten.contains("https://invidious.example/embed/cjxnVO9RpaQ"))
    }

    @Test
    fun testAudioPlayer() {
        val html =
            """
            <div id="audioPlayerArticle"
                 data-mp3="https://audio.example/file.mp3"
                 data-id="123">Caricamento player</div>
            <p>Content</p>
            """
        val rewritten = WebViewContent.rewrite(html, "https://www.ilpost.it/article/")
        Assert.assertTrue(rewritten.contains("<audio"))
        Assert.assertTrue(rewritten.contains("https://audio.example/file.mp3"))
        Assert.assertFalse(rewritten.contains("Caricamento player"))
    }

    @Test
    fun testUnrelatedDivIsUntouched() {
        val html = """<div class="note">Hello</div>"""
        val rewritten = WebViewContent.rewrite(html, "https://www.ilpost.it/article/")
        Assert.assertTrue(rewritten.contains("Hello"))
    }

    @Test
    fun testSandboxAttrRemovedForVideoIframes() {
        val html =
            """
            <iframe src="https://invidious.example.com/embed/cjxnVO9RpaQ?si=abc"
                    sandbox="allow-scripts allow-same-origin"
                    allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"></iframe>
            <iframe src="https://www.youtube.com/embed/cjxnVO9RpaQ"></iframe>
            <iframe src="https://example.com/player" sandbox="allow-scripts"></iframe>
            """
        val rewritten = WebViewContent.rewrite(html, "https://www.example.com/article/")
        val document = org.jsoup.Jsoup.parse(rewritten)
        val iframes = document.select("iframe")
        Assert.assertEquals(3, iframes.size)
        Assert.assertFalse(iframes[0].hasAttr("sandbox"))
        Assert.assertEquals(
            "accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen",
            iframes[0].attr("allow"),
        )
        Assert.assertEquals("fullscreen", iframes[1].attr("allow"))
        Assert.assertTrue(iframes[2].hasAttr("sandbox"))
        Assert.assertFalse(iframes[2].hasAttr("allow"))
    }
}
