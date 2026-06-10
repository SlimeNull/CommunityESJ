package com.silmenull.communityesj.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LocalStore(context: Context) {
    val preferences: SharedPreferences = context.getSharedPreferences("community_esj", Context.MODE_PRIVATE)
    private val cacheDir = File(context.cacheDir, "reader_cache").apply { mkdirs() }

    fun saveProgress(progress: ReadingProgress) {
        preferences.edit()
            .putString("progress:${progress.detailUrl}", progress.toJson().toString())
            .apply()
    }

    fun getProgress(detailUrl: String): ReadingProgress? {
        return preferences.getString("progress:$detailUrl", null)
            ?.let { runCatching { JSONObject(it).toProgress() }.getOrNull() }
    }

    fun clearSession() {
        preferences.edit()
            .remove("cookies")
            .apply()
    }

    fun saveChapter(chapter: ReaderChapter) {
        writeJson("chapter-v2", chapter.url, chapter.toJson())
    }

    fun getChapter(url: String): ReaderChapter? {
        return readJson("chapter-v2", url)?.toReaderChapter()
    }

    fun isReaderDarkMode(): Boolean {
        return preferences.getBoolean("reader_dark_mode", false)
    }

    fun setReaderDarkMode(enabled: Boolean) {
        preferences.edit()
            .putBoolean("reader_dark_mode", enabled)
            .apply()
    }

    fun saveChapters(detailUrl: String, chapters: List<ChapterLink>) {
        val json = JSONObject()
            .put("detailUrl", detailUrl)
            .put("chapters", JSONArray(chapters.map { it.toJson() }))
        writeJson("chapters", detailUrl, json)
    }

    fun getChapters(detailUrl: String): List<ChapterLink>? {
        return readJson("chapters", detailUrl)
            ?.optJSONArray("chapters")
            ?.let { array ->
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val title = item.optString("title")
                        val url = item.optString("url")
                        if (title.isNotBlank() && url.isNotBlank()) {
                            add(ChapterLink(title, url))
                        }
                    }
                }
            }
            ?.takeIf { it.isNotEmpty() }
    }

    private fun writeJson(prefix: String, key: String, json: JSONObject) {
        File(cacheDir, "$prefix-${key.safeFileName()}.json").writeText(json.toString(), StandardCharsets.UTF_8)
    }

    private fun readJson(prefix: String, key: String): JSONObject? {
        val file = File(cacheDir, "$prefix-${key.safeFileName()}.json")
        if (!file.exists()) return null
        return runCatching { JSONObject(file.readText(StandardCharsets.UTF_8)) }.getOrNull()
    }

    private fun String.safeFileName(): String {
        return URLEncoder.encode(this, StandardCharsets.UTF_8.name())
    }

    private fun ReadingProgress.toJson(): JSONObject {
        return JSONObject()
            .put("detailUrl", detailUrl)
            .put("bookTitle", bookTitle)
            .put("chapterTitle", chapterTitle)
            .put("chapterUrl", chapterUrl)
            .put("scrollProgress", scrollProgress)
    }

    private fun JSONObject.toProgress(): ReadingProgress {
        return ReadingProgress(
            detailUrl = optString("detailUrl"),
            bookTitle = optString("bookTitle"),
            chapterTitle = optString("chapterTitle"),
            chapterUrl = optString("chapterUrl"),
            scrollProgress = optDouble("scrollProgress", 0.0).toFloat().coerceIn(0f, 1f),
        )
    }

    private fun ChapterLink.toJson(): JSONObject {
        return JSONObject()
            .put("title", title)
            .put("url", url)
    }

    private fun ReaderChapter.toJson(): JSONObject {
        return JSONObject()
            .put("url", url)
            .put("bookTitle", bookTitle)
            .put("chapterTitle", chapterTitle)
            .put("paragraphs", JSONArray(paragraphs))
            .put("chapters", JSONArray(chapters.map { it.toJson() }))
            .put("previousUrl", previousUrl)
            .put("nextUrl", nextUrl)
            .put("detailUrl", detailUrl)
    }

    private fun JSONObject.toReaderChapter(): ReaderChapter {
        val paragraphArray = optJSONArray("paragraphs") ?: JSONArray()
        val chapterArray = optJSONArray("chapters") ?: JSONArray()

        return ReaderChapter(
            url = optString("url"),
            bookTitle = optString("bookTitle"),
            chapterTitle = optString("chapterTitle"),
            paragraphs = buildList {
                for (index in 0 until paragraphArray.length()) {
                    add(paragraphArray.optString(index))
                }
            },
            chapters = buildList {
                for (index in 0 until chapterArray.length()) {
                    val item = chapterArray.optJSONObject(index) ?: continue
                    val title = item.optString("title")
                    val url = item.optString("url")
                    if (title.isNotBlank() && url.isNotBlank()) {
                        add(ChapterLink(title, url))
                    }
                }
            },
            previousUrl = optString("previousUrl").takeIf { it.isNotBlank() && it != "null" },
            nextUrl = optString("nextUrl").takeIf { it.isNotBlank() && it != "null" },
            detailUrl = optString("detailUrl").takeIf { it.isNotBlank() && it != "null" },
        )
    }
}
