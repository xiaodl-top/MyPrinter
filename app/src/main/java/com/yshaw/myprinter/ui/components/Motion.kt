package com.yshaw.myprinter.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * One place for the timing curves of the app. Everything that moves reads these tokens so the
 * whole UI feels like it runs on a single clock:
 *
 * * transitions use a decelerating ease-out curve (fast out of the finger, slow into place),
 * * presses settle with a lightly under-damped spring,
 * * entrances are staggered by [EntranceStagger] so screens unfold instead of appearing.
 */
object Motion {
    const val Fast = 170
    const val Normal = 280
    const val Slow = 460

    /** Duration of one reveal (fade + rise). */
    const val Entrance = 620
    const val EntranceStagger = 70L

    /** A decelerating curve: quick start, gentle landing. */
    val EaseOut: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    /** Standard spatial movement (sheets, cards, content swaps). */
    val EaseInOut: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    fun <T> pressSpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = 0.45f,
            stiffness = 780f,
            visibilityThreshold = null,
        )

    fun <T> settleSpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = 0.82f,
            stiffness = 380f,
        )

    fun <T> bouncySpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 520f,
        )
}

/**
 * Scales the element down while it is pressed. Apply after [Modifier.clip] so the ripple stays
 * inside the shape, and give the same [interactionSource] to the clickable modifier.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.965f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.pressSpring(),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Entrance animation: the content fades in while rising into place. [index] staggers several
 * blocks so a screen "assembles" itself top to bottom.
 */
@Composable
fun Reveal(
    index: Int = 0,
    modifier: Modifier = Modifier,
    offsetY: Dp = 26.dp,
    content: @Composable () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (index > 0) delay(index * Motion.EntranceStagger)
        started = true
    }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = Motion.Entrance, easing = Motion.EaseOut),
        label = "reveal",
    )
    Box(
        modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * offsetY.toPx()
        },
    ) {
        content()
    }
}
