package com.knowledge.sporedoc.data.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** SD-13: [Sha256FileDigest] is the platform-provided [FileDigest] used in production. */
class Sha256FileDigestTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val digest = Sha256FileDigest()

    @Test
    fun `digestOf returns the same value for two files with identical content`() {
        val a = File(tempFolder.root, "a.pdf").apply { writeText("SAME CONTENT") }
        val b = File(tempFolder.root, "b.pdf").apply { writeText("SAME CONTENT") }

        assertEquals(digest.digestOf(a), digest.digestOf(b))
    }

    @Test
    fun `digestOf returns different values for files with different content`() {
        val a = File(tempFolder.root, "a.pdf").apply { writeText("CONTENT ONE") }
        val b = File(tempFolder.root, "b.pdf").apply { writeText("CONTENT TWO") }

        assertNotEquals(digest.digestOf(a), digest.digestOf(b))
    }

    @Test
    fun `digestOf returns null for a file that does not exist`() {
        val missing = File(tempFolder.root, "missing.pdf")

        assertNull(digest.digestOf(missing))
    }
}
