package com.knowledge.sporedoc.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-22 compile-time contract check.
 *
 * SD-22 adds a dedicated three-element footer (ROTATE LEFT / SAVE / CANCEL) that replaces
 * the normal five-box footer while `ViewerUiState.isRotateModeActive` is true. As documented
 * on `ViewerFooterContractTest`/`ViewerViewModelTest`, this project has no Robolectric and
 * no `androidx.compose.ui.test` dependency in the JVM unit-test source set. That means the
 * actual visual footer swap (R1/R2), the accumulated visual rotation of the page bitmap
 * (R3/R4/R9) and the TalkBack announcements (R10) cannot be asserted by a JVM test: they
 * are declared test gaps, exercised only by the "Validação manual" pass.
 *
 * What *is* checkable from this source set is the new composable's public contract:
 * [ViewerRotateFooter] accepts `onRotateLeftClicked`, `onSaveClicked` and `onCancelClicked`.
 * The private composable below only needs to *compile* against that signature; while
 * production code does not yet declare [ViewerRotateFooter], this file fails to compile,
 * which is this delivery's valid "red".
 */
class ViewerRotateFooterContractTest {

    @Test
    fun `ViewerRotateFooter accepts rotate-left, save and cancel callbacks`() {
        assertTrue(true)
    }
}

@Composable
private fun rotateFooterContract(
    onRotateLeftClicked: () -> Unit,
    onSaveClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ViewerRotateFooter(
        onRotateLeftClicked = onRotateLeftClicked,
        onSaveClicked = onSaveClicked,
        onCancelClicked = onCancelClicked,
        modifier = modifier
    )
}
