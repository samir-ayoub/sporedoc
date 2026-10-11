package com.knowledge.sporedoc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.knowledge.sporedoc.di.AppContainer
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.ui.documents.DocumentsScreen
import com.knowledge.sporedoc.ui.documents.DocumentsViewModel
import com.knowledge.sporedoc.ui.viewer.ViewerScreen
import com.knowledge.sporedoc.ui.viewer.ViewerViewModel
import java.net.URLDecoder
import java.net.URLEncoder

private const val DOCUMENTS_ROUTE = "documents"
private const val VIEWER_ROUTE = "viewer/{documentId}/{fileName}"
private const val ENCODING = "UTF-8"

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
            DocumentsScreen(
                viewModel = viewModel,
                onOpenDocument = { id, fileName ->
                    val encodedId = URLEncoder.encode(id.value, ENCODING)
                    val encodedFileName = URLEncoder.encode(fileName, ENCODING)
                    navController.navigate("viewer/$encodedId/$encodedFileName")
                }
            )
        }
        composable(
            route = VIEWER_ROUTE,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType },
                navArgument("fileName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedId = backStackEntry.arguments?.getString("documentId").orEmpty()
            val encodedFileName = backStackEntry.arguments?.getString("fileName").orEmpty()
            val documentId = DocumentId(URLDecoder.decode(encodedId, ENCODING))
            val fileName = URLDecoder.decode(encodedFileName, ENCODING)
            val factory = viewModelFactory {
                initializer {
                    ViewerViewModel(
                        documentId = documentId,
                        fileName = fileName,
                        documentRepository = appContainer.documentRepository,
                        shareUriFor = appContainer.shareUriFor
                    )
                }
            }
            val viewModel: ViewerViewModel = viewModel(factory = factory)
            ViewerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
