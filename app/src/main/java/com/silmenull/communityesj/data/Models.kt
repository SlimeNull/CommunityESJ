package com.silmenull.communityesj.data

data class BookshelfPage(
    val books: List<BookItem>,
    val currentPage: Int,
    val totalPages: Int,
    val username: String = "",
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
    val comments: List<ReaderComment> = emptyList(),
) {
    val paragraphs: List<String>
        get() = contentBlocks.mapNotNull { (it as? ReaderContentBlock.Text)?.text }
}

data class ReaderComment(
    val id: String,
    val username: String,
    val content: List<CommentBlock>,
    val quote: List<CommentBlock> = emptyList(),
)

data class CommentBlock(
    val parts: List<CommentTextPart>,
)

data class CommentTextPart(
    val text: String,
    val strikeThrough: Boolean = false,
)

sealed interface ReaderContentBlock {
    data class Text(val text: String) : ReaderContentBlock
    data class Image(val url: String, val alt: String = "") : ReaderContentBlock
}

data class LoginResult(
    val success: Boolean,
    val message: String,
)

data class RememberedLogin(
    val enabled: Boolean,
    val email: String,
    val password: String,
)

data class ReadingProgress(
    val detailUrl: String,
    val bookTitle: String,
    val chapterTitle: String,
    val chapterUrl: String,
    val scrollProgress: Float,
)

data class ReaderLayoutSettings(
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SYSTEM,
    val fontSizeSp: Float = 19f,
    val lineHeightMultiplier: Float = 1.3f,
    val paragraphSpacingDp: Float = 18f,
    val firstLineIndentEm: Float = 1.5f,
    val horizontalPaddingDp: Float = 22f,
    val shortcutPageTurnPercent: Float = 0.8f,
    val shortcutAnimationMillis: Int = 300,
)

data class EpubExport(
    val fileName: String,
    val bytes: ByteArray,
)

enum class ReaderFontFamily(
    val displayName: String,
) {
    SYSTEM("系统默认"),
    SERIF("衬线"),
    SANS_SERIF("无衬线"),
    MONOSPACE("等宽");

    companion object {
        fun fromName(name: String?): ReaderFontFamily {
            return entries.firstOrNull { it.name == name } ?: SYSTEM
        }
    }
}

data class BookCacheProgress(
    val detailUrl: String,
    val cached: Int,
    val total: Int,
    val isRunning: Boolean,
) {
    val isComplete: Boolean
        get() = total > 0 && cached >= total
}

enum class LoginSessionState {
    VALID,
    EXPIRED,
    MISSING,
}

enum class ReaderThemePreset(
    val displayName: String,
    val dark: Boolean,
) {
    PURE_WHITE("纯白", false),
    PAPER("纸页", false),
    WARM("暖黄", false),
    MINT("浅绿", false),
    PURE_BLACK("纯黑", true),
    NIGHT("夜间", true),
    COFFEE("咖啡", true),
    SLATE("石墨", true);

    companion object {
        fun fromNameOrNull(name: String?): ReaderThemePreset? {
            return entries.firstOrNull { it.name == name }
        }

        fun fromName(name: String?): ReaderThemePreset {
            return fromNameOrNull(name) ?: PAPER
        }
    }
}

enum class EsjHost(
    val displayName: String,
    val host: String,
) {
    MAGIC("魔法线路", "www.esjzone.cc"),
    DIRECT("直连线路", "www.esjzone.one");

    val baseUrl: String
        get() = "https://$host"

    companion object {
        fun fromHost(host: String?): EsjHost {
            return entries.firstOrNull { it.host == host } ?: DIRECT
        }
    }
}
