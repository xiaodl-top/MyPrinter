package top.xiaodl.myprinter.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.BuildConfig
import top.xiaodl.myprinter.R
import top.xiaodl.myprinter.ui.components.AuroraBackdrop
import top.xiaodl.myprinter.ui.components.GlassCard
import top.xiaodl.myprinter.ui.components.Hairline
import top.xiaodl.myprinter.ui.components.IconTile
import top.xiaodl.myprinter.ui.components.LogoMark
import top.xiaodl.myprinter.ui.components.PageHeader
import top.xiaodl.myprinter.ui.components.Reveal
import top.xiaodl.myprinter.ui.components.SectionLabel
import top.xiaodl.myprinter.ui.components.SegmentedControl
import top.xiaodl.myprinter.ui.components.SoftIconButton
import top.xiaodl.myprinter.ui.components.pressScale

/** 项目源码仓库地址。 */
private const val PROJECT_URL = "https://github.com/xiaodl-top/MyPrinter"

/**
 * 应用设置页：外观（浅色 / 深色 / 跟随系统）与「关于」。
 *
 * <p>打印相关配置不在此页，而是在主页打印机卡片的「修改」中进入的打印设置页。
 *
 * @param onBack 返回上一页的回调
 */
@Composable
fun AppSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    // 读取为状态：切换模式后本页（以及整个应用）会立即重组。
    val themeMode = AppTheme.mode.value
    val scrollState = rememberScrollState()
    val headerProgress = remember {
        derivedStateOf { (scrollState.value / 160f).coerceIn(0f, 1f) }
    }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        AuroraBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = topInset + 84.dp, bottom = bottomInset + 28.dp),
            ) {
                Reveal(index = 0) {
                    Column {
                        SectionLabel("外观")
                        Spacer(Modifier.height(10.dp))
                        GlassCard(contentPadding = 18.dp) {
                            Text(
                                text = "主题模式",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "跟随系统时会随系统夜间模式自动切换。",
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                color = TextTertiary,
                            )
                            Spacer(Modifier.height(13.dp))
                            SegmentedControl(
                                options = ThemeMode.entries,
                                selectedIndex = themeMode.ordinal,
                                label = { mode ->
                                    when (mode) {
                                        ThemeMode.SYSTEM -> "跟随系统"
                                        ThemeMode.LIGHT -> "浅色"
                                        ThemeMode.DARK -> "深色"
                                    }
                                },
                                onSelect = { index ->
                                    AppTheme.select(context, ThemeMode.entries[index])
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Reveal(index = 1) {
                    Column {
                        SectionLabel("关于")
                        Spacer(Modifier.height(10.dp))
                        GlassCard(contentPadding = 18.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LogoMark(size = 54.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp,
                                        color = TextPrimary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.3.sp,
                                        color = TextSecondary,
                                    )
                                    Text(
                                        text = "OnePrinter 云打印客户端",
                                        fontSize = 11.sp,
                                        color = TextTertiary,
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Hairline()
                            Spacer(Modifier.height(6.dp))
                            // 源码仓库入口：整行可点，用浏览器打开。
                            ProjectRepositoryRow(
                                onClick = {
                                    runCatching { uriHandler.openUri(PROJECT_URL) }
                                        .onFailure {
                                            Toast.makeText(
                                                context,
                                                "无法打开浏览器",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                },
                            )
                        }
                    }
                }
            }
        }

        PageHeader(
            title = "设置",
            subtitle = "外观与关于",
            progress = { headerProgress.value },
            leading = {
                SoftIconButton(
                    iconRes = R.drawable.ic_arrow_back,
                    contentDescription = "返回",
                    onClick = onBack,
                    tint = Primary,
                )
            },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

/**
 * 「关于」卡片里的源码仓库入口。
 *
 * @param onClick 打开仓库地址的回调
 */
@Composable
private fun ProjectRepositoryRow(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.98f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClickLabel = "在浏览器中打开项目源码",
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(iconRes = R.drawable.ic_code, tint = Primary, size = 40.dp, corner = 13.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "项目源码",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = PROJECT_URL,
                fontSize = 11.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}
