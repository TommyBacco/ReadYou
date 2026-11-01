package me.ash.reader.ui.component.reader

import org.junit.Assert
import org.junit.Test

class VideoTagHunterTest {

    @Test
    fun testYoutubeEmbed() {
        val video = getVideo("https://www.youtube.com/embed/cjxnVO9RpaQ?feature=oembed")
        Assert.assertNotNull(video)
        Assert.assertEquals("https://www.youtube.com/embed/cjxnVO9RpaQ?feature=oembed", video!!.src)
        Assert.assertEquals(
            "http://img.youtube.com/vi/cjxnVO9RpaQ/hqdefault.jpg",
            video.imageUrl,
        )
        Assert.assertEquals("https://www.youtube.com/watch?v=cjxnVO9RpaQ", video.link)
    }

    @Test
    fun testYoutubeNoCookieEmbed() {
        val video = getVideo("https://www.youtube-nocookie.com/embed/cjxnVO9RpaQ")
        Assert.assertNotNull(video)
        Assert.assertEquals(
            "http://img.youtube.com/vi/cjxnVO9RpaQ/hqdefault.jpg",
            video!!.imageUrl,
        )
        Assert.assertEquals("https://www.youtube.com/watch?v=cjxnVO9RpaQ", video.link)
        Assert.assertTrue(video.isYoutubeEmbed())
    }

    @Test
    fun testInvidiousEmbed() {
        val video = getVideo("https://invidious.example/embed/cjxnVO9RpaQ?feature=oembed")
        Assert.assertNotNull(video)
        Assert.assertEquals(
            "https://invidious.example/vi/cjxnVO9RpaQ/hqdefault.jpg",
            video!!.imageUrl,
        )
        Assert.assertEquals("https://invidious.example/watch?v=cjxnVO9RpaQ", video.link)
        Assert.assertFalse(video.isYoutubeEmbed())
    }

    @Test
    fun testInvidiousWithPort() {
        val video = getVideo("http://192.168.1.10:3000/embed/cjxnVO9RpaQ")
        Assert.assertNotNull(video)
        Assert.assertEquals(
            "http://192.168.1.10:3000/vi/cjxnVO9RpaQ/hqdefault.jpg",
            video!!.imageUrl,
        )
        Assert.assertEquals("http://192.168.1.10:3000/watch?v=cjxnVO9RpaQ", video.link)
    }

    @Test
    fun testInvidiousOnCustomDomain() {
        val video = getVideo("https://tube.example.com/embed/cjxnVO9RpaQ")
        Assert.assertNotNull(video)
        Assert.assertEquals(
            "https://tube.example.com/vi/cjxnVO9RpaQ/hqdefault.jpg",
            video!!.imageUrl,
        )
        Assert.assertEquals("https://tube.example.com/watch?v=cjxnVO9RpaQ", video.link)
    }

    @Test
    fun testUnsupportedOrInvalidSources() {
        Assert.assertNull(getVideo(null))
        Assert.assertNull(getVideo(""))
        Assert.assertNull(getVideo("https://player.vimeo.com/video/123456789"))
        Assert.assertNull(getVideo("https://www.dailymotion.com/embed/video/x7tgad0"))
        Assert.assertNull(
            getVideo("https://open.spotify.com/embed/track/4uLU6hMCjMI75M1A2tKUQC")
        )
        Assert.assertNull(getVideo("https://example.com/not-a-video"))
    }
}
