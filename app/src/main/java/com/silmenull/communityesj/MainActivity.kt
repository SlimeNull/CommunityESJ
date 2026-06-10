package com.silmenull.communityesj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silmenull.communityesj.data.BookItem
import com.silmenull.communityesj.data.BookshelfPage
import com.silmenull.communityesj.data.ChapterLink
import com.silmenull.communityesj.data.EsjRepository
import com.silmenull.communityesj.data.ReaderChapter
import com.silmenull.communityesj.ui.theme.CommunityESJTheme
import kotlinx.coroutines.launch

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
}

@Composable
private fun EsjReaderApp() {
    val appState = remember { AppState(EsjRepository()) }
    val scope = rememberCoroutineScope()

    fun loadBookshelf(page: Int = 1) {
        scope.launch {
            appState.isLoading = true
            appState.message = null
            runCatching {
                appState.repository.loadBookshelf(page)
            }.onSuccess { result ->
                appState.bookshelf = result
                appState.currentPage = result.currentPage
                appState.screen = Screen.Bookshelf
            }.onFailure { error ->
                appState.message = error.userMessage("加载书架失败")
            }
            appState.isLoading = false
        }
    }

    fun openReader(url: String, detailUrlHint: String? = null) {
        appState.screen = Screen.Reader(url, detailUrlHint)
        scope.launch {
            appState.isLoading = true
            appState.message = null
            appState.readerChapter = null
            runCatching {
                appState.repository.loadReader(url, detailUrlHint)
            }.onSuccess { chapter ->
                appState.readerChapter = chapter
            }.onFailure { error ->
                appState.message = error.userMessage("加载章节失败")
            }
            appState.isLoading = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val screen = appState.screen) {
            Screen.Login -> LoginScreen(
                isLoading = appState.isLoading,
                message = appState.message,
                onLogin = { email, password ->
                    scope.launch {
                        appState.isLoading = true
                        appState.message = null
                        runCatching {
                            appState.repository.login(email, password)
                        }.onSuccess { result ->
                            appState.message = result.message
                            if (result.success) {
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
                onOpenBook = { book ->
                    val chapterUrl = book.lastReadChapterUrl ?: book.latestChapterUrl
                    if (chapterUrl != null) {
                        openReader(chapterUrl, book.detailUrl)
                    } else if (book.detailUrl != null) {
                        scope.launch {
                            appState.isLoading = true
                            appState.message = null
                            runCatching {
                                appState.repository.loadFirstChapterFromDetail(book.detailUrl)
                            }.onSuccess { firstChapter ->
                                if (firstChapter == null) {
                                    appState.message = "没有找到可阅读章节"
                                } else {
                                    openReader(firstChapter, book.detailUrl)
                                }
                            }.onFailure { error ->
                                appState.message = error.userMessage("加载目录失败")
                            }
                            appState.isLoading = false
                        }
                    } else {
                        appState.message = "这本书缺少可打开的链接"
                    }
                },
                onLogout = {
                    appState.screen = Screen.Login
                    appState.message = null
                    appState.readerChapter = null
                    appState.bookshelf = BookshelfPage(emptyList(), 1, 1)
                },
            )

            is Screen.Reader -> ReaderScreen(
                chapter = appState.readerChapter,
                isLoading = appState.isLoading,
                message = appState.message,
                onBack = {
                    appState.screen = Screen.Bookshelf
                    appState.message = null
                },
                onOpenChapter = { chapter ->
                    openReader(chapter.url, appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                },
                onOpenUrl = { url ->
                    openReader(url, appState.readerChapter?.detailUrl ?: screen.detailUrlHint)
                },
            )
        }
    }
}

@Composable
private fun LoginScreen(
    isLoading: Boolean,
    message: String?,
    onLogin: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                text = "CommunityESJ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "ESJ Zone 阅读客户端",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("邮箱") },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onLogin(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookshelfScreen(
    page: BookshelfPage,
    isLoading: Boolean,
    message: String?,
    onRefresh: () -> Unit,
    onPage: (Int) -> Unit,
    onOpenBook: (BookItem) -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书架") },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !isLoading) { Text("刷新") }
                    TextButton(onClick = onLogout) { Text("退出") }
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
                    item { MessageText(message) }
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
    onBack: () -> Unit,
    onOpenChapter: (ChapterLink) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(false) }
    var chapterSheetVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6EC))
            .clickable { controlsVisible = !controlsVisible },
    ) {
        when {
            isLoading && chapter == null -> LoadingOverlay()
            chapter == null -> EmptyState(
                text = message ?: "章节尚未加载",
                modifier = Modifier.align(Alignment.Center),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, top = 44.dp, end = 22.dp, bottom = 44.dp),
            ) {
                item {
                    Text(
                        text = chapter.chapterTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF241D16),
                        fontWeight = FontWeight.Bold,
                    )
                    if (chapter.bookTitle.isNotBlank()) {
                        Text(
                            text = chapter.bookTitle,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                            color = Color(0xFF6B5E51),
                        )
                    } else {
                        Spacer(Modifier.height(24.dp))
                    }
                }
                items(chapter.paragraphs) { paragraph ->
                    Text(
                        text = paragraph,
                        modifier = Modifier.padding(bottom = 14.dp),
                        color = Color(0xFF241D16),
                        fontSize = 19.sp,
                        lineHeight = 32.sp,
                    )
                }
                if (message != null) {
                    item { MessageText(message) }
                }
            }
        }

        if (controlsVisible && chapter != null) {
            ReaderControls(
                chapterTitle = chapter.chapterTitle,
                hasPrevious = chapter.previousUrl != null,
                hasNext = chapter.nextUrl != null,
                onBack = onBack,
                onMenu = {
                    controlsVisible = false
                    chapterSheetVisible = true
                },
                onPrevious = { chapter.previousUrl?.let(onOpenUrl) },
                onNext = { chapter.nextUrl?.let(onOpenUrl) },
            )
        }

        if (isLoading && chapter != null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    LazyColumn {
                        items(chapter.chapters) { item ->
                            Text(
                                text = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        chapterSheetVisible = false
                                        onOpenChapter(item)
                                    }
                                    .padding(vertical = 14.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderControls(
    chapterTitle: String,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEE201B16))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text("书架", color = Color.White)
            }
            Text(
                text = chapterTitle,
                modifier = Modifier.weight(1f),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xEE201B16))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onMenu,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("目录")
            }
            Button(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = hasPrevious,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("上一章")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = hasNext,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("下一章")
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

private fun Throwable.userMessage(prefix: String): String {
    val detail = message?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
    return "$prefix：$detail"
}
