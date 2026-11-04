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
    private val leadingHeadingRegex = Regex("""^\s*(?:<h[123]\b[^>]*>[\s\S]*?</h[123]>\s*)+""")
    private val headingRegex = Regex("""<h([123])\b[^>]*>([\s\S]*?)</h[123]>""")

    fun parseToText(htmlContent: String?, uri: String?): String {
        htmlContent ?: return ""
        return try {
            val content =
                keepLeadingHeading(
                    audioPlayerRegex.replace(
                        figcaptionRegex.replace(captionShortcodeRegex.replace(htmlContent, ""), ""),
                        "",
                    ),
                )
            Readability4JExtended(uri, content).parse().textContent?.trim() ?: ""
        } catch (e: Exception) {
            Log.e("RLog", "Readability.parseToText '$uri' is error: ", e)
            ""
        }
    }

    fun parseToElement(htmlContent: String?, uri: String?): Element? {
        htmlContent ?: return null
        return Readability4JExtended(uri, htmlContent).parse().articleContent
    }

    private fun keepLeadingHeading(html: String): String =
        leadingHeadingRegex.replace(html) { leading ->
            headingRegex.replace(leading.value) { heading ->
                if (heading.groupValues[1] == "1") ""
                else "<p><strong>${heading.groupValues[2]}</strong></p>"
            }
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
