package com.knowledge.sporedoc.data.repository

import com.knowledge.sporedoc.domain.model.DocumentSource
import java.io.InputStream

/** Hand-written test double for [DocumentSource], supplying fixed bytes as the "external origin". */
class FakeDocumentSource(private val content: String) : DocumentSource {
    override fun openInputStream(): InputStream = content.byteInputStream()
}
