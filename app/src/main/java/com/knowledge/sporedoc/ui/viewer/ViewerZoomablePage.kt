package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.knowledge.sporedoc.domain.model.PageImage
import com.knowledge.sporedoc.domain.model.PixelSize
import com.knowledge.sporedoc.ui.components.pixelSizeForDp
import com.knowledge.sporedoc.ui.components.toImageBitmapOrNull
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing
import com.knowledge.sporedoc.ui.theme.effects.LaserReticle
import com.knowledge.sporedoc.ui.theme.effects.pageShadow

/**
 * One viewer page: renders the page image with the palco chrome (R9) at 1x, and -
 * above 1x - the same image full-bleed across the whole page-stage slot (R8),
 * driven by pinch, pan and double-tap gestures routed into a per-page [PageZoomState].
 *
 * [onRestChanged] reports whether this page is currently at 1x, so the caller (the
 * pager) can gate its own drag handling (R10).
 */
@Composable
fun ViewerZoomablePage(
    pageIndex: Int,
    loadPage: suspend (Int, PixelSize) -> PageImage?,
    onRestChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomState = remember(pageIndex) { PageZoomState() }
    val density = LocalDensity.current
    val targetSize = remember(density) {
        val heightDp: Dp = SporeDocSpacing.viewerPageWidthDp / SporeDocSpacing.viewerPageAspectRatio
        density.pixelSizeForDp(
            widthDp = SporeDocSpacing.viewerPageWidthDp,
            heightDp = heightDp
        )
    }
    val pageImage by produceState<PageImage?>(initialValue = null, key1 = pageIndex) {
        value = loadPage(pageIndex, targetSize)
    }
    val bitmap = remember(pageImage) { pageImage?.toImageBitmapOrNull() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember(pageIndex) { mutableFloatStateOf(1f) }
    var offsetX by remember(pageIndex) { mutableFloatStateOf(0f) }
    var offsetY by remember(pageIndex) { mutableFloatStateOf(0f) }

    fun syncFromState() {
        scale = zoomState.scale
        offsetX = zoomState.offsetX
        offsetY = zoomState.offsetY
        onRestChanged(zoomState.scale <= 1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(pageIndex) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    zoomState.onGestureChange(
                        contentWidth = targetSize.widthPx.toFloat(),
                        contentHeight = targetSize.heightPx.toFloat(),
                        containerWidth = containerSize.width.toFloat(),
                        containerHeight = containerSize.height.toFloat(),
                        zoomChange = zoom,
                        panChangeX = pan.x,
                        panChangeY = pan.y,
                        centroidX = centroid.x,
                        centroidY = centroid.y
                    )
                    syncFromState()
                }
            }
            .pointerInput(pageIndex) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    zoomState.onGestureEnd()
                    syncFromState()
                }
            }
            .pointerInput(pageIndex) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        zoomState.onDoubleTap(
                            contentWidth = targetSize.widthPx.toFloat(),
                            contentHeight = targetSize.heightPx.toFloat(),
                            containerWidth = containerSize.width.toFloat(),
                            containerHeight = containerSize.height.toFloat(),
                            tapX = tap.x,
                            tapY = tap.y
                        )
                        syncFromState()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(SporeDocSpacing.viewerPageWidthDp)
                .aspectRatio(SporeDocSpacing.viewerPageAspectRatio)
                .pageShadow()
                .background(SporeDocColors.paper)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            LaserReticle(modifier = Modifier.fillMaxSize())
        }

        if (scale > 1f) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
