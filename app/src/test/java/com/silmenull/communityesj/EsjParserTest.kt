package com.silmenull.communityesj

import com.silmenull.communityesj.data.EsjParser
import com.silmenull.communityesj.data.ReaderContentBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EsjParserTest {
    @Test
    fun containsLoginRedirectDetectsScriptNavigationToLogin() {
        val html = """
            <html>
              <body>
                <script>window.location.href = '/my/login';</script>
              </body>
            </html>
        """.trimIndent()

        assertTrue(EsjParser.containsRedirectInstruction(html))
    }

    @Test
    fun containsLoginRedirectIgnoresScriptNavigationToOtherPath() {
        val html = """
            <html>
              <body>
                <script>window.location.href = '/detail/123.html';</script>
              </body>
            </html>
        """.trimIndent()

        assertFalse(EsjParser.containsRedirectInstruction(html))
    }

    @Test
    fun containsLoginRedirectIgnoresNormalFavoritePage() {
        val html = """
            <html>
              <body>
                <div class="product-item">书籍</div>
              </body>
            </html>
        """.trimIndent()

        assertFalse(EsjParser.containsRedirectInstruction(html))
    }

    @Test
    fun parseReaderSplitsPlainTextSeparatedByDoubleBreaks() {
        val html = """
            <html>
              <body>
                <h2>第一章</h2>
                <div class="forum-content">
                  第一段<br><br>
                  第二段<br>
                  仍然第二段<br><br>
                  第三段
                </div>
              </body>
            </html>
        """.trimIndent()

        val chapter = EsjParser.parseReader(html, "https://www.esjzone.cc/forum/1/1.html")
        val paragraphs = chapter.contentBlocks.mapNotNull { (it as? ReaderContentBlock.Text)?.text }

        assertEquals(listOf("第一段", "第二段\n仍然第二段", "第三段"), paragraphs)
    }

    @Test
    fun parseReaderSplitsDoubleBreaksInsideParagraphElement() {
        val html = """
            <html>
              <body>
                <h2>第一章</h2>
                <div class="forum-content">
                  <p>第一段<br><br>第二段<br>仍然第二段</p>
                </div>
              </body>
            </html>
        """.trimIndent()

        val chapter = EsjParser.parseReader(html, "https://www.esjzone.cc/forum/1/1.html")
        val paragraphs = chapter.contentBlocks.mapNotNull { (it as? ReaderContentBlock.Text)?.text }

        assertEquals(listOf("第一段", "第二段\n仍然第二段"), paragraphs)
    }
}
