package com.knowledge.sporedoc.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.domain.model.CropCorner
import com.knowledge.sporedoc.domain.model.CropRect
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocSpacing

/**
 * Four draggable nodes, one per [CropRect] corner, overlaid on the current page's box
 * (SD-23 R1/R4/R5/R11). Translates pixel drag deltas into the normalized (0f..1f)
 * coordinates [onNodeDragged] expects, as the *absolute* requested position for that
 * corner; it does not clamp or validate that position itself - that decision belongs to
 * `AdjustCropRect`, reached through the ViewModel.
 */
@Composable
fun ViewerCropOverlay(
    cropRect: CropRect,
    onNodeDragged: (CropCorner, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        CropNode(corner = CropCorner.TOP_LEFT, fractionX = cropRect.left, fractionY = cropRect.top, widthPx = widthPx, heightPx = heightPx, onNodeDragged = onNodeDragged)
        CropNode(corner = CropCorner.TOP_RIGHT, fractionX = cropRect.right, fractionY = cropRect.top, widthPx = widthPx, heightPx = heightPx, onNodeDragged = onNodeDragged)
        CropNode(corner = CropCorner.BOTTOM_LEFT, fractionX = cropRect.left, fractionY = cropRect.bottom, widthPx = widthPx, heightPx = heightPx, onNodeDragged = onNodeDragged)
        CropNode(corner = CropCorner.BOTTOM_RIGHT, fractionX = cropRect.right, fractionY = cropRect.bottom, widthPx = widthPx, heightPx = heightPx, onNodeDragged = onNodeDragged)
    }
}

/** One draggable corner node, at least `minTouchTargetDp` so it can be grabbed (R11). */
@Composable
private fun CropNode(
    corner: CropCorner,
    fractionX: Float,
    fractionY: Float,
    widthPx: Float,
    heightPx: Float,
    onNodeDragged: (CropCorner, Float, Float) -> Unit
) {
    val nodeSize = SporeDocSpacing.minTouchTargetDp
    val density = LocalDensity.current
    val nodeSizePx = with(density) { nodeSize.toPx() }
    val currentFractionX = rememberUpdatedState(fractionX)
    val currentFractionY = rememberUpdatedState(fractionY)
    val currentOnNodeDragged = rememberUpdatedState(onNodeDragged)
    val nodeContentDescription = stringResource(R.string.crop_node_content_description)

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { (fractionX * widthPx - nodeSizePx / 2f).toDp() },
                y = with(density) { (fractionY * heightPx - nodeSizePx / 2f).toDp() }
            )
            .size(nodeSize)
            .background(SporeDocColors.goldBrushed, CircleShape)
            .pointerInput(corner, widthPx, heightPx) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newFractionX = currentFractionX.value + dragAmount.x / widthPx
                    val newFractionY = currentFractionY.value + dragAmount.y / heightPx
                    currentOnNodeDragged.value(corner, newFractionX, newFractionY)
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = nodeContentDescription
            }
    )
}
