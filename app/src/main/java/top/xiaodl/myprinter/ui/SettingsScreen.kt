package top.xiaodl.myprinter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ConfigStore
import top.xiaodl.myprinter.R
import top.xiaodl.myprinter.ui.components.AppTextField
import top.xiaodl.myprinter.ui.components.AuroraBackdrop
import top.xiaodl.myprinter.ui.components.FieldLabel
import top.xiaodl.myprinter.ui.components.GlassCard
import top.xiaodl.myprinter.ui.components.GradientButton
import top.xiaodl.myprinter.ui.components.Hairline
import top.xiaodl.myprinter.ui.components.PageHeader
import top.xiaodl.myprinter.ui.components.Reveal
import top.xiaodl.myprinter.ui.components.SectionLabel
import top.xiaodl.myprinter.ui.components.SoftIconButton
import top.xiaodl.myprinter.ui.components.ToggleRow

/**
 * 打印设置页：这是修改打印机连接参数的唯一入口。
 *
 * <p>页面只负责编辑与校验输入，保存动作交给宿主 Activity 写入本机配置。
 *
 * @param onBack 返回回调
 * @param onSave 保存回调（服务地址、打印机名称、纸张、口令、是否彩色）
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSave: (
        apiUrl: String,
        printer: String,
        paperSize: String,
        code: String,
        color: Boolean,
    ) -> Unit,
) {
    val context = LocalContext.current
    val stored = remember { ConfigStore.load(context) }
    var apiUrl by remember { mutableStateOf(stored.apiUrl) }
    var code by remember { mutableStateOf(stored.code) }
    var printer by remember { mutableStateOf(stored.printer) }
    var paperSize by remember { mutableStateOf(stored.paperSize) }
    var color by remember { mutableStateOf(stored.color) }
    var codeFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
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
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .imePadding()
                // 点击输入框以外的区域时收起软键盘。
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = topInset + 84.dp, bottom = bottomInset + 28.dp),
            ) {
                Reveal(index = 0) { WarningBanner() }

                Spacer(Modifier.height(24.dp))

                Reveal(index = 1) {
                    Column {
                        SectionLabel("连接配置")
                        Spacer(Modifier.height(10.dp))
                        GlassCard(contentPadding = 18.dp) {
                            FieldLabel("打印服务 API")
                            Spacer(Modifier.height(7.dp))
                            AppTextField(
                                value = apiUrl,
                                onValueChange = { apiUrl = it },
                                placeholder = "例如：http://192.168.1.10:8080/upload/single",
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Next,
                            )
                            Spacer(Modifier.height(16.dp))
                            FieldLabel("验证口令")
                            Spacer(Modifier.height(7.dp))
                            // 默认以圆点掩码显示，仅在获得焦点时显示明文。
                            AppTextField(
                                value = code,
                                onValueChange = { code = it },
                                placeholder = "请输入打印服务的验证口令",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                visualTransformation = if (codeFocused) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                onFocusChange = { codeFocused = it },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Reveal(index = 2) {
                    Column {
                        SectionLabel("打印默认值")
                        Spacer(Modifier.height(10.dp))
                        GlassCard(contentPadding = 18.dp) {
                            FieldLabel("打印机名称")
                            Spacer(Modifier.height(7.dp))
                            AppTextField(
                                value = printer,
                                onValueChange = { printer = it },
                                placeholder = "输入打印机名称",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                            )
                            Spacer(Modifier.height(16.dp))
                            FieldLabel("纸张大小")
                            Spacer(Modifier.height(7.dp))
                            AppTextField(
                                value = paperSize,
                                onValueChange = { paperSize = it },
                                placeholder = "例如：A4",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                            )
                            Spacer(Modifier.height(14.dp))
                            Hairline()
                            Spacer(Modifier.height(8.dp))
                            ToggleRow(
                                title = "彩色打印",
                                subtitle = "关闭后按黑白发送打印任务",
                                checked = color,
                                onCheckedChange = { color = it },
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }

                Spacer(Modifier.height(26.dp))

                Reveal(index = 3) {
                    GradientButton(
                        label = "保存配置",
                        iconRes = R.drawable.ic_check_circle,
                        onClick = {
                            focusManager.clearFocus()
                            onSave(apiUrl, printer, paperSize, code, color)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Reveal(index = 4) {
                    Text(
                        text = "配置只保存在这台设备上，不会上传到任何服务器。",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        PageHeader(
            title = "打印设置",
            subtitle = "连接参数与打印默认值",
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

/** 必须保留的警告：这里的每个值都会直接影响打印请求。 */
@Composable
private fun WarningBanner() {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(WarningBackground)
            .border(1.dp, WarningText.copy(alpha = 0.18f), shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = null,
            tint = WarningText,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "请勿随意修改该页面配置，否则打印可能失败！",
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = WarningText,
        )
    }
}

