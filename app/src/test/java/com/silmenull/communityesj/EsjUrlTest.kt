package com.silmenull.communityesj

import com.silmenull.communityesj.data.EsjHost
import com.silmenull.communityesj.data.EsjUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EsjUrlTest {
    @Test
    fun cacheKeyTreatsEsjHostsAsTheSameSite() {
        val ccUrl = "https://www.esjzone.cc/forum/123/456.html"
        val directUrl = "https://www.esjzone.one/forum/123/456.html"

        assertEquals("/forum/123/456.html", EsjUrl.cacheKey(ccUrl))
        assertEquals(EsjUrl.cacheKey(ccUrl), EsjUrl.cacheKey(directUrl))
        assertTrue(EsjUrl.equivalent(ccUrl, directUrl))
    }

    @Test
    fun toHostRewritesKnownEsjHostsOnly() {
        val ccUrl = "https://www.esjzone.cc/detail/123.html"
        val externalUrl = "https://example.com/image.png"

        assertEquals("https://www.esjzone.one/detail/123.html", EsjUrl.toHost(ccUrl, EsjHost.DIRECT))
        assertEquals(externalUrl, EsjUrl.toHost(externalUrl, EsjHost.DIRECT))
    }
}
