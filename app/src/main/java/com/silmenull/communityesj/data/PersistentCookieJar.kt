package com.silmenull.communityesj.data

import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(
    private val preferences: SharedPreferences,
    baseHost: String,
) : CookieJar {
    private val cookies = mutableListOf<Cookie>()
    private val baseUrl: HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host(baseHost)
        .build()

    init {
        restore()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.removeAll { old ->
            cookies.any { new ->
                old.name == new.name && old.domain == new.domain && old.path == new.path
            }
        }
        this.cookies.addAll(cookies)
        persist()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val removed = cookies.removeAll { it.expiresAt < now }
        if (removed) persist()
        return cookies.filter { it.matches(url) }
    }

    @Synchronized
    fun clear() {
        cookies.clear()
        persist()
    }

    @Synchronized
    fun hasCookies(): Boolean {
        return cookies.any { it.expiresAt > System.currentTimeMillis() }
    }

    @Synchronized
    fun hasCookie(name: String): Boolean {
        val now = System.currentTimeMillis()
        val removed = cookies.removeAll { it.expiresAt < now }
        if (removed) persist()
        return cookies.any { it.name == name && it.expiresAt > now }
    }

    private fun restore() {
        preferences.getStringSet(KEY_COOKIES, emptySet()).orEmpty()
            .mapNotNull { Cookie.parse(baseUrl, it) }
            .let { restored ->
                cookies.clear()
                cookies.addAll(restored)
            }
    }

    private fun persist() {
        preferences.edit()
            .putStringSet(KEY_COOKIES, cookies.map { it.toString() }.toSet())
            .apply()
    }

    private companion object {
        const val KEY_COOKIES = "cookies"
    }
}
