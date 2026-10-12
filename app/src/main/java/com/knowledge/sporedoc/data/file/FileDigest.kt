package com.knowledge.sporedoc.data.file

import java.io.File

/** Computes a content digest for a file on disk; returns null when the file cannot be read. */
fun interface FileDigest {
    fun digestOf(file: File): String?
}
