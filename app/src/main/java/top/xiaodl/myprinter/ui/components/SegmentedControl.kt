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
 * A segmented control with a sliding selection pill. The pill moves with a light spring, so
 * switching between two neighbouring options reads as one continuous gesture instead of a
 * colour flip.
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
        // Read in the layout phase: the pill slides without recomposing the whole control.
        val offset by animateDpAsState(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
            label = "segmentOffset",
        )

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
