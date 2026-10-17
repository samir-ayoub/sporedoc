package com.knowledge.sporedoc.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-23 compile-time contract check.
 *
 * SD-23 adds a dedicated two-element footer (SAVE / CANCEL) that replaces the normal
 * five-box footer while `ViewerUiState.isCropModeActive` is true. As documented on
 * `ViewerFooterContractTest`/`ViewerRotateFooterContractTest`/`ViewerViewModelTest`, this
 * project has no Robolectric and no `androidx.compose.ui.test` dependency in the JVM
 * unit-test source set. That means the actual visual footer swap (R1/R2), the four draggable
 * nodes over the page (R1/R4/R5) and the TalkBack announcements (R10) cannot be asserted by a
 * JVM test: they are declared test gaps, exercised only by the "Validação manual" pass.
 *
 * What *is* checkable from this source set is the new composable's public contract:
 * [ViewerCropFooter] accepts `onSaveClicked` and `onCancelClicked`. The private composable
 * below only needs to *compile* against that signature; while production code does not yet
 * declare [ViewerCropFooter], this file fails to compile, which is this delivery's valid "red".
 */
class ViewerCropFooterContractTest {

    @Test
    fun `ViewerCropFooter accepts save and cancel callbacks`() {
        assertTrue(true)
    }
}

@Composable
private fun cropFooterContract(
    onSaveClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ViewerCropFooter(
        onSaveClicked = onSaveClicked,
        onCancelClicked = onCancelClicked,
        modifier = modifier
    )
}
