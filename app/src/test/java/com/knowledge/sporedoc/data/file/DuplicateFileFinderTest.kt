package com.knowledge.sporedoc.data.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * SD-13: [DuplicateFileFinder] decides which existing file, if any, has the same content as a
 * candidate, honoring the "size before digest" rule (requirements 2/3) and tolerating an
 * unreadable existing file without failing the search (requirement 9/10, AC12).
 */
class DuplicateFileFinderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /** Hand-written [FileDigest] fake that uses the file's text content as its digest, and records every call. */
    private class CountingFileDigest : FileDigest {
        val digestedFiles = mutableListOf<File>()
        override fun digestOf(file: File): String? {
            digestedFiles.add(file)
            return file.readText()
        }
    }

    /** Hand-written [FileDigest] fake where a configured set of files fail to produce a digest. */
    private class FailingFileDigest(private val unreadable: Set<File>) : FileDigest {
        override fun digestOf(file: File): String? =
            if (file in unreadable) null else file.readText()
    }

    private fun file(name: String, content: String): File =
        File(tempFolder.root, name).apply { writeText(content) }

    @Test
    fun `find returns null and computes no digest when no existing file matches the candidate's size`() {
        val digest = CountingFileDigest()
        val finder = DuplicateFileFinder(digest)
        val candidate = file("candidate.pdf", "AAAA")
        val a = file("a.pdf", "A")
        val b = file("b.pdf", "AA")
        val c = file("c.pdf", "AAAAAAAAA")

        val match = finder.find(candidate, listOf(a, b, c))

        assertNull(match)
        assertTrue(digest.digestedFiles.isEmpty())
    }

    @Test
    fun `find computes a digest for exactly the one existing file whose size matches the candidate`() {
        val digest = CountingFileDigest()
        val finder = DuplicateFileFinder(digest)
        val candidate = file("candidate.pdf", "SAME")
        val sameSize = file("same-size.pdf", "DIFF")
        val smaller = file("smaller.pdf", "A")
        val bigger = file("bigger.pdf", "MUCHBIGGER")

        finder.find(candidate, listOf(smaller, sameSize, bigger))

        assertEquals(listOf(sameSize), digest.digestedFiles)
    }

    @Test
    fun `find returns the existing file whose size and digest both match the candidate`() {
        val digest = CountingFileDigest()
        val finder = DuplicateFileFinder(digest)
        val candidate = file("candidate.pdf", "CONTENT")
        val other = file("other.pdf", "DIFFERS")
        val duplicate = file("duplicate.pdf", "CONTENT")

        val match = finder.find(candidate, listOf(other, duplicate))

        assertEquals(duplicate, match)
    }

    @Test
    fun `find returns null when an existing file has the same size but a different digest`() {
        val digest = CountingFileDigest()
        val finder = DuplicateFileFinder(digest)
        val candidate = file("candidate.pdf", "AAAA")
        val sameSizeDifferentContent = file("other.pdf", "BBBB")

        val match = finder.find(candidate, listOf(sameSizeDifferentContent))

        assertNull(match)
    }

    @Test
    fun `find returns null for an empty library without computing any digest`() {
        val digest = CountingFileDigest()
        val finder = DuplicateFileFinder(digest)
        val candidate = file("candidate.pdf", "CONTENT")

        val match = finder.find(candidate, emptyList())

        assertNull(match)
        assertTrue(digest.digestedFiles.isEmpty())
    }

    @Test
    fun `find treats an existing file whose digest cannot be computed as not matching, without failing`() {
        val candidate = file("candidate.pdf", "CONTENT")
        val unreadable = file("unreadable.pdf", "CONTENT")
        val digest = FailingFileDigest(unreadable = setOf(unreadable))
        val finder = DuplicateFileFinder(digest)

        val match = finder.find(candidate, listOf(unreadable))

        assertNull(match)
    }

    @Test
    fun `find skips an unreadable existing file and still matches a later one with identical content`() {
        val candidate = file("candidate.pdf", "CONTENT")
        val unreadable = file("unreadable.pdf", "CONTENT")
        val duplicate = file("duplicate.pdf", "CONTENT")
        val digest = FailingFileDigest(unreadable = setOf(unreadable))
        val finder = DuplicateFileFinder(digest)

        val match = finder.find(candidate, listOf(unreadable, duplicate))

        assertEquals(duplicate, match)
    }
}
