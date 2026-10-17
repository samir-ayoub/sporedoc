package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * Full-width bar that replaces the SD-15 tripartite footer while in selection mode (R10/R11):
 * a summary line of totals, then the `SHARE` / `MERGE` / `PURGE` actions. Tapping any action
 * is intentionally wired to a no-op (R12): this delivery has no behavior behind them.
 */
@Composable
fun DocumentsContextualBar(
    summary: SelectionSummaryUi,
    onShareRequested: () -> Unit = {},
    onPurgeRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SporeDocColors.footerBoxBackground)
            .padding(
                start = SporeDocSpacing.contextualBarPaddingHorizontalDp,
                end = SporeDocSpacing.contextualBarPaddingHorizontalDp,
                top = SporeDocSpacing.contextualBarPaddingTopDp,
                bottom = SporeDocSpacing.contextualBarPaddingBottomDp
            )
    ) {
        Text(
            text = stringResource(
                R.string.selection_summary_template,
                summary.documentCount,
                summary.totalPages,
                summary.totalSizeMb
            ),
            style = SporeDocTypography.fileMeta
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SporeDocSpacing.contextualBarSummaryGapDp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ContextualAction(
                label = stringResource(R.string.contextual_share_label),
                description = stringResource(R.string.contextual_share_content_description),
                color = SporeDocColors.magma,
                onClick = onShareRequested
            )
            ContextualAction(
                label = stringResource(R.string.contextual_merge_label),
                description = stringResource(R.string.contextual_merge_content_description),
                color = SporeDocColors.magma,
                onClick = {}
            )
            ContextualAction(
                label = stringResource(R.string.contextual_purge_label),
                description = stringResource(R.string.contextual_purge_content_description),
                color = SporeDocColors.rust,
                onClick = onPurgeRequested
            )
        }
    }
}

@Composable
private fun ContextualAction(
    label: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(SporeDocSpacing.minTouchTargetDp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = SporeDocTypography.textAction.copy(color = color))
    }
}
