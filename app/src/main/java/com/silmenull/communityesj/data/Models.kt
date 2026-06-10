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
)

data class ReaderChapter(
    val bookTitle: String,
    val chapterTitle: String,
    val paragraphs: List<String>,
    val chapters: List<ChapterLink>,
    val previousUrl: String?,
    val nextUrl: String?,
    val detailUrl: String?,
)

data class LoginResult(
    val success: Boolean,
    val message: String,
)
