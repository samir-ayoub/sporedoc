package com.knowledge.sporedoc.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.knowledge.sporedoc.ui.theme.effects.BackgroundScreen
import com.knowledge.sporedoc.ui.theme.effects.EclipseOrb
import com.knowledge.sporedoc.ui.theme.effects.Grain
import com.knowledge.sporedoc.ui.theme.effects.ScreenBackground
import com.knowledge.sporedoc.ui.theme.effects.Vignette

/**
 * Preview workflow #1 (R15): the DocumentsScreen background stack on its own -
 * background, orb, vignette and grain, in screens.DocumentsScreen.layers order.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun DocumentsBackgroundPreview() {
    SporeDocTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenBackground(screen = BackgroundScreen.Documents)
            EclipseOrb()
            Vignette()
            Grain()
        }
    }
}
