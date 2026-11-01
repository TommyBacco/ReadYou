package me.ash.reader.ui.component.webview

import java.net.URI
import me.ash.reader.ui.component.reader.Video
import me.ash.reader.ui.ext.formatUrl

object WebViewHtml {

    fun resolveBaseUrl(url: String?): String {
        val normalized = url?.trim()?.takeIf { it.isNotBlank() }?.formatUrl() ?: return ""
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return ""
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> normalized.takeIf { uri.host != null } ?: ""
            "file" -> normalized
            else -> ""
        }
    }

    fun escapeAttribute(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    fun videoFallback(video: Video, playLabel: String): String {
        val link = escapeAttribute(video.link)
        val image = escapeAttribute(video.imageUrl)
        val label = escapeAttribute(playLabel)
        return """
<!DOCTYPE html>
<html dir="auto">
<head>
    <meta name="viewport" content="initial-scale=1, user-scalable=no" />
    <style type="text/css">
        html, body { margin: 0; padding: 0; height: 100%; background: #000; }
        a { position: relative; display: block; width: 100%; height: 100%; }
        img { display: block; width: 100%; height: 100%; object-fit: cover; }
        .play {
            position: absolute; top: 50%; left: 50%;
            width: 64px; height: 64px; margin: -32px 0 0 -32px;
            border-radius: 50%; background: rgba(0, 0, 0, .45);
            color: #fff; font: 36px/64px sans-serif; text-align: center;
        }
    </style>
</head>
<body>
    <a href="$link" target="_top" title="$label">
        <img src="$image" alt="$label" />
        <span class="play">&#9654;</span>
    </a>
</body>
</html>
""".trimIndent()
    }

    const val HTML: String = """
<!DOCTYPE html>
<html dir="auto">
<head>
    <meta name="viewport" content="initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no, width=device-width, viewport-fit=cover" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type"/>
    <style type="text/css">
        %s
    </style>
    <base href="%s" />
</head>
<body>
<main>
    <article>
        %s
    </article>
</main>
<script>
%s
</script>
</body>
</html>
"""
}
