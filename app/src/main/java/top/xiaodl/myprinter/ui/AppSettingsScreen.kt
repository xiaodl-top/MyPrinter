package top.xiaodl.myprinter.ui

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.BuildConfig
import top.xiaodl.myprinter.R
import top.xiaodl.myprinter.ui.components.AuroraBackdrop
import top.xiaodl.myprinter.ui.components.GlassCard
import top.xiaodl.myprinter.ui.components.Hairline
import top.xiaodl.myprinter.ui.components.LogoMark
import top.xiaodl.myprinter.ui.components.PageHeader
import top.xiaodl.myprinter.ui.components.Reveal
import top.xiaodl.myprinter.ui.components.SectionLabel
import top.xiaodl.myprinter.ui.components.SegmentedControl
import top.xiaodl.myprinter.ui.components.SoftIconButton

/**
 * App-wide preferences: appearance and the about block. Print configuration has its own screen,
 * reachable from the printer card on the home screen.
 */
@Composable
fun AppSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // Read as state: selecting another mode recomposes this screen (and the whole app) at once.
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
//                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = "OnePrinter 云打印客户端",
                                        fontSize = 11.sp,
                                        color = TextTertiary,
                                    )
                                }
                            }
                            /*
                            Spacer(Modifier.height(16.dp))
                            Hairline()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "打印配置（服务地址、口令、打印机等）请在主页点击打印机卡片右上角的「修改」。",
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                color = TextTertiary,
                            )
                            */
                        }
                    }
                }
                /*
                Spacer(Modifier.height(22.dp))

                Reveal(index = 2) {
                    Text(
                        text = "设置只保存在这台设备上，不会上传到任何服务器。",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                */
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
