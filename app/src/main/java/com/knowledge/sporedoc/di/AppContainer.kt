package com.knowledge.sporedoc.di

import android.content.Context
import com.knowledge.sporedoc.data.file.AndroidPdfFileAccess
import com.knowledge.sporedoc.data.repository.FileDocumentRepository
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
import com.knowledge.sporedoc.domain.util.Clock
import java.time.Instant

/**
 * The one legal crossing point between `data/` and `domain/`: wires the disk-backed
 * repository implementation to the domain interfaces, and the shared clock seam to
 * whatever depends on "now".
 */
class AppContainer(context: Context) {

    private val clock: Clock = { Instant.now() }

    val documentRepository: DocumentRepository = FileDocumentRepository(
        documentsDir = context.filesDir,
        pdfFileAccess = AndroidPdfFileAccess(),
        clock = clock
    )

    val groupDocumentsIntoSections: GroupDocumentsIntoSections = GroupDocumentsIntoSections(clock)

    val formatDocumentMetaLine: FormatDocumentMetaLine = FormatDocumentMetaLine()
}
