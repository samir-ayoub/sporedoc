package com.knowledge.sporedoc.ui.viewer

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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

    // Guards against a navigation action firing more than once for this screen instance
    // (e.g. two rapid taps on the back icon during the exit transition, SD-14): the first
    // call wins, every later one — tap, system back, or a view-model effect — is a no-op.
    val latestOnBack = rememberUpdatedState(onBack)
    var hasNavigatedBack by remember { mutableStateOf(false) }
    val guardedOnBack = remember {
        {
            if (!hasNavigatedBack) {
                hasNavigatedBack = true
                latestOnBack.value()
            }
        }
    }

    BackHandler(onBack = guardedOnBack)

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
                ViewerEffect.DocumentDeleted -> guardedOnBack()
                ViewerEffect.LoadFailed -> {
                    Toast.makeText(context, loadErrorMessage, Toast.LENGTH_LONG).show()
                    guardedOnBack()
                }
            }
        }
    }

    ViewerContent(
        state = state,
        loadPage = viewModel::loadPage,
        onBack = guardedOnBack,
        onPageChanged = viewModel::onPageChanged,
        onShareClicked = viewModel::onExportRequested,
        onDeleteClicked = viewModel::onDeleteRequested,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteCancelled = viewModel::onDeleteCancelled
    )
}
