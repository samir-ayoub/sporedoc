package com.knowledge.sporedoc.ui.documents

import android.net.Uri
import com.knowledge.sporedoc.domain.model.DocumentId

/** One-time effects the DocumentsScreen observes from [DocumentsViewModel]. */
sealed interface DocumentsEffect {
    /** A generic capture failure: the scanner failed, or the scanned PDF could not be saved. */
    data object ShowCaptureError : DocumentsEffect

    /** Open the native share sheet for the PDF at this content Uri. */
    data class ShareDocument(val uri: Uri) : DocumentsEffect

    /** An unexpected delete failure (e.g. the file was already missing on disk). */
    data object ShowDeleteError : DocumentsEffect

    /** An unexpected rename failure other than invalid name or name collision. */
    data object ShowRenameError : DocumentsEffect

    /** The picked PDF could not be imported (unreadable file, read failure, etc.). */
    data object ShowImportError : DocumentsEffect

    /** A document was added from an external source (system Intent); open it in the viewer. */
    data class OpenDocument(val id: DocumentId, val fileName: String) : DocumentsEffect
}
