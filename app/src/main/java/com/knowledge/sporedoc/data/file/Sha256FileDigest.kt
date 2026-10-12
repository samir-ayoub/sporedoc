package com.knowledge.sporedoc.data.file

import java.io.File
import java.security.MessageDigest

/** Platform-provided (java.security.MessageDigest) implementation of [FileDigest]. */
class Sha256FileDigest : FileDigest {
    override fun digestOf(file: File): String? =
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    messageDigest.update(buffer, 0, read)
                }
            }
            messageDigest.digest().joinToString("") { byte -> "%02x".format(byte) }
        } catch (e: java.io.IOException) {
            null
        }
}
