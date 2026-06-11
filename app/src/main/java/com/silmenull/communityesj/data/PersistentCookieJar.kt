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
    private var baseHost = baseHost
    private var baseUrl: HttpUrl = buildBaseUrl(baseHost)

    init {
        restore()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val normalizedCookies = cookies.mapNotNull { it.copyForHost(baseHost) }
        this.cookies.removeAll { old ->
            normalizedCookies.any { new ->
                old.name == new.name && old.domain == new.domain && old.path == new.path
            }
        }
        this.cookies.addAll(normalizedCookies)
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
        return cookies.any { it.name == name && it.expiresAt > now && it.matches(baseUrl) }
    }

    @Synchronized
    fun switchHost(host: String) {
        baseHost = host
        baseUrl = buildBaseUrl(host)
        val remapped = cookies.mapNotNull { it.copyForHost(host) }
        cookies.clear()
        cookies.addAll(remapped.distinctBy { "${it.name}|${it.domain}|${it.path}" })
        persist()
    }

    private fun restore() {
        preferences.getStringSet(KEY_COOKIES, emptySet()).orEmpty()
            .mapNotNull(::parseStoredCookie)
            .mapNotNull { it.copyForHost(baseHost) }
            .distinctBy { "${it.name}|${it.domain}|${it.path}" }
            .let { restored ->
                cookies.clear()
                cookies.addAll(restored)
            }
        persist()
    }

    private fun persist() {
        preferences.edit()
            .putStringSet(KEY_COOKIES, cookies.map { it.toString() }.toSet())
            .apply()
    }

    private fun Cookie.copyForHost(host: String): Cookie? {
        return runCatching {
            Cookie.Builder()
                .name(name)
                .value(value)
                .expiresAt(expiresAt)
                .path(path)
                .apply {
                    if (hostOnly) {
                        hostOnlyDomain(host)
                    } else {
                        domain(host)
                    }
                    if (secure) secure()
                    if (httpOnly) httpOnly()
                }
                .build()
        }.getOrNull()
    }

    private fun parseStoredCookie(cookie: String): Cookie? {
        val urls = buildList {
            add(baseUrl)
            EsjHost.entries.forEach { host -> add(buildBaseUrl(host.host)) }
        }.distinctBy { it.host }
        return urls.firstNotNullOfOrNull { url -> Cookie.parse(url, cookie) }
    }

    private companion object {
        const val KEY_COOKIES = "cookies"

        fun buildBaseUrl(host: String): HttpUrl {
            return HttpUrl.Builder()
                .scheme("https")
                .host(host)
                .build()
        }
    }
}
