package me.ash.reader.ui.component.webview

import me.ash.reader.ui.component.reader.Video
import me.ash.reader.ui.component.reader.getVideo
import me.ash.reader.ui.component.reader.isYoutubeEmbed
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object WebViewContent {

    private val VideoIdPattern = "(?:^|[?&])v=([A-Za-z0-9_-]{11})".toRegex()

    fun rewrite(html: String, baseUrl: String?): String {
        if (html.isBlank()) return html
        val document = Jsoup.parse(html, baseUrl ?: "")
        rewriteAudioPlayers(document)
        rewriteVideoPlayers(document)
        rewriteVideoIframes(document)
        return document.body().html()
    }

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

    private fun rewriteVideoPlayers(document: Document) {
        document.select("div[data-component=ilPostSocial], div[data-type]").forEach { element ->
            val video = element.toVideo() ?: return@forEach
            element.replaceWith(document.videoIframe(video))
        }
    }

    private fun rewriteVideoIframes(document: Document) {
        document.select("iframe").forEach { iframe ->
            if (getVideo(iframe.attr("abs:src")) != null) {
                iframe.removeAttr("sandbox")
                val allow = iframe.attr("allow")
                if (allow.isBlank()) {
                    iframe.attr("allow", "fullscreen")
                } else if (!allow.split(';').any { it.trim().equals("fullscreen", true) }) {
                    iframe.attr("allow", "$allow; fullscreen")
                }
            }
        }
    }

    private fun Element.toVideo(): Video? {
        val dataUrl = attr("data-url").takeIf { it.isNotBlank() }
        val thumbnail = attr("data-thumbnail").takeIf { it.isNotBlank() }
        getVideo(dataUrl)?.let { video ->
            return if (thumbnail != null && video.isYoutubeEmbed()) {
                video.copy(imageUrl = thumbnail)
            } else {
                video
            }
        }

        val videoId =
            VideoIdPattern.find(attr("data-params"))?.groupValues?.get(1)
                ?: VideoIdPattern.find(dataUrl.orEmpty())?.groupValues?.get(1)
                ?: return null
        return Video(
            src = dataUrl ?: "https://www.youtube.com/embed/$videoId",
            imageUrl = thumbnail ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
            link = "https://www.youtube.com/watch?v=$videoId",
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
