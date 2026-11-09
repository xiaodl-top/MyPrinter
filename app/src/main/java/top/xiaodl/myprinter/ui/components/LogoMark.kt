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

/** The app mark: the brand gradient tile with the white printer glyph. */
@Composable
fun LogoMark(size: Dp = 42.dp) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(size / 3.1f)
    Box(
        modifier = Modifier
            .size(size)
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
