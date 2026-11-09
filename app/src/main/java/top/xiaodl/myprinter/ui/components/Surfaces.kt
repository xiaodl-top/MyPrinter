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
 * 带细描边与柔和品牌投影的抬升表面。
 *
 * <p>深色模式下投影不可见，改用更亮的描边来表现层级——这正是「高度」在深色画布上的读法。
 *
 * @param modifier     修饰符
 * @param shape        形状
 * @param color        背景色
 * @param borderColor  描边色
 * @param elevation    投影高度（仅浅色生效）
 * @param contentPadding 内边距
 * @param content      内容
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
 * 主操作按钮：品牌渐变的胶囊，按下时弹簧缩放并带一次轻触感反馈，
 * 可用状态下还有一道缓慢扫过的高光。
 *
 * @param label        文案
 * @param onClick      点击回调
 * @param modifier     修饰符
 * @param enabled      是否可用
 * @param loading      是否处于加载中（显示进度圈与 [loadingLabel]）
 * @param loadingLabel 加载中的文案
 * @param iconRes      左侧图标（可选）
 * @param shape        形状
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

/**
 * 缓慢扫过高光带，让表面有一点「活着」的质感。
 *
 * @param durationMillis 一次扫过的时长
 * @param delayMillis    两次之间的间隔
 * @param highlight      高光颜色
 */
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

/**
 * 标题栏里使用的柔和图标按钮（设置、返回）。
 *
 * @param iconRes            图标资源
 * @param contentDescription 无障碍描述
 * @param onClick            点击回调
 * @param modifier           修饰符
 * @param tint               图标颜色
 * @param size               按钮边长
 */
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

/**
 * 一组控件上方的小标题，前面带一条渐变色短竖条。
 *
 * @param text     标题文字
 * @param modifier 修饰符
 * @param trailing 右侧附加内容（可选）
 */
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

/**
 * 胶囊形状的元信息标签。
 *
 * @param text        文字
 * @param modifier    修饰符
 * @param iconRes     左侧小图标（可选）
 * @param tint        文字与图标颜色
 * @param background  背景色
 * @param borderColor 描边色
 */
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

/** 1dp 极细分割线，不喧宾夺主。 */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = DividerColor) {
    Box(
        modifier
            .height(1.dp)
            .background(color),
    )
}

/**
 * 选择文件之前的空状态：干净的细描边卡片，图标背后一层柔和的品牌光晕。
 *
 * <p>刻意不画虚线框——它更像一个安静的占位，而不是「拖放目标」。
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
        // 图标背后的柔光：在到达卡片边缘之前就完全淡出。
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
