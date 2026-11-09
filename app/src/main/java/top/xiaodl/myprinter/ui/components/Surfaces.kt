package top.xiaodl.myprinter.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ui.BorderColor
import top.xiaodl.myprinter.ui.DividerColor
import top.xiaodl.myprinter.ui.ShadowTint
import top.xiaodl.myprinter.ui.SurfaceColor
import top.xiaodl.myprinter.ui.TextPrimary
import top.xiaodl.myprinter.ui.TextTertiary
import top.xiaodl.myprinter.ui.brandGradient
import top.xiaodl.myprinter.ui.myPrinterColors

/**
 * A raised surface with a hairline border and a soft, brand-tinted shadow. In dark mode the shadow
 * is replaced by a brighter border, which is what "elevation" reads as on a dark canvas.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    color: Color = SurfaceColor,
    borderColor: Color = BorderColor,
    elevation: Dp = 18.dp,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = myPrinterColors.isDark
    Column(
        modifier = modifier
            .shadow(
                elevation = if (dark) 0.dp else elevation,
                shape = shape,
                clip = false,
                ambientColor = ShadowTint.copy(alpha = 0.16f),
                spotColor = ShadowTint.copy(alpha = 0.20f),
            )
            .clip(shape)
            .background(color)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * The primary call to action: a brand-gradient pill that answers every touch with a spring scale,
 * a light haptic tick and a slow highlight that crosses the surface while it is enabled.
 */
@Composable
fun GradientButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingLabel: String = label,
    iconRes: Int? = null,
    shape: Shape = RoundedCornerShape(20.dp),
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val active = enabled && !loading

    Box(
        modifier = modifier
            .heightIn(min = 58.dp)
            .pressScale(interaction, pressedScale = 0.975f)
            .shadow(
                elevation = if (active && !colors.isDark) 22.dp else 0.dp,
                shape = shape,
                ambientColor = colors.brandStart.copy(alpha = 0.45f),
                spotColor = colors.brandEnd.copy(alpha = 0.40f),
            )
            .clip(shape)
            .background(
                brush = if (active) {
                    brandGradient()
                } else {
                    Brush.linearGradient(listOf(colors.surface, colors.surface))
                },
            )
            .border(
                width = 1.dp,
                color = if (active) Color.Transparent else colors.borderStrong,
                shape = shape,
            )
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                enabled = active,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(10.dp))
            } else if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (active) Color.White else colors.textSecondary,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(9.dp))
            }
            AnimatedContent(
                targetState = if (loading) loadingLabel else label,
                transitionSpec = {
                    (
                        fadeIn(tween(Motion.Fast)) + slideInVertically(
                            animationSpec = tween(Motion.Fast, easing = Motion.EaseOut),
                            initialOffsetY = { it / 2 },
                        )
                        ).togetherWith(
                        fadeOut(tween(Motion.Fast / 2)) + slideOutVertically(
                            animationSpec = tween(Motion.Fast / 2),
                            targetOffsetY = { -it / 2 },
                        ),
                    )
                },
                label = "buttonLabel",
            ) { text ->
                Text(
                    text = text,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    color = if (active) Color.White else colors.textSecondary,
                )
            }
        }
        if (active) {
            ShineOverlay()
        }
    }
}

/** A highlight band that slowly crosses a surface, giving it a subtle "live" quality. */
@Composable
fun BoxScope.ShineOverlay(
    durationMillis: Int = 3800,
    delayMillis: Int = 900,
    highlight: Color = Color.White.copy(alpha = 0.16f),
) {
    val transition = rememberInfiniteTransition(label = "shine")
    val position by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shinePosition",
    )
    Canvas(Modifier.matchParentSize()) {
        if (size.width <= 0f) return@Canvas
        val centerX = size.width * position
        val halfBand = size.width * 0.20f
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    highlight,
                    Color.Transparent,
                ),
                start = Offset(centerX - halfBand, 0f),
                end = Offset(centerX + halfBand, size.height),
            ),
        )
    }
}

/** A soft icon button used for the header actions (settings, back). */
@Composable
fun SoftIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    size: Dp = 44.dp,
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(size / 2.6f)
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interaction)
            .clip(shape)
            .background(colors.surface.copy(alpha = if (colors.isDark) 0.92f else 1f))
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = false, radius = size),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

/** Small caption above a group of controls, with a leading gradient dash. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 13.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(brandGradient()),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp,
            color = TextPrimary,
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            trailing()
        }
    }
}

/** A pill-shaped metadata chip. */
@Composable
fun MetaPill(
    text: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    tint: Color = myPrinterColors.textSecondary,
    background: Color = myPrinterColors.surfaceMuted,
    borderColor: Color = Color.Transparent,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 11.5.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One-pixel separator that never looks heavy. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = DividerColor) {
    Box(
        modifier
            .height(1.dp)
            .background(color),
    )
}

/**
 * The empty state before a file is chosen. A clean hairline card with a soft brand glow behind
 * the gently breathing icon 鈥?no dashed outline, so it reads as a calm placeholder rather than a
 * drop target.
 */
@Composable
fun EmptyStatePanel(
    title: String,
    subtitle: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(26.dp)
    val transition = rememberInfiniteTransition(label = "empty")
    val breathe by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = Motion.EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emptyBreathe",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surface.copy(alpha = if (colors.isDark) 0.55f else 0.72f))
            .border(1.dp, colors.border, shape),
        contentAlignment = Alignment.Center,
    ) {
        // A soft light behind the icon; it fades out well before the card edges.
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height * 0.36f)
            val radius = size.width * 0.36f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.brandMid.copy(alpha = if (colors.isDark) 0.16f else 0.10f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp),
        ) {
            Box(
                Modifier
                    .size(54.dp)
                    .graphicsLayer {
                        scaleX = breathe
                        scaleY = breathe
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.brandStart.copy(alpha = 0.14f),
                                colors.brandEnd.copy(alpha = 0.14f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = colors.brandMid,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
