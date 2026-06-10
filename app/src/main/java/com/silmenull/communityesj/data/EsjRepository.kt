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
    private val cookieJar = PersistentCookieJar(store.preferences)
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun loginSessionState(): LoginSessionState {
        val hasRequiredCookies = cookieJar.hasCookie("ews_key") && cookieJar.hasCookie("ews_token")
        return when {
            hasRequiredCookies -> LoginSessionState.VALID
            store.hasLoggedInBefore() -> LoginSessionState.EXPIRED
            else -> LoginSessionState.MISSING
        }
    }

    fun progressFor(detailUrl: String): ReadingProgress? = store.getProgress(detailUrl)

    fun saveProgress(progress: ReadingProgress) {
        store.saveProgress(progress)
    }

    fun isReaderDarkMode(): Boolean = store.isReaderDarkMode()

    fun setReaderDarkMode(enabled: Boolean) {
        store.setReaderDarkMode(enabled)
    }

    fun logout() {
        cookieJar.clear()
        store.clearSession()
    }

    suspend fun login(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        cookieJar.clear()

        val tokenBody = FormBody.Builder()
            .add("plxf", "getAuthToken")
            .build()
        val tokenRequest = baseRequest("https://www.esjzone.cc/my/login")
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
        val loginRequest = baseRequest("https://www.esjzone.cc/inc/mem_login.php")
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
            if (result.success) store.markLoggedIn()
        }
    }

    suspend fun loadBookshelf(page: Int = 1): BookshelfPage = withContext(Dispatchers.IO) {
        val url = if (page <= 1) {
            "https://www.esjzone.cc/my/favorite"
        } else {
            "https://www.esjzone.cc/my/favorite/$page.html"
        }
        val html = executeText(baseRequest(url).get().build())
        if (EsjParser.containsLoginRedirect(html)) {
            throw LoginExpiredException()
        }
        EsjParser.parseBookshelf(html, page)
    }

    suspend fun resolveBookStart(book: BookItem): ChapterLink? = withContext(Dispatchers.IO) {
        val detailUrl = book.detailUrl ?: return@withContext null
        val chapters = loadChapters(detailUrl, forceRefresh = false)
        val saved = store.getProgress(detailUrl)
        val savedChapter = saved?.chapterUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url ->
                ChapterLink(
                    title = saved.chapterTitle.ifBlank { book.lastReadChapter.ifBlank { book.title } },
                    url = url,
                    isCached = store.hasChapter(url),
                )
            }
        val remoteLastReadChapter = book.lastReadChapterUrl?.let { url -> chapters.firstOrNull { it.url == url } }
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
                val chapters = (cached.detailUrl ?: detailUrlHint)
                    ?.let { loadChapters(it, forceRefresh = false) }
                    .orEmpty()
                return@withContext cached.copy(chapters = markCached(chapters.ifEmpty { cached.chapters }))
            }
        }

        val readerHtml = executeText(baseRequest(url).get().build())
        val parsedReader = EsjParser.parseReader(readerHtml, url)
        val detailUrl = detailUrlHint ?: parsedReader.detailUrl
        val chapters = detailUrl?.let { loadChapters(it, forceRefresh = forceRefresh) }.orEmpty()
        val chapter = parsedReader.copy(chapters = chapters)
        store.saveChapter(chapter)
        chapter
    }

    suspend fun loadChapters(detailUrl: String, forceRefresh: Boolean = false): List<ChapterLink> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            store.getChapters(detailUrl)?.let { return@withContext markCached(it) }
        }
        val html = executeText(baseRequest(detailUrl).get().build())
        val chapters = EsjParser.parseChapters(html)
        if (chapters.isNotEmpty()) {
            store.saveChapters(detailUrl, chapters)
        }
        markCached(chapters)
    }

    suspend fun prefetchNextChapters(currentUrl: String, chapters: List<ChapterLink>, count: Int = 3) = withContext(Dispatchers.IO) {
        val currentIndex = chapters.indexOfFirst { it.url == currentUrl }
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

    private fun chaptersDetailHint(chapters: List<ChapterLink>): String? {
        val firstUrl = chapters.firstOrNull()?.url ?: return null
        val match = Regex("""/forum/(\d+)/""").find(firstUrl) ?: return null
        return "https://www.esjzone.cc/detail/${match.groupValues[1]}.html"
    }

    private fun markCached(chapters: List<ChapterLink>): List<ChapterLink> {
        return chapters.map { it.copy(isCached = store.hasChapter(it.url)) }
    }

    private fun baseRequest(url: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .header("User-Agent", "CommunityESJ Android Reader")
            .header("Accept", "text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .header("Referer", "https://www.esjzone.cc/")
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
