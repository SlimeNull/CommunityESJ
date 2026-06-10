package com.silmenull.communityesj.data

data class BookshelfPage(
    val books: List<BookItem>,
    val currentPage: Int,
    val totalPages: Int,
)

data class BookItem(
    val title: String,
    val latestChapter: String,
    val lastReadChapter: String,
    val updateDate: String,
    val detailUrl: String?,
    val latestChapterUrl: String?,
    val lastReadChapterUrl: String?,
)

data class ChapterLink(
    val title: String,
    val url: String,
    val isCached: Boolean = false,
)

data class ReaderChapter(
    val url: String,
    val bookTitle: String,
    val chapterTitle: String,
    val contentBlocks: List<ReaderContentBlock>,
    val chapters: List<ChapterLink>,
    val previousUrl: String?,
    val nextUrl: String?,
    val detailUrl: String?,
) {
    val paragraphs: List<String>
        get() = contentBlocks.mapNotNull { (it as? ReaderContentBlock.Text)?.text }
}

sealed interface ReaderContentBlock {
    data class Text(val text: String) : ReaderContentBlock
    data class Image(val url: String, val alt: String = "") : ReaderContentBlock
}

data class LoginResult(
    val success: Boolean,
    val message: String,
)

data class ReadingProgress(
    val detailUrl: String,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterUrl: String,
    val scrollProgress: Float,
)

enum class LoginSessionState {
    VALID,
    EXPIRED,
    MISSING,
}
