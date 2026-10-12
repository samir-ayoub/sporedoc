package com.knowledge.sporedoc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.knowledge.sporedoc.ui.navigation.SporeDocNavHost
import com.knowledge.sporedoc.ui.navigation.externalDocumentUriFrom
import com.knowledge.sporedoc.ui.theme.SporeDocTheme

/** Host of the navigation; also forwards an incoming external-document Uri, if any. */
class MainActivity : ComponentActivity() {

    private var pendingExternalUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingExternalUri = externalDocumentUriFrom(intent)
        val appContainer = (application as SporeDocApp).appContainer
        setContent {
            SporeDocTheme {
                SporeDocNavHost(
                    appContainer = appContainer,
                    pendingExternalUri = pendingExternalUri,
                    onPendingExternalUriConsumed = { pendingExternalUri = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingExternalUri = externalDocumentUriFrom(intent)
    }
}
