package com.knowledge.sporedoc.ui.viewer

import android.net.Uri

/** One-time effects the ViewerScreen observes from [ViewerViewModel]. */
sealed interface ViewerEffect {
    /** Open the native share sheet with the document's PDF file. */
    data class ShareDocument(val uri: Uri) : ViewerEffect

    /** The document was deleted; the screen should return to the list. */
    data object DocumentDeleted : ViewerEffect

    /** The document could not be opened (missing or unreadable); return to the list with an error. */
    data object LoadFailed : ViewerEffect

    /** SAVE failed while persisting an accumulated rotation; rotate mode and the pending map stay as they were. */
    data object RotateSaveFailed : ViewerEffect
}
