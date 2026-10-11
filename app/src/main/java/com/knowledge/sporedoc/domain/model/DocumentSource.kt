package com.knowledge.sporedoc.domain.model

import java.io.InputStream

/**
 * Abstraction over "a PDF byte stream from an external origin" (e.g. a share-sheet
 * selection or a file picker result). Domain cannot know about `Uri`/`ContentResolver`,
 * so this is the seam the data layer implements against.
 */
interface DocumentSource {
    fun openInputStream(): InputStream
}
