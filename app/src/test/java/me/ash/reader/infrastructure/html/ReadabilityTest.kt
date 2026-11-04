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
    fun testParseToTextKeepsLeadingHeadingInsideFullDocument() {
        val html =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\" \"http://www.w3.org/TR/REC-html40/loose.dtd\">\n" +
                "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head>\n" +
                "<body>\n" +
                "<article>\n <div data-sanitized-class=\"index_main-content__header___h72f \">\n" +
                "  <h2>Quasi tutte nella zona orientale della citt&agrave;, e molte indirizzate ad attivit&agrave; commerciali: per ora ci sono solo ipotesi</h2>\n" +
                " </div>\n" +
                " <div data-sanitized-id=\"singleBody\" data-sanitized-class=\"contenuto all \">\n" +
                (1..20)
                    .joinToString(" ") { "<p>Da giugno nella zona est di Roma sono esplose 19 bombe, paragrafo $it.</p>" } +
                "\n </div>\n</article>\n" +
                "</body></html>"
        val result = Readability.parseToText(html, "https://www.ilpost.it/2026/11/04/bombe-roma-est/")
        assertTrue(result.startsWith("Quasi tutte nella zona orientale della città, e molte indirizzate"))
        assertFalse(result.contains("&agrave;"))
    }

    @Test
    fun testParseToTextKeepsLeadingHeadingExactIlPostShape() {
        val html =
            "<article>\n" +
                " <div data-sanitized-class=\"index_main-content__header___h72f \">\n" +
                "  <h2>Quasi tutte nella zona orientale della città, e molte indirizzate ad attività commerciali: per ora ci sono solo ipotesi</h2>\n" +
                "  <div data-sanitized-class=\"index_actions__NFnjX \">\n" +
                "   <div data-sanitized-class=\"_saved_1hbvv_1\">\n" +
                "    <div data-sanitized-class=\"_ilpost_saved_1hbvv_1 false\"></div>\n" +
                "   </div>\n" +
                "  </div>\n" +
                " </div>\n" +
                " <div data-sanitized-class=\"index_main-content__image___g7jQ\">\n" +
                "  <figure>\n" +
                "   <div>\n" +
                "    <img alt=\"Bomba carta in Via Casilina 953\" src=\"https://static-prod.cdnilpost.com/x.jpg\">\n" +
                "   </div>\n" +
                "   <figcaption>Bomba carta in Via Casilina 953 davanti un autosalone</figcaption>\n" +
                "  </figure>\n" +
                " </div>\n" +
                " <div data-sanitized-id=\"singleBody\" data-sanitized-class=\"contenuto all \">\n" +
                (1..20)
                    .joinToString(" ") {
                        "<p>Da giugno nella zona est di Roma sono esplose 19 bombe, paragrafo $it.</p>"
                    } +
                "\n </div>\n" +
                "</article>"
        val result = Readability.parseToText(html, "https://www.ilpost.it/2026/11/04/bombe-roma-est/")
        println("RESULT => ${result.take(80)}")
        assertTrue(result.startsWith("Quasi tutte nella zona orientale"))
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
