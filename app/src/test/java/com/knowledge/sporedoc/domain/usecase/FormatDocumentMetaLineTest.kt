package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class FormatDocumentMetaLineTest {

    private fun documentOf(pageCount: Int, sizeBytes: Long, dateTime: ZonedDateTime): Document =
        Document(
            id = DocumentId("doc"),
            fileName = "doc.pdf",
            pageCount = pageCount,
            sizeBytes = sizeBytes,
            modifiedAt = dateTime.toInstant()
        )

    @Test
    fun `formats pages, size and date according to the fileMeta pattern`() {
        val document = documentOf(
            pageCount = 2,
            sizeBytes = 1_468_006L,
            dateTime = ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault())
        )

        val line = FormatDocumentMetaLine()(document)

        assertEquals("2 PP  /  1.4 MB  /  11.10.2026 14:30", line)
    }

    @Test
    fun `tiny file size is floored to 0,1 MB and never shown as 0,0 MB`() {
        val document = documentOf(
            pageCount = 1,
            sizeBytes = 20_000L,
            dateTime = ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault())
        )

        val line = FormatDocumentMetaLine()(document)

        assertTrue(line.contains("0.1 MB"))
        assertFalse(line.contains("0.0 MB"))
    }

    @Test
    fun `size rounds half-up to one decimal place`() {
        val document = documentOf(
            pageCount = 1,
            sizeBytes = 1_450_000L,
            dateTime = ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault())
        )

        val line = FormatDocumentMetaLine()(document)

        assertTrue(line.contains("1.4 MB"))
    }
}
