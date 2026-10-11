package com.knowledge.sporedoc.domain.usecase

import com.knowledge.sporedoc.domain.model.Document
import java.math.BigDecimal
import java.math.RoundingMode
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

    private fun formatSizeMb(sizeBytes: Long): String {
        val raw = BigDecimal(sizeBytes).divide(BYTES_PER_MB, 4, RoundingMode.HALF_UP)
        var rounded = raw.setScale(1, RoundingMode.HALF_UP)
        if (rounded < MIN_SIZE) rounded = MIN_SIZE
        return rounded.toPlainString()
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val BYTES_PER_MB: BigDecimal = BigDecimal(1_048_576)
        val MIN_SIZE: BigDecimal = BigDecimal("0.1")
    }
}
