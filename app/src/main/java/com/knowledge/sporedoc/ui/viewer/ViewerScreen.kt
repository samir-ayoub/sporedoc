package com.knowledge.sporedoc.ui.viewer

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledge.sporedoc.R

/**
 * Stateful glue: collects [ViewerViewModel]'s state/effects, launches the share intent,
 * shows the load-error toast, and wires back navigation (R16, R17).
 */
@Composable
fun ViewerScreen(viewModel: ViewerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val loadErrorMessage = stringResource(R.string.viewer_load_error_message)

    BackHandler(onBack = onBack)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ViewerEffect.ShareDocument -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
                ViewerEffect.DocumentDeleted -> onBack()
                ViewerEffect.LoadFailed -> {
                    Toast.makeText(context, loadErrorMessage, Toast.LENGTH_LONG).show()
                    onBack()
                }
            }
        }
    }

    ViewerContent(
        state = state,
        loadPage = viewModel::loadPage,
        onBack = onBack,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteCancelled = viewModel::onDeleteCancelled,
        onExport = viewModel::onExportRequested,
        onPageChanged = viewModel::onPageChanged
    )
}
