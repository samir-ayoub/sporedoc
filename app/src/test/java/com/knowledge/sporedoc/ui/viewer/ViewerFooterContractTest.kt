package com.knowledge.sporedoc.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-21 compile-time contract checks.
 *
 * SD-21 reconnects the `share` and `delete` footer boxes (left inert by SD-20) to the
 * already-existing `ViewerViewModel` methods `onExportRequested`/`onDeleteRequested`/
 * `onDeleteConfirmed`/`onDeleteCancelled`. Per the Architect's design, this adds no new
 * `ViewerViewModel`/`ViewerUiState`/`ViewerEffect` behavior — that surface is already fully
 * exercised by `ViewerViewModelTest` (onExportRequested, onDeleteRequested,
 * onDeleteConfirmed, onDeleteCancelled) — so there is nothing new to assert there.
 *
 * As already documented on `ViewerViewModelTest`/the previous version of this file, this
 * project has no Robolectric and no `androidx.compose.ui.test` dependency in the JVM
 * unit-test source set. That means content-description text (R6/R7), touch-target area
 * (R5) and the continued inert/disabled behavior of `crop`/`rotate`/`sign` under a tap (R8)
 * cannot be asserted by a JVM test: they can only be exercised by the "Validação manual"
 * pass described in the ticket. This is a declared test gap, not an oversight.
 *
 * What *is* checkable from this source set is the public Compose contract itself. SD-21's
 * design restores `onShareClicked`/`onDeleteClicked` to [ViewerFooter] and adds
 * `onShareClicked`/`onDeleteClicked`/`onDeleteConfirmed`/`onDeleteCancelled` to
 * [ViewerContent]. The private composables below only need to *compile* against that
 * signature; while the production code does not yet accept these parameters, this file
 * fails to compile, which is this delivery's valid "red".
 *
 * SD-22 extends the same contract: [ViewerFooter] gains `onRotateClicked` (the normal
 * footer's `rotate` box, previously inert) and [ViewerContent] gains
 * `onRotateRequested`/`onRotateLeftClicked`/`onRotateSaveClicked`/`onRotateCancelClicked`,
 * for the same compile-only reason.
 */
class ViewerFooterContractTest {

    @Test
    fun `ViewerFooter accepts share, delete and rotate callbacks`() {
        assertTrue(true)
    }

    @Test
    fun `ViewerContent accepts share, delete, rotate callbacks plus delete dialog confirm and cancel`() {
        assertTrue(true)
    }
}

@Composable
private fun footerContract(
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRotateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ViewerFooter(
        onShareClicked = onShareClicked,
        onDeleteClicked = onDeleteClicked,
        onRotateClicked = onRotateClicked,
        modifier = modifier
    )
}

@Composable
private fun contentContract(
    state: ViewerUiState,
    loadPage: suspend (Int, PixelSize) -> PageImage?,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteCancelled: () -> Unit,
    onRotateRequested: () -> Unit,
    onRotateLeftClicked: () -> Unit,
    onRotateSaveClicked: () -> Unit,
    onRotateCancelClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ViewerContent(
        state = state,
        loadPage = loadPage,
        onBack = onBack,
        onPageChanged = onPageChanged,
        onShareClicked = onShareClicked,
        onDeleteClicked = onDeleteClicked,
        onDeleteConfirmed = onDeleteConfirmed,
        onDeleteCancelled = onDeleteCancelled,
        onRotateRequested = onRotateRequested,
        onRotateLeftClicked = onRotateLeftClicked,
        onRotateSaveClicked = onRotateSaveClicked,
        onRotateCancelClicked = onRotateCancelClicked,
        modifier = modifier
    )
}
