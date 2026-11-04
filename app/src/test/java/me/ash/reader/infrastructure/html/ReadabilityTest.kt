package me.ash.reader.infrastructure.html

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadabilityTest {

    @Test
    fun testParseToTextKeepsRealExcerpt() {
        assertEquals(
            "Just a plain summary paragraph.",
            Readability.parseToText("<p>Just a plain summary paragraph.</p>", "https://example.com/post"),
        )
    }

    @Test
    fun testParseToTextKeepsTextAfterImage() {
        assertEquals(
            "The real subtitle text with plenty of words follows the image.",
            Readability.parseToText(
                "<p><img src=\"https://example.com/a.jpg\" alt=\"Cover image\"/></p>" +
                    "<p>The real subtitle text with plenty of words follows the image.</p>",
                "https://example.com/post",
            ),
        )
    }

    @Test
    fun testParseToTextStripsWordPressCaptionShortcode() {
        assertEquals(
            "Real article excerpt starts here and is quite long.",
            Readability.parseToText(
                "[caption id=\"attachment_1\" align=\"alignnone\" width=\"300\"]" +
                    "<img src=\"https://example.com/a.jpg\" alt=\"The scene at the stadium\"/>" +
                    " The scene at the stadium, Saturday.[/caption]" +
                    "<p>Real article excerpt starts here and is quite long.</p>",
                "https://example.com/post",
            ),
        )
    }

    @Test
    fun testParseToTextStripsFigcaption() {
        assertEquals(
            "Real summary here.",
            Readability.parseToText(
                "<figure><img src=\"https://example.com/a.jpg\"/>" +
                    "<figcaption>A chart showing revenue growth</figcaption></figure>" +
                    "<p>Real summary here.</p>",
                "https://example.com/post",
            ),
        )
    }

    @Test
    fun testParseToTextStripsAudioPlayerPlaceholder() {
        assertEquals(
            "The lead paragraph of the article.",
            Readability.parseToText(
                "<div id=\"audioPlayerArticle\" data-mp3=\"https://example.com/a.mp3\"" +
                    " data-id=\"1\">Caricamento player</div>" +
                    "<p>The lead paragraph of the article.</p>",
                "https://example.com/post",
            ),
        )
    }

    @Test
    fun testParseToTextImageOnlyDescriptionIsEmpty() {
        assertEquals(
            "",
            Readability.parseToText(
                "<img src=\"https://example.com/a.jpg\" alt=\"A photo of a cat sitting on a mat\"/>",
                "https://example.com/post",
            ),
        )
    }

    @Test
    fun testParseToTextKeepsLeadingHeading() {
        val result =
            Readability.parseToText(
                "<h2>Il governo vuole togliere soldi a molti pensionati</h2>" +
                    (1..20)
                        .joinToString(" ") {
                            "<p>Nel disegno di legge di bilancio il governo ha inserito misure " +
                                "paradossali e contraddittorie sulle pensioni, paragrafo $it.</p>"
                        },
                "https://example.com/post",
            )
        assertTrue(result.startsWith("Il governo vuole togliere soldi a molti pensionati"))
        assertTrue(result.contains("paragrafo 1"))
    }

    @Test
    fun testParseToTextKeepsLeadingHeadingBehindWrappers() {
        val result =
            Readability.parseToText(
                "<article>\n <div class=\"index_main-content__header\">\n" +
                    "<h2>Poi ci sono state proteste e il comune di Roma è intervenuto</h2>" +
                    (1..20)
                        .joinToString(" ") {
                            "<p>Una ringhiera dei Fori Imperiali di Roma è stata ricoperta, " +
                                "paragrafo $it.</p>"
                        } +
                    "\n</div>\n</article>",
                "https://example.com/post",
            )
        assertTrue(result.startsWith("Poi ci sono state proteste e il comune di Roma è intervenuto"))
        assertTrue(result.contains("paragrafo 1"))
    }

    @Test
    fun testParseToTextSkipsRepeatedLeadingH1() {
        val result =
            Readability.parseToText(
                "<h1>Article Title</h1>" +
                    "<h2>Subtitle text</h2>" +
                    (1..20)
                        .joinToString(" ") {
                            "<p>Real body content of the article that is long enough, paragrafo $it.</p>"
                        },
                "https://example.com/post",
            )
        assertFalse(result.startsWith("Article Title"))
        assertTrue(result.startsWith("Subtitle text"))
    }

    @Test
    fun testParseToTextNullAndEmptyInputs() {
        assertEquals("", Readability.parseToText(null, null))
        assertEquals("", Readability.parseToText("", null))
    }
}
