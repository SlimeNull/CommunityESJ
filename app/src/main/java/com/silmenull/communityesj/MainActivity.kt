package com.silmenull.communityesj

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.net.Uri
import android.os.Bundle
import android.content.ContentValues
import android.os.Environment
import android.view.autofill.AutofillManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silmenull.communityesj.data.BookItem
import com.silmenull.communityesj.data.BookshelfPage
import com.silmenull.communityesj.data.ChapterLink
import com.silmenull.communityesj.data.EsjRepository
import com.silmenull.communityesj.data.EsjHost
import com.silmenull.communityesj.data.LoginExpiredException
import com.silmenull.communityesj.data.LoginSessionState
import com.silmenull.communityesj.data.ReaderChapter
import com.silmenull.communityesj.data.ReaderContentBlock
import com.silmenull.communityesj.data.ReadingProgress
import com.silmenull.communityesj.ui.theme.CommunityESJTheme
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommunityESJTheme {
                EsjReaderApp()
            }
        }
    }
}

private sealed interface Screen {
    data object Login : Screen
    data object Bookshelf : Screen
    data class Reader(val chapterUrl: String, val detailUrlHint: String? = null) : Screen
}

@Stable
private class AppState(
    val repository: EsjRepository,
) {
    var screen by mutableStateOf<Screen>(Screen.Login)
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var bookshelf by mutableStateOf(BookshelfPage(emptyList(), 1, 1))
    var readerChapter by mutableStateOf<ReaderChapter?>(null)
    var currentPage by mutableIntStateOf(1)
    var readerInitialProgress by mutableFloatStateOf(0f)
    var readerScrollProgress by mutableFloatStateOf(0f)
    var readerDarkMode by mutableStateOf(repository.isReaderDarkMode())
    var bookshelfReloginAction by mutableStateOf(false)
    var selectedHost by mutableStateOf(repository.currentHost())
}

@Composable
private fun EsjReaderApp() {
    val context = LocalContext.current
    val appState = remember(context) { AppState(EsjRepository(context)) }
    val scope = rememberCoroutineScope()
    val autofillManager = remember(context) {
        context.getSystemService(AutofillManager::class.java)
    }

    fun openWeb(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure { error ->
            appState.message = error.userMessage("无法打开网页")
        }
    }

    fun switchHost(host: EsjHost) {
        if (!appState.repository.switchHost(host)) return
        appState.selectedHost = appState.repository.currentHost()
        appState.screen = Screen.Login
        appState.message = "已切换到${host.displayName},请重新登录"
        appState.bookshelfReloginAction = false
        appState.readerChapter = null
        appState.bookshelf = BookshelfPage(emptyList(), 1, 1)
        appState.currentPage = 1
    }

    fun loadBookshelf(page: Int = 1, goLoginOnFailure: Boolean = false) {
        scope.launch {
            appState.isLoading = true
            appState.message = null
            appState.bookshelfReloginAction = false
            runCatching {
                appState.repository.loadBookshelf(page)
            }.onSuccess { result ->
                appState.bookshelf = result
                appState.currentPage = result.currentPage
                appState.screen = Screen.Bookshelf
            }.onFailure { error ->
                val loginExpired = error is LoginExpiredException
                if (loginExpired) {
                    appState.repository.logout()
                }
                if (goLoginOnFailure && loginExpired) {
                    appState.screen = Screen.Login
                    appState.message = "登录凭证已过期,请重新登录"
                } else {
                    appState.screen = Screen.Bookshelf
                    appState.bookshelfReloginAction = true
                    appState.message = error.userMessage("加载书架失败")
                }
            }
            appState.isLoading = false
        }
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
            appState.message = null
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
                appState.message = error.userMessage("加载章节失败")
            }
            appState.isLoading = false
        }
    }

    fun openBook(book: BookItem) {
        val detailUrl = book.detailUrl
        if (detailUrl == null) {
            appState.message = "这本书缺少目录链接"
            return
        }

        scope.launch {
            appState.isLoading = true
            appState.message = null
            runCatching {
                appState.repository.resolveBookStart(book)
            }.onSuccess { chapter ->
                if (chapter == null) {
                    appState.message = "没有找到可阅读章节"
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
                appState.message = error.userMessage("加载目录失败")
                appState.isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        when (appState.repository.loginSessionState()) {
            LoginSessionState.VALID -> loadBookshelf(goLoginOnFailure = true)
            LoginSessionState.EXPIRED -> appState.message = "登录凭证已过期,请重新登录"
            LoginSessionState.MISSING -> Unit
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = appState.screen,
            transitionSpec = { screenTransition(initialState, targetState) },
            label = "screen_transition",
        ) { screen ->
            when (screen) {
                Screen.Login -> LoginScreen(
                    isLoading = appState.isLoading,
                    message = appState.message,
                    currentHost = appState.selectedHost,
                    onHostChange = ::switchHost,
                    onLogin = { email, password ->
                        scope.launch {
                            appState.isLoading = true
                            appState.message = null
                            appState.bookshelfReloginAction = false
                            runCatching {
                                appState.repository.login(email, password)
                            }.onSuccess { result ->
                                appState.message = result.message
                                if (result.success) {
                                    autofillManager?.commit()
                                    loadBookshelf()
                                }
                            }.onFailure { error ->
                                appState.message = error.userMessage("登录失败")
                            }
                            appState.isLoading = false
                        }
                    },
                )

                Screen.Bookshelf -> BookshelfScreen(
                    page = appState.bookshelf,
                    isLoading = appState.isLoading,
                    message = appState.message,
                    onRefresh = { loadBookshelf(appState.currentPage) },
                    onPage = { loadBookshelf(it) },
                    onOpenBook = ::openBook,
                    onOpenWeb = { openWeb(appState.repository.favoriteUrl()) },
                    currentHost = appState.selectedHost,
                    onHostChange = ::switchHost,
                    showReloginAction = appState.bookshelfReloginAction,
                    onRelogin = {
                        appState.repository.logout()
                        appState.screen = Screen.Login
                        appState.message = null
                        appState.bookshelfReloginAction = false
                    },
                    onOpenGithub = { openWeb("https://github.com/SlimeNull/CommunityESJ") },
                    onLogout = {
                        appState.repository.logout()
                        appState.screen = Screen.Login
                        appState.message = null
                        appState.bookshelfReloginAction = false
                        appState.readerChapter = null
                        appState.bookshelf = BookshelfPage(emptyList(), 1, 1)
                    },
                )

                is Screen.Reader -> ReaderScreen(
                    chapter = appState.readerChapter,
                    isLoading = appState.isLoading,
                    message = appState.message,
                    initialScrollProgress = appState.readerInitialProgress,
                    darkMode = appState.readerDarkMode,
                    onBack = {
                        appState.screen = Screen.Bookshelf
                        appState.message = null
                    },
                    onProgress = { progress ->
                        appState.readerScrollProgress = progress.scrollProgress
                        appState.repository.saveProgress(progress)
                    },
                    onOpenChapter = { chapter ->
                        openReader(chapter.url, appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                    },
                    onOpenUrl = { url ->
                        openReader(url, appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                    },
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
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun LoginScreen(
    isLoading: Boolean,
    message: String?,
    currentHost: EsjHost,
    onHostChange: (EsjHost) -> Unit,
    onLogin: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canSubmit = !isLoading && email.isNotBlank() && password.isNotBlank()

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
            verticalArrangement = Arrangement.Center,
        ) {
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
            Text(
                text = currentHost.displayName,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .autofill(
                        autofillTypes = listOf(AutofillType.EmailAddress, AutofillType.Username),
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
                onValueChange = { password = it },
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
            Spacer(Modifier.height(20.dp))
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
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
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
    message: String?,
    onRefresh: () -> Unit,
    onPage: (Int) -> Unit,
    onOpenBook: (BookItem) -> Unit,
    onOpenWeb: () -> Unit,
    currentHost: EsjHost,
    onHostChange: (EsjHost) -> Unit,
    showReloginAction: Boolean,
    onRelogin: () -> Unit,
    onOpenGithub: () -> Unit,
    onLogout: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var aboutVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书架") },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !isLoading) { Text("刷新") }
                    Box {
                        TextButton(onClick = { menuExpanded = true }) { Text("菜单") }
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
                if (message != null) {
                    item {
                        BookshelfMessage(
                            message = message,
                            showReloginAction = showReloginAction,
                            onRelogin = onRelogin,
                        )
                    }
                }
                if (!isLoading && page.books.isEmpty()) {
                    item {
                        EmptyState(
                            text = "书架为空或未解析到书籍",
                            modifier = Modifier.padding(top = 64.dp),
                        )
                    }
                }
                items(page.books) { book ->
                    BookRow(book = book, onClick = { onOpenBook(book) })
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

@Composable
private fun BookRow(
    book: BookItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        InfoLine(label = "最新章节", value = book.latestChapter)
        InfoLine(label = "最后观看", value = book.lastReadChapter)
        InfoLine(label = "更新日期", value = book.updateDate)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.padding(end = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Text(
            text = value.ifBlank { "-" },
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
    message: String?,
    initialScrollProgress: Float,
    darkMode: Boolean,
    onBack: () -> Unit,
    onProgress: (ReadingProgress) -> Unit,
    onOpenChapter: (ChapterLink) -> Unit,
    onOpenUrl: (String) -> Unit,
    onRefresh: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(false) }
    var chapterSheetVisible by remember { mutableStateOf(false) }
    var imageViewerUrl by remember { mutableStateOf<String?>(null) }
    var imageSaveMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val chapterListState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }
    val colors = readerColors(darkMode)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler(onBack = onBack)

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

    LaunchedEffect(chapterSheetVisible, chapter?.url, chapter?.chapters) {
        val current = chapter ?: return@LaunchedEffect
        if (!chapterSheetVisible) return@LaunchedEffect
        val index = current.chapters.indexOfFirst { it.url == current.url }
        if (index >= 0) {
            chapterListState.scrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.page)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { controlsVisible = !controlsVisible },
    ) {
        when {
            isLoading && chapter == null -> LoadingOverlay()
            chapter == null -> EmptyState(
                text = message ?: "章节尚未加载",
                modifier = Modifier.align(Alignment.Center),
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, top = 44.dp, end = 22.dp, bottom = 44.dp),
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
                            modifier = Modifier.padding(bottom = 14.dp),
                            color = colors.text,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                        )

                        is ReaderContentBlock.Image -> ReaderImage(
                            block = block,
                            colors = colors,
                            onClick = { imageViewerUrl = block.url },
                        )
                    }
                }
                if (message != null) {
                    item { MessageText(message) }
                }
                chapter.nextUrl?.let { nextUrl ->
                    item {
                        NextChapterFooterButton(
                            colors = colors,
                            onClick = { onOpenUrl(nextUrl) },
                        )
                    }
                }
            }
        }

        if (chapter != null) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(120)),
            ) {
                ReaderControls(
                    chapterTitle = chapter.chapterTitle,
                    colors = colors,
                    darkMode = darkMode,
                    hasPrevious = chapter.previousUrl != null,
                    hasNext = chapter.nextUrl != null,
                    onBack = onBack,
                    onMenu = {
                        controlsVisible = false
                        chapterSheetVisible = true
                    },
                    onRefresh = onRefresh,
                    onDarkModeChange = onDarkModeChange,
                    onPrevious = { chapter.previousUrl?.let(onOpenUrl) },
                    onNext = { chapter.nextUrl?.let(onOpenUrl) },
                )
            }
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

    if (chapterSheetVisible && chapter != null) {
        ModalBottomSheet(onDismissRequest = { chapterSheetVisible = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "目录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                if (chapter.chapters.isEmpty()) {
                    EmptyState("没有解析到目录", modifier = Modifier.fillMaxWidth().padding(top = 48.dp))
                } else {
                    LazyColumn(
                        state = chapterListState,
                    ) {
                        itemsIndexed(chapter.chapters) { _, item ->
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
                }
            }
        }
    }

    val currentImage = imageViewerUrl
    if (currentImage != null) {
        ImageViewer(
            imageUrl = currentImage,
            message = imageSaveMessage,
            onDismiss = {
                imageViewerUrl = null
                imageSaveMessage = null
            },
            onSave = {
                scope.launch {
                    imageSaveMessage = "保存中..."
                    imageSaveMessage = runCatching {
                        saveImageToGallery(context, currentImage)
                        "已保存到相册"
                    }.getOrElse { error ->
                        error.userMessage("保存失败")
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
    chapterTitle: String,
    colors: ReaderColors,
    darkMode: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
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
            .background(Color.Black.copy(alpha = if (darkMode) 0.28f else 0.16f)),
    ) {
        AnimatedVisibility(
            visible = true,
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
                TextButton(onClick = onBack) {
                    Text("书架", color = colors.accent)
                }
                Text(
                    text = chapterTitle,
                    modifier = Modifier.weight(1f),
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box {
                    TextButton(onClick = { menuExpanded = true }) {
                        Text("菜单", color = colors.accent)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
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
            visible = true,
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 28.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text("下一章")
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
    message: String?,
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
        if (message != null) {
            Text(
                text = message,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(18.dp)
                    .background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun BookshelfMessage(
    message: String,
    showReloginAction: Boolean,
    onRelogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
        )
        if (showReloginAction) {
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "你可以尝试",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "重新登录",
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .clickable(onClick = onRelogin),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

@Composable
private fun MessageText(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.error,
    )
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
    val viewport = (info.viewportEndOffset - info.viewportStartOffset).coerceAtLeast(1)
    val itemProgress = firstVisibleItemScrollOffset.toFloat() / viewport.toFloat()
    return ((firstVisibleItemIndex + itemProgress) / (total - 1).toFloat()).coerceIn(0f, 1f)
}

private fun readerColors(darkMode: Boolean): ReaderColors {
    return if (darkMode) {
        ReaderColors(
            page = ReaderDarkPage,
            text = ReaderDarkText,
            mutedText = ReaderDarkMutedText,
            bar = ReaderDarkBar,
            accent = ReaderDarkAccent,
            disabled = ReaderDarkDisabled,
            disabledText = ReaderDarkDisabledText,
        )
    } else {
        ReaderColors(
            page = ReaderPage,
            text = ReaderText,
            mutedText = ReaderMutedText,
            bar = ReaderBar,
            accent = ReaderAccent,
            disabled = ReaderDisabled,
            disabledText = ReaderDisabledText,
        )
    }
}

private fun screenTransition(initial: Screen, target: Screen): ContentTransform {
    val duration = 220
    val enteringReader = target is Screen.Reader && initial !is Screen.Reader
    val leavingReader = initial is Screen.Reader && target !is Screen.Reader

    return when {
        enteringReader -> (slideInHorizontally(animationSpec = tween(duration)) { it } + fadeIn(tween(duration)))
            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { -it / 3 } + fadeOut(tween(duration)))

        leavingReader -> (slideInHorizontally(animationSpec = tween(duration)) { -it / 3 } + fadeIn(tween(duration)))
            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { it } + fadeOut(tween(duration)))

        else -> (fadeIn(tween(duration))).togetherWith(fadeOut(tween(duration)))
    }
}

private fun Throwable.userMessage(prefix: String): String {
    val detail = message?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
    return "$prefix：$detail"
}

private suspend fun saveImageToGallery(context: android.content.Context, imageUrl: String) = withContext(Dispatchers.IO) {
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
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CommunityESJ")
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
