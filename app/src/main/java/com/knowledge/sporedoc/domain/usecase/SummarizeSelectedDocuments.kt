package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.model.SelectionSummary
import com.knowledge.sporedoc.domain.util.formatSizeMb

/**
 * Reduces the selected [Document]s to their count, page total and formatted size total
 * (1 decimal, HALF_UP, minimum "0.1", the same rule [FormatDocumentMetaLine] uses).
 */
class SummarizeSelectedDocuments {

    operator fun invoke(documents: List<Document>): SelectionSummary {
        val totalSizeBytes = documents.sumOf { it.sizeBytes }
        return SelectionSummary(
            documentCount = documents.size,
            totalPages = documents.sumOf { it.pageCount },
            totalSizeMb = formatSizeMb(totalSizeBytes)
        )
    }
}
