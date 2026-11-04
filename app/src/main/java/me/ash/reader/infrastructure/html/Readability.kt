package me.ash.reader.infrastructure.html

import android.util.Log
import net.dankito.readability4j.extended.Readability4JExtended
import net.dankito.readability4j.extended.processor.PostprocessorExtended
import net.dankito.readability4j.extended.util.RegExUtilExtended
import net.dankito.readability4j.model.ReadabilityOptions
import net.dankito.readability4j.processor.MetadataParser
import net.dankito.readability4j.processor.Preprocessor
import org.jsoup.nodes.Element

object Readability {

    private val captionShortcodeRegex = Regex("""\[[^\]]*caption[^\]]*\][\s\S]*?\[/caption\]""")
    private val figcaptionRegex = Regex("""<figcaption[^>]*>[\s\S]*?</figcaption>""")
    private val audioPlayerRegex = Regex("""<div[^>]*id="audioPlayerArticle"[^>]*>[\s\S]*?</div>""")
    private val leadingWrapperRegex = Regex("""^\s*(?:<(?:article|main|section|header|div)\b[^>]*>\s*)*""")
    private val leadingHeadingRegex = Regex("""(?:<h[123]\b[^>]*>[\s\S]*?</h[123]>\s*)+""")
    private val headingRegex = Regex("""<h([123])\b[^>]*>([\s\S]*?)</h[123]>""")
    private val documentPrefixRegex = Regex("""^[\s\S]*?<body\b[^>]*>\s*""")

    fun parseToText(htmlContent: String?, uri: String?): String {
        htmlContent ?: return ""
        return try {
            val (withoutHeading, headingText) =
                extractLeadingHeading(
                    audioPlayerRegex.replace(
                        figcaptionRegex.replace(captionShortcodeRegex.replace(htmlContent, ""), ""),
                        "",
                    ),
                )
            val text =
                Readability4JExtended(uri, withoutHeading).parse().textContent?.trim() ?: ""
            if (headingText.isEmpty()) text else "$headingText $text".trim()
        } catch (e: Exception) {
            Log.e("RLog", "Readability.parseToText '$uri' is error: ", e)
            ""
        }
    }

    fun parseToElement(htmlContent: String?, uri: String?): Element? {
        htmlContent ?: return null
        return Readability4JExtended(uri, htmlContent).parse().articleContent
    }

    private fun extractLeadingHeading(document: String): Pair<String, String> {
        val html = if (document.contains("<body")) documentPrefixRegex.replaceFirst(document, "").toString() else document
        val wrappers = leadingWrapperRegex.matchAt(html, 0) ?: return html to ""
        val headingStart = wrappers.range.last + 1
        val headings = leadingHeadingRegex.matchAt(html, headingStart) ?: return html to ""
        val headingText =
            headingRegex
                .findAll(headings.value)
                .firstOrNull { it.groupValues[1] != "1" }
                ?.groupValues
                ?.get(2)
                ?.replace(Regex("<[^>]+>"), "")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: return html to ""
        return html.replaceRange(headings.range, "") to headingText
    }

    private fun Readability4JExtended(uri: String?, html: String): Readability4JExtended {
        val options = ReadabilityOptions()
        val regExUtil = RegExUtilExtended()
        return Readability4JExtended(
            uri = uri ?: "",
            html = html,
            options = options,
            regExUtil = regExUtil,
            preprocessor = Preprocessor(regExUtil),
            metadataParser = MetadataParser(regExUtil),
            articleGrabber = RYArticleGrabberExtended(options, regExUtil),
            postprocessor = PostprocessorExtended(),
        )
    }
}
