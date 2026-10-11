package com.knowledge.sporedoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.knowledge.sporedoc.di.AppContainer
import com.knowledge.sporedoc.ui.documents.DocumentsScreen
import com.knowledge.sporedoc.ui.documents.DocumentsViewModel

private const val DOCUMENTS_ROUTE = "documents"

/** Declares the nav graph and starts it at the documents screen. */
@Composable
fun SporeDocNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = DOCUMENTS_ROUTE) {
        composable(DOCUMENTS_ROUTE) {
            val factory = viewModelFactory {
                initializer {
                    DocumentsViewModel(
                        documentRepository = appContainer.documentRepository,
                        groupDocumentsIntoSections = appContainer.groupDocumentsIntoSections,
                        formatDocumentMetaLine = appContainer.formatDocumentMetaLine,
                        documentSourceFor = appContainer.documentSourceFor
                    )
                }
            }
            val viewModel: DocumentsViewModel = viewModel(factory = factory)
            DocumentsScreen(viewModel = viewModel)
        }
    }
}
