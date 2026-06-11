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
            .putString(progressKey(progress.detailUrl), progress.toJson().toString())
            .apply()
    }

    fun getProgress(detailUrl: String): ReadingProgress? {
        return EsjUrl.cacheKeys(detailUrl)
            .firstNotNullOfOrNull { key -> preferences.getString("progress:$key", null) }
            ?.let { runCatching { JSONObject(it).toProgress() }.getOrNull() }
    }

    fun saveBookshelf(page: BookshelfPage) {
        val json = JSONObject()
            .put("currentPage", page.currentPage)
            .put("totalPages", page.totalPages)
            .put("books", JSONArray(page.books.map { it.toJson() }))
        writeJson("bookshelf-v1", page.currentPage.toString(), json)
    }

    fun getBookshelf(host: EsjHost, page: Int = 1): BookshelfPage? {
        val json = readJson("bookshelf-v1", page.toString())
            ?: readJson("bookshelf-v1", "${host.host}:$page")
            ?: EsjHost.entries
                .asSequence()
                .filter { it != host }
                .mapNotNull { fallbackHost -> readJson("bookshelf-v1", "${fallbackHost.host}:$page") }
                .firstOrNull()
            ?: return null
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

    fun clearUserCache() {
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
        preferences.edit().apply {
            preferences.all.keys
                .filter { it.startsWith("progress:") }
                .forEach(::remove)
        }.apply()
    }

    fun markLoggedIn(email: String) {
        preferences.edit()
            .putBoolean("has_logged_in", true)
            .putString("login_email", email)
            .apply()
    }

    fun hasLoggedInBefore(): Boolean {
        return preferences.getBoolean("has_logged_in", false)
    }

    fun getLoginEmail(): String? {
        return preferences.getString("login_email", null)
            ?.takeIf { it.isNotBlank() }
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
        writeJson(CHAPTER_CACHE_PREFIX, EsjUrl.cacheKey(chapter.url), chapter.toJson())
    }

    fun getChapter(url: String): ReaderChapter? {
        return readJson(CHAPTER_CACHE_PREFIX, EsjUrl.cacheKeys(url))?.toReaderChapter()
    }

    fun hasChapter(url: String): Boolean {
        return EsjUrl.cacheKeys(url).any { key -> jsonFile(CHAPTER_CACHE_PREFIX, key).exists() }
    }

    fun isReaderDarkMode(): Boolean {
        return preferences.getBoolean("reader_dark_mode", legacyReaderThemePreset()?.dark ?: false)
    }

    fun setReaderDarkMode(enabled: Boolean) {
        preferences.edit()
            .putBoolean("reader_dark_mode", enabled)
            .apply()
    }

    fun getReaderThemePreset(): ReaderThemePreset {
        return if (isReaderDarkMode()) getReaderDarkThemePreset() else getReaderLightThemePreset()
    }

    fun setReaderThemePreset(preset: ReaderThemePreset) {
        if (preset.dark) {
            setReaderDarkThemePreset(preset)
        } else {
            setReaderLightThemePreset(preset)
        }
        setReaderDarkMode(preset.dark)
    }

    fun getReaderLightThemePreset(): ReaderThemePreset {
        return ReaderThemePreset.fromNameOrNull(preferences.getString("reader_light_theme_preset", null))
            ?.takeUnless { it.dark }
            ?: legacyReaderThemePreset()?.takeUnless { it.dark }
            ?: ReaderThemePreset.PAPER
    }

    fun setReaderLightThemePreset(preset: ReaderThemePreset) {
        val safePreset = preset.takeUnless { it.dark } ?: ReaderThemePreset.PAPER
        preferences.edit()
            .putString("reader_light_theme_preset", safePreset.name)
            .apply()
    }

    fun getReaderDarkThemePreset(): ReaderThemePreset {
        return ReaderThemePreset.fromNameOrNull(preferences.getString("reader_dark_theme_preset", null))
            ?.takeIf { it.dark }
            ?: legacyReaderThemePreset()?.takeIf { it.dark }
            ?: ReaderThemePreset.NIGHT
    }

    fun setReaderDarkThemePreset(preset: ReaderThemePreset) {
        val safePreset = preset.takeIf { it.dark } ?: ReaderThemePreset.NIGHT
        preferences.edit()
            .putString("reader_dark_theme_preset", safePreset.name)
            .apply()
    }

    fun getReaderLayoutSettings(): ReaderLayoutSettings {
        return ReaderLayoutSettings(
            fontFamily = ReaderFontFamily.fromName(preferences.getString("reader_font_family", null)),
            fontSizeSp = preferences.getFloat("reader_font_size_sp", 19f).coerceIn(14f, 30f),
            paragraphSpacingDp = preferences.getFloat("reader_paragraph_spacing_dp", 14f).coerceIn(0f, 36f),
            firstLineIndentEm = preferences.getFloat("reader_first_line_indent_em", 2f).coerceIn(0f, 4f),
            horizontalPaddingDp = preferences.getFloat("reader_horizontal_padding_dp", 22f).coerceIn(12f, 48f),
        )
    }

    fun setReaderLayoutSettings(settings: ReaderLayoutSettings) {
        preferences.edit()
            .putString("reader_font_family", settings.fontFamily.name)
            .putFloat("reader_font_size_sp", settings.fontSizeSp.coerceIn(14f, 30f))
            .putFloat("reader_paragraph_spacing_dp", settings.paragraphSpacingDp.coerceIn(0f, 36f))
            .putFloat("reader_first_line_indent_em", settings.firstLineIndentEm.coerceIn(0f, 4f))
            .putFloat("reader_horizontal_padding_dp", settings.horizontalPaddingDp.coerceIn(12f, 48f))
            .apply()
    }

    fun showLatestChapterOnBookshelf(): Boolean {
        return preferences.getBoolean("show_latest_chapter", true)
    }

    fun setShowLatestChapterOnBookshelf(enabled: Boolean) {
        preferences.edit()
            .putBoolean("show_latest_chapter", enabled)
            .apply()
    }

    fun saveChapters(detailUrl: String, chapters: List<ChapterLink>) {
        val json = JSONObject()
            .put("detailUrl", detailUrl)
            .put("chapters", JSONArray(chapters.map { it.toJson() }))
        writeJson("chapters", EsjUrl.cacheKey(detailUrl), json)
    }

    fun getChapters(detailUrl: String): List<ChapterLink>? {
        return readJson("chapters", EsjUrl.cacheKeys(detailUrl))
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

    private fun readJson(prefix: String, keys: List<String>): JSONObject? {
        return keys.firstNotNullOfOrNull { key -> readJson(prefix, key) }
    }

    private fun jsonFile(prefix: String, key: String): File {
        return File(cacheDir, "$prefix-${key.safeFileName()}.json")
    }

    private fun progressKey(detailUrl: String): String {
        return "progress:${EsjUrl.cacheKey(detailUrl)}"
    }

    private fun legacyReaderThemePreset(): ReaderThemePreset? {
        return ReaderThemePreset.fromNameOrNull(preferences.getString("reader_theme_preset", null))
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
