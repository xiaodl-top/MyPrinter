package top.xiaodl.myprinter.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * 全应用的设计令牌：深靛蓝 → 紫罗兰的品牌渐变、青绿点缀、柔和的层叠表面，以及克制的高对比字号。
 *
 * <p>界面只读取这里的语义色（不直接写十六进制），浅色与深色两套值因此能始终保持同步。
 */
data class MyPrinterColors(
    /** 是否深色模式。 */
    val isDark: Boolean,
    // —— 品牌渐变的三个色标 ——
    val brandStart: Color,
    val brandMid: Color,
    val brandEnd: Color,
    // —— 次要高亮用的强调色 ——
    val accent: Color,
    // —— 页面与卡片表面 ——
    val pageBackground: Color,
    val pageBackgroundAlt: Color,
    val surface: Color,
    val surfaceMuted: Color,
    // —— 分割线与描边 ——
    val border: Color,
    val borderStrong: Color,
    val divider: Color,
    // —— 文字 ——
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val hint: Color,
    // —— 控件填充色 ——
    val fieldFill: Color,
    val previewBackground: Color,
    // —— 语义色（成功 / 失败 / 警告）——
    val warningBackground: Color,
    val warningText: Color,
    val warningBorder: Color,
    val success: Color,
    val danger: Color,
    /** 品牌色块上的前景色（通常是白色）。 */
    val onBrand: Color,
    /** 弹层遮罩色。 */
    val scrim: Color,
    // —— 环境光晕颜色 ——
    val glowA: Color,
    val glowB: Color,
    val glowC: Color,
    /** 投影着色，浅色模式下用于彩色阴影。 */
    val shadowTint: Color,
)

private val LightColors = MyPrinterColors(
    isDark = false,
    brandStart = Color(0xFF2F6BFF),
    brandMid = Color(0xFF5B4BFF),
    brandEnd = Color(0xFF9A5CFF),
    accent = Color(0xFF00BFA6),
    pageBackground = Color(0xFFF5F6FB),
    pageBackgroundAlt = Color(0xFFF1F4FB),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF7F8FD),
    border = Color(0xFFE8EBF5),
    borderStrong = Color(0xFFD6DDEE),
    divider = Color(0xFFEFF2F9),
    textPrimary = Color(0xFF0B1220),
    textSecondary = Color(0xFF5A6577),
    textTertiary = Color(0xFF98A1B4),
    hint = Color(0xFF9BA4B7),
    fieldFill = Color(0xFFF7F8FD),
    previewBackground = Color(0xFFF1F4FB),
    warningBackground = Color(0xFFFFF3E2),
    warningText = Color(0xFFA15C00),
    warningBorder = Color(0xFFFFD9A3),
    success = Color(0xFF12B76A),
    danger = Color(0xFFF04438),
    onBrand = Color.White,
    scrim = Color(0x7F0B1020),
    glowA = Color(0xFF7C93FF),
    glowB = Color(0xFF59E3D0),
    glowC = Color(0xFFC38BFF),
    shadowTint = Color(0xFF2F6BFF),
)

private val DarkColors = MyPrinterColors(
    isDark = true,
    brandStart = Color(0xFF3D5BFF),
    brandMid = Color(0xFF6A45F5),
    brandEnd = Color(0xFF9A45F7),
    accent = Color(0xFF35E0C6),
    pageBackground = Color(0xFF070B14),
    pageBackgroundAlt = Color(0xFF0B1220),
    surface = Color(0xFF101728),
    surfaceMuted = Color(0xFF0D1526),
    border = Color(0xFF1E2A44),
    borderStrong = Color(0xFF2B3A5C),
    divider = Color(0xFF1B2439),
    textPrimary = Color(0xFFEEF2FB),
    textSecondary = Color(0xFF9AA7C0),
    textTertiary = Color(0xFF6C7894),
    hint = Color(0xFF6C7894),
    fieldFill = Color(0xFF0D1526),
    previewBackground = Color(0xFF0B1222),
    warningBackground = Color(0xFF33260F),
    warningText = Color(0xFFF5C173),
    warningBorder = Color(0xFF5A431C),
    success = Color(0xFF32D583),
    danger = Color(0xFFFF7B72),
    onBrand = Color.White,
    scrim = Color(0xB302050C),
    glowA = Color(0xFF3355FF),
    glowB = Color(0xFF00B5A5),
    glowC = Color(0xFF8B3BFF),
    shadowTint = Color(0xFF000000),
)

private val LocalMyPrinterColors = staticCompositionLocalOf { LightColors }

private val LightScheme = lightColorScheme(
    primary = LightColors.brandStart,
    onPrimary = LightColors.onBrand,
    secondary = LightColors.accent,
    onSecondary = Color(0xFF00251F),
    background = LightColors.pageBackground,
    onBackground = LightColors.textPrimary,
    surface = LightColors.surface,
    onSurface = LightColors.textPrimary,
    surfaceVariant = LightColors.surfaceMuted,
    onSurfaceVariant = LightColors.textSecondary,
    outline = LightColors.borderStrong,
    outlineVariant = LightColors.border,
    error = LightColors.danger,
)

private val DarkScheme = darkColorScheme(
    primary = DarkColors.brandStart,
    onPrimary = DarkColors.onBrand,
    secondary = DarkColors.accent,
    onSecondary = Color(0xFF00251F),
    background = DarkColors.pageBackground,
    onBackground = DarkColors.textPrimary,
    surface = DarkColors.surface,
    onSurface = DarkColors.textPrimary,
    surfaceVariant = DarkColors.surfaceMuted,
    onSurfaceVariant = DarkColors.textSecondary,
    outline = DarkColors.borderStrong,
    outlineVariant = DarkColors.border,
    error = DarkColors.danger,
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.5.sp,
        lineHeight = 21.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

/** 当前调色板；在 Composable 中通过下面的简写访问器读取。 */
val myPrinterColors: MyPrinterColors
    @Composable get() = LocalMyPrinterColors.current

// 常用颜色的简写访问器：只能在 @Composable 中使用，会自动随主题切换。
val Primary: Color @Composable get() = myPrinterColors.brandStart
val Accent: Color @Composable get() = myPrinterColors.accent
val PageBackground: Color @Composable get() = myPrinterColors.pageBackground
val SurfaceColor: Color @Composable get() = myPrinterColors.surface
val TextPrimary: Color @Composable get() = myPrinterColors.textPrimary
val TextSecondary: Color @Composable get() = myPrinterColors.textSecondary
val TextTertiary: Color @Composable get() = myPrinterColors.textTertiary
val DividerColor: Color @Composable get() = myPrinterColors.divider
val BorderColor: Color @Composable get() = myPrinterColors.border
val FieldStroke: Color @Composable get() = myPrinterColors.borderStrong
val HintColor: Color @Composable get() = myPrinterColors.hint
val PreviewBackground: Color @Composable get() = myPrinterColors.previewBackground
val WarningBackground: Color @Composable get() = myPrinterColors.warningBackground
val WarningText: Color @Composable get() = myPrinterColors.warningText
val SecondaryAction: Color @Composable get() = myPrinterColors.accent
val OnAccent: Color @Composable get() = myPrinterColors.onBrand
val SuccessColor: Color @Composable get() = myPrinterColors.success
val DangerColor: Color @Composable get() = myPrinterColors.danger
val ScrimColor: Color @Composable get() = myPrinterColors.scrim
val ShadowTint: Color @Composable get() = myPrinterColors.shadowTint

/** 品牌标志性渐变：天蓝 → 靛蓝 → 紫罗兰。 */
@Composable
fun brandGradient(): Brush = Brush.linearGradient(
    colors = listOf(
        myPrinterColors.brandStart,
        myPrinterColors.brandMid,
        myPrinterColors.brandEnd,
    ),
)

/** 水平方向的同色渐变，用于标题条与进度等横向元素。 */
@Composable
fun brandGradientHorizontal(): Brush = Brush.horizontalGradient(
    colors = listOf(
        myPrinterColors.brandStart,
        myPrinterColors.brandMid,
        myPrinterColors.brandEnd,
    ),
)

/**
 * 按当前 [ThemeMode] 安装调色板。
 *
 * <p>模式从 [AppTheme] 读取，因此在设置页切换后整个应用会立即重绘；
 * 系统栏图标颜色通过 [SideEffect] 与当前深浅色保持同步。
 *
 * @param mode    配色模式，默认跟随全局状态
 * @param content 页面内容
 */
@Composable
fun MyPrinterTheme(
    mode: ThemeMode = AppTheme.mode.value,
    content: @Composable () -> Unit,
) {
    val isDark = mode.isDark(isSystemInDarkTheme())
    val colors = if (isDark) DarkColors else LightColors
    val view = LocalView.current
    val activity = remember(view) { view.context.findActivity() }
    SideEffect {
        if (activity != null) {
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }
    CompositionLocalProvider(LocalMyPrinterColors provides colors) {
        MaterialTheme(
            colorScheme = if (isDark) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** 从任意 Context 向外找到宿主 Activity（用于设置系统栏外观）。 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
