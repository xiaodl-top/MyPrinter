package top.xiaodl.myprinter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ui.BorderColor
import top.xiaodl.myprinter.ui.MyPrinterColors
import top.xiaodl.myprinter.ui.ScrimColor
import top.xiaodl.myprinter.ui.SurfaceColor
import top.xiaodl.myprinter.ui.TextPrimary
import top.xiaodl.myprinter.ui.TextSecondary
import top.xiaodl.myprinter.ui.brandGradient
import top.xiaodl.myprinter.ui.myPrinterColors
import kotlin.math.roundToInt

/** Full-screen dimming layer; tapping it dismisses the overlay. */
@Composable
fun ModalScrim(visible: Boolean, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.Normal)),
        exit = fadeOut(tween(Motion.Fast)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }
}

/**
 * A bottom sheet that slides up over the page. It can be dismissed by tapping the scrim, by
 * dragging the handle down, or by the caller changing [visible]; the exit animation always plays
 * because [AnimatedVisibility] keeps the content composed until it finishes.
 */
@Composable
fun BoxScope.BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalScrim(visible = visible, onDismiss = onDismiss)

    val density = LocalDensity.current
    var dragOffset by remember { mutableStateOf(0f) }
    val dismissThreshold = with(density) { 120.dp.toPx() }

    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
        enter = slideInVertically(
            animationSpec = tween(Motion.Slow, easing = Motion.EaseOut),
            initialOffsetY = { it },
        ) + fadeIn(tween(Motion.Normal)),
        exit = slideOutVertically(
            animationSpec = tween(Motion.Normal, easing = Motion.EaseInOut),
            targetOffsetY = { it },
        ) + fadeOut(tween(Motion.Fast)),
    ) {
        val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .clip(shape)
                .background(SurfaceColor)
                .border(1.dp, BorderColor, shape)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        ) {
            // Drag handle: the only place that accepts the dismiss gesture, so rows stay tappable.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset > dismissThreshold) {
                                    dragOffset = 0f
                                    onDismiss()
                                } else {
                                    dragOffset = 0f
                                }
                            },
                            onDragCancel = { dragOffset = 0f },
                            onVerticalDrag = { _, delta ->
                                dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(TextPrimary.copy(alpha = 0.16f)),
                )
            }
            content()
        }
    }
}

/** A centred dialog card that scales in with a light spring. */
@Composable
fun BoxScope.CenterDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalScrim(visible = visible, onDismiss = onDismiss)
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 26.dp)
            .widthIn(max = 420.dp),
        enter = scaleIn(
            animationSpec = tween(Motion.Slow, easing = Motion.EaseOut),
            initialScale = 0.90f,
        ) + fadeIn(tween(Motion.Normal)),
        exit = scaleOut(
            animationSpec = tween(Motion.Fast, easing = Motion.EaseInOut),
            targetScale = 0.94f,
        ) + fadeOut(tween(Motion.Fast)),
    ) {
        DialogCard(content = content)
    }
}

/** The visual shell of a dialog: rounded surface, soft shadow, then content. */
@Composable
fun DialogCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 0.dp else 30.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.28f),
            )
            .clip(shape)
            .background(SurfaceColor)
            .border(1.dp, BorderColor, shape)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        content = content,
    )
}

/** Dialog title + message block, shared by the result and the configuration prompts. */
@Composable
fun DialogMessage(title: String, message: String) {
    Text(
        text = title,
        fontSize = 18.5.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = message,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = TextSecondary,
    )
}

/** Two horizontal actions, primary on the right, matching the platform's dialog order. */
@Composable
fun DialogActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (secondaryLabel != null && onSecondary != null) {
            TextActionButton(
                label = secondaryLabel,
                onClick = onSecondary,
                modifier = Modifier.weight(1f),
            )
        }
        PrimaryActionButton(
            label = primaryLabel,
            onClick = onPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrimaryActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(15.dp)
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(48.dp)
            .pressScale(interaction)
            .clip(shape)
            .background(brandGradient())
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                onClick = {
                    haptics.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress,
                    )
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun TextActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .pressScale(interaction)
            .clip(shape)
            .background(colors.surfaceMuted)
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
        )
    }
}

/** A soft circular badge that carries a status icon inside a dialog. */
@Composable
fun StatusBadge(
    iconRes: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(tint.copy(alpha = 0.20f), tint.copy(alpha = 0.06f)),
                ),
            )
            .border(1.dp, tint.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}

/** A leading tinted icon tile, used by the source options in the sheet. */
@Composable
fun IconTile(
    iconRes: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    corner: Dp = 15.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = 0.16f), tint.copy(alpha = 0.07f)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}
