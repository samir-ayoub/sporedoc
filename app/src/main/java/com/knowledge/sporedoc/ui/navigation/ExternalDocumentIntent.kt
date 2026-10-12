package com.knowledge.sporedoc.ui.navigation

import android.content.Intent
import android.net.Uri

/**
 * Decides whether a system [Intent] carries an external PDF the app should add to the
 * library and open in the viewer, and extracts the [Uri] it points to.
 *
 * Returns `null` for anything else, in particular a plain icon launch (`ACTION_MAIN`),
 * so the Documents list is shown with no viewer on top in that case.
 */
fun externalDocumentUriFrom(intent: Intent?): Uri? {
    if (intent == null) return null
    return when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
        else -> null
    }
}
