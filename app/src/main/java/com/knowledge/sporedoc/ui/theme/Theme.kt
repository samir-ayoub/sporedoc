package com.knowledge.sporedoc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The app is dark-only (see AGENTS.md "Modo claro: a app é só escura"), so there is no
// light/dark branching and no dynamic color: named tokens are read directly by screens,
// this color scheme only backs default Material3 component behavior.
private val SporeDocColorScheme = darkColorScheme(
    primary = SporeDocColors.goldBrushed,
    onPrimary = SporeDocColors.ink,
    secondary = SporeDocColors.goldMatte,
    onSecondary = SporeDocColors.ink,
    background = SporeDocColors.bgEdge,
    onBackground = SporeDocColors.ivory,
    surface = SporeDocColors.bgEdge,
    onSurface = SporeDocColors.ivory,
)

@Composable
fun SporeDocTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SporeDocColorScheme,
        shapes = SporeDocShapes,
        content = content,
    )
}
