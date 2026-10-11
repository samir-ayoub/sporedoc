package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/** Renders the CAPTURE button, per `components.OutlinedPrimaryButton`. */
@Composable
fun OutlinedPrimaryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.capture_content_description)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SporeDocSpacing.buttonHeightDp)
            .border(SporeDocStroke.hairlineDp, SporeDocColors.goldBrushed)
            .background(SporeDocColors.bgEdge.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptureIcon()
        Spacer(modifier = Modifier.width(SporeDocSpacing.buttonIconGapDp))
        Text(text = stringResource(R.string.capture_label), style = SporeDocTypography.primaryButton)
    }
}

/** Decorative viewfinder-corners icon; the button's own semantics carry the content description. */
@Composable
private fun CaptureIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidthPx = SporeDocStroke.iconStrokeDp.toPx()
        val w = size.width
        val h = size.height
        val arm = w * 0.3f
        val color = SporeDocColors.goldBrushed
        val cap = StrokeCap.Square

        drawLine(color, Offset(0f, arm), Offset(0f, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(0f, 0f), Offset(arm, 0f), strokeWidthPx, cap)

        drawLine(color, Offset(w - arm, 0f), Offset(w, 0f), strokeWidthPx, cap)
        drawLine(color, Offset(w, 0f), Offset(w, arm), strokeWidthPx, cap)

        drawLine(color, Offset(w, h - arm), Offset(w, h), strokeWidthPx, cap)
        drawLine(color, Offset(w, h), Offset(w - arm, h), strokeWidthPx, cap)

        drawLine(color, Offset(arm, h), Offset(0f, h), strokeWidthPx, cap)
        drawLine(color, Offset(0f, h), Offset(0f, h - arm), strokeWidthPx, cap)
    }
}
