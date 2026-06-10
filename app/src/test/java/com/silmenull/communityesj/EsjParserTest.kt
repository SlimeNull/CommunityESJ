package com.silmenull.communityesj

import com.silmenull.communityesj.data.EsjParser
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

        assertTrue(EsjParser.containsLoginRedirect(html))
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

        assertFalse(EsjParser.containsLoginRedirect(html))
    }
}
