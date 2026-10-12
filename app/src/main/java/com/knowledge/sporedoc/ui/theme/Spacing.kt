package com.knowledge.sporedoc.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing tokens from docs/design/sporedoc-handover.json -> tokens.spacing. */
object SporeDocSpacing {
    val screenGutterDp: Dp = 24.dp
    val rowPaddingVerticalDp: Dp = 14.dp
    val rowGapThumbToTextDp: Dp = 18.dp
    val sectionTopGapDp: Dp = 14.dp
    val sectionLabelPaddingTopDp: Dp = 16.dp
    val sectionLabelPaddingBottomDp: Dp = 4.dp
    val minTouchTargetDp: Dp = 44.dp

    // screens.DocumentsScreen.header
    val headerHeightDp: Dp = 196.dp
    val headerPaddingBottomDp: Dp = 28.dp
    val subtitleMarginTopDp: Dp = 16.dp

    // components.DocumentRow
    val rowPaddingStartDp: Dp = 24.dp
    val rowPaddingEndDp: Dp = 10.dp
    val rowDividerInsetDp: Dp = 24.dp
    val textColumnGapDp: Dp = 6.dp
    val thumbnailWidthDp: Dp = 38.dp
    val thumbnailHeightDp: Dp = 52.dp
    val thumbnailFramePaddingDp: Dp = 2.dp

    // screens.DocumentsScreen.body
    val bodyBottomContentPaddingDp: Dp = 144.dp

    // screens.DocumentsScreen.bottomDock
    val dockPaddingTopDp: Dp = 56.dp
    val dockPaddingBottomDp: Dp = 30.dp

    // ui/documents/DocumentsFooter
    val footerBoxHeightDp: Dp = 72.dp
    val footerBoxGapDp: Dp = 10.dp
    val footerIconSizeDp: Dp = 20.dp
    val footerIconLabelGapDp: Dp = 10.dp
    val footerBoxBorderInsetDp: Dp = 0.dp

    // screens.ViewerScreen.appBar
    val viewerAppBarHeightDp: Dp = 68.dp
    val viewerAppBarIconColumnWidthDp: Dp = 48.dp
    val viewerAppBarHorizontalPaddingDp: Dp = 8.dp

    val viewerAppBarVerticalPaddingDp: Dp = 8.dp
    val viewerAppBarColumnGapDp: Dp = 8.dp

    // screens.ViewerScreen.pageStage
    val viewerPageWidthDp: Dp = 282.dp
    val viewerPageAspectRatio: Float = 282f / 398f // tokens.pageStage ratio (width / height), A4 1:1.414
    val viewerPageIndicatorGapDp: Dp = 30.dp

    // screens.ViewerScreen.footer
    val viewerFooterPaddingTopDp: Dp = 18.dp
    val viewerFooterPaddingHorizontalDp: Dp = 24.dp
    val viewerFooterPaddingBottomDp: Dp = 30.dp
    val viewerFooterButtonHeightDp: Dp = 58.dp

    // ui/viewer/DeleteConfirmationDialog
    val dialogPaddingDp: Dp = 20.dp
}
