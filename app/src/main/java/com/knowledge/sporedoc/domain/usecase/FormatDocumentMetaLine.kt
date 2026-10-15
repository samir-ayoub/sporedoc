package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import com.knowledge.sporedoc.domain.util.formatSizeMb
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Formats a document's metadata line according to `tokens.typography.styles.fileMeta.format`:
 * page count, size in MB and modification date/time.
 */
class FormatDocumentMetaLine {

    operator fun invoke(document: Document): String {
        val sizeMb = formatSizeMb(document.sizeBytes)
        val date = document.modifiedAt.atZone(ZoneId.systemDefault()).format(DATE_FORMAT)
        return "${document.pageCount} PP  /  $sizeMb MB  /  $date"
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    }
}
