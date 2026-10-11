package com.knowledge.sporedoc.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.SectionLabel
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocStroke
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/** Renders one section's top rule and label, per `components.SectionHeader`. */
@Composable
fun DocumentSectionHeader(label: SectionLabel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SporeDocStroke.hairlineDp)
                .background(SporeDocColors.goldMatte)
        )
        Text(
            text = stringResource(sectionLabelRes(label)),
            style = SporeDocTypography.sectionLabel,
            modifier = Modifier.padding(
                start = SporeDocSpacing.screenGutterDp,
                top = SporeDocSpacing.sectionLabelPaddingTopDp,
                bottom = SporeDocSpacing.sectionLabelPaddingBottomDp
            )
        )
    }
}

private fun sectionLabelRes(label: SectionLabel): Int = when (label) {
    SectionLabel.TODAY -> R.string.section_today
    SectionLabel.RECENT_DOCUMENTS -> R.string.section_recent_documents
    SectionLabel.ARCHIVE -> R.string.section_archive
}
