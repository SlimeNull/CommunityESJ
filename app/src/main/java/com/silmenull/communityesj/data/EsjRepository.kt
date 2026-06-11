package com.silmenull.communityesj.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoginExpiredException : IOException("登录凭证已失效,请重新登录")

class EsjRepository(context: Context) {
    private val store = LocalStore(context.applicationContext)
    private var selectedHost = store.getHost()
    private val cookieJar = PersistentCookieJar(store.preferences, selectedHost.host)
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun currentHost(): EsjHost = selectedHost

    fun favoriteUrl(): String = siteUrl("/my/favorite")

    fun switchHost(host: EsjHost): Boolean {
        if (host == selectedHost) return false
        selectedHost = host
        cookieJar.switchHost(host.host)
        store.setHost(host)
        return true
    }

    fun loginSessionState(): LoginSessionState {
        val hasRequiredCookies = cookieJar.hasCookie("ews_key") && cookieJar.hasCookie("ews_token")
        return when {
            hasRequiredCookies -> LoginSessionState.VALID
            store.hasLoggedInBefore() -> LoginSessionState.EXPIRED
            else -> LoginSessionState.MISSING
        }
    }

    fun progressFor(detailUrl: String): ReadingProgress? = store.getProgress(detailUrl)?.forCurrentHost()

    fun hasLoggedInBefore(): Boolean = store.hasLoggedInBefore()

    fun saveProgress(progress: ReadingProgress) {
        store.saveProgress(progress)
    }

    fun isReaderDarkMode(): Boolean = store.isReaderDarkMode()

    fun setReaderDarkMode(enabled: Boolean) {
        store.setReaderDarkMode(enabled)
    }

    fun readerThemePreset(): ReaderThemePreset = store.getReaderThemePreset()

    fun setReaderThemePreset(preset: ReaderThemePreset) {
        store.setReaderThemePreset(preset)
    }

    fun readerLightThemePreset(): ReaderThemePreset = store.getReaderLightThemePreset()

    fun setReaderLightThemePreset(preset: ReaderThemePreset) {
        store.setReaderLightThemePreset(preset)
    }

    fun readerDarkThemePreset(): ReaderThemePreset = store.getReaderDarkThemePreset()

    fun setReaderDarkThemePreset(preset: ReaderThemePreset) {
        store.setReaderDarkThemePreset(preset)
    }

    fun readerLayoutSettings(): ReaderLayoutSettings = store.getReaderLayoutSettings()

    fun setReaderLayoutSettings(settings: ReaderLayoutSettings) {
        store.setReaderLayoutSettings(settings)
    }

    fun resetDisplaySettings() {
        store.setReaderLightThemePreset(ReaderThemePreset.PAPER)
        store.setReaderDarkThemePreset(ReaderThemePreset.NIGHT)
        store.setReaderLayoutSettings(ReaderLayoutSettings())
        store.setShowLatestChapterOnBookshelf(true)
    }

    fun showLatestChapterOnBookshelf(): Boolean = store.showLatestChapterOnBookshelf()

    fun setShowLatestChapterOnBookshelf(enabled: Boolean) {
        store.setShowLatestChapterOnBookshelf(enabled)
    }

    fun logout() {
        cookieJar.clear()
        store.clearSession()
    }

    fun expireLogin() {
        cookieJar.clear()
    }

    fun cachedBookshelf(page: Int = 1): BookshelfPage? {
        return store.getBookshelf(selectedHost, page)?.forCurrentHost()
    }

    fun hasCachedBookshelf(page: Int = 1): Boolean {
        return cachedBookshelf(page) != null
    }

    fun hasAnyCachedBookshelf(page: Int = 1): Boolean {
        return hasCachedBookshelf(page)
    }

    fun cacheProgressFor(detailUrl: String): BookCacheProgress? {
        val currentDetailUrl = currentHostUrl(detailUrl)
        val chapters = store.getChapters(currentDetailUrl) ?: return null
        val total = chapters.size
        if (total <= 0) return null
        return BookCacheProgress(
            detailUrl = currentDetailUrl,
            cached = chapters.count { store.hasChapter(it.url) },
            total = total,
            isRunning = false,
        )
    }

    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        cookieJar.clear()

        val tokenBody = FormBody.Builder()
            .add("plxf", "getAuthToken")
            .build()
        val tokenRequest = baseRequest(siteUrl("/my/login"))
            .post(tokenBody)
            .build()
        val tokenResponse = executeText(tokenRequest)
        EsjParser.parseAuthToken(tokenResponse)
            ?: throw IOException("登录令牌响应格式不正确")

        val loginBody = FormBody.Builder()
            .add("email", email)
            .add("pwd", password)
            .add("remember_me", "on")
            .build()
        val loginRequest = baseRequest(siteUrl("/inc/mem_login.php"))
            .post(loginBody)
            .build()
        val loginResponse = executeText(loginRequest)
        val json = runCatching { JSONObject(loginResponse) }.getOrNull()
        val status = json?.optInt("status") ?: 0
        val message = json?.optString("msg").orEmpty()

        LoginResult(
            success = status == 200,
            message = if (status == 200) "登录成功" else message.ifBlank { "登录失败，服务器返回：$loginResponse" },
        ).also { result ->
            if (result.success) {
                val normalizedEmail = email.trim().lowercase()
                val previousEmail = store.getLoginEmail()?.lowercase()
                if (previousEmail != null && previousEmail != normalizedEmail) {
                    store.clearUserCache()
                }
                store.markLoggedIn(normalizedEmail)
            }
        }
    }

    suspend fun loadBookshelf(page: Int = 1): BookshelfPage = withContext(Dispatchers.IO) {
        val url = if (page <= 1) {
            favoriteUrl()
        } else {
            siteUrl("/my/favorite/$page.html")
        }
        val html = executeText(baseRequest(url).get().build())
        if (EsjParser.containsLoginRedirect(html)) {
            throw LoginExpiredException()
        }
        EsjParser.parseBookshelf(html, page, selectedHost.baseUrl)
            .also { store.saveBookshelf(it) }
    }

    suspend fun resolveBookStart(book: BookItem): ChapterLink? = withContext(Dispatchers.IO) {
        val currentBook = book.forCurrentHost()
        val detailUrl = currentBook.detailUrl ?: return@withContext null
        val saved = store.getProgress(detailUrl)
        val cachedChapters = store.getChapters(detailUrl)?.let(::markCached)
        val savedCachedChapter = saved?.chapterUrl
            ?.takeIf { store.hasChapter(it) }
            ?.let { url ->
                ChapterLink(
                    title = saved.chapterTitle.ifBlank { currentBook.lastReadChapter.ifBlank { currentBook.title } },
                    url = currentHostUrl(url),
                    isCached = true,
                )
            }
        val remoteCachedChapterWithoutList = currentBook.lastReadChapterUrl
            ?.takeIf { store.hasChapter(it) }
            ?.let { url ->
                ChapterLink(
                    title = currentBook.lastReadChapter.ifBlank { currentBook.title },
                    url = currentHostUrl(url),
                    isCached = true,
                )
            }
        if (cachedChapters != null) {
            val remoteCachedChapter = currentBook.lastReadChapterUrl
                ?.let { url -> cachedChapters.firstOrNull { EsjUrl.equivalent(it.url, url) && it.isCached } }
            val firstCachedChapter = cachedChapters.firstOrNull { it.isCached }
            if (savedCachedChapter != null || remoteCachedChapter != null || firstCachedChapter != null) {
                return@withContext savedCachedChapter ?: remoteCachedChapter ?: firstCachedChapter
            }
        }
        if (savedCachedChapter != null || remoteCachedChapterWithoutList != null) {
            return@withContext savedCachedChapter ?: remoteCachedChapterWithoutList
        }

        val chapters = loadChapters(detailUrl, forceRefresh = false)
        val savedChapter = saved?.chapterUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url ->
                ChapterLink(
                    title = saved.chapterTitle.ifBlank { currentBook.lastReadChapter.ifBlank { currentBook.title } },
                    url = currentHostUrl(url),
                    isCached = store.hasChapter(url),
                )
            }
        val remoteLastReadChapter = currentBook.lastReadChapterUrl
            ?.let { url -> chapters.firstOrNull { EsjUrl.equivalent(it.url, url) } }
        savedChapter ?: remoteLastReadChapter ?: chapters.firstOrNull()
    }

    suspend fun loadReader(
        url: String,
        detailUrlHint: String? = null,
        forceRefresh: Boolean = false,
    ): ReaderChapter = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val cached = store.getChapter(url)
            if (cached != null) {
                val detailUrl = cached.detailUrl ?: detailUrlHint
                val chapters = (cached.detailUrl ?: detailUrlHint)
                    ?.let { store.getChapters(it)?.let(::markCached) }
                    .orEmpty()
                return@withContext cached.copy(
                    url = currentHostUrl(cached.url),
                    contentBlocks = cached.contentBlocks.map { it.forCurrentHost() },
                    chapters = markCached(chapters.ifEmpty { cached.chapters }),
                    previousUrl = currentHostUrlOrNull(cached.previousUrl),
                    nextUrl = currentHostUrlOrNull(cached.nextUrl),
                    detailUrl = currentHostUrlOrNull(detailUrl),
                )
            }
        }

        val currentUrl = currentHostUrl(url)
        val readerHtml = executeText(baseRequest(currentUrl).get().build())
        if (EsjParser.containsLoginRedirect(readerHtml)) {
            throw LoginExpiredException()
        }
        val parsedReader = EsjParser.parseReader(readerHtml, currentUrl)
        val detailUrl = currentHostUrlOrNull(detailUrlHint) ?: parsedReader.detailUrl
        val chapters = detailUrl
            ?.let { store.getChapters(it)?.let(::markCached) }
            .orEmpty()
        val chapter = parsedReader.copy(
            chapters = chapters,
            detailUrl = detailUrl,
        )
        store.saveChapter(chapter)
        chapter
    }

    suspend fun loadChapters(detailUrl: String, forceRefresh: Boolean = false): List<ChapterLink> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            store.getChapters(detailUrl)?.let { return@withContext markCached(it) }
        }
        val currentDetailUrl = currentHostUrl(detailUrl)
        val html = executeText(baseRequest(currentDetailUrl).get().build())
        if (EsjParser.containsLoginRedirect(html)) {
            throw LoginExpiredException()
        }
        val chapters = EsjParser.parseChapters(html, currentDetailUrl)
        if (chapters.isNotEmpty()) {
            store.saveChapters(currentDetailUrl, chapters)
        }
        markCached(chapters)
    }

    suspend fun refreshChapters(detailUrl: String): List<ChapterLink> {
        return loadChapters(detailUrl, forceRefresh = true)
    }

    suspend fun prefetchNextChapters(currentUrl: String, chapters: List<ChapterLink>, count: Int = 3) = withContext(Dispatchers.IO) {
        val currentIndex = chapters.indexOfFirst { EsjUrl.equivalent(it.url, currentUrl) }
        if (currentIndex < 0) return@withContext

        chapters.drop(currentIndex + 1)
            .take(count)
            .forEach { chapter ->
                if (store.getChapter(chapter.url) == null) {
                    runCatching {
                        val parsed = loadReader(
                            url = chapter.url,
                            detailUrlHint = chaptersDetailHint(chapters),
                            forceRefresh = false,
                        )
                        store.saveChapter(parsed)
                    }
                }
            }
    }

    suspend fun cacheWholeBook(
        detailUrl: String,
        knownChapters: List<ChapterLink>,
        onProgress: suspend (BookCacheProgress) -> Unit,
    ) = withContext(Dispatchers.IO) {
        val currentDetailUrl = currentHostUrl(detailUrl)
        val chapters = knownChapters
            .ifEmpty { loadChapters(currentDetailUrl, forceRefresh = false) }
            .let(::markCached)
        if (chapters.isEmpty()) {
            throw IOException("没有解析到目录")
        }

        suspend fun emit(isRunning: Boolean) {
            onProgress(
                BookCacheProgress(
                    detailUrl = currentDetailUrl,
                    cached = chapters.count { store.hasChapter(it.url) },
                    total = chapters.size,
                    isRunning = isRunning,
                ),
            )
        }

        emit(isRunning = true)
        chapters.forEach { chapter ->
            if (!store.hasChapter(chapter.url)) {
                loadReader(
                    url = chapter.url,
                    detailUrlHint = currentDetailUrl,
                    forceRefresh = false,
                )
            }
            emit(isRunning = true)
        }
        emit(isRunning = false)
    }

    private fun chaptersDetailHint(chapters: List<ChapterLink>): String? {
        val firstUrl = chapters.firstOrNull()?.url ?: return null
        val match = Regex("""/forum/(\d+)/""").find(firstUrl) ?: return null
        return siteUrl("/detail/${match.groupValues[1]}.html")
    }

    private fun markCached(chapters: List<ChapterLink>): List<ChapterLink> {
        return chapters.map { chapter ->
            val url = currentHostUrl(chapter.url)
            chapter.copy(
                url = url,
                isCached = store.hasChapter(url),
            )
        }
    }

    private fun BookshelfPage.forCurrentHost(): BookshelfPage {
        return copy(books = books.map { it.forCurrentHost() })
    }

    private fun BookItem.forCurrentHost(): BookItem {
        return copy(
            detailUrl = currentHostUrlOrNull(detailUrl),
            latestChapterUrl = currentHostUrlOrNull(latestChapterUrl),
            lastReadChapterUrl = currentHostUrlOrNull(lastReadChapterUrl),
        )
    }

    private fun ReadingProgress.forCurrentHost(): ReadingProgress {
        return copy(
            detailUrl = currentHostUrl(detailUrl),
            chapterUrl = currentHostUrl(chapterUrl),
        )
    }

    private fun ReaderContentBlock.forCurrentHost(): ReaderContentBlock {
        return when (this) {
            is ReaderContentBlock.Text -> this
            is ReaderContentBlock.Image -> copy(url = currentHostUrl(url))
        }
    }

    private fun currentHostUrl(url: String): String {
        return EsjUrl.toHost(url, selectedHost) ?: url
    }

    private fun currentHostUrlOrNull(url: String?): String? {
        return EsjUrl.toHost(url, selectedHost)
    }

    private fun baseRequest(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .header("User-Agent", "ESJ Read Android Reader")
            .header("Accept", "text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .header("Referer", "${selectedHost.baseUrl}/")
    }

    private fun siteUrl(path: String): String {
        return "${selectedHost.baseUrl}$path"
    }

    private fun executeText(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${body.take(200)}")
            }
            return body
        }
    }
}
