package top.xiaodl.myprinter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.xiaodl.myprinter.R
import top.xiaodl.myprinter.ui.brandGradient
import top.xiaodl.myprinter.ui.myPrinterColors

/**
 * 应用标识：品牌渐变的圆角方块 + 白色打印机字形。
 *
 * <p>首页标题栏与「关于」卡片共用同一个标识，保证应用内外视觉一致。
 *
 * @param size 边长
 */
@Composable
fun LogoMark(size: Dp = 42.dp) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(size / 3.1f)
    Box(
        modifier = Modifier
            .size(size)
            // 深色模式下彩色投影不可见，直接省略，靠渐变本身区分层次。
            .shadow(
                elevation = if (colors.isDark) 0.dp else 12.dp,
                shape = shape,
                ambientColor = colors.brandStart.copy(alpha = 0.40f),
                spotColor = colors.brandEnd.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(brandGradient()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_print),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.50f),
        )
    }
}
