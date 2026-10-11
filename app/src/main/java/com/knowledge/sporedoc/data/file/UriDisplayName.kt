package com.knowledge.sporedoc.data.file

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Resolves the display name of a content [Uri] (e.g. one returned by the system document
 * picker), so an imported PDF can keep the name it had at its source. Returns `null` when
 * the name cannot be resolved (missing cursor, empty column, etc.).
 */
fun displayNameFor(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null) ?: return null
    return cursor.use {
        if (!it.moveToFirst()) return null
        val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (columnIndex < 0) return null
        it.getString(columnIndex)
    }
}
