package me.ash.reader.infrastructure.html

import org.junit.Assert.assertEquals
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
    fun testParseToTextNullAndEmptyInputs() {
        assertEquals("", Readability.parseToText(null, null))
        assertEquals("", Readability.parseToText("", null))
    }
}
