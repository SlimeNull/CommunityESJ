package com.silmenull.communityesj

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.provider.MediaStore
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.ContentValues
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.SpanStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silmenull.communityesj.data.BookItem
import com.silmenull.communityesj.data.BookCacheProgress
import com.silmenull.communityesj.data.BookshelfPage
import com.silmenull.communityesj.data.ChapterLink
import com.silmenull.communityesj.data.CommentBlock
import com.silmenull.communityesj.data.CommentTextPart
import com.silmenull.communityesj.data.EsjRepository
import com.silmenull.communityesj.data.EsjHost
import com.silmenull.communityesj.data.EpubExport
import com.silmenull.communityesj.data.LoginExpiredException
import com.silmenull.communityesj.data.LoginSessionState
import com.silmenull.communityesj.data.ReaderChapter
import com.silmenull.communityesj.data.ReaderComment
import com.silmenull.communityesj.data.ReaderContentBlock
import com.silmenull.communityesj.data.ReaderFontFamily
import com.silmenull.communityesj.data.ReaderLayoutSettings
import com.silmenull.communityesj.data.ReaderThemePreset
import com.silmenull.communityesj.data.RememberedLogin
import com.silmenull.communityesj.data.ReadingProgress
import com.silmenull.communityesj.ui.theme.CommunityESJTheme
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

private val ReaderPage = Color(0xFFFBF3E3)
private val ReaderText = Color(0xFF261E16)
private val ReaderMutedText = Color(0xFF6D5F50)
private val ReaderBar = Color(0xF7FFF7E8)
private val ReaderAccent = Color(0xFF5B4630)
private val ReaderDisabled = Color(0xFFE0D2BE)
private val ReaderDisabledText = Color(0xFF776855)
private val ReaderDarkPage = Color(0xFF181613)
private val ReaderDarkText = Color(0xFFEDE2D0)
private val ReaderDarkMutedText = Color(0xFFBFAE96)
private val ReaderDarkBar = Color(0xF7231F1A)
private val ReaderDarkAccent = Color(0xFFD6B986)
private val ReaderDarkDisabled = Color(0xFF40382F)
private val ReaderDarkDisabledText = Color(0xFF8F7F6D)

private data class ReaderColors(
    val page: Color,
    val text: Color,
    val mutedText: Color,
    val bar: Color,
    val accent: Color,
    val disabled: Color,
    val disabledText: Color,
)

private data class ReaderSystemBarsState(
    val showStatusBar: Boolean,
    val darkMode: Boolean,
)

private enum class ReaderShortcutDirection {
    Up,
    Down,
}

class MainActivity : ComponentActivity() {
    private val mediaKeyHandler = Handler(Looper.getMainLooper())
    private var pendingMediaKeyClick = false
    private var readerShortcutHandler: ((ReaderShortcutDirection) -> Boolean)? = null
    private var mediaSession: MediaSession? = null
    private val mediaSingleClickRunnable = Runnable {
        pendingMediaKeyClick = false
        handleReaderShortcut(ReaderShortcutDirection.Down)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mediaSession = createMediaSession()
        setContent {
            CommunityESJTheme {
                EsjReaderApp(onReaderShortcutHandlerChange = ::setReaderShortcutHandler)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            val handled = when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> handleReaderShortcut(ReaderShortcutDirection.Down)
                KeyEvent.KEYCODE_VOLUME_UP -> handleReaderShortcut(ReaderShortcutDirection.Up)
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_HEADSETHOOK -> handleMediaKeyClick()

                else -> false
            }
            if (handled) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (readerShortcutHandler != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                KeyEvent.KEYCODE_HEADSETHOOK -> return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        mediaKeyHandler.removeCallbacks(mediaSingleClickRunnable)
        mediaSession?.isActive = false
        mediaSession?.release()
        super.onDestroy()
    }

    private fun setReaderShortcutHandler(handler: ((ReaderShortcutDirection) -> Boolean)?) {
        readerShortcutHandler = handler
        mediaSession?.isActive = handler != null
        if (handler == null) {
            pendingMediaKeyClick = false
            mediaKeyHandler.removeCallbacks(mediaSingleClickRunnable)
        }
    }

    private fun handleReaderShortcut(direction: ReaderShortcutDirection): Boolean {
        return readerShortcutHandler?.invoke(direction) == true
    }

    private fun handleMediaKeyClick(): Boolean {
        if (readerShortcutHandler == null) return false
        if (pendingMediaKeyClick) {
            pendingMediaKeyClick = false
            mediaKeyHandler.removeCallbacks(mediaSingleClickRunnable)
            return handleReaderShortcut(ReaderShortcutDirection.Up)
        }

        pendingMediaKeyClick = true
        mediaKeyHandler.postDelayed(mediaSingleClickRunnable, MEDIA_CLICK_WINDOW_MS)
        return true
    }

    private fun createMediaSession(): MediaSession {
        return MediaSession(this, "ESJReadShortcut").apply {
            setPlaybackState(
                PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                    .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                    .build(),
            )
            setCallback(
                object : MediaSession.Callback() {
                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonIntent.getParcelableExtra(
                                Intent.EXTRA_KEY_EVENT,
                                KeyEvent::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        }
                        if (
                            event?.action == KeyEvent.ACTION_DOWN &&
                            event.repeatCount == 0 &&
                            (event.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || event.keyCode == KeyEvent.KEYCODE_HEADSETHOOK)
                        ) {
                            return handleMediaKeyClick()
                        }
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }
                },
            )
        }
    }

    private companion object {
        const val MEDIA_CLICK_WINDOW_MS = 500L
    }
}

private sealed interface Screen {
    data object Login : Screen
    data object Bookshelf : Screen
    data object Settings : Screen
    data class Reader(val chapterUrl: String, val detailUrlHint: String? = null) : Screen
}

@Stable
private class AppState(
    val repository: EsjRepository,
) {
    var screen by mutableStateOf<Screen>(initialScreen(repository))
    var isLoading by mutableStateOf(false)
    var feedbackDialogMessage by mutableStateOf<String?>(null)
    var bookshelf by mutableStateOf(BookshelfPage(emptyList(), 1, 1))
    var readerChapter by mutableStateOf<ReaderChapter?>(null)
    var currentPage by mutableIntStateOf(1)
    var readerInitialProgress by mutableFloatStateOf(0f)
    var readerScrollProgress by mutableFloatStateOf(0f)
    var readerControlsVisible by mutableStateOf(false)
    var readerChaptersRefreshing by mutableStateOf(false)
    var readerCommentPosting by mutableStateOf(false)
    var readerLightThemePreset by mutableStateOf(repository.readerLightThemePreset())
    var readerDarkThemePreset by mutableStateOf(repository.readerDarkThemePreset())
    var readerLayoutSettings by mutableStateOf(repository.readerLayoutSettings())
    var rememberedLogin by mutableStateOf(repository.rememberedLogin())
    var readerDarkMode by mutableStateOf(repository.isReaderDarkMode())
    var bookshelfReloginAction by mutableStateOf(false)
    var bookshelfOfflineMessage by mutableStateOf<String?>(null)
    var bookshelfRefreshing by mutableStateOf(false)
    var selectedHost by mutableStateOf(repository.currentHost())
    var showLatestChapter by mutableStateOf(repository.showLatestChapterOnBookshelf())
    var cacheProgress by mutableStateOf<Map<String, BookCacheProgress>>(emptyMap())
    var localProgress by mutableStateOf<Map<String, ReadingProgress>>(emptyMap())
    var cacheJobs = mutableMapOf<String, Job>()

    private companion object {
        fun initialScreen(repository: EsjRepository): Screen {
            return if (repository.hasLoggedInBefore() || repository.loginSessionState() == LoginSessionState.VALID) {
                Screen.Bookshelf
            } else {
                Screen.Login
            }
        }
    }
}

@Composable
private fun EsjReaderApp(
    onReaderShortcutHandlerChange: (((ReaderShortcutDirection) -> Boolean)?) -> Unit,
) {
    val context = LocalContext.current
    val appState = remember(context) { AppState(EsjRepository(context)) }
    val scope = rememberCoroutineScope()
    val autofillManager = remember(context) {
        context.getSystemService(AutofillManager::class.java)
    }
    var readerBarsState by remember { mutableStateOf<ReaderSystemBarsState?>(null) }
    var pendingEpubExportBook by remember { mutableStateOf<BookItem?>(null) }
    var isExportingEpub by remember { mutableStateOf(false) }

    fun showFeedback(message: String, forceDialog: Boolean = false) {
        val text = message.trim()
        if (text.isBlank()) return
        if (forceDialog || text.length > 36 || text.contains('\n')) {
            appState.feedbackDialogMessage = text
        } else {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCacheProgress(progress: BookCacheProgress) {
        appState.cacheProgress = appState.cacheProgress + (progress.detailUrl to progress)
    }

    fun clearCacheJob(detailUrl: String) {
        appState.cacheJobs.remove(detailUrl)
        appState.cacheProgress[detailUrl]?.let { progress ->
            updateCacheProgress(progress.copy(isRunning = false))
        }
    }

    fun syncBookshelfCacheProgress(page: BookshelfPage) {
        val progress = page.books
            .mapNotNull { book -> book.detailUrl?.let(appState.repository::cacheProgressFor) }
            .filter { it.cached > 0 || it.isRunning }
            .associateBy { it.detailUrl }
        if (progress.isNotEmpty()) {
            appState.cacheProgress = appState.cacheProgress + progress
        }
        val readingProgress = page.books
            .mapNotNull { book ->
                book.detailUrl?.let { detailUrl ->
                    appState.repository.progressFor(
                        detailUrl
                    )
                }
            }
            .associateBy { it.detailUrl }
        appState.localProgress = readingProgress
    }

    fun showCachedBookshelf(page: Int = 1): Boolean {
        val cached = appState.repository.cachedBookshelf(page) ?: return false
        appState.bookshelf = cached
        appState.currentPage = cached.currentPage
        syncBookshelfCacheProgress(cached)
        appState.screen = Screen.Bookshelf
        return true
    }

    fun openWeb(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure { error -> showFeedback(error.userMessage("无法打开网页")) }
    }

    fun copyText(label: String, text: String) {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        showFeedback("已复制")
    }

    fun exportEpub(book: BookItem) {
        scope.launch {
            isExportingEpub = true
            runCatching {
                val export = appState.repository.exportBookAsEpub(book)
                saveEpubToDownloads(context, export)
                export.fileName
            }.onSuccess { fileName ->
                showFeedback("已保存到下载：$fileName")
            }.onFailure { error ->
                if (error is SecurityException) {
                    showFeedback("软件没有权限写入文件", forceDialog = true)
                } else {
                    showFeedback(error.userMessage("导出 EPUB 失败"), forceDialog = true)
                }
            }
            isExportingEpub = false
        }
    }

    fun switchHost(host: EsjHost) {
        if (!appState.repository.switchHost(host)) return
        appState.selectedHost = appState.repository.currentHost()
        showFeedback("已切换到${host.displayName}")
        appState.bookshelfReloginAction = false
        appState.bookshelfOfflineMessage = null
        when (appState.screen) {
            Screen.Login -> {
                if (appState.repository.hasLoggedInBefore()) {
                    appState.screen = Screen.Bookshelf
                    appState.bookshelf =
                        appState.repository.cachedBookshelf(1) ?: BookshelfPage(emptyList(), 1, 1)
                    syncBookshelfCacheProgress(appState.bookshelf)
                    appState.currentPage = 1
                }
            }

            Screen.Bookshelf, Screen.Settings -> {
                appState.screen = Screen.Bookshelf
                appState.readerChapter = null
                appState.bookshelf =
                    appState.repository.cachedBookshelf(1) ?: BookshelfPage(emptyList(), 1, 1)
                syncBookshelfCacheProgress(appState.bookshelf)
                appState.currentPage = 1
            }

            is Screen.Reader -> Unit
        }
    }

    fun loadBookshelf(
        page: Int = 1,
        goLoginOnFailure: Boolean = false,
        navigateToBookshelf: Boolean = true,
    ) {
        scope.launch {
            appState.isLoading = true
            appState.bookshelfRefreshing = false
            appState.bookshelfReloginAction = false
            appState.bookshelfOfflineMessage = null
            runCatching {
                appState.repository.loadBookshelf(page)
            }.onSuccess { result ->
                appState.bookshelf = result
                appState.currentPage = result.currentPage
                syncBookshelfCacheProgress(result)
                if (navigateToBookshelf) {
                    appState.screen = Screen.Bookshelf
                }
            }.onFailure { error ->
                val loginExpired = error is LoginExpiredException
                if (loginExpired) {
                    appState.repository.expireLogin()
                }
                val cached = appState.repository.cachedBookshelf(page)
                if (cached != null) {
                    appState.bookshelf = cached
                    appState.currentPage = cached.currentPage
                    syncBookshelfCacheProgress(cached)
                    if (navigateToBookshelf) {
                        appState.screen = Screen.Bookshelf
                    }
                    appState.bookshelfReloginAction = true
                    appState.bookshelfOfflineMessage = if (loginExpired) {
                        "登录凭证已过期，已进入离线模式"
                    } else {
                        error.userMessage("加载书架失败，已显示本地缓存")
                    }
                } else if (goLoginOnFailure && loginExpired) {
                    if (navigateToBookshelf) {
                        appState.screen = Screen.Bookshelf
                    }
                    appState.bookshelfReloginAction = true
                    appState.bookshelfOfflineMessage = null
                    showFeedback("登录凭证已过期，没有可用的本地书架缓存")
                } else {
                    if (navigateToBookshelf) {
                        appState.screen = Screen.Bookshelf
                    }
                    appState.bookshelfReloginAction = true
                    appState.bookshelfOfflineMessage = null
                    showFeedback(error.userMessage("加载书架失败"))
                }
            }
            appState.isLoading = false
        }
    }

    fun refreshBookshelfInBackground(page: Int = appState.currentPage) {
        scope.launch {
            appState.bookshelfRefreshing = true
            appState.bookshelfOfflineMessage = null
            appState.bookshelfReloginAction = false
            runCatching {
                appState.repository.loadBookshelf(page)
            }.onSuccess { result ->
                appState.bookshelf = result
                appState.currentPage = result.currentPage
                syncBookshelfCacheProgress(result)
            }.onFailure { error ->
                val loginExpired = error is LoginExpiredException
                if (loginExpired) {
                    appState.repository.expireLogin()
                }
                appState.bookshelfReloginAction = true
                appState.bookshelfOfflineMessage = if (loginExpired) {
                    "登录凭证已过期，已进入离线模式"
                } else {
                    error.userMessage("加载书架失败，已显示本地缓存")
                }
            }
            appState.bookshelfRefreshing = false
        }
    }

    fun cacheWholeCurrentBook() {
        val chapter = appState.readerChapter ?: return
        val detailUrl = chapter.detailUrl
        if (detailUrl == null) {
            showFeedback("当前书籍缺少目录链接，无法缓存整本")
            return
        }
        val existing = appState.cacheProgress[detailUrl]
        if (existing?.isRunning == true) {
            appState.cacheJobs.remove(detailUrl)?.cancel()
            updateCacheProgress(existing.copy(isRunning = false))
            showFeedback("已停止缓存")
            return
        }
        showFeedback("已开始缓存")
        updateCacheProgress(
            BookCacheProgress(
                detailUrl = detailUrl,
                cached = existing?.cached ?: chapter.chapters.count { it.isCached },
                total = existing?.total?.takeIf { it > 0 } ?: chapter.chapters.size,
                isRunning = true,
            ),
        )
        val job = scope.launch {
            runCatching {
                appState.repository.cacheWholeBook(
                    detailUrl = detailUrl,
                    knownChapters = chapter.chapters,
                    onProgress = { progress ->
                        withContext(Dispatchers.Main) {
                            updateCacheProgress(progress)
                        }
                    },
                )
            }.onFailure { error ->
                if (error is CancellationException) {
                    return@onFailure
                }
                if (error is LoginExpiredException) {
                    appState.repository.expireLogin()
                }
                val total = chapter.chapters.size
                updateCacheProgress(
                    BookCacheProgress(
                        detailUrl = detailUrl,
                        cached = appState.cacheProgress[detailUrl]?.cached ?: 0,
                        total = total,
                        isRunning = false,
                    ),
                )
                showFeedback(error.userMessage("缓存整本失败"))
            }.also {
                clearCacheJob(detailUrl)
            }
        }
        appState.cacheJobs[detailUrl] = job
    }

    fun openReader(
        url: String,
        detailUrlHint: String? = null,
        initialProgress: Float = 0f,
        forceRefresh: Boolean = false,
    ) {
        val wasReader = appState.screen is Screen.Reader
        appState.screen = Screen.Reader(url, detailUrlHint)
        appState.readerInitialProgress = initialProgress.coerceIn(0f, 1f)
        appState.readerScrollProgress = appState.readerInitialProgress
        scope.launch {
            appState.isLoading = true
            if (!wasReader) {
                appState.readerChapter = null
            }
            runCatching {
                appState.repository.loadReader(url, detailUrlHint, forceRefresh)
            }.onSuccess { chapter ->
                appState.readerChapter = chapter
                scope.launch {
                    appState.repository.prefetchNextChapters(chapter.url, chapter.chapters)
                }
            }.onFailure { error ->
                if (error is LoginExpiredException) {
                    appState.repository.expireLogin()
                }
                showFeedback(error.userMessage("加载章节失败"))
            }
            appState.isLoading = false
        }
    }

    fun refreshCurrentBookChapters() {
        val chapter = appState.readerChapter ?: return
        val detailUrl = chapter.detailUrl
        if (detailUrl == null) {
            showFeedback("当前书籍缺少目录链接，无法刷新目录")
            return
        }
        scope.launch {
            appState.readerChaptersRefreshing = true
            runCatching {
                appState.repository.refreshChapters(detailUrl)
            }.onSuccess { chapters ->
                appState.readerChapter = appState.readerChapter?.copy(chapters = chapters)
            }.onFailure { error ->
                if (error is LoginExpiredException) {
                    appState.repository.expireLogin()
                }
                showFeedback(error.userMessage("刷新目录失败"))
            }
            appState.readerChaptersRefreshing = false
        }
    }

    fun postReaderComment(content: String) {
        val chapter = appState.readerChapter ?: return
        scope.launch {
            appState.readerCommentPosting = true
            runCatching {
                appState.repository.postComment(chapter, content)
            }.onSuccess { comment ->
                appState.readerChapter = appState.readerChapter?.copy(
                    comments = appState.readerChapter?.comments.orEmpty() + comment,
                )
                showFeedback("评论成功")
            }.onFailure { error ->
                if (error is LoginExpiredException) {
                    appState.repository.expireLogin()
                }
                showFeedback(error.userMessage("评论失败"), forceDialog = true)
            }
            appState.readerCommentPosting = false
        }
    }

    fun openBook(book: BookItem) {
        val detailUrl = book.detailUrl
        if (detailUrl == null) {
            showFeedback("这本书缺少目录链接")
            return
        }

        scope.launch {
            appState.isLoading = true
            runCatching {
                appState.repository.resolveBookStart(book)
            }.onSuccess { chapter ->
                if (chapter == null) {
                    showFeedback("没有找到可阅读章节")
                    appState.isLoading = false
                    return@onSuccess
                }
                val restoredProgress = appState.repository.progressFor(detailUrl)
                    ?.takeIf { it.chapterUrl == chapter.url }
                    ?.scrollProgress
                    ?: 0f
                appState.isLoading = false
                openReader(chapter.url, detailUrl, restoredProgress)
            }.onFailure { error ->
                if (error is LoginExpiredException) {
                    appState.repository.expireLogin()
                }
                showFeedback(error.userMessage("加载目录失败"))
                appState.isLoading = false
            }
        }
    }

    if (appState.screen !is Screen.Reader) {
        readerBarsState = null
        appState.readerControlsVisible = false
    }

    LaunchedEffect(Unit) {
        val sessionState = appState.repository.loginSessionState()
        if (appState.repository.hasLoggedInBefore()) {
            val hasCache = showCachedBookshelf(1)
            if (!hasCache) {
                appState.screen = Screen.Bookshelf
            }
            refreshBookshelfInBackground(1)
            return@LaunchedEffect
        }

        when (sessionState) {
            LoginSessionState.VALID -> loadBookshelf(goLoginOnFailure = true)
            LoginSessionState.EXPIRED -> {
                appState.screen = Screen.Bookshelf
                appState.bookshelfReloginAction = true
                appState.bookshelfOfflineMessage = null
                showFeedback("登录凭证已过期,请重新登录")
            }

            LoginSessionState.MISSING -> Unit
        }
    }

    ApplySystemBars(readerBarsState)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = appState.screen,
            transitionSpec = { screenTransition(initialState, targetState) },
            label = "screen_transition",
        ) { screen ->
            when (screen) {
                Screen.Login -> LoginScreen(
                    isLoading = appState.isLoading,
                    currentHost = appState.selectedHost,
                    rememberedLogin = appState.rememberedLogin,
                    showOfflineAction = appState.repository.hasAnyCachedBookshelf(),
                    onHostChange = ::switchHost,
                    onOffline = {
                        if (showCachedBookshelf(1)) {
                            appState.bookshelfReloginAction = true
                            appState.bookshelfOfflineMessage = "当前正在查看本地缓存"
                        }
                    },
                    onLogin = { email, password ->
                        scope.launch {
                            appState.isLoading = true
                            appState.bookshelfReloginAction = false
                            appState.bookshelfOfflineMessage = null
                            runCatching {
                                appState.repository.login(email, password)
                            }.onSuccess { result ->
                                showFeedback(result.message)
                                if (result.success) {
                                    appState.repository.setRememberedLogin(
                                        enabled = appState.rememberedLogin.enabled,
                                        email = email,
                                        password = password,
                                    )
                                    appState.rememberedLogin = appState.repository.rememberedLogin()
                                    autofillManager?.commit()
                                    loadBookshelf()
                                }
                            }.onFailure { error -> showFeedback(error.userMessage("登录失败")) }
                            appState.isLoading = false
                        }
                    },
                    onRememberChange = { remembered ->
                        appState.rememberedLogin = remembered
                        appState.repository.setRememberedLogin(
                            enabled = remembered.enabled,
                            email = remembered.email,
                            password = remembered.password,
                        )
                    },
                )

                Screen.Bookshelf -> BookshelfScreen(
                    page = appState.bookshelf,
                    isLoading = appState.isLoading,
                    offlineMessage = appState.bookshelfOfflineMessage,
                    isRefreshing = appState.bookshelfRefreshing,
                    onRefresh = { loadBookshelf(appState.currentPage) },
                    onPage = { loadBookshelf(it) },
                    onOpenBook = ::openBook,
                    onOpenWeb = { openWeb(appState.repository.favoriteUrl()) },
                    currentHost = appState.selectedHost,
                    onHostChange = ::switchHost,
                    cacheProgress = appState.cacheProgress,
                    localProgress = appState.localProgress,
                    showLatestChapter = appState.showLatestChapter,
                    onCopyBookTitle = { book -> copyText("书名", book.title) },
                    onCopyBookLink = { book ->
                        val link =
                            book.detailUrl ?: book.lastReadChapterUrl ?: book.latestChapterUrl
                        if (link.isNullOrBlank()) {
                            showFeedback("这本书缺少链接")
                        } else {
                            copyText("书籍链接", link)
                        }
                    },
                    onExportBook = { book ->
                        pendingEpubExportBook = book
                    },
                    onOpenSettings = {
                        appState.screen = Screen.Settings
                    },
                    onOpenGithub = { openWeb("https://github.com/SlimeNull/CommunityESJ") },
                    onLogout = {
                        appState.repository.logout()
                        appState.screen = Screen.Login
                        appState.bookshelfReloginAction = false
                        appState.bookshelfOfflineMessage = null
                        appState.readerChapter = null
                        appState.bookshelf = BookshelfPage(emptyList(), 1, 1)
                        appState.localProgress = emptyMap()
                    },
                )

                Screen.Settings -> SettingsScreen(
                    readerLightThemePreset = appState.readerLightThemePreset,
                    readerDarkThemePreset = appState.readerDarkThemePreset,
                    readerLayoutSettings = appState.readerLayoutSettings,
                    showLatestChapter = appState.showLatestChapter,
                    onBack = {
                        appState.screen = Screen.Bookshelf
                    },
                    onReaderLightThemePresetChange = { preset ->
                        appState.readerLightThemePreset = preset
                        appState.repository.setReaderLightThemePreset(preset)
                    },
                    onReaderDarkThemePresetChange = { preset ->
                        appState.readerDarkThemePreset = preset
                        appState.repository.setReaderDarkThemePreset(preset)
                    },
                    onReaderLayoutSettingsChange = { settings ->
                        appState.readerLayoutSettings = settings
                        appState.repository.setReaderLayoutSettings(settings)
                    },
                    onShowLatestChapterChange = { enabled ->
                        appState.showLatestChapter = enabled
                        appState.repository.setShowLatestChapterOnBookshelf(enabled)
                    },
                    onResetSettings = {
                        appState.readerLightThemePreset = ReaderThemePreset.PAPER
                        appState.readerDarkThemePreset = ReaderThemePreset.NIGHT
                        appState.readerLayoutSettings = ReaderLayoutSettings()
                        appState.showLatestChapter = true
                        appState.repository.resetDisplaySettings()
                        showFeedback("已重置设置")
                    },
                )

                is Screen.Reader -> ReaderScreen(
                    chapter = appState.readerChapter,
                    isLoading = appState.isLoading,
                    initialScrollProgress = appState.readerInitialProgress,
                    darkMode = appState.readerDarkMode,
                    onBack = {
                        appState.screen = Screen.Bookshelf
                    },
                    onProgress = { progress ->
                        appState.readerScrollProgress = progress.scrollProgress
                        appState.repository.saveProgress(progress)
                        appState.localProgress =
                            appState.localProgress + (progress.detailUrl to progress)
                    },
                    onOpenChapter = { chapter ->
                        openReader(
                            chapter.url,
                            appState.readerChapter?.detailUrl ?: screen.detailUrlHint
                        )
                    },
                    onOpenUrl = { url ->
                        openReader(url, appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                    },
                    onOpenWeb = { url -> openWeb(url) },
                    onRefresh = {
                        val chapter = appState.readerChapter ?: return@ReaderScreen
                        openReader(
                            url = chapter.url,
                            detailUrlHint = chapter.detailUrl ?: screen.detailUrlHint,
                            initialProgress = appState.readerScrollProgress,
                            forceRefresh = true,
                        )
                    },
                    onDarkModeChange = { enabled ->
                        appState.readerDarkMode = enabled
                        appState.repository.setReaderDarkMode(enabled)
                    },
                    readerThemePreset = if (appState.readerDarkMode) {
                        appState.readerDarkThemePreset
                    } else {
                        appState.readerLightThemePreset
                    },
                    readerLayoutSettings = appState.readerLayoutSettings,
                    controlsVisible = appState.readerControlsVisible,
                    onControlsVisibleChange = { visible ->
                        appState.readerControlsVisible = visible
                    },
                    cacheProgress = (appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                        ?.let(appState.cacheProgress::get),
                    chaptersRefreshing = appState.readerChaptersRefreshing,
                    commentPosting = appState.readerCommentPosting,
                    onCacheWholeBook = ::cacheWholeCurrentBook,
                    onRefreshChapters = ::refreshCurrentBookChapters,
                    onPostComment = ::postReaderComment,
                    onSystemBarsState = { readerBarsState = it },
                    onFeedback = ::showFeedback,
                    onReaderShortcutHandlerChange = onReaderShortcutHandlerChange,
                )
            }
        }
    }

    appState.feedbackDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { appState.feedbackDialogMessage = null },
            title = { Text("提示") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { appState.feedbackDialogMessage = null }) {
                    Text("确定")
                }
            },
        )
    }

    pendingEpubExportBook?.let { book ->
        AlertDialog(
            onDismissRequest = {
                if (!isExportingEpub) {
                    pendingEpubExportBook = null
                }
            },
            title = { Text("导出 EPUB") },
            text = { Text("仅会导出已缓存过的章节, 确认继续?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingEpubExportBook = null
                        exportEpub(book)
                    },
                    enabled = !isExportingEpub,
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingEpubExportBook = null },
                    enabled = !isExportingEpub,
                ) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun ApplySystemBars(readerState: ReaderSystemBarsState?) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val window = activity?.window
    val decorView = window?.decorView
    val controller = if (window != null && decorView != null) {
        WindowCompat.getInsetsController(window, decorView)
    } else {
        null
    }

    LaunchedEffect(window, controller, readerState) {
        if (window != null && controller != null) {
            applySystemBars(window, controller, readerState)
        }
    }

    DisposableEffect(window, controller) {
        onDispose {
            if (window != null && controller != null) {
                applySystemBars(window, controller, null)
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Suppress("DEPRECATION")
private fun applySystemBars(
    window: Window,
    controller: WindowInsetsControllerCompat,
    readerState: ReaderSystemBarsState?,
) {
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    if (readerState == null) {
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.isAppearanceLightStatusBars = true
        window.statusBarColor = Color.Transparent.toArgb()
        return
    }

    if (readerState.showStatusBar) {
        controller.show(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
    controller.isAppearanceLightStatusBars = !readerState.darkMode
    window.statusBarColor = if (readerState.darkMode) {
        ReaderDarkBar.toArgb()
    } else {
        ReaderBar.toArgb()
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LoginScreen(
    isLoading: Boolean,
    currentHost: EsjHost,
    rememberedLogin: RememberedLogin,
    showOfflineAction: Boolean,
    onHostChange: (EsjHost) -> Unit,
    onOffline: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRememberChange: (RememberedLogin) -> Unit,
) {
    var email by remember { mutableStateOf(rememberedLogin.email) }
    var password by remember { mutableStateOf(rememberedLogin.password) }
    val canSubmit = !isLoading && email.isNotBlank() && password.isNotBlank()
    val view = LocalView.current

    val balabala by remember { derivedStateOf { email + password } }

    DisposableEffect(view) {
        val previous = view.importantForAutofill
        view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        onDispose {
            view.importantForAutofill = previous
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("ESJ 轻阅") },
                actions = {
                    HostMenuButton(
                        currentHost = currentHost,
                        onHostChange = onHostChange,
                    )
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.weight(0.82f))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ESJ 轻阅",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "ESJ Read",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (rememberedLogin.enabled) {
                            onRememberChange(rememberedLogin.copy(email = it))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .autofill(
                            autofillTypes = listOf(
                                AutofillType.EmailAddress,
                                AutofillType.Username
                            ),
                            onFill = { email = it },
                        ),
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text("邮箱") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (rememberedLogin.enabled) {
                            onRememberChange(rememberedLogin.copy(password = it))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .autofill(
                            autofillTypes = listOf(AutofillType.Password),
                            onFill = { password = it },
                        ),
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmit) {
                                onLogin(email.trim(), password)
                            }
                        },
                    ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isLoading) {
                            onRememberChange(
                                rememberedLogin.copy(
                                    enabled = !rememberedLogin.enabled,
                                    email = email,
                                    password = password,
                                ),
                            )
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = rememberedLogin.enabled,
                        onCheckedChange = { checked ->
                            onRememberChange(
                                RememberedLogin(
                                    enabled = checked,
                                    email = email,
                                    password = password,
                                ),
                            )
                        },
                        enabled = !isLoading,
                    )
                    Text("记住我")
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onLogin(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("登录")
                    }
                }
                if (showOfflineAction) {
                    Text(
                        text = "离线模式",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                            .clickable(onClick = onOffline),
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
            Spacer(Modifier.weight(1.18f))
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: (String) -> Unit,
): Modifier {
    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current
    val autofillNode = remember(autofillTypes) {
        AutofillNode(
            autofillTypes = autofillTypes,
            onFill = onFill,
        )
    }

    DisposableEffect(autofillNode) {
        autofillTree += autofillNode
        onDispose {
            autofillTree.children.remove(autofillNode.id)
        }
    }

    return this
        .onGloballyPositioned { coordinates ->
            autofillNode.boundingBox = coordinates.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                autofill?.requestAutofillForNode(autofillNode)
            } else {
                autofill?.cancelAutofillForNode(autofillNode)
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookshelfScreen(
    page: BookshelfPage,
    isLoading: Boolean,
    offlineMessage: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPage: (Int) -> Unit,
    onOpenBook: (BookItem) -> Unit,
    onOpenWeb: () -> Unit,
    currentHost: EsjHost,
    onHostChange: (EsjHost) -> Unit,
    cacheProgress: Map<String, BookCacheProgress>,
    localProgress: Map<String, ReadingProgress>,
    showLatestChapter: Boolean,
    onCopyBookTitle: (BookItem) -> Unit,
    onCopyBookLink: (BookItem) -> Unit,
    onExportBook: (BookItem) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGithub: () -> Unit,
    onLogout: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var aboutVisible by remember { mutableStateOf(false) }
    var offlineDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("书架")
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else if (offlineMessage != null) {
                            IconButton(
                                onClick = { offlineDialogVisible = true },
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudOff,
                                    contentDescription = "离线模式",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "刷新书架",
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "菜单",
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("打开网页") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenWeb()
                                },
                            )
                            HorizontalDivider()
                            HostMenuItems(
                                currentHost = currentHost,
                                onHostChange = { host ->
                                    menuExpanded = false
                                    onHostChange(host)
                                },
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = {
                                    menuExpanded = false
                                    onOpenSettings()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("退出登录") },
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("关于") },
                                onClick = {
                                    menuExpanded = false
                                    aboutVisible = true
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            PageBar(
                currentPage = page.currentPage,
                totalPages = page.totalPages,
                isLoading = isLoading,
                onPage = onPage,
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (!isLoading && page.books.isEmpty()) {
                    item {
                        EmptyState(
                            text = "书架为空或未解析到书籍",
                            modifier = Modifier.padding(top = 64.dp),
                        )
                    }
                }
                items(page.books) { book ->
                    BookRow(
                        book = book,
                        cacheProgress = book.detailUrl?.let(cacheProgress::get),
                        localProgress = book.detailUrl?.let(localProgress::get),
                        showLatestChapter = showLatestChapter,
                        onClick = { onOpenBook(book) },
                        onCopyTitle = { onCopyBookTitle(book) },
                        onCopyLink = { onCopyBookLink(book) },
                        onExport = { onExportBook(book) },
                    )
                    HorizontalDivider()
                }
            }
            if (isLoading) {
                LoadingOverlay()
            }
        }
    }

    if (aboutVisible) {
        AlertDialog(
            onDismissRequest = { aboutVisible = false },
            title = { Text("关于") },
            text = {
                Column {
                    Text("ESJ 轻阅是一个简化的 ESJ Zone 阅读客户端，仅包含登录、书架和阅读页。")
                    Spacer(Modifier.height(12.dp))
                    Text("GitHub")
                    Text(
                        text = "SlimeNull/CommunityESJ",
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                aboutVisible = false
                                onOpenGithub()
                            },
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { aboutVisible = false }) {
                    Text("确定")
                }
            },
        )
    }

    if (offlineDialogVisible && offlineMessage != null) {
        AlertDialog(
            onDismissRequest = { offlineDialogVisible = false },
            title = { Text("离线模式") },
            text = { Text(offlineMessage) },
            confirmButton = {
                TextButton(onClick = { offlineDialogVisible = false }) {
                    Text("知道了")
                }
            },
        )
    }
}

@Composable
private fun HostMenuButton(
    currentHost: EsjHost,
    onHostChange: (EsjHost) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(currentHost.displayName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            HostMenuItems(
                currentHost = currentHost,
                onHostChange = { host ->
                    expanded = false
                    onHostChange(host)
                },
            )
        }
    }
}

@Composable
private fun HostMenuItems(
    currentHost: EsjHost,
    onHostChange: (EsjHost) -> Unit,
) {
    EsjHost.entries.forEach { host ->
        DropdownMenuItem(
            text = {
                Column {
                    Text(host.displayName)
                    Text(
                        text = host.host,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            },
            onClick = { onHostChange(host) },
            enabled = host != currentHost,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookRow(
    book: BookItem,
    cacheProgress: BookCacheProgress?,
    localProgress: ReadingProgress?,
    showLatestChapter: Boolean,
    onClick: () -> Unit,
    onCopyTitle: () -> Unit,
    onCopyLink: () -> Unit,
    onExport: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val lastRead = localProgress?.chapterTitle
        ?.takeIf { it.isNotBlank() }
        ?: book.lastReadChapter
    val updateText = remember(book.updateDate) { relativeUpdateText(book.updateDate) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                )
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (showLatestChapter && book.latestChapter.isNotBlank()) {
                Text(
                    text = book.latestChapter,
                    modifier = Modifier.padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (lastRead.isNotBlank()) {
                    CompactMetaChip(
                        text = "读到 $lastRead",
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                if (updateText.isNotBlank()) {
                    CompactMetaChip(text = updateText)
                }
                if (cacheProgress != null && cacheProgress.total > 0) {
                    CompactMetaChip(
                        text = if (cacheProgress.isRunning) {
                            "缓存中 ${cacheProgress.cached}/${cacheProgress.total}"
                        } else {
                            "已缓存 ${cacheProgress.cached}/${cacheProgress.total}"
                        },
                        active = cacheProgress.isRunning,
                    )
                }
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("复制书名") },
                onClick = {
                    menuExpanded = false
                    onCopyTitle()
                },
            )
            DropdownMenuItem(
                text = { Text("复制书链接") },
                onClick = {
                    menuExpanded = false
                    onCopyLink()
                },
            )
            DropdownMenuItem(
                text = { Text("导出为 EPUB") },
                onClick = {
                    menuExpanded = false
                    onExport()
                },
            )
        }
    }
}

@Composable
private fun CompactMetaChip(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val containerColor = if (active) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
    }
    val textColor = if (active) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        color = textColor,
        fontSize = 10.sp,
        lineHeight = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    readerLightThemePreset: ReaderThemePreset,
    readerDarkThemePreset: ReaderThemePreset,
    readerLayoutSettings: ReaderLayoutSettings,
    showLatestChapter: Boolean,
    onBack: () -> Unit,
    onReaderLightThemePresetChange: (ReaderThemePreset) -> Unit,
    onReaderDarkThemePresetChange: (ReaderThemePreset) -> Unit,
    onReaderLayoutSettingsChange: (ReaderLayoutSettings) -> Unit,
    onShowLatestChapterChange: (Boolean) -> Unit,
    onResetSettings: () -> Unit,
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "返回书架",
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "阅读配色",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                Text(
                    text = "亮色模式",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            item {
                ReaderThemePresetRow(
                    presets = ReaderThemePreset.entries.filterNot { it.dark },
                    selectedPreset = readerLightThemePreset,
                    onClick = onReaderLightThemePresetChange,
                )
            }
            item {
                Text(
                    text = "暗色模式",
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            item {
                ReaderThemePresetRow(
                    presets = ReaderThemePreset.entries.filter { it.dark },
                    selectedPreset = readerDarkThemePreset,
                    onClick = onReaderDarkThemePresetChange,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Text(
                    text = "阅读布局",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                Text(
                    text = "字体",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
            items(ReaderFontFamily.entries) { family ->
                ReaderFontRow(
                    family = family,
                    selected = family == readerLayoutSettings.fontFamily,
                    onClick = {
                        onReaderLayoutSettingsChange(readerLayoutSettings.copy(fontFamily = family))
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "字体大小",
                    valueText = "${readerLayoutSettings.fontSizeSp.roundToInt()} sp",
                    value = readerLayoutSettings.fontSizeSp,
                    valueRange = 14f..30f,
                    steps = 15,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(
                                fontSizeSp = value.roundToInt().toFloat()
                            )
                        )
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "行间距",
                    valueText = "${(readerLayoutSettings.lineHeightMultiplier * 100f).roundToInt()}%",
                    value = readerLayoutSettings.lineHeightMultiplier,
                    valueRange = 1.2f..2.4f,
                    steps = 23,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(lineHeightMultiplier = (value * 20f).roundToInt() / 20f),
                        )
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "段落间距",
                    valueText = "${readerLayoutSettings.paragraphSpacingDp.roundToInt()} dp",
                    value = readerLayoutSettings.paragraphSpacingDp,
                    valueRange = 0f..36f,
                    steps = 17,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(paragraphSpacingDp = (value / 2f).roundToInt() * 2f),
                        )
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "首行缩进",
                    valueText = "${readerLayoutSettings.firstLineIndentEm.formatOneDecimal()} em",
                    value = readerLayoutSettings.firstLineIndentEm,
                    valueRange = 0f..4f,
                    steps = 7,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(firstLineIndentEm = (value * 2f).roundToInt() / 2f),
                        )
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "页面左右边距",
                    valueText = "${readerLayoutSettings.horizontalPaddingDp.roundToInt()} dp",
                    value = readerLayoutSettings.horizontalPaddingDp,
                    valueRange = 12f..48f,
                    steps = 17,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(horizontalPaddingDp = (value / 2f).roundToInt() * 2f),
                        )
                    },
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Text(
                    text = "快捷翻页",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                ReaderSettingSlider(
                    title = "翻页距离",
                    valueText = "${(readerLayoutSettings.shortcutPageTurnPercent * 100f).roundToInt()}%",
                    value = readerLayoutSettings.shortcutPageTurnPercent,
                    valueRange = 0.1f..1f,
                    steps = 17,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(
                                shortcutPageTurnPercent = (value * 20f).roundToInt()
                                    .coerceIn(2, 20) / 20f,
                            ),
                        )
                    },
                )
            }
            item {
                ReaderSettingSlider(
                    title = "缓动时长",
                    valueText = "${readerLayoutSettings.shortcutAnimationMillis} ms",
                    value = readerLayoutSettings.shortcutAnimationMillis.toFloat(),
                    valueRange = 100f..1000f,
                    steps = 17,
                    onValueChange = { value ->
                        onReaderLayoutSettingsChange(
                            readerLayoutSettings.copy(
                                shortcutAnimationMillis = ((value / 50f).roundToInt() * 50).coerceIn(
                                    100,
                                    1000
                                ),
                            ),
                        )
                    },
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShowLatestChapterChange(!showLatestChapter) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "显示最新章节",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "关闭后书架列表会更紧凑",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                        )
                    }
                    Switch(
                        checked = showLatestChapter,
                        onCheckedChange = onShowLatestChapterChange,
                    )
                }
            }
            item {
                OutlinedButton(
                    onClick = onResetSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                ) {
                    Text("重置")
                }
            }
        }
    }
}

@Composable
private fun ReaderThemePresetRow(
    presets: List<ReaderThemePreset>,
    selectedPreset: ReaderThemePreset,
    onClick: (ReaderThemePreset) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(presets) { preset ->
            ReaderThemePresetCard(
                preset = preset,
                selected = preset == selectedPreset,
                onClick = { onClick(preset) },
            )
        }
    }
}

@Composable
private fun ReaderThemePresetCard(
    preset: ReaderThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = readerColors(preset)
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.page),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ColorSwatch(colors.text)
                ColorSwatch(colors.mutedText)
                ColorSwatch(colors.accent)
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(borderColor.copy(alpha = 0.12f)),
                )
            }
        }
        Text(
            text = preset.displayName,
            modifier = Modifier.padding(top = 6.dp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Text(
                text = "当前",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ReaderFontRow(
    family: ReaderFontFamily,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "字",
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 9.dp, vertical = 4.dp),
            fontFamily = family.toFontFamily(),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = family.displayName,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
        if (selected) {
            Text(
                text = "当前",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ReaderSettingSlider(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = valueText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color),
    )
}

@Composable
private fun PageBar(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onPage: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { onPage(currentPage - 1) },
            enabled = !isLoading && currentPage > 1,
        ) {
            Text("上一页")
        }
        Text("$currentPage / $totalPages")
        OutlinedButton(
            onClick = { onPage(currentPage + 1) },
            enabled = !isLoading && currentPage < totalPages,
        ) {
            Text("下一页")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderScreen(
    chapter: ReaderChapter?,
    isLoading: Boolean,
    initialScrollProgress: Float,
    darkMode: Boolean,
    onBack: () -> Unit,
    onProgress: (ReadingProgress) -> Unit,
    onOpenChapter: (ChapterLink) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenWeb: (String) -> Unit,
    onRefresh: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    readerThemePreset: ReaderThemePreset,
    readerLayoutSettings: ReaderLayoutSettings,
    controlsVisible: Boolean,
    onControlsVisibleChange: (Boolean) -> Unit,
    cacheProgress: BookCacheProgress?,
    chaptersRefreshing: Boolean,
    commentPosting: Boolean,
    onCacheWholeBook: () -> Unit,
    onRefreshChapters: () -> Unit,
    onPostComment: (String) -> Unit,
    onSystemBarsState: (ReaderSystemBarsState) -> Unit,
    onFeedback: (String, Boolean) -> Unit,
    onReaderShortcutHandlerChange: (((ReaderShortcutDirection) -> Boolean)?) -> Unit,
) {
    var chapterSheetVisible by remember { mutableStateOf(false) }
    var isCommentTextFillFocused by remember { mutableStateOf(false) }
    var imageViewerUrl by remember { mutableStateOf<String?>(null) }
    var commentInput by remember(chapter?.url) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val chapterListState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val colors = readerColors(readerThemePreset)
    val readerFontSize = readerLayoutSettings.fontSizeSp.sp
    val readerLineHeight =
        (readerLayoutSettings.fontSizeSp * readerLayoutSettings.lineHeightMultiplier).sp
    val readerTextStyle = TextStyle(
        fontFamily = readerLayoutSettings.fontFamily.toFontFamily(),
        textIndent = TextIndent(firstLine = (readerLayoutSettings.fontSizeSp * readerLayoutSettings.firstLineIndentEm).sp),
    )
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val showStatusBar = controlsVisible || chapterSheetVisible || imageViewerUrl != null
    val shortcutEnabled =
        chapter != null && !controlsVisible && !chapterSheetVisible && imageViewerUrl == null
    val shortcutPageTurnPercent = readerLayoutSettings.shortcutPageTurnPercent.coerceIn(0.1f, 1f)
    val shortcutAnimationMillis = readerLayoutSettings.shortcutAnimationMillis.coerceIn(100, 1000)
    val latestControlsVisible by rememberUpdatedState(controlsVisible)

    BackHandler {
        if (chapterSheetVisible) {
            chapterSheetVisible = false
        } else {
            onBack()
        }
    }

    LaunchedEffect(showStatusBar, darkMode) {
        onSystemBarsState(
            ReaderSystemBarsState(
                showStatusBar = showStatusBar,
                darkMode = darkMode,
            ),
        )
    }

    LaunchedEffect(chapter?.url, initialScrollProgress) {
        if (chapter == null) return@LaunchedEffect
        val totalItems = snapshotFlow { listState.layoutInfo.totalItemsCount }
            .first { it > 0 }
        if (initialScrollProgress <= 0f) {
            listState.scrollToItem(0)
        } else {
            val targetIndex = ((totalItems - 1) * initialScrollProgress)
                .roundToInt()
                .coerceIn(0, totalItems - 1)
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(chapter?.url, chapter?.detailUrl) {
        val current = chapter ?: return@LaunchedEffect
        val detailUrl = current.detailUrl ?: return@LaunchedEffect
        var lastSaved = -1f
        snapshotFlow { listState.readingProgress() }.collect { scrollProgress ->
            if (abs(scrollProgress - lastSaved) >= 0.01f) {
                lastSaved = scrollProgress
                onProgress(
                    ReadingProgress(
                        detailUrl = detailUrl,
                        bookTitle = current.bookTitle,
                        chapterTitle = current.chapterTitle,
                        chapterUrl = current.url,
                        scrollProgress = scrollProgress,
                    ),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) {
                if (isCommentTextFillFocused) {
                    focusManager.clearFocus()
                }

                onControlsVisibleChange(false)
            }
        }
    }

    LaunchedEffect(chapterSheetVisible, chapter?.url, chapter?.chapters) {
        val current = chapter ?: return@LaunchedEffect
        if (!chapterSheetVisible) return@LaunchedEffect
        val index = current.chapters.indexOfFirst { it.url == current.url }
        if (index >= 0) {
            chapterListState.scrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    DisposableEffect(
        shortcutEnabled,
        shortcutPageTurnPercent,
        shortcutAnimationMillis,
        listState,
        scope
    ) {
        if (shortcutEnabled) {
            onReaderShortcutHandlerChange { direction ->
                val viewportHeight = listState.layoutInfo.viewportSize.height
                    .takeIf { it > 0 }
                    ?: return@onReaderShortcutHandlerChange false
                val distance = viewportHeight * shortcutPageTurnPercent
                val delta = when (direction) {
                    ReaderShortcutDirection.Down -> distance
                    ReaderShortcutDirection.Up -> -distance
                }
                scope.launch {
                    listState.animateScrollBy(
                        value = delta,
                        animationSpec = tween(durationMillis = shortcutAnimationMillis),
                    )
                }
                true
            }
        } else {
            onReaderShortcutHandlerChange(null)
        }

        onDispose {
            onReaderShortcutHandlerChange(null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.page),
    ) {
        when {
            isLoading && chapter == null -> LoadingOverlay()
            chapter == null -> EmptyState(
                text = "章节尚未加载",
                modifier = Modifier.align(Alignment.Center),
            )

            else -> Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            val controlsVisibleNewValue = !controlsVisible
                            onControlsVisibleChange(controlsVisibleNewValue)

                            if (controlsVisibleNewValue && isCommentTextFillFocused) {
                                focusManager.clearFocus()
                            }
                        },
                    contentPadding = PaddingValues(
                        start = readerLayoutSettings.horizontalPaddingDp.dp,
                        top = 44.dp,
                        end = readerLayoutSettings.horizontalPaddingDp.dp,
                        bottom = 44.dp,
                    ),
                ) {
                    item {
                        Text(
                            text = chapter.chapterTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )
                        if (chapter.bookTitle.isNotBlank()) {
                            Text(
                                text = chapter.bookTitle,
                                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                                color = colors.mutedText,
                            )
                        } else {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                    items(chapter.contentBlocks) { block ->
                        when (block) {
                            is ReaderContentBlock.Text -> Text(
                                text = block.text,
                                modifier = Modifier.padding(bottom = readerLayoutSettings.paragraphSpacingDp.dp),
                                color = colors.text,
                                fontSize = readerFontSize,
                                lineHeight = readerLineHeight,
                                style = readerTextStyle,
                            )

                            is ReaderContentBlock.Image -> ReaderImage(
                                block = block,
                                colors = colors,
                                onClick = { imageViewerUrl = block.url },
                            )
                        }
                    }
                    chapter.nextUrl?.let { nextUrl ->
                        item {
                            NextChapterFooterButton(
                                colors = colors,
                                onClick = { onOpenUrl(nextUrl) },
                            )
                        }
                    }
                    item {
                        ReaderCommentsSection(
                            comments = chapter.comments,
                            input = commentInput,
                            posting = commentPosting,
                            colors = colors,
                            onInputChange = { commentInput = it },
                            onTextFillFocusChanged = {
                                isCommentTextFillFocused = it
                                if (it) {
                                    onControlsVisibleChange(false)
                                }
                            },
                            onSubmit = {
                                val text = commentInput.trim()
                                if (text.isNotBlank()) {
                                    commentInput = ""
                                    onPostComment(text)
                                } else {
                                    Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                        )
                    }
                }
                LazyListScrollbar(
                    state = listState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp, top = 44.dp, bottom = 44.dp),
                    thumbColor = colors.accent.copy(alpha = 0.42f),
                    trackColor = colors.disabled.copy(alpha = 0.28f),
                )
            }
        }

        if (chapter != null) {
            ReaderControls(
                visible = controlsVisible,
                chapterTitle = chapter.chapterTitle,
                colors = colors,
                darkMode = darkMode,
                hasPrevious = chapter.previousUrl != null,
                hasNext = chapter.nextUrl != null,
                onBack = onBack,
                onOpenWeb = { onOpenWeb(chapter.url) },
                onMenu = {
                    chapterSheetVisible = true
                },
                onRefresh = onRefresh,
                cacheProgress = cacheProgress,
                onCacheWholeBook = onCacheWholeBook,
                onDarkModeChange = onDarkModeChange,
                onPrevious = { chapter.previousUrl?.let(onOpenUrl) },
                onNext = { chapter.nextUrl?.let(onOpenUrl) },
            )
        }

        if (isLoading && chapter != null) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = colors.accent,
                trackColor = colors.bar,
            )
        }
    }

    if (chapter != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = chapterSheetVisible,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(120)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (darkMode) 0.42f else 0.24f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { chapterSheetVisible = false },
                )
            }
            AnimatedVisibility(
                visible = chapterSheetVisible,
                enter = slideInHorizontally(animationSpec = tween(180)) { -it },
                exit = slideOutHorizontally(animationSpec = tween(180)) { -it },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.86f)
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "目录",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(
                            onClick = onRefreshChapters,
                            enabled = !chaptersRefreshing,
                        ) {
                            if (chaptersRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "刷新目录",
                                )
                            }
                        }
                    }
                    if (chapter?.chapters.isNullOrEmpty()) {
                        EmptyState(
                            "没有解析到目录",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = chapterListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(end = 0.dp),
                            ) {
                                itemsIndexed(chapter!!.chapters) { _, item ->
                                    ChapterListRow(
                                        item = item,
                                        selected = item.url == chapter.url,
                                        onClick = {
                                            chapterSheetVisible = false
                                            if (item.url != chapter.url) {
                                                onOpenChapter(item)
                                            }
                                        },
                                    )
                                }
                            }
                            LazyListScrollbar(
                                state = chapterListState,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 0.dp),
                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            )
                        }
                    }
                }
            }
        }
    }

    val currentImage = imageViewerUrl
    if (currentImage != null) {
        ImageViewer(
            imageUrl = currentImage,
            onDismiss = {
                imageViewerUrl = null
            },
            onSave = {
                scope.launch {
                    onFeedback("保存中...", false)
                    runCatching {
                        saveImageToGallery(context, currentImage)
                        onFeedback("已保存到相册", false)
                    }.getOrElse { error ->
                        onFeedback(error.userMessage("保存失败"), false)
                    }
                }
            },
        )
    }
}

@Composable
private fun ChapterListRow(
    item: ChapterLink,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val mutedColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(background)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Text(
                    text = "当前",
                    modifier = Modifier.padding(start = 12.dp),
                    color = mutedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (item.isCached) {
                Text(
                    text = "已缓存",
                    modifier = Modifier.padding(start = 12.dp),
                    color = mutedColor,
                    fontSize = 12.sp,
                )
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ReaderControls(
    visible: Boolean,
    chapterTitle: String,
    colors: ReaderColors,
    darkMode: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onBack: () -> Unit,
    onOpenWeb: () -> Unit,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    cacheProgress: BookCacheProgress?,
    onCacheWholeBook: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = colors.accent,
        contentColor = Color.White,
        disabledContainerColor = colors.disabled,
        disabledContentColor = colors.disabledText,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (darkMode) 0.28f else 0.16f))
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(animationSpec = tween(180)) { -it } + fadeIn(tween(180)),
            exit = slideOutVertically(animationSpec = tween(160)) { -it } + fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bar)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "返回书架",
                        tint = colors.accent,
                    )
                }
                Text(
                    text = chapterTitle,
                    modifier = Modifier.weight(1f),
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "菜单",
                            tint = colors.accent,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("打开网页") },
                            onClick = {
                                menuExpanded = false
                                onOpenWeb()
                            },
                        )
                        val cacheText =
                            if (cacheProgress?.isRunning == true && cacheProgress.total > 0) {
                                "正在缓存(${cacheProgress.cached}/${cacheProgress.total})"
                            } else if (cacheProgress?.isRunning == true) {
                                "正在缓存"
                            } else {
                                "缓存整本"
                            }
                        DropdownMenuItem(
                            text = { Text(cacheText) },
                            onClick = {
                                menuExpanded = false
                                onCacheWholeBook()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("刷新") },
                            onClick = {
                                menuExpanded = false
                                onRefresh()
                            },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(animationSpec = tween(180)) { it } + fadeIn(tween(180)),
            exit = slideOutVertically(animationSpec = tween(160)) { it } + fadeOut(tween(120)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bar)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = buttonColors,
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "目录")
                }
                Button(
                    onClick = { onDarkModeChange(!darkMode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = buttonColors,
                ) {
                    Icon(
                        imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = if (darkMode) "亮色模式" else "暗色模式",
                    )
                }
                Button(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    enabled = hasPrevious,
                    shape = RoundedCornerShape(8.dp),
                    colors = buttonColors,
                ) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "上一章")
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = hasNext,
                    shape = RoundedCornerShape(8.dp),
                    colors = buttonColors,
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "下一章")
                }
            }
        }
    }
}

@Composable
private fun NextChapterFooterButton(
    colors: ReaderColors,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "下一章",
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 12.dp)
                .clickable(onClick = onClick)
                .padding(4.dp),
            color = colors.accent,
            textDecoration = TextDecoration.Underline,
        )
    }
}

@Composable
private fun ReaderCommentsSection(
    comments: List<ReaderComment>,
    input: String,
    posting: Boolean,
    colors: ReaderColors,
    onTextFillFocusChanged: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
    ) {
        HorizontalDivider(color = colors.disabled.copy(alpha = 0.7f))
        Text(
            text = "评论",
            modifier = Modifier.padding(top = 18.dp),
            color = colors.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .onFocusChanged({ onTextFillFocusChanged(it.isFocused) })
                .padding(top = 12.dp)
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 18.dp),
            enabled = !posting,
            minLines = 1,
            maxLines = 4,
            placeholder = { Text("发表评论") },
            trailingIcon = {
                Box(modifier = Modifier.fillMaxHeight()) {
                    if (posting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp,
                            color = colors.accent,
                        )
                    } else {
                        IconButton(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            enabled = input.isNotBlank(),
                            onClick = onSubmit,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddComment,
                                contentDescription = "发送",
                                tint = if (input.isNotBlank()) colors.accent else colors.disabledText,
                            )
                        }
                    }
                }
            }
        )

        if (comments.isEmpty()) {
            Text(
                text = "暂无评论",
                modifier = Modifier.padding(top = 18.dp),
                color = colors.mutedText,
                fontSize = 14.sp,
            )
        } else {
            comments.forEach { comment ->
                ReaderCommentRow(
                    comment = comment,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun ReaderCommentRow(
    comment: ReaderComment,
    colors: ReaderColors,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
    ) {
        Text(
            text = comment.username,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
        if (comment.quote.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.disabled.copy(alpha = 0.34f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                comment.quote.forEach { block ->
                    CommentBlockText(
                        block = block,
                        color = colors.mutedText,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        comment.content.forEach { block ->
            CommentBlockText(
                block = block,
                color = colors.text,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CommentBlockText(
    block: CommentBlock,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = block.toAnnotatedString(),
        modifier = modifier.fillMaxWidth(),
        color = color,
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.55f).sp,
    )
}

private fun CommentBlock.toAnnotatedString() = buildAnnotatedString {
    parts.forEach { part ->
        val start = length
        append(part.text)
        if (part.strikeThrough) {
            addStyle(
                style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                start = start,
                end = length,
            )
        }
    }
}

@Composable
private fun LazyListScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thumbColor: Color,
    trackColor: Color,
) {
    val layoutInfo = state.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo
    if (totalItems <= 1 || visibleItems.isEmpty()) return
    if (!state.canScrollBackward && !state.canScrollForward) return

    val progress = state.readingProgress()

    BoxWithConstraints(
        modifier = modifier
            .width(3.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor),
    ) {
        val density = LocalDensity.current
        val maxHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val thumbHeightPx = with(density) { 48.dp.toPx() }.coerceAtMost(maxHeightPx)
        val thumbOffsetPx = (progress * (maxHeightPx - thumbHeightPx))
            .coerceIn(0f, maxHeightPx - thumbHeightPx)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { thumbHeightPx.toDp() })
                .graphicsLayer {
                    translationY = thumbOffsetPx
                }
                .clip(RoundedCornerShape(999.dp))
                .background(thumbColor),
        )
    }
}

@Composable
private fun ReaderImage(
    block: ReaderContentBlock.Image,
    colors: ReaderColors,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    SubcomposeAsyncImage(
        model = block.url,
        contentDescription = block.alt.ifBlank { "章节图片" },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        loading = {
            ImageLoadingPlaceholder(colors)
        },
        error = {
            Text(
                text = "图片加载失败",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.disabled, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 24.dp),
                color = colors.disabledText,
            )
        },
    )
}

@Composable
private fun ImageLoadingPlaceholder(colors: ReaderColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(colors.disabled, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            strokeWidth = 2.5.dp,
            color = colors.accent,
        )
    }
}

@Composable
private fun ImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(imageUrl) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val nextScale = (scale * zoom).coerceIn(1f, 5f)
                    scale = nextScale
                    offset = if (nextScale == 1f) Offset.Zero else offset + pan
                }
            },
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "图片预览",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("图片加载失败", color = Color.White)
                }
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.58f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "保存到相册",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.62f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

private fun LazyListState.readingProgress(): Float {
    val info = layoutInfo
    val total = info.totalItemsCount
    if (total <= 1) return 0f
    if (!canScrollBackward && !canScrollForward) return 0f
    if (!canScrollForward) return 1f

    val firstVisibleItem = info.visibleItemsInfo.firstOrNull() ?: return 0f
    val itemProgress =
        firstVisibleItemScrollOffset.toFloat() / firstVisibleItem.size.coerceAtLeast(1).toFloat()
    return ((firstVisibleItemIndex + itemProgress) / (total - 1).toFloat()).coerceIn(0f, 1f)
}

private fun ReaderFontFamily.toFontFamily(): FontFamily? {
    return when (this) {
        ReaderFontFamily.SYSTEM -> null
        ReaderFontFamily.SERIF -> FontFamily.Serif
        ReaderFontFamily.SANS_SERIF -> FontFamily.SansSerif
        ReaderFontFamily.MONOSPACE -> FontFamily.Monospace
    }
}

private fun Float.formatOneDecimal(): String {
    val tenths = (this * 10f).roundToInt()
    val whole = tenths / 10
    val decimal = abs(tenths % 10)
    return if (decimal == 0) {
        whole.toString()
    } else {
        "$whole.$decimal"
    }
}

private fun readerColors(darkMode: Boolean): ReaderColors {
    return readerColors(if (darkMode) ReaderThemePreset.NIGHT else ReaderThemePreset.PAPER)
}

private fun readerColors(preset: ReaderThemePreset): ReaderColors {
    return when (preset) {
        ReaderThemePreset.PURE_WHITE -> ReaderColors(
            page = Color.White,
            text = Color.Black,
            mutedText = Color(0xFF555555),
            bar = Color(0xF7FFFFFF),
            accent = Color.Black,
            disabled = Color(0xFFE3E3E3),
            disabledText = Color(0xFF707070),
        )

        ReaderThemePreset.PAPER -> ReaderColors(
            page = ReaderPage,
            text = ReaderText,
            mutedText = ReaderMutedText,
            bar = ReaderBar,
            accent = ReaderAccent,
            disabled = ReaderDisabled,
            disabledText = ReaderDisabledText,
        )

        ReaderThemePreset.WARM -> ReaderColors(
            page = Color(0xFFFFF4D8),
            text = Color(0xFF2F2216),
            mutedText = Color(0xFF7A664E),
            bar = Color(0xF7FFF0C8),
            accent = Color(0xFF6E4E20),
            disabled = Color(0xFFE6D5B2),
            disabledText = Color(0xFF806B4E),
        )

        ReaderThemePreset.MINT -> ReaderColors(
            page = Color(0xFFF0F8EE),
            text = Color(0xFF1F2A22),
            mutedText = Color(0xFF5D6E61),
            bar = Color(0xF7F4FBF0),
            accent = Color(0xFF3E684D),
            disabled = Color(0xFFD7E4D6),
            disabledText = Color(0xFF657466),
        )

        ReaderThemePreset.PURE_BLACK -> ReaderColors(
            page = Color.Black,
            text = Color.White,
            mutedText = Color(0xFFB8B8B8),
            bar = Color(0xF7000000),
            accent = Color.White,
            disabled = Color(0xFF2B2B2B),
            disabledText = Color(0xFF8E8E8E),
        )

        ReaderThemePreset.NIGHT -> ReaderColors(
            page = ReaderDarkPage,
            text = ReaderDarkText,
            mutedText = ReaderDarkMutedText,
            bar = ReaderDarkBar,
            accent = ReaderDarkAccent,
            disabled = ReaderDarkDisabled,
            disabledText = ReaderDarkDisabledText,
        )

        ReaderThemePreset.COFFEE -> ReaderColors(
            page = Color(0xFF211A15),
            text = Color(0xFFEBD8C2),
            mutedText = Color(0xFFBBA28B),
            bar = Color(0xF72B211B),
            accent = Color(0xFFD3A56F),
            disabled = Color(0xFF45372C),
            disabledText = Color(0xFF9E8670),
        )

        ReaderThemePreset.SLATE -> ReaderColors(
            page = Color(0xFF171A1F),
            text = Color(0xFFE1E5EA),
            mutedText = Color(0xFFA8B1BD),
            bar = Color(0xF720242B),
            accent = Color(0xFF9DB7E8),
            disabled = Color(0xFF343A44),
            disabledText = Color(0xFF8C96A5),
        )
    }
}

private fun relativeUpdateText(raw: String): String {
    val text = raw.trim()
    if (text.isBlank()) return ""
    val date = parseUpdateDate(text) ?: return text
    val days = Duration.between(date.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
    return when {
        days <= 0L -> "今天更新"
        days == 1L -> "一天前更新"
        days < 7L -> "${days}天前更新"
        days < 14L -> "一周前更新"
        days < 30L -> "${days / 7}周前更新"
        days < 365L -> "${days / 30}个月前更新"
        else -> "${days / 365}年前更新"
    }
}

private fun parseUpdateDate(text: String): LocalDate? {
    val normalized = text.replace('/', '-')
    val patterns = listOf(
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy-M-d H:mm",
        "yyyy-M-d H:mm:ss",
        "yyyy-M-d",
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        val formatter = DateTimeFormatter.ofPattern(pattern)
        runCatching {
            if (pattern.contains("H")) {
                LocalDateTime.parse(normalized, formatter).toLocalDate()
            } else {
                LocalDate.parse(normalized, formatter)
            }
        }.getOrNull()
    }
}

private fun screenTransition(initial: Screen, target: Screen): ContentTransform {
    val duration = 220
    val enteringReader = target is Screen.Reader && initial !is Screen.Reader
    val leavingReader = initial is Screen.Reader && target !is Screen.Reader

    return when {
        enteringReader -> (slideInHorizontally(animationSpec = tween(duration)) { it } + fadeIn(
            tween(duration)
        ))
            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 } + fadeOut(
                tween(duration)
            ))

        leavingReader -> (slideInHorizontally(animationSpec = tween(duration)) { -it / 3 } + fadeIn(
            tween(duration)
        ))
            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { it } + fadeOut(
                tween(duration)
            ))

        else -> (fadeIn(tween(duration))).togetherWith(fadeOut(tween(duration)))
    }
}

private fun Throwable.userMessage(prefix: String): String {
    if (this is LoginExpiredException) {
        return "登录凭证已失效，请重新登录"
    }
    val detail = message?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
    return "$prefix：$detail"
}

private suspend fun saveImageToGallery(context: android.content.Context, imageUrl: String) =
    withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request) as? SuccessResult
            ?: throw IOException("图片加载失败")
        val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            ?: throw IOException("图片格式不支持保存")

        val fileName = "community_esj_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/CommunityESJ"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("无法创建相册文件")
        runCatching {
            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("图片写入失败")
                }
            } ?: throw IOException("无法打开相册文件")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.onFailure {
            resolver.delete(uri, null, null)
            throw it
        }
    }

private suspend fun saveEpubToDownloads(context: android.content.Context, export: EpubExport) =
    withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, export.fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/epub+zip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("无法创建下载文件")
        runCatching {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(export.bytes)
                stream.flush()
            } ?: throw IOException("无法打开下载文件")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.onFailure {
            resolver.delete(uri, null, null)
            throw it
        }
    }
