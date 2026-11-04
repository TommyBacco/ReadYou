package me.ash.reader.ui.component.webview

import me.ash.reader.ui.component.reader.Video
import me.ash.reader.ui.component.reader.getVideo
import me.ash.reader.ui.component.reader.isYoutubeEmbed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object WebViewContent {

    private val VideoIdPattern = "(?:^|[?&])v=([A-Za-z0-9_-]{11})".toRegex()
    private val InstanceLinkPattern =
        "https?://([a-zA-Z0-9.-]+(?::\\d+)?)/(?:watch\\?v=|embed/)([A-Za-z0-9_-]{11})".toRegex()
    private val YoutubeIdPattern = "youtube.com/embed/([A-Za-z0-9_-]{11})".toRegex()
    private val youtubeHosts =
        setOf(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "youtube-nocookie.com",
            "www.youtube-nocookie.com",
            "youtu.be",
        )

    fun rewrite(html: String, baseUrl: String?, instanceHost: String? = null): String {
        if (html.isBlank()) return html
        val document = Jsoup.parse(html, baseUrl ?: "")
        val effectiveInstanceHost = instanceHost ?: detectInstanceHost(document.body().html())
        rewriteAudioPlayers(document)
        rewriteVideoPlayers(document, effectiveInstanceHost)
        rewriteVideoIframes(document, effectiveInstanceHost)
        return document.body().html()
    }

    fun detectInstanceHost(html: String): String? =
        InstanceLinkPattern
            .findAll(html)
            .map { match -> match.groupValues[1] }
            .firstOrNull { it !in youtubeHosts }

    private fun rewriteAudioPlayers(document: Document) {
        document.select("div#audioPlayerArticle").forEach { element ->
            val src = element.attr("data-mp3")
            if (src.isBlank()) {
                element.remove()
            } else {
                val audio =
                    document
                        .createElement("audio")
                        .attr("controls", "controls")
                        .attr("preload", "none")
                        .attr("src", src)
                        .attr("style", "width: 100%; margin: 16px 0;")
                element.replaceWith(audio)
            }
        }
    }

    private fun rewriteVideoPlayers(document: Document, instanceHost: String?) {
        document.select("div[data-component=ilPostSocial], div[data-type]").forEach { element ->
            val video = element.toVideo(instanceHost) ?: return@forEach
            element.replaceWith(document.videoIframe(video))
        }
    }

    private fun rewriteVideoIframes(document: Document, instanceHost: String?) {
        document.select("iframe").forEach { iframe ->
            val video = getVideo(iframe.attr("abs:src")) ?: return@forEach
            onInstance(video, instanceHost).takeIf { it != video }?.let { rewritten ->
                iframe.attr("src", rewritten.src)
            }
            iframe.run {
                removeAttr("sandbox")
                val allow = iframe.attr("allow")
                if (allow.isBlank()) {
                    iframe.attr("allow", "fullscreen")
                } else if (!allow.split(';').any { it.trim().equals("fullscreen", true) }) {
                    iframe.attr("allow", "$allow; fullscreen")
                }
            }
        }
    }

    private fun Element.toVideo(instanceHost: String?): Video? {
        val dataUrl = attr("data-url").takeIf { it.isNotBlank() }
        val thumbnail = attr("data-thumbnail").takeIf { it.isNotBlank() }
        val video =
            getVideo(dataUrl)?.let { detected ->
                if (thumbnail != null && detected.isYoutubeEmbed()) {
                    detected.copy(imageUrl = thumbnail)
                } else {
                    detected
                }
            }
                ?: VideoIdPattern.find(attr("data-params"))?.groupValues?.get(1)?.let { videoId ->
                    Video(
                        src = dataUrl ?: "https://www.youtube.com/embed/$videoId",
                        imageUrl = thumbnail ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                        link = "https://www.youtube.com/watch?v=$videoId",
                    )
                }
                ?: return null
        return onInstance(video, instanceHost)
    }

    private fun onInstance(video: Video, instanceHost: String?): Video {
        if (instanceHost == null) return video
        val videoId = YoutubeIdPattern.find(video.src)?.groupValues?.get(1) ?: return video
        return video.copy(
            src = "https://$instanceHost/embed/$videoId",
            imageUrl = "https://$instanceHost/vi/$videoId/hqdefault.jpg",
            link = "https://$instanceHost/watch?v=$videoId",
        )
    }

    private fun Document.videoIframe(video: Video): Element =
        createElement("iframe")
            .attr("src", video.src)
            .attr("allowfullscreen", "allowfullscreen")
            .attr("allow", "fullscreen; autoplay; encrypted-media; picture-in-picture")
            .attr(
                "style",
                "width: 100%; aspect-ratio: 16 / 9; border: 0; margin: 16px 0; " +
                    "border-radius: var(--img-border-radius);",
            )
}
