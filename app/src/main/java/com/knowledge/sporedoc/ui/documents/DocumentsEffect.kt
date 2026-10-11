package com.knowledge.sporedoc.ui.documents

import android.net.Uri

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
}
