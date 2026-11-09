package top.xiaodl.myprinter.ui.components

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
 * 全局动效参数集中在这里，整个界面的运动都读同一套曲线，观感统一：
 *
 * <ul>
 *   <li>转场使用先快后慢的减速曲线（离开手指快、落位慢）；</li>
 *   <li>按压用轻微欠阻尼的弹簧收尾；</li>
 *   <li>入场按 [EntranceStagger] 错峰，让页面「展开」而不是突然出现。</li>
 * </ul>
 */
object Motion {

    /** 快速反馈时长（毫秒）。 */
    const val Fast = 170

    /** 常规转场时长（毫秒）。 */
    const val Normal = 280

    /** 较慢的转场时长（毫秒）。 */
    const val Slow = 460

    /** 单次入场动画时长（毫秒）。 */
    const val Entrance = 620

    /** 相邻内容块入场的时间差（毫秒）。 */
    const val EntranceStagger = 70L

    /** 减速曲线：起步快、落位柔和。 */
    val EaseOut: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    /** 标准缓入缓出曲线（弹层、内容切换）。 */
    val EaseInOut: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** 按压回弹：阻尼略低，松手时有一点点「弹」的感觉。 */
    fun <T> pressSpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = 0.45f,
            stiffness = 780f,
            visibilityThreshold = null,
        )

    /** 常规收束：干净利落，不过冲。 */
    fun <T> settleSpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = 0.82f,
            stiffness = 380f,
        )

    /** 带轻微过冲的弹簧（开关滑块等）。 */
    fun <T> bouncySpring(): androidx.compose.animation.core.SpringSpec<T> =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 520f,
        )
}

/**
 * 按压时缩小元素。请放在 [Modifier.clip] 之前，并把同一个 [interactionSource] 传给 clickable，
 * 这样按压缩放与水波纹能同步。
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
 * 入场动画：内容淡入的同时向上归位。
 *
 * @param index    序号，用于错峰（每个序号延迟 [Motion.EntranceStagger] 毫秒）
 * @param modifier 修饰符
 * @param offsetY  起始的向下位移
 * @param content  内容
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
            // 位移在绘制阶段计算，不触发重组。
            alpha = progress
            translationY = (1f - progress) * offsetY.toPx()
        },
    ) {
        content()
    }
}
