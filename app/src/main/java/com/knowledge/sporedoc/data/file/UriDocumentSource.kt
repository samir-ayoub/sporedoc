package com.knowledge.sporedoc.data.file

import android.content.ContentResolver
import android.net.Uri
import com.knowledge.sporedoc.domain.model.DocumentSource
import java.io.IOException
import java.io.InputStream

/**
 * Adapts a scanner-result [Uri] (e.g. from the ML Kit Document Scanner) to the domain's
 * [DocumentSource] seam, so [com.knowledge.sporedoc.domain.repository.DocumentRepository]
 * can read it without knowing about `Uri`/`ContentResolver`.
 */
class UriDocumentSource(
    private val contentResolver: ContentResolver,
    private val uri: Uri
) : DocumentSource {

    override fun openInputStream(): InputStream =
        contentResolver.openInputStream(uri)
            ?: throw IOException("ContentResolver returned no stream for $uri")
}
