package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/** Renders the export button, per `components.ExportButton`: label plus a muted "(PDF)" suffix. */
@Composable
fun ExportButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.viewer_export_label)
    val suffix = stringResource(R.string.viewer_export_suffix)
    val text = buildAnnotatedString {
        append(label)
        withStyle(SporeDocTypography.exportButton.copy(color = SporeDocColors.ivoryMuted).toSpanStyle()) {
            append(suffix)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SporeDocSpacing.viewerFooterButtonHeightDp)
            .border(SporeDocStroke.hairlineDp, SporeDocColors.goldBrushed)
            .background(SporeDocColors.bgEdge.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = SporeDocTypography.exportButton)
    }
}
