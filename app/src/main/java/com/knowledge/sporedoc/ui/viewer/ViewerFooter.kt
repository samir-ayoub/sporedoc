package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke

/** The footer (R10): top rule, padding and the [ExportButton]. */
@Composable
fun ViewerFooter(onExport: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SporeDocStroke.hairlineDp)
                .background(SporeDocColors.goldDividerFooter)
        )
        Box(
            modifier = Modifier.padding(
                start = SporeDocSpacing.viewerFooterPaddingHorizontalDp,
                end = SporeDocSpacing.viewerFooterPaddingHorizontalDp,
                top = SporeDocSpacing.viewerFooterPaddingTopDp,
                bottom = SporeDocSpacing.viewerFooterPaddingBottomDp
            )
        ) {
            ExportButton(onClick = onExport)
        }
    }
}
