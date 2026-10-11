package com.knowledge.sporedoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.knowledge.sporedoc.ui.navigation.SporeDocNavHost
import com.knowledge.sporedoc.ui.theme.SporeDocTheme

/** Host of the navigation, nothing else. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as SporeDocApp).appContainer
        setContent {
            SporeDocTheme {
                SporeDocNavHost(appContainer = appContainer)
            }
        }
    }
}
