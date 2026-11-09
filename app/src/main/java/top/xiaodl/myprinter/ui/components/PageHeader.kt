package top.xiaodl.myprinter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ui.TextPrimary
import top.xiaodl.myprinter.ui.TextTertiary
import top.xiaodl.myprinter.ui.myPrinterColors

/**
 * 两个页面共用的吸顶标题栏。
 *
 * <p>页面未滚动时背景透明，内容可以从状态栏下方穿过；滚动后底色逐渐变实，标题始终清晰。
 * [progress] 用 lambda 传入，滚动位置只在绘制阶段读取，永远不会因为滚动而重组标题栏。
 *
 * @param title    主标题
 * @param subtitle 副标题（小号、大写字距）
 * @param progress 0..1 的滚动进度，驱动底色与分隔线的不透明度
 * @param leading  左侧内容（Logo 或返回按钮）
 * @param modifier 修饰符
 * @param trailing 右侧内容（可选）
 */
@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    progress: () -> Float,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = myPrinterColors
    Box(modifier = modifier.fillMaxWidth()) {
        // 铺满整个头部（含状态栏区域）的底色，随滚动进度淡入。
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = progress() }
                .background(colors.pageBackground),
        )
        // 滚动后出现的 1dp 分隔线。
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer { alpha = progress() }
                .background(colors.border),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(72.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (trailing != null) {
                Spacer(Modifier.size(10.dp))
                trailing()
            }
        }
    }
}
