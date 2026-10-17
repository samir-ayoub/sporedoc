package com.knowledge.sporedoc.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SD-20 compile-time contract checks.
 *
 * SD-20 is a pure Compose UI change: a fixed, inert five-box footer (`crop`, `rotate`,
 * `sign`, `delete`, `share`) and the removal of the top-bar delete icon. It adds no new
 * `ViewerViewModel`/`ViewerUiState`/`ViewerEffect` behavior (per the Architect's design,
 * those stay unchanged), so there is nothing new to assert through `ViewerViewModelTest`.
 *
 * As already documented on `ViewerViewModelTest` for SD-4, this project has no Robolectric
 * and no `androidx.compose.ui.test` dependency in the JVM unit-test source set. That means
 * the box order, the icon-only vs. icon+"SHARE" treatment, the disabled/non-clickable
 * semantics (R6/R7), the removed EXPORT button and delete icon (R1/R8), and the footer's
 * fixed position under gestures (R9) cannot be asserted by a JVM test: they can only be
 * exercised by the "Validação manual" pass described in the ticket. This is a declared
 * test gap, not an oversight.
 *
 * What *is* checkable from this source set is the public Compose contract itself. The
 * Architect's design drops `onExport` from [ViewerFooter], drops `onDeleteRequested` from
 * [ViewerAppBar], and drops `onDeleteRequested`/`onDeleteConfirmed`/`onDeleteCancelled`/
 * `onExport` from [ViewerContent] (their only trigger — the app-bar delete icon and the
 * full-width export button — is removed by this delivery). The private composables below
 * only need to *compile* against that reduced signature; while the current production code
 * still requires the removed parameters, this file fails to compile, which is this
 * delivery's valid "red" (SD-20 touches no `ViewerViewModel`/`ViewerUiState` surface that a
 * passing/failing assertion could otherwise exercise).
 */
class ViewerFooterContractTest {

    @Test
    fun `ViewerFooter no longer accepts an export callback`() {
        assertTrue(true)
    }

    @Test
    fun `ViewerAppBar no longer accepts a delete callback`() {
        assertTrue(true)
    }

    @Test
    fun `ViewerContent no longer accepts delete or export callbacks`() {
        assertTrue(true)
    }
}

@Composable
private fun footerContract(modifier: Modifier = Modifier) {
    ViewerFooter(modifier = modifier)
}

@Composable
private fun appBarContract(fileName: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    ViewerAppBar(fileName = fileName, onBack = onBack, modifier = modifier)
}

@Composable
private fun contentContract(
    state: ViewerUiState,
    loadPage: suspend (Int, PixelSize) -> PageImage?,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ViewerContent(
        state = state,
        loadPage = loadPage,
        onBack = onBack,
        onPageChanged = onPageChanged,
        modifier = modifier
    )
}
