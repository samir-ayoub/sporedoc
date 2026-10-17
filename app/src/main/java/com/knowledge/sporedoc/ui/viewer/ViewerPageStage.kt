package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.CropCorner
import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocTypography

/**
 * The page stage (R5-R9): a horizontal pager over all pages, the current page centered,
 * paper-filled, with page shadow and laser reticles on its four corners, and the
 * `PAGE n / total` indicator below it. Pager drag is disabled while the current page
 * is zoomed above 1x (R10), since a horizontal drag then means pan, not paging.
 *
 * SD-23: while [isCropModeActive], overlays [ViewerCropOverlay] over the current page's
 * fixed-size box only - the one place that already knows that box's layout (centered,
 * `viewerPageWidthDp` x `viewerPageAspectRatio`), so it anchors the crop nodes without
 * recomputing that geometry elsewhere.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewerPageStage(
    pageCount: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    loadPage: suspend (Int, PixelSize) -> PageImage?,
    pageRotationDegrees: Map<Int, Int> = emptyMap(),
    contentRevision: Int = 0,
    isCropModeActive: Boolean = false,
    cropRect: CropRect? = null,
    onCropNodeDragged: (CropCorner, Float, Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = (currentPage - 1).coerceIn(0, (pageCount - 1).coerceAtLeast(0))) {
        pageCount.coerceAtLeast(1)
    }
    var currentPageAtRest by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPageAtRest = true
            onPageChanged(page)
        }
    }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = SporeDocSpacing.viewerPageIndicatorGapDp)) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = currentPageAtRest,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ViewerZoomablePage(
                        pageIndex = pageIndex,
                        loadPage = loadPage,
                        rotationDegrees = pageRotationDegrees[pageIndex] ?: 0,
                        contentRevision = contentRevision,
                        onRestChanged = { isAtRest ->
                            if (pageIndex == pagerState.currentPage) {
                                currentPageAtRest = isAtRest
                            }
                        }
                    )
                    if (isCropModeActive && cropRect != null && pageIndex == currentPage - 1) {
                        Box(
                            modifier = Modifier
                                .width(SporeDocSpacing.viewerPageWidthDp)
                                .aspectRatio(SporeDocSpacing.viewerPageAspectRatio)
                        ) {
                            ViewerCropOverlay(
                                cropRect = cropRect,
                                onNodeDragged = onCropNodeDragged,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.viewer_page_indicator, currentPage, pageCount),
            style = SporeDocTypography.pageIndicator
        )
    }
}
