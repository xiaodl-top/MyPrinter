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
import top.xiaodl.myprinter.ui.ScrimColor
import top.xiaodl.myprinter.ui.SurfaceColor
import top.xiaodl.myprinter.ui.TextPrimary
import top.xiaodl.myprinter.ui.TextSecondary
import top.xiaodl.myprinter.ui.brandGradient
import top.xiaodl.myprinter.ui.myPrinterColors
import kotlin.math.roundToInt

/** 全屏遮罩层；点击遮罩关闭弹层。 */
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
 * 从底部滑入的弹层。
 *
 * <p>三种关闭方式：点击遮罩、向下拖动顶部把手、调用方把 [visible] 置为 false；
 * 退出动画一定会播放，因为 [AnimatedVisibility] 会在动画结束前保持内容处于组合中。
 *
 * @param visible   是否显示
 * @param onDismiss 请求关闭的回调
 * @param content   弹层内容
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
            // 拖动把手：只有这里接受关闭手势，保证下面的选项行仍可正常点击。
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

/**
 * 居中显示的对话框卡片，带轻微弹簧的缩放进场。
 *
 * @param visible   是否显示
 * @param onDismiss 请求关闭的回调
 * @param content   卡片内容
 */
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

/** 对话框的视觉外壳：圆角表面、柔和投影，内部由调用方填充内容。 */
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

/** 对话框的标题 + 正文，结果弹窗与配置提示共用。 */
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

/** 一行两个操作按钮，主操作在右，与系统对话框的按钮顺序一致。 */
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

/** 对话框主操作按钮：品牌渐变 + 白色文字 + 触感反馈。 */
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

/** 对话框次要操作按钮：浅色填充 + 细描边。 */
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

/** 对话框里的圆形状态徽章（成功 / 失败 / 警告）。 */
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

/** 带品牌色调的方形图标底座，用于弹层里的来源选项。 */
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
