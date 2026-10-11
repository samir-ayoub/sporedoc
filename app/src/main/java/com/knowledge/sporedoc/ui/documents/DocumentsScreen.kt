package com.knowledge.sporedoc.ui.documents

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.knowledge.sporedoc.R

/**
 * Stateful glue: collects [DocumentsViewModel]'s state/effects, owns the ML Kit Document
 * Scanner `ActivityResultLauncher`, the resume-reload lifecycle observer, and error display.
 */
@Composable
fun DocumentsScreen(viewModel: DocumentsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.capture_error_message)

    val scanLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val scanResult = result.data?.let { GmsDocumentScanningResult.fromActivityResultIntent(it) }
                val uri = scanResult?.pdf?.uri
                if (uri != null) {
                    viewModel.onScanSucceeded(uri)
                } else {
                    viewModel.onScanFailed()
                }
            }
            Activity.RESULT_CANCELED -> Unit
            else -> viewModel.onScanFailed()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                DocumentsEffect.ShowCaptureError -> snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DocumentsContent(
            state = state,
            onCapture = {
                val options = GmsDocumentScannerOptions.Builder()
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                    .build()
                val scanner = GmsDocumentScanning.getClient(options)
                scanner.getStartScanIntent(context as Activity)
                    .addOnSuccessListener { intentSender ->
                        scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    }
                    .addOnFailureListener {
                        viewModel.onScanFailed()
                    }
            },
            loadThumbnail = viewModel::loadThumbnail
        )
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
