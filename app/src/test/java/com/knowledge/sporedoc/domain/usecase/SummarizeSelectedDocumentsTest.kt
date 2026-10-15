package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

/**
 * Tests for [SummarizeSelectedDocuments]: reduces the selected documents to a count, a
 * page total and a size total formatted with the shared 1-decimal/HALF_UP/0.1-minimum
 * rule (R11 and its acceptance criterion: 3 docs / 11 pp / 7.3 MB).
 */
class SummarizeSelectedDocumentsTest {

    private val fixedInstant: Instant = Instant.parse("2026-10-11T14:30:00Z")

    private fun document(name: String, pageCount: Int, sizeBytes: Long): Document = Document(
        id = DocumentId(name),
        fileName = name,
        pageCount = pageCount,
        sizeBytes = sizeBytes,
        modifiedAt = fixedInstant
    )

    @Test
    fun `an empty selection summarizes to zero documents and zero pages`() {
        val summary = SummarizeSelectedDocuments()(emptyList())

        assertEquals(0, summary.documentCount)
        assertEquals(0, summary.totalPages)
    }

    @Test
    fun `three documents summarize to 3 docs, 11 pages and 7,3 MB per the acceptance criterion`() {
        val documents = listOf(
            document("a.pdf", pageCount = 3, sizeBytes = 2_000_000L),
            document("b.pdf", pageCount = 4, sizeBytes = 3_000_000L),
            document("c.pdf", pageCount = 4, sizeBytes = 2_654_605L)
        )

        val summary = SummarizeSelectedDocuments()(documents)

        assertEquals(3, summary.documentCount)
        assertEquals(11, summary.totalPages)
        assertEquals("7.3", summary.totalSizeMb)
    }

    @Test
    fun `a single tiny document is floored to the 0,1 MB minimum`() {
        val documents = listOf(document("tiny.pdf", pageCount = 1, sizeBytes = 20_000L))

        val summary = SummarizeSelectedDocuments()(documents)

        assertEquals("0.1", summary.totalSizeMb)
    }

    @Test
    fun `the summed size rounds half-up to one decimal place`() {
        val documents = listOf(document("doc.pdf", pageCount = 1, sizeBytes = 1_450_000L))

        val summary = SummarizeSelectedDocuments()(documents)

        assertEquals("1.4", summary.totalSizeMb)
    }
}
