package top.xiaodl.myprinter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ui.myPrinterColors

/**
 * 分段选择控件，选中项是一个会滑动的胶囊。
 *
 * <p>胶囊用弹簧移动，因此在相邻选项之间切换时是一段连续运动，而不是颜色突变。
 *
 * @param options       选项列表
 * @param selectedIndex 当前选中项下标
 * @param label         选项文案
 * @param onSelect      选中回调（参数为下标）
 * @param modifier      修饰符
 * @param enabled       是否可交互
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selectedIndex: Int,
    label: (T) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (options.isEmpty()) return
    val colors = myPrinterColors
    val dark = colors.isDark
    val trackShape = RoundedCornerShape(16.dp)
    val pillShape = RoundedCornerShape(12.dp)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(trackShape)
            .background(colors.surfaceMuted)
            .border(1.dp, colors.border, trackShape)
            .padding(4.dp),
    ) {
        val itemWidth = maxWidth / options.size.toFloat()
        val targetOffset = itemWidth * selectedIndex
        // 在布局阶段读取偏移，胶囊滑动时不会重组整个控件。
        val offset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
            label = "segmentOffset",
        )

        // 选中胶囊：浅色模式是带投影的白块，深色模式用更亮的描边色提亮。
        Box(
            Modifier
                .offset { IntOffset(offset.roundToPx(), 0) }
                .width(itemWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = if (dark) 0.dp else 6.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.10f),
                    spotColor = Color.Black.copy(alpha = 0.14f),
                )
                .clip(pillShape)
                .background(
                    if (dark) colors.borderStrong.copy(alpha = 0.45f) else colors.surface,
                ),
        )

        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                val active = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (active) colors.textPrimary else colors.textTertiary,
                    animationSpec = tween(Motion.Normal),
                    label = "segmentText",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(pillShape)
                        // 用 selectable 而不是 clickable：读屏会朗读成「单选按钮」。
                        .selectable(
                            selected = active,
                            enabled = enabled,
                            role = Role.RadioButton,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(option),
                        fontSize = 13.5.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
