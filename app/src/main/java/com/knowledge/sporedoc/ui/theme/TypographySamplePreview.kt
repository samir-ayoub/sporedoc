package com.knowledge.sporedoc.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val typographySamples: List<Pair<String, TextStyle>> = listOf(
    "wordmark" to SporeDocTypography.wordmark,
    "subtitle" to SporeDocTypography.subtitle,
    "sectionLabel" to SporeDocTypography.sectionLabel,
    "fileName" to SporeDocTypography.fileName,
    "fileMeta" to SporeDocTypography.fileMeta,
    "textAction" to SporeDocTypography.textAction,
    "primaryButton" to SporeDocTypography.primaryButton,
    "exportButton" to SporeDocTypography.exportButton,
    "appBarTitle" to SporeDocTypography.appBarTitle,
    "pageIndicator" to SporeDocTypography.pageIndicator,
)

/**
 * Preview workflow #2 (R15): a sample of every tokens.typography.styles entry, one row per
 * style, identified by the style's own name.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
fun TypographySamplePreview() {
    SporeDocTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            typographySamples.forEach { (name, style) ->
                Text(text = "$name — Sample", style = style)
            }
        }
    }
}
