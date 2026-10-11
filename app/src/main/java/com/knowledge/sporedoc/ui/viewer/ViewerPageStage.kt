package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.ui.components.toImageBitmapOrNull
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.SporeDocTypography
import com.knowledge.sporedoc.ui.theme.effects.LaserReticle
import com.knowledge.sporedoc.ui.theme.effects.pageShadow

/**
 * The page stage (R5-R9): a horizontal pager over all pages, the current page centered,
 * paper-filled, with page shadow and laser reticles on its four corners, and the
 * `PAGE n / total` indicator below it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewerPageStage(
    pageCount: Int,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    loadPage: suspend (Int) -> PageImage?,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = (currentPage - 1).coerceIn(0, (pageCount - 1).coerceAtLeast(0))) {
        pageCount.coerceAtLeast(1)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page -> onPageChanged(page) }
    }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = SporeDocSpacing.viewerPageIndicatorGapDp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PageStageContent(pageIndex = pageIndex, loadPage = loadPage)
                }
            }
        }
        Text(
            text = stringResource(R.string.viewer_page_indicator, currentPage, pageCount),
            style = SporeDocTypography.pageIndicator
        )
    }
}

@Composable
private fun PageStageContent(pageIndex: Int, loadPage: suspend (Int) -> PageImage?) {
    val pageImage by produceState<PageImage?>(initialValue = null, key1 = pageIndex) {
        value = loadPage(pageIndex)
    }
    Box(
        modifier = Modifier
            .width(SporeDocSpacing.viewerPageWidthDp)
            .aspectRatio(SporeDocSpacing.viewerPageAspectRatio)
            .pageShadow()
            .background(SporeDocColors.paper)
    ) {
        val currentImage = pageImage
        if (currentImage != null) {
            val bitmap = remember(currentImage) { currentImage.toImageBitmapOrNull() }
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        LaserReticle(modifier = Modifier.fillMaxSize())
    }
}
