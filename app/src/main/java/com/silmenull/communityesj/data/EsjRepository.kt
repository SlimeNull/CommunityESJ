package com.silmenull.communityesj.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class EsjRepository {
    private val cookieJar = InMemoryCookieJar()
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

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
        )
    }

    suspend fun loadBookshelf(page: Int = 1): BookshelfPage = withContext(Dispatchers.IO) {
        val url = if (page <= 1) {
            "https://www.esjzone.cc/my/favorite"
        } else {
            "https://www.esjzone.cc/my/favorite/$page.html"
        }
        val html = executeText(baseRequest(url).get().build())
        EsjParser.parseBookshelf(html, page)
    }

    suspend fun loadReader(url: String, detailUrlHint: String? = null): ReaderChapter = withContext(Dispatchers.IO) {
        val readerHtml = executeText(baseRequest(url).get().build())
        val parsedReader = EsjParser.parseReader(readerHtml, url)
        val detailUrl = detailUrlHint ?: parsedReader.detailUrl
        val chapters = detailUrl?.let { detail ->
            val detailHtml = executeText(baseRequest(detail).get().build())
            EsjParser.parseChapters(detailHtml)
        }.orEmpty()

        parsedReader.copy(chapters = chapters)
    }

    suspend fun loadFirstChapterFromDetail(detailUrl: String): String? = withContext(Dispatchers.IO) {
        val html = executeText(baseRequest(detailUrl).get().build())
        EsjParser.parseChapters(html).firstOrNull()?.url
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
