package com.yshaw.myprinter.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.yshaw.myprinter.ui.myPrinterColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The ambient backdrop shared by both screens: a soft page tint with two very large colour blobs
 * that drift slowly around the screen. The movement is deliberately slow (one full orbit takes
 * minutes) so it reads as light in a room rather than as an animation.
 *
 * Only the draw phase is invalidated, so the rest of the screen never recomposes because of it.
 */
@Composable
fun AuroraBackdrop(modifier: Modifier = Modifier) {
    val colors = myPrinterColors
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "auroraPhase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                0f to colors.pageBackgroundAlt,
                0.45f to colors.pageBackground,
                1f to colors.pageBackground,
            ),
        )

        val w = size.width
        val h = size.height
        val longest = size.maxDimension

        // Indigo light source, slowly orbiting the upper half of the screen.
        val ax = w * (0.18f + 0.22f * (0.5f + 0.5f * sin(phase)))
        val ay = h * (0.10f + 0.14f * (0.5f + 0.5f * cos(phase * 0.9f)))
        val aRadius = longest * 0.78f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.glowA.copy(alpha = if (colors.isDark) 0.40f else 0.18f),
                    Color.Transparent,
                ),
                center = Offset(ax, ay),
                radius = aRadius,
            ),
            radius = aRadius,
            center = Offset(ax, ay),
        )

        // Teal counter-light in the lower right corner.
        val bx = w * (1.05f - 0.26f * (0.5f + 0.5f * sin(phase + 2.1f)))
        val by = h * (0.62f + 0.16f * (0.5f + 0.5f * cos(phase + 1.2f)))
        val bRadius = longest * 0.66f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.glowB.copy(alpha = if (colors.isDark) 0.24f else 0.12f),
                    Color.Transparent,
                ),
                center = Offset(bx, by),
                radius = bRadius,
            ),
            radius = bRadius,
            center = Offset(bx, by),
        )
    }
}
