package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.DocumentSection
import com.knowledge.sporedoc.domain.model.SectionLabel
import com.knowledge.sporedoc.domain.util.Clock
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Groups documents into the `TODAY` / `RECENT DOCUMENTS` / `ARCHIVE` sections defined by
 * `components.SectionHeader`. Sections with no documents are not produced.
 */
class GroupDocumentsIntoSections(private val clock: Clock) {

    operator fun invoke(documents: List<Document>): List<DocumentSection> {
        val zone = ZoneId.systemDefault()
        val today = clock().atZone(zone).toLocalDate()

        val sorted = documents.sortedByDescending { it.modifiedAt }

        val todayDocs = mutableListOf<Document>()
        val recentDocs = mutableListOf<Document>()
        val archiveDocs = mutableListOf<Document>()

        for (document in sorted) {
            val documentDate = document.modifiedAt.atZone(zone).toLocalDate()
            when {
                documentDate == today -> todayDocs.add(document)
                ChronoUnit.DAYS.between(documentDate, today) < 30 -> recentDocs.add(document)
                else -> archiveDocs.add(document)
            }
        }

        val sections = mutableListOf<DocumentSection>()
        if (todayDocs.isNotEmpty()) sections.add(DocumentSection(SectionLabel.TODAY, todayDocs))
        if (recentDocs.isNotEmpty()) sections.add(DocumentSection(SectionLabel.RECENT_DOCUMENTS, recentDocs))
        if (archiveDocs.isNotEmpty()) sections.add(DocumentSection(SectionLabel.ARCHIVE, archiveDocs))
        return sections
    }
}
