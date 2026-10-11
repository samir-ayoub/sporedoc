package com.knowledge.sporedoc.ui.components

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.domain.model.PixelSize
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * SD-10 R1/R9/technical-approach: the dp -> px decision for the render target lives in
 * `ui/`, where [Density] is available, and is shared by the list's thumbnail and the
 * viewer's page stage instead of being duplicated or pushed into domain/data.
 */
class PixelSizeForDpTest {

    @Test
    fun `at density 1, dp values convert one-to-one to pixels`() {
        val density = Density(density = 1f)

        val size = density.pixelSizeForDp(widthDp = 38.dp, heightDp = 52.dp)

        assertEquals(PixelSize(widthPx = 38, heightPx = 52), size)
    }

    @Test
    fun `at density 2, dp values convert to double the pixels`() {
        val density = Density(density = 2f)

        val size = density.pixelSizeForDp(widthDp = 38.dp, heightDp = 52.dp)

        assertEquals(PixelSize(widthPx = 76, heightPx = 104), size)
    }

    @Test
    fun `at density 3, the viewer page tokens convert using the same rule as the thumbnail`() {
        val density = Density(density = 3f)

        val size = density.pixelSizeForDp(widthDp = 282.dp, heightDp = 398.dp)

        assertEquals(PixelSize(widthPx = 846, heightPx = 1194), size)
    }
}
