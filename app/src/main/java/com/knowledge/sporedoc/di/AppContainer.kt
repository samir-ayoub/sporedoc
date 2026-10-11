package com.knowledge.sporedoc.di

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.knowledge.sporedoc.data.file.AndroidPdfFileAccess
import com.knowledge.sporedoc.data.file.UriDocumentSource
import com.knowledge.sporedoc.data.repository.FileDocumentRepository
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.domain.model.DocumentSource
import com.knowledge.sporedoc.domain.repository.DocumentRepository
import com.knowledge.sporedoc.domain.usecase.FormatDocumentMetaLine
import com.knowledge.sporedoc.domain.usecase.GroupDocumentsIntoSections
import com.knowledge.sporedoc.domain.util.Clock
import java.io.File
import java.time.Instant

/**
 * The one legal crossing point between `data/` and `domain/`: wires the disk-backed
 * repository implementation to the domain interfaces, and the shared clock seam to
 * whatever depends on "now".
 */
class AppContainer(context: Context) {

    private val clock: Clock = { Instant.now() }

    private val documentsDir: File = context.filesDir

    val documentRepository: DocumentRepository = FileDocumentRepository(
        documentsDir = documentsDir,
        pdfFileAccess = AndroidPdfFileAccess(),
        clock = clock
    )

    val groupDocumentsIntoSections: GroupDocumentsIntoSections = GroupDocumentsIntoSections(clock)

    val formatDocumentMetaLine: FormatDocumentMetaLine = FormatDocumentMetaLine()

    /** Wires a scanner-result `Uri` to the domain's [DocumentSource] seam. */
    val documentSourceFor: (Uri) -> DocumentSource = { uri ->
        UriDocumentSource(context.contentResolver, uri)
    }

    /** Resolves a shareable `content://` Uri for a document's PDF file, via [FileProvider]. */
    val shareUriFor: (DocumentId) -> Uri = { id ->
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(documentsDir, id.value)
        )
    }
}
