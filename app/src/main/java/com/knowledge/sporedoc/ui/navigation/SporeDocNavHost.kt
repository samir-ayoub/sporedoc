package com.knowledge.sporedoc.ui.navigation

import androidx.compose.animation.core.tween
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.knowledge.sporedoc.di.AppContainer
import com.knowledge.sporedoc.domain.model.DocumentId
import com.knowledge.sporedoc.ui.documents.DocumentsScreen
import com.knowledge.sporedoc.ui.documents.DocumentsViewModel
import com.knowledge.sporedoc.ui.splash.SplashScreen
import com.knowledge.sporedoc.ui.viewer.ViewerScreen
import com.knowledge.sporedoc.ui.viewer.ViewerViewModel
import java.net.URLDecoder
import java.net.URLEncoder

private const val SPLASH_ROUTE = "splash"
private const val DOCUMENTS_ROUTE = "documents"
private const val VIEWER_ROUTE = "viewer/{documentId}/{fileName}"
private const val ENCODING = "UTF-8"
private const val TRANSITION_DURATION_MILLIS = 150
private const val SPLASH_CROSSFADE_DURATION_MILLIS = 250

/** Declares the nav graph and starts it at the splash screen. */
@Composable
fun SporeDocNavHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
        enterTransition = {
            if (initialState.destination.route == SPLASH_ROUTE) {
                fadeIn(animationSpec = tween(SPLASH_CROSSFADE_DURATION_MILLIS))
            } else {
                fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS))
            }
        },
        exitTransition = {
            if (targetState.destination.route == DOCUMENTS_ROUTE && initialState.destination.route == SPLASH_ROUTE) {
                fadeOut(animationSpec = tween(SPLASH_CROSSFADE_DURATION_MILLIS))
            } else {
                fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS))
            }
        },
        popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS)) },
        popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS)) }
    ) {
        composable(SPLASH_ROUTE) {
            SplashScreen(
                onFinished = {
                    navController.navigate(DOCUMENTS_ROUTE) {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        composable(DOCUMENTS_ROUTE) {
            val factory = viewModelFactory {
                initializer {
                    DocumentsViewModel(
                        documentRepository = appContainer.documentRepository,
                        groupDocumentsIntoSections = appContainer.groupDocumentsIntoSections,
                        formatDocumentMetaLine = appContainer.formatDocumentMetaLine,
                        documentSourceFor = appContainer.documentSourceFor,
                        shareUriFor = appContainer.shareUriFor,
                        displayNameFor = appContainer.displayNameFor
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
