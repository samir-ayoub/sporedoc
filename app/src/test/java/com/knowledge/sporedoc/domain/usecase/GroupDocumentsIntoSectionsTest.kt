package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.SectionLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class GroupDocumentsIntoSectionsTest {

    private val fixedNow: Instant =
        ZonedDateTime.of(2026, 10, 11, 14, 30, 0, 0, ZoneId.systemDefault()).toInstant()
    private val clock: () -> Instant = { fixedNow }

    private fun documentAt(name: String, dateTime: ZonedDateTime): Document =
        Document(
            id = DocumentId(name),
            fileName = name,
            pageCount = 1,
            sizeBytes = 1_000L,
            modifiedAt = dateTime.toInstant()
        )

    @Test
    fun `groups documents into TODAY, RECENT DOCUMENTS and ARCHIVE in that order`() {
        val today = documentAt("today.pdf", ZonedDateTime.of(2026, 10, 11, 9, 0, 0, 0, ZoneId.systemDefault()))
        val recent = documentAt("recent.pdf", ZonedDateTime.of(2026, 10, 8, 9, 0, 0, 0, ZoneId.systemDefault()))
        val archived = documentAt("archived.pdf", ZonedDateTime.of(2026, 8, 21, 9, 0, 0, 0, ZoneId.systemDefault()))

        // Input intentionally not pre-sorted: the use case must not rely on caller ordering.
        val sections = GroupDocumentsIntoSections(clock)(listOf(archived, today, recent))

        assertEquals(3, sections.size)
        assertEquals(SectionLabel.TODAY, sections[0].label)
        assertEquals(listOf(today), sections[0].documents)
        assertEquals(SectionLabel.RECENT_DOCUMENTS, sections[1].label)
        assertEquals(listOf(recent), sections[1].documents)
        assertEquals(SectionLabel.ARCHIVE, sections[2].label)
        assertEquals(listOf(archived), sections[2].documents)
    }

    @Test
    fun `document from the same calendar day as now falls into TODAY`() {
        val earlyToday = documentAt("early.pdf", ZonedDateTime.of(2026, 10, 11, 0, 5, 0, 0, ZoneId.systemDefault()))

        val sections = GroupDocumentsIntoSections(clock)(listOf(earlyToday))

        assertEquals(1, sections.size)
        assertEquals(SectionLabel.TODAY, sections[0].label)
        assertEquals(listOf(earlyToday), sections[0].documents)
    }

    @Test
    fun `document older than 30 days falls into ARCHIVE`() {
        val oldDocument = documentAt("old.pdf", ZonedDateTime.of(2026, 9, 11, 9, 0, 0, 0, ZoneId.systemDefault()))

        val sections = GroupDocumentsIntoSections(clock)(listOf(oldDocument))

        assertEquals(1, sections.size)
        assertEquals(SectionLabel.ARCHIVE, sections[0].label)
        assertEquals(listOf(oldDocument), sections[0].documents)
    }

    @Test
    fun `only today documents produce a single TODAY section, sorted newest first, with no empty sections`() {
        val earlier = documentAt("a.pdf", ZonedDateTime.of(2026, 10, 11, 8, 0, 0, 0, ZoneId.systemDefault()))
        val later = documentAt("b.pdf", ZonedDateTime.of(2026, 10, 11, 10, 0, 0, 0, ZoneId.systemDefault()))

        // Deliberately passed out of chronological order.
        val sections = GroupDocumentsIntoSections(clock)(listOf(earlier, later))

        assertEquals(1, sections.size)
        assertEquals(SectionLabel.TODAY, sections[0].label)
        assertEquals(listOf(later, earlier), sections[0].documents)
    }

    @Test
    fun `empty list produces no sections`() {
        val sections = GroupDocumentsIntoSections(clock)(emptyList())

        assertTrue(sections.isEmpty())
    }
}
