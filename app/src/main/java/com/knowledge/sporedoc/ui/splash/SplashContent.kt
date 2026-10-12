package com.knowledge.sporedoc.ui.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.knowledge.sporedoc.R
import com.knowledge.sporedoc.ui.theme.SporeDocColors
import com.knowledge.sporedoc.ui.theme.SporeDocTypography
import com.knowledge.sporedoc.ui.theme.effects.BackgroundScreen
import com.knowledge.sporedoc.ui.theme.effects.Grain
import com.knowledge.sporedoc.ui.theme.effects.ScreenBackground
import com.knowledge.sporedoc.ui.theme.effects.Vignette

private const val GRAIN_OPACITY = 0.08f
private const val HALO_BASE_RADIUS_DP = 28f

/**
 * Stateless rendering of one splash frame from progress values (all 0f..1f).
 * Reuses the design system's own background, vignette and grain (R9), the existing
 * documents wordmark/subtitle strings, and the new splash baseline string (R10).
 */
@Composable
fun SplashContent(
    atmosphereProgress: Float,
    beamProgress: Float,
    haloProgress: Float,
    wordmarkProgress: Float,
    subtitleProgress: Float,
    modifier: Modifier = Modifier,
) {
    val appName = stringResource(R.string.app_name)
    Box(
        modifier
            .fillMaxSize()
            .semantics { contentDescription = appName }
    ) {
        ScreenBackground(screen = BackgroundScreen.Splash, opacity = atmosphereProgress)

        Canvas(Modifier.fillMaxSize().clearAndSetSemantics { }) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val px = 1.dp.toPx()
            val topStart = center.y - 96 * px
            val top = topStart * (1f - beamProgress)
            val bottom = center.y + 70 * px
            val fadeStart = ((bottom - 70 * px - top) / (bottom - top)).coerceIn(0f, 1f)

            drawRect(
                Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.5f to SporeDocColors.laserGreen.copy(alpha = 0.22f),
                    1f to Color.Transparent,
                    startX = center.x - 6 * px,
                    endX = center.x + 6 * px,
                ),
                topLeft = Offset(center.x - 6 * px, top),
                size = Size(12 * px, bottom - top - 70 * px),
                alpha = 0.9f,
            )
            drawRect(
                Brush.verticalGradient(
                    0f to SporeDocColors.laserGreen,
                    fadeStart to SporeDocColors.laserGreen,
                    1f to Color.Transparent,
                    startY = top,
                    endY = bottom,
                ),
                topLeft = Offset(center.x - 1.07f * px, top),
                size = Size(2.13f * px, bottom - top),
            )
            drawRect(
                SporeDocColors.laserHot,
                topLeft = Offset(center.x - 0.4f * px, top),
                size = Size(0.8f * px, bottom - top - 70 * px),
                alpha = 0.85f,
            )

            if (haloProgress > 0f) {
                val apex = Offset(center.x, center.y - 56 * px)
                val baseRadius = HALO_BASE_RADIUS_DP * px
                drawCircle(
                    Brush.radialGradient(
                        0f to SporeDocColors.laserHot.copy(alpha = 0.6f * haloProgress),
                        1f to Color.Transparent,
                        center = apex,
                        radius = baseRadius,
                    ),
                    radius = baseRadius * (1f + 0.25f * haloProgress),
                    center = apex,
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.splash_mark),
            contentDescription = null,
            modifier = Modifier
                .size(288.dp)
                .align(Alignment.Center)
                .clearAndSetSemantics { },
        )

        Column(
            Modifier
                .align(Alignment.Center)
                .offset(y = 151.dp)
                .fillMaxWidth()
                .clearAndSetSemantics { },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.documents_wordmark),
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkProgress
                    translationY = (1f - wordmarkProgress) * 8.dp.toPx()
                },
                style = SporeDocTypography.wordmark,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.documents_subtitle),
                modifier = Modifier
                    .padding(top = 14.dp)
                    .graphicsLayer { alpha = subtitleProgress },
                style = SporeDocTypography.subtitle,
            )
        }

        Text(
            text = stringResource(R.string.splash_baseline),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .graphicsLayer { alpha = subtitleProgress }
                .clearAndSetSemantics { },
            style = SporeDocTypography.splashBaseline,
        )

        Vignette(opacity = atmosphereProgress)
        Grain(opacity = GRAIN_OPACITY * atmosphereProgress, modifier = Modifier.clearAndSetSemantics { })
    }
}

@Preview
@Composable
private fun SplashContentFinalFramePreview() {
    SplashContent(
        atmosphereProgress = 1f,
        beamProgress = 1f,
        haloProgress = 0f,
        wordmarkProgress = 1f,
        subtitleProgress = 1f,
    )
}
