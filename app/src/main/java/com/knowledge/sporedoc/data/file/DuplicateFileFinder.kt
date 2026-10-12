package com.knowledge.sporedoc.data.file

import java.io.File

/**
 * Finds, among [existingFiles], the one whose content equals [candidate]'s: same length,
 * then same [FileDigest.digestOf]. Computes a digest for an existing file only when its
 * length equals the candidate's; a file whose digest cannot be computed is treated as
 * not matching, never as a failure of [find].
 */
class DuplicateFileFinder(private val fileDigest: FileDigest) {

    fun find(candidate: File, existingFiles: List<File>): File? {
        val candidateLength = candidate.length()
        val candidateBytes by lazy { candidate.readBytes() }

        return existingFiles
            .asSequence()
            .filter { it.length() == candidateLength }
            .firstOrNull { existing ->
                fileDigest.digestOf(existing) != null && candidateBytes.contentEquals(existing.readBytes())
            }
    }
}
