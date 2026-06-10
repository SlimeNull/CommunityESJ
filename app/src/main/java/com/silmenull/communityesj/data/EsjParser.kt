package com.silmenull.communityesj.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object EsjParser {
    private val loginRedirectRegex = Regex(
        pattern = """\b(?:window\.)?location(?:\.href)?\s*=\s*['"][^'"]*login[^'"]*['"]""",
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
        )
    }

    fun containsLoginRedirect(html: String): Boolean {
        return loginRedirectRegex.containsMatchIn(html)
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

        return ReaderChapter(
            url = fallbackUrl,
            bookTitle = bookTitle,
            chapterTitle = chapterTitle,
            contentBlocks = contentBlocks,
            chapters = emptyList(),
            previousUrl = previous,
            nextUrl = next,
            detailUrl = detailUrl,
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
        contentElement.childNodes().forEach { node ->
            appendContentNode(node, blocks)
        }
        if (blocks.isNotEmpty()) return blocks

        return contentElement.wholeText()
            .split('\n')
            .mapNotNull { it.cleanText().takeIf(String::isNotBlank) }
            .map { ReaderContentBlock.Text(it) }
    }

    private fun appendContentNode(node: Node, blocks: MutableList<ReaderContentBlock>) {
        when (node) {
            is TextNode -> {
                node.wholeText.cleanText()
                    .takeIf(String::isNotBlank)
                    ?.let { blocks.add(ReaderContentBlock.Text(it)) }
            }

            is Element -> {
                when (node.tagName().lowercase()) {
                    "img" -> node.toImageBlock()?.let(blocks::add)
                    "br" -> Unit
                    "p" -> appendParagraph(node, blocks)
                    else -> node.childNodes().forEach { appendContentNode(it, blocks) }
                }
            }
        }
    }

    private fun appendParagraph(paragraph: Element, blocks: MutableList<ReaderContentBlock>) {
        val images = paragraph.select("img[src]")
        if (images.isEmpty()) {
            paragraph.wholeText().cleanText()
                .takeIf(String::isNotBlank)
                ?.let { blocks.add(ReaderContentBlock.Text(it)) }
            return
        }

        paragraph.childNodes().forEach { appendContentNode(it, blocks) }
    }

    private fun Element.toImageBlock(): ReaderContentBlock.Image? {
        val src = absUrl("src").ifBlank { attr("src") }
        if (src.isBlank()) return null
        return ReaderContentBlock.Image(
            url = src,
            alt = attr("alt").cleanText(),
        )
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
}
