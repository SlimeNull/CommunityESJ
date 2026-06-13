package com.silmenull.communityesj.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object EsjParser {
    private val scriptRedirectRegex = Regex(
        pattern = """\b(?:(?:window|document|top|self|parent)\.)?location(?:\.(?:href|replace|assign))?\s*(?:=|\()\s*['"][^'"]*/my/login(?:[?#][^'"]*)?['"]""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val metaRefreshRegex = Regex(
        pattern = """<meta[^>]+http-equiv\s*=\s*['"]?refresh['"]?[^>]+content\s*=\s*['"][^'"]+['"]""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )

    fun parseAuthToken(xml: String): String? {
        return Regex("<JinJing>(.*?)</JinJing>", RegexOption.DOT_MATCHES_ALL)
            .find(xml)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    fun parseBookshelf(html: String, page: Int, baseUrl: String): BookshelfPage {
        val doc = Jsoup.parse(html, baseUrl)
        val productItems = doc.select("div.product-item")
        val books = productItems.mapNotNull { parseFavoriteItem(it) }

        return BookshelfPage(
            books = books,
            currentPage = page,
            totalPages = parseTotalPages(doc).coerceAtLeast(page),
            username = doc.selectFirst(".user-data h4")?.text()?.cleanText().orEmpty(),
        )
    }

    fun containsRedirectInstruction(html: String): Boolean {
        return scriptRedirectRegex.containsMatchIn(html) ||
            metaRefreshRegex.containsMatchIn(html)
    }

    fun parseChapters(html: String, baseUrl: String): List<ChapterLink> {
        val doc = Jsoup.parse(html, baseUrl)
        val chapterLinks = doc.select("#chapterList a[href]")
            .ifEmpty { doc.select("a[href*=/forum/]") }

        return chapterLinks.mapNotNull { link ->
            val href = link.absUrl("href").takeIf { it.contains("/forum/") } ?: return@mapNotNull null
            val title = link.attr("data-title")
                .ifBlank { link.text() }
                .cleanText()
            if (title.isBlank()) null else ChapterLink(title, href)
        }.distinctBy { it.url }
    }

    fun parseReader(html: String, fallbackUrl: String): ReaderChapter {
        val doc = Jsoup.parse(html, fallbackUrl)
        val detailUrl = doc.selectFirst(".entry-navigation .view-all[href], a.view-all[href]")
            ?.absUrl("href")
            ?.takeIf { it.isNotBlank() }

        val contentElement = doc.selectFirst(".forum-content")
        val contentBlocks = contentElement?.let { parseContentBlocks(it) }.orEmpty()

        val chapterTitle = doc.selectFirst("h2")?.text()?.cleanText()
            ?: doc.title().substringBefore(" - ESJ Zone").substringAfterLast(" - ").cleanText()
            ?: "当前章节"
        val bookTitle = doc.select(".breadcrumbs li a").lastOrNull()?.text()?.cleanText()
            ?: doc.title().substringBefore(" - ").cleanText()
            ?: ""

        val previous = doc.selectFirst(".entry-navigation .btn-prev[href], a.btn-prev[href]")
            ?.absUrl("href")
            ?.takeIf { it.isNotBlank() }
        val next = doc.selectFirst(".entry-navigation .btn-next[href], a.btn-next[href]")
            ?.absUrl("href")
            ?.takeIf { it.isNotBlank() }
        val comments = parseComments(doc)

        return ReaderChapter(
            url = fallbackUrl,
            bookTitle = bookTitle,
            chapterTitle = chapterTitle,
            contentBlocks = contentBlocks,
            chapters = emptyList(),
            previousUrl = previous,
            nextUrl = next,
            detailUrl = detailUrl,
            comments = comments,
        )
    }

    private fun parseFavoriteItem(item: Element): BookItem? {
        val detailLink = item.selectFirst("a[href*=/detail/]")
        val title = item.selectFirst(".product-title, .card-title, h5, h4, h3")
            ?.text()
            ?.cleanText()
            ?: detailLink?.text()?.cleanText()
            ?: item.attr("title").cleanText()

        if (title.isBlank()) return null

        val chapterLinks = item.select("a[href*=/forum/]")
        val latestLink = findByLabel(item, "最新", "更新", "last", "latest")
            ?: chapterLinks.firstOrNull()
        val lastReadLink = findByLabel(item, "最後", "观看", "觀看", "看到", "last read")
            ?: chapterLinks.getOrNull(1)
            ?: latestLink

        val updateDate = Regex("""\d{4}[-/]\d{1,2}[-/]\d{1,2}(?:\s+\d{1,2}:\d{2})?""")
            .find(item.text())
            ?.value
            .orEmpty()

        return BookItem(
            title = title,
            latestChapter = latestLink?.text()?.cleanText().orEmpty(),
            lastReadChapter = lastReadLink?.text()?.cleanText().orEmpty(),
            updateDate = updateDate,
            detailUrl = detailLink?.absUrl("href")?.takeIf { it.isNotBlank() },
            latestChapterUrl = latestLink?.absUrl("href")?.takeIf { it.isNotBlank() },
            lastReadChapterUrl = lastReadLink?.absUrl("href")?.takeIf { it.isNotBlank() },
        )
    }

    private fun parseContentBlocks(contentElement: Element): List<ReaderContentBlock> {
        val blocks = mutableListOf<ReaderContentBlock>()
        val state = ContentParseState(blocks)

        contentElement.childNodes().forEach { node ->
            collectContentNode(node, state)
        }
        state.flushText()
        if (blocks.isNotEmpty()) return blocks

        return contentElement.wholeText()
            .split('\n')
            .mapNotNull { it.cleanText().takeIf(String::isNotBlank) }
            .map { ReaderContentBlock.Text(it) }
    }

    private fun parseComments(doc: Document): List<ReaderComment> {
        return doc.select("#comments .comment").mapNotNull { comment ->
            val username = comment.selectFirst(".comment-title a")?.text()?.cleanText().orEmpty()
            if (username.isBlank()) return@mapNotNull null

            val commentText = comment.selectFirst(".comment-text") ?: return@mapNotNull null
            val quoteBlocks = commentText.select("blockquote")
                .flatMap { blockquote -> parseCommentBlocks(blockquote) }
            val contentBlocks = commentText.select("p")
                .filter { paragraph -> paragraph.parents().none { it.tagName().equals("blockquote", ignoreCase = true) } }
                .flatMap { paragraph -> parseCommentBlocks(paragraph) }
                .ifEmpty {
                    val clone = commentText.clone()
                    clone.select("blockquote").remove()
                    parseCommentBlocks(clone)
                }

            ReaderComment(
                id = comment.id().takeIf { it.isNotBlank() }.orEmpty(),
                username = username,
                content = contentBlocks,
                quote = quoteBlocks,
            )
        }
    }

    private fun parseCommentBlocks(element: Element): List<CommentBlock> {
        val blocks = mutableListOf<CommentBlock>()
        val parts = mutableListOf<CommentTextPart>()

        fun flush() {
            val normalized = normalizeCommentParts(parts)
            if (normalized.isNotEmpty()) {
                blocks.add(CommentBlock(normalized))
            }
            parts.clear()
        }

        fun collect(node: Node, strikeThrough: Boolean) {
            when (node) {
                is TextNode -> {
                    val text = node.wholeText
                        .replace('\u00A0', ' ')
                        .replace(Regex("""[ \t\r\n]+"""), " ")
                    if (text.isNotBlank()) {
                        parts.add(CommentTextPart(text, strikeThrough))
                    }
                }

                is Element -> when (node.tagName().lowercase()) {
                    "br" -> flush()
                    "p" -> {
                        if (parts.isNotEmpty()) flush()
                        node.childNodes().forEach { child -> collect(child, strikeThrough) }
                        flush()
                    }
                    "blockquote" -> Unit
                    "s", "strike", "del" -> node.childNodes().forEach { child -> collect(child, true) }
                    else -> node.childNodes().forEach { child -> collect(child, strikeThrough) }
                }
            }
        }

        element.childNodes().forEach { node -> collect(node, false) }
        flush()
        return blocks
    }

    private fun normalizeCommentParts(parts: List<CommentTextPart>): List<CommentTextPart> {
        return buildList {
            parts.forEach { part ->
                val text = part.text.cleanText()
                if (text.isBlank()) return@forEach
                val previous = lastOrNull()
                if (previous != null && previous.strikeThrough == part.strikeThrough) {
                    removeAt(lastIndex)
                    add(previous.copy(text = "${previous.text} $text".cleanText()))
                } else {
                    add(part.copy(text = text))
                }
            }
        }
    }

    private fun collectContentNode(node: Node, state: ContentParseState) {
        when (node) {
            is TextNode -> state.appendText(node.wholeText)

            is Element -> {
                when (node.tagName().lowercase()) {
                    "img" -> {
                        state.flushText()
                        node.toImageBlock()?.let(state::addBlock)
                    }
                    "br" -> state.appendBreak()
                    "p" -> collectParagraph(node, state)
                    else -> node.childNodes().forEach { collectContentNode(it, state) }
                }
            }
        }
    }

    private fun collectParagraph(paragraph: Element, state: ContentParseState) {
        if (paragraph.childNodeSize() == 0) {
            paragraph.ownText().cleanMultilineText()
                .takeIf(String::isNotBlank)
                ?.let { state.addBlock(ReaderContentBlock.Text(it)) }
            return
        }

        paragraph.childNodes().forEach { collectContentNode(it, state) }
        state.flushText()
    }

    private fun Element.toImageBlock(): ReaderContentBlock.Image? {
        val src = absUrl("src").ifBlank { attr("src") }
        if (src.isBlank()) return null
        return ReaderContentBlock.Image(
            url = src,
            alt = attr("alt").cleanText(),
        )
    }

    private class ContentParseState(
        private val blocks: MutableList<ReaderContentBlock>,
    ) {
        private val buffer = StringBuilder()
        private var pendingBreaks = 0

        fun appendText(text: String) {
            val normalized = text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace(Regex("""[ \t]*\n[ \t]*"""), " ")
            if (normalized.isBlank()) return
            if (pendingBreaks == 1 && buffer.isNotEmpty() && buffer.last() != '\n') {
                buffer.append('\n')
            }
            pendingBreaks = 0
            buffer.append(normalized)
        }

        fun appendBreak() {
            pendingBreaks += 1
            if (pendingBreaks >= 2) {
                flushText()
            }
        }

        fun flushText() {
            buffer.toString()
                .cleanMultilineText()
                .takeIf(String::isNotBlank)
                ?.let { addBlock(ReaderContentBlock.Text(it)) }
            buffer.clear()
            pendingBreaks = 0
        }

        fun addBlock(block: ReaderContentBlock) {
            blocks.add(block)
        }
    }

    private fun findByLabel(item: Element, vararg labels: String): Element? {
        return item.select("a[href*=/forum/]").firstOrNull { link ->
            val context = generateSequence(link as Element?) { it.parent() }
                .take(3)
                .joinToString(" ") { it.text() }
                .lowercase()
            labels.any { context.contains(it.lowercase()) }
        }
    }

    private fun parseTotalPages(doc: Document): Int {
        val bootpagTotal = Regex("""total\s*:\s*(\d+)""")
            .find(doc.html())
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
        if (bootpagTotal != null) return bootpagTotal

        return doc.select("#page-selection a[href], .page-selection a[href], .pagination a[href]")
            .mapNotNull { it.text().toIntOrNull() }
            .maxOrNull()
            ?: 1
    }

    private fun String?.cleanText(): String {
        return this
            ?.replace('\u00A0', ' ')
            ?.replace(Regex("""[ \t\r\n]+"""), " ")
            ?.trim()
            .orEmpty()
    }

    private fun String?.cleanMultilineText(): String {
        return this
            ?.replace('\u00A0', ' ')
            ?.replace("\r\n", "\n")
            ?.replace('\r', '\n')
            ?.split('\n')
            ?.joinToString("\n") { line ->
                line.replace(Regex("""[ \t]+"""), " ").trim()
            }
            ?.trim()
            .orEmpty()
    }
}
