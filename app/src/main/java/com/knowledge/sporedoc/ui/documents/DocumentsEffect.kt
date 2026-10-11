package com.knowledge.sporedoc.ui.documents

/** One-time effects the DocumentsScreen observes from [DocumentsViewModel]. */
sealed interface DocumentsEffect {
    /** A generic capture failure: the scanner failed, or the scanned PDF could not be saved. */
    data object ShowCaptureError : DocumentsEffect
}
