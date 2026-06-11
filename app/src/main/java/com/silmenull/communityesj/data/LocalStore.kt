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

    fun saveBookshelf(host: EsjHost, page: BookshelfPage) {
        val json = JSONObject()
            .put("currentPage", page.currentPage)
            .put("totalPages", page.totalPages)
            .put("books", JSONArray(page.books.map { it.toJson() }))
        writeJson("bookshelf-v1", "${host.host}:${page.currentPage}", json)
    }

    fun getBookshelf(host: EsjHost, page: Int = 1): BookshelfPage? {
        val json = readJson("bookshelf-v1", "${host.host}:$page") ?: return null
        val booksArray = json.optJSONArray("books") ?: JSONArray()
        val books = buildList {
            for (index in 0 until booksArray.length()) {
                booksArray.optJSONObject(index)?.toBookItem()?.let(::add)
            }
        }
        return BookshelfPage(
            books = books,
            currentPage = json.optInt("currentPage", page).coerceAtLeast(1),
            totalPages = json.optInt("totalPages", page).coerceAtLeast(1),
        )
    }

    fun clearSession() {
        preferences.edit()
            .remove("cookies")
            .remove("has_logged_in")
            .apply()
    }

    fun markLoggedIn() {
        preferences.edit()
            .putBoolean("has_logged_in", true)
            .apply()
    }

    fun hasLoggedInBefore(): Boolean {
        return preferences.getBoolean("has_logged_in", false)
    }

    fun getHost(): EsjHost {
        return EsjHost.fromHost(preferences.getString("selected_host", null))
    }

    fun setHost(host: EsjHost) {
        preferences.edit()
            .putString("selected_host", host.host)
            .apply()
    }

    fun saveChapter(chapter: ReaderChapter) {
        writeJson(CHAPTER_CACHE_PREFIX, chapter.url, chapter.toJson())
    }

    fun getChapter(url: String): ReaderChapter? {
        return readJson(CHAPTER_CACHE_PREFIX, url)?.toReaderChapter()
    }

    fun hasChapter(url: String): Boolean {
        return jsonFile(CHAPTER_CACHE_PREFIX, url).exists()
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
        jsonFile(prefix, key).writeText(json.toString(), StandardCharsets.UTF_8)
    }

    private fun readJson(prefix: String, key: String): JSONObject? {
        val file = jsonFile(prefix, key)
        if (!file.exists()) return null
        return runCatching { JSONObject(file.readText(StandardCharsets.UTF_8)) }.getOrNull()
    }

    private fun jsonFile(prefix: String, key: String): File {
        return File(cacheDir, "$prefix-${key.safeFileName()}.json")
    }

    private companion object {
        const val CHAPTER_CACHE_PREFIX = "chapter-v3"
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

    private fun BookItem.toJson(): JSONObject {
        return JSONObject()
            .put("title", title)
            .put("latestChapter", latestChapter)
            .put("lastReadChapter", lastReadChapter)
            .put("updateDate", updateDate)
            .put("detailUrl", detailUrl)
            .put("latestChapterUrl", latestChapterUrl)
            .put("lastReadChapterUrl", lastReadChapterUrl)
    }

    private fun JSONObject.toBookItem(): BookItem? {
        val title = optString("title")
        if (title.isBlank()) return null
        return BookItem(
            title = title,
            latestChapter = optString("latestChapter"),
            lastReadChapter = optString("lastReadChapter"),
            updateDate = optString("updateDate"),
            detailUrl = optString("detailUrl").takeIf { it.isNotBlank() && it != "null" },
            latestChapterUrl = optString("latestChapterUrl").takeIf { it.isNotBlank() && it != "null" },
            lastReadChapterUrl = optString("lastReadChapterUrl").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    private fun ReaderChapter.toJson(): JSONObject {
        return JSONObject()
            .put("url", url)
            .put("bookTitle", bookTitle)
            .put("chapterTitle", chapterTitle)
            .put("contentBlocks", JSONArray(contentBlocks.map { it.toJson() }))
            .put("chapters", JSONArray(chapters.map { it.toJson() }))
            .put("previousUrl", previousUrl)
            .put("nextUrl", nextUrl)
            .put("detailUrl", detailUrl)
    }

    private fun JSONObject.toReaderChapter(): ReaderChapter {
        val chapterArray = optJSONArray("chapters") ?: JSONArray()
        val blockArray = optJSONArray("contentBlocks")
        val legacyParagraphArray = optJSONArray("paragraphs")

        return ReaderChapter(
            url = optString("url"),
            bookTitle = optString("bookTitle"),
            chapterTitle = optString("chapterTitle"),
            contentBlocks = readContentBlocks(blockArray, legacyParagraphArray),
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

    private fun ReaderContentBlock.toJson(): JSONObject {
        return when (this) {
            is ReaderContentBlock.Text -> JSONObject()
                .put("type", "text")
                .put("text", text)

            is ReaderContentBlock.Image -> JSONObject()
                .put("type", "image")
                .put("url", url)
                .put("alt", alt)
        }
    }

    private fun readContentBlocks(blockArray: JSONArray?, legacyParagraphArray: JSONArray?): List<ReaderContentBlock> {
        if (blockArray != null) {
            return buildList {
                for (index in 0 until blockArray.length()) {
                    val item = blockArray.optJSONObject(index) ?: continue
                    when (item.optString("type")) {
                        "text" -> item.optString("text")
                            .takeIf { it.isNotBlank() }
                            ?.let { add(ReaderContentBlock.Text(it)) }

                        "image" -> item.optString("url")
                            .takeIf { it.isNotBlank() }
                            ?.let { add(ReaderContentBlock.Image(it, item.optString("alt"))) }
                    }
                }
            }
        }

        return buildList {
            val paragraphs = legacyParagraphArray ?: JSONArray()
            for (index in 0 until paragraphs.length()) {
                paragraphs.optString(index)
                    .takeIf { it.isNotBlank() }
                    ?.let { add(ReaderContentBlock.Text(it)) }
            }
        }
    }
}
