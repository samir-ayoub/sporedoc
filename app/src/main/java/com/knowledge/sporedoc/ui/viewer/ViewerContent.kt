package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.ui.components.DeleteConfirmationDialog
import com.knowledge.sporedoc.ui.theme.effects.BackgroundScreen
import com.knowledge.sporedoc.ui.theme.effects.Grain
import com.knowledge.sporedoc.ui.theme.effects.ScreenBackground
import com.knowledge.sporedoc.ui.theme.effects.Vignette

/**
 * Stateless composition of the ViewerScreen's layer stack (`screens.ViewerScreen.layers`):
 * background, appBar + pageStage + footer, vignette, grain, in that order (R2). SD-21 adds
 * the delete-confirmation dialog overlay, shown when [ViewerUiState.isDeleteDialogVisible]
 * is true, and forwards the `share`/`delete` footer callbacks (R1-R4).
 */
@Composable
fun ViewerContent(
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
    Box(modifier = modifier.fillMaxSize()) {
        ScreenBackground(screen = BackgroundScreen.Viewer)
        Column(modifier = Modifier.fillMaxSize()) {
            ViewerAppBar(
                fileName = state.fileName,
                onBack = onBack
            )
            ViewerPageStage(
                pageCount = state.pageCount,
                currentPage = state.currentPage,
                onPageChanged = onPageChanged,
                loadPage = loadPage,
                pageRotationDegrees = state.pageRotationDegrees,
                contentRevision = state.contentRevision,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            if (state.isRotateModeActive) {
                ViewerRotateFooter(
                    onRotateLeftClicked = onRotateLeftClicked,
                    onSaveClicked = onRotateSaveClicked,
                    onCancelClicked = onRotateCancelClicked
                )
            } else {
                ViewerFooter(
                    onShareClicked = onShareClicked,
                    onDeleteClicked = onDeleteClicked,
                    onRotateClicked = onRotateRequested
                )
            }
        }
        Vignette()
        Grain()
        if (state.isDeleteDialogVisible) {
            DeleteConfirmationDialog(
                onConfirm = onDeleteConfirmed,
                onDismiss = onDeleteCancelled
            )
        }
    }
}
