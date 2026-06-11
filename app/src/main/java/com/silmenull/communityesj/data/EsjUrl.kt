package com.silmenull.communityesj.data

import java.net.URI

object EsjUrl {
    private val hosts = EsjHost.entries.map { it.host.lowercase() }.toSet()

    fun cacheKey(url: String): String {
        val trimmed = url.trim()
        val path = sitePath(trimmed) ?: return trimmed
        return path
    }

    fun cacheKeys(url: String): List<String> {
        val trimmed = url.trim()
        val path = sitePath(trimmed)
        return buildList {
            if (path != null) {
                add(path)
                EsjHost.entries.forEach { host -> add("${host.baseUrl}$path") }
            }
            if (trimmed.isNotBlank()) {
                add(trimmed)
            }
        }.distinct()
    }

    fun toHost(url: String?, host: EsjHost): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        val path = sitePath(trimmed) ?: return trimmed
        return "${host.baseUrl}$path"
    }

    fun equivalent(left: String, right: String): Boolean {
        return cacheKey(left) == cacheKey(right)
    }

    private fun sitePath(url: String): String? {
        if (url.startsWith("/")) return url
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in hosts) return null
        val path = uri.rawPath?.takeIf { it.isNotBlank() } ?: "/"
        return buildString {
            append(path)
            uri.rawQuery?.let { query -> append('?').append(query) }
            uri.rawFragment?.let { fragment -> append('#').append(fragment) }
        }
    }
}
