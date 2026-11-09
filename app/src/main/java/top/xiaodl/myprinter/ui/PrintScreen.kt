package top.xiaodl.myprinter.ui

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.AppLog
import top.xiaodl.myprinter.CameraTarget
import top.xiaodl.myprinter.FileIntake
import top.xiaodl.myprinter.PrinterConfig
import top.xiaodl.myprinter.R
import top.xiaodl.myprinter.ui.components.AppTextField
import top.xiaodl.myprinter.ui.components.AuroraBackdrop
import top.xiaodl.myprinter.ui.components.BottomSheet
import top.xiaodl.myprinter.ui.components.CenterDialog
import top.xiaodl.myprinter.ui.components.DashedEmptyPanel
import top.xiaodl.myprinter.ui.components.DialogActions
import top.xiaodl.myprinter.ui.components.DialogMessage
import top.xiaodl.myprinter.ui.components.FieldLabel
import top.xiaodl.myprinter.ui.components.GlassCard
import top.xiaodl.myprinter.ui.components.GradientButton
import top.xiaodl.myprinter.ui.components.Hairline
import top.xiaodl.myprinter.ui.components.IconTile
import top.xiaodl.myprinter.ui.components.MetaPill
import top.xiaodl.myprinter.ui.components.Motion
import top.xiaodl.myprinter.ui.components.PageHeader
import top.xiaodl.myprinter.ui.components.QuantityStepper
import top.xiaodl.myprinter.ui.components.Reveal
import top.xiaodl.myprinter.ui.components.SectionLabel
import top.xiaodl.myprinter.ui.components.ShineOverlay
import top.xiaodl.myprinter.ui.components.SoftIconButton
import top.xiaodl.myprinter.ui.components.StatusBadge
import top.xiaodl.myprinter.ui.components.ToggleRow
import top.xiaodl.myprinter.ui.components.pressScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val DOCUMENT_MIME_TYPES = arrayOf(
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
)

private const val SUPPORTED_FORMATS =
    "PDF · JPG · PNG · GIF · WEBP · BMP · CAD · DWG · DOC · DOCX · PPT · PPTX · XLS · XLSX"

/** Main screen: pick a file (or receive one from another app) and send it to the printer. */
@Composable
fun PrintScreen(
    viewModel: PrintViewModel,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraTarget by remember { mutableStateOf<CameraTarget?>(null) }

    // The system photo picker (gallery) is used for images; it falls back to a document picker
    // only on devices that do not ship one.
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            FileIntake.persistReadPermission(context, null, uri)
            viewModel.selectFile(uri, FileIntake.displayName(context, uri), true)
        } else {
            AppLog.event("相册未返回图片，用户可能取消了选择")
        }
    }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            FileIntake.persistReadPermission(context, null, uri)
            viewModel.selectFile(uri, FileIntake.displayName(context, uri), false)
        } else {
            AppLog.event("文件选择器未返回文件，用户可能取消了选择")
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val target = cameraTarget
        if (saved && target != null && target.file.exists() && target.file.length() > 0) {
            AppLog.event("拍照完成", "文件=${target.file.name}, 大小=${target.file.length()} 字节")
            viewModel.selectFile(target.uri, target.file.name, true)
        } else {
            AppLog.warn("相机没有返回可用的照片", "saved=$saved")
            Toast.makeText(context, "未获取到照片，请重试。", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()
    val headerProgress = remember {
        derivedStateOf { (scrollState.value / 160f).coerceIn(0f, 1f) }
    }

    val selection = viewModel.selection.value
    val printing = viewModel.printing.value
    val dialog = viewModel.dialog.value

    // Keep the last dialog around while its exit animation plays, otherwise the text would vanish
    // before the card has finished scaling away.
    var lastDialog by remember { mutableStateOf(dialog) }
    LaunchedEffect(dialog) {
        if (dialog != null) lastDialog = dialog
    }

    BackHandler(enabled = showImageSourceDialog) { showImageSourceDialog = false }
    BackHandler(enabled = dialog != null) { viewModel.dismissDialog() }

    Box(Modifier.fillMaxSize()) {
        AuroraBackdrop()

        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .imePadding()
                // Tapping outside the inputs closes the soft keyboard.
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = topInset + 84.dp, bottom = bottomInset + 132.dp),
            ) {
                Reveal(index = 0) {
                    ConfigHero(config = viewModel.config.value, onOpenSettings = onOpenSettings)
                }

                Spacer(Modifier.height(26.dp))

                Reveal(index = 1) {
                    SectionLabel(
                        text = "选择文件",
                        trailing = {
                            Text(
                                text = "图片或文档",
                                fontSize = 11.sp,
                                color = TextTertiary,
                            )
                        },
                    )
                }

                Spacer(Modifier.height(12.dp))

                Reveal(index = 2) {
                    SourceTiles(
                        enabled = !printing,
                        onPickImage = {
                            AppLog.user("点击「选择图片」按钮")
                            showImageSourceDialog = true
                        },
                        onPickDocument = {
                            AppLog.user("点击「选择文档」按钮", "类型=PDF/Word/PPT")
                            documentPicker.launch(DOCUMENT_MIME_TYPES)
                        },
                    )
                }

                Spacer(Modifier.height(10.dp))

                Reveal(index = 3) {
                    Text(
                        text = "支持 " + SUPPORTED_FORMATS,
                        fontSize = 10.5.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.2.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    )
                }

                Spacer(Modifier.height(22.dp))

                Reveal(index = 4) {
                    AnimatedContent(
                        targetState = selection,
                        transitionSpec = {
                            (
                                fadeIn(tween(Motion.Normal, easing = Motion.EaseOut)) + slideInVertically(
                                    animationSpec = tween(Motion.Slow, easing = Motion.EaseOut),
                                    initialOffsetY = { it / 8 },
                                ) + scaleIn(initialScale = 0.985f)
                                ).togetherWith(
                                fadeOut(tween(Motion.Fast)) + slideOutVertically(
                                    animationSpec = tween(Motion.Normal),
                                    targetOffsetY = { -it / 12 },
                                ),
                            ).using(SizeTransform(clip = true))
                        },
                        label = "selection",
                    ) { current ->
                        if (current == null) {
                            DashedEmptyPanel(
                                title = "还没有待打印的文件",
                                subtitle = "从相册、相机或文件管理器里选一个文件，\n接着就能设置份数和版式。",
                                iconRes = R.drawable.ic_print,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp),
                            )
                        } else {
                            SelectionCard(
                                selection = current,
                                copiesText = viewModel.copiesText.value,
                                onCopiesChange = viewModel::updateCopies,
                                layout = viewModel.layout.value,
                                onLayoutChange = viewModel::updateLayout,
                                duplex = viewModel.duplex.value,
                                onDuplexChange = viewModel::updateDuplex,
                                pageRange = viewModel.pageRange.value,
                                onPageRangeChange = viewModel::updatePageRange,
                                openedFromAnotherApp = viewModel.openedFromAnotherApp.value,
                            )
                        }
                    }
                }
            }
        }

        HeaderBar(
            progress = { headerProgress.value },
            onOpenSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        PrintBar(
            printing = printing,
            enabled = selection != null && !printing,
            onPrint = viewModel::printNow,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        BottomSheet(
            visible = showImageSourceDialog,
            onDismiss = { showImageSourceDialog = false },
        ) {
            Text(
                text = "选择图片来源",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "拍一张新的照片，或从相册里挑一张已有图片。",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(18.dp))
            ImageSourceOption(
                iconRes = R.drawable.ic_select_image,
                title = "从相册选择",
                subtitle = "挑选相册中已有的图片",
                tint = Primary,
                onClick = {
                    showImageSourceDialog = false
                    AppLog.user("选择图片来源：系统相册")
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )
            Spacer(Modifier.height(10.dp))
            ImageSourceOption(
                iconRes = R.drawable.ic_camera,
                title = "打开相机拍摄",
                subtitle = "拍一张新的照片来打印",
                tint = Accent,
                onClick = {
                    showImageSourceDialog = false
                    if (hasCameraApp(context)) {
                        AppLog.user("选择图片来源：相机拍照")
                        val target = FileIntake.newCameraTarget(context)
                        cameraTarget = target
                        cameraLauncher.launch(target.uri)
                    } else {
                        AppLog.warn("设备上没有可用的相机应用")
                        Toast.makeText(context, "设备上没有可用的相机应用。", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "取消",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { showImageSourceDialog = false },
                    )
                    .padding(vertical = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        CenterDialog(
            visible = dialog != null,
            onDismiss = viewModel::dismissDialog,
        ) {
            val shown = lastDialog
            if (shown != null) {
                PrintDialogBody(
                    dialog = shown,
                    onDismiss = viewModel::dismissDialog,
                    onOpenSettings = {
                        AppLog.user("打印配置未完成，前往设置页")
                        viewModel.dismissDialog()
                        onOpenSettings()
                    },
                )
            }
        }
    }
}

/** The result / configuration dialog: badge, message and one or two actions. */
@Composable
private fun PrintDialogBody(
    dialog: PrintDialog,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val iconRes = when (dialog.kind) {
        PrintDialog.Kind.NEED_CONFIG -> R.drawable.ic_warning
        PrintDialog.Kind.SUCCESS -> R.drawable.ic_check_circle
        PrintDialog.Kind.ERROR -> R.drawable.ic_error
        else -> R.drawable.ic_print
    }
    val tint = when (dialog.kind) {
        PrintDialog.Kind.NEED_CONFIG -> WarningText
        PrintDialog.Kind.SUCCESS -> SuccessColor
        PrintDialog.Kind.ERROR -> DangerColor
        else -> Primary
    }
    StatusBadge(iconRes = iconRes, tint = tint)
    Spacer(Modifier.height(18.dp))
    DialogMessage(title = dialog.title, message = dialog.message)
    if (dialog.kind == PrintDialog.Kind.NEED_CONFIG) {
        DialogActions(
            primaryLabel = "去设置",
            onPrimary = onOpenSettings,
            secondaryLabel = "取消",
            onSecondary = {
                AppLog.user("取消前往设置")
                onDismiss()
            },
        )
    } else {
        DialogActions(
            primaryLabel = "好的",
            onPrimary = {
                AppLog.event("关闭提示弹窗", "标题=${dialog.title}")
                onDismiss()
            },
        )
    }
}

@Composable
private fun ImageSourceOption(
    iconRes: Int,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.98f)
            .clip(shape)
            .background(colors.surfaceMuted)
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(iconRes = iconRes, tint = tint, size = 44.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.5.sp, color = TextTertiary)
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** Header: logo mark, wordmark and the settings entry. */
@Composable
private fun HeaderBar(
    progress: () -> Float,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageHeader(
        title = "我的打印机",
        subtitle = "OnePrinter 云打印",
        progress = progress,
        leading = { LogoMark() },
        modifier = modifier,
        trailing = {
            SoftIconButton(
                iconRes = R.drawable.ic_settings,
                contentDescription = "打开打印设置",
                onClick = onOpenSettings,
            )
        },
    )
}

@Composable
private fun LogoMark(size: androidx.compose.ui.unit.Dp = 42.dp) {
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

/** Hero card: the configured printer at a glance; tapping it opens the settings screen. */
@Composable
private fun ConfigHero(config: PrinterConfig, onOpenSettings: () -> Unit) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(30.dp)
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.985f)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 26.dp,
                shape = shape,
                ambientColor = colors.brandStart.copy(alpha = 0.45f),
                spotColor = colors.brandEnd.copy(alpha = 0.40f),
            )
            .clip(shape)
            .background(brandGradient())
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                onClick = onOpenSettings,
            ),
    ) {
        // Decorative light: two translucent discs give the flat gradient some depth.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(1000f, -220f),
                        radius = 900f,
                    ),
                ),
        )
        Column(Modifier.padding(start = 22.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sparkle),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "当前打印配置",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.4.sp,
                            color = Color.White.copy(alpha = 0.80f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = displayValue(config.printer),
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                        .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "修改",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill(
                    text = displayValue(config.paperSize),
                    iconRes = R.drawable.ic_select_document,
                )
                HeroPill(
                    text = if (config.color) "彩色" else "黑白",
                    iconRes = R.drawable.ic_sparkle,
                )
            }
        }
        // A slow, wide sheen keeps the hero feeling alive without competing with the content.
        ShineOverlay(
            durationMillis = 8000,
            delayMillis = 2600,
            highlight = Color.White.copy(alpha = 0.10f),
        )
    }
}

@Composable
private fun HeroPill(text: String, iconRes: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

/** The two file sources, drawn as equal tiles that respond to touch. */
@Composable
private fun SourceTiles(
    enabled: Boolean,
    onPickImage: () -> Unit,
    onPickDocument: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SourceTile(
            title = "图片",
            subtitle = "相册 · 拍照",
            iconRes = R.drawable.ic_select_image,
            tint = Primary,
            enabled = enabled,
            onClick = onPickImage,
            modifier = Modifier.weight(1f),
        )
        SourceTile(
            title = "文档",
            subtitle = "PDF · Office",
            iconRes = R.drawable.ic_select_document,
            tint = myPrinterColors.brandEnd,
            enabled = enabled,
            onClick = onPickDocument,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SourceTile(
    title: String,
    subtitle: String,
    iconRes: Int,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .height(122.dp)
            .pressScale(interaction, pressedScale = 0.96f)
            .shadow(
                elevation = if (colors.isDark) 0.dp else 10.dp,
                shape = shape,
                ambientColor = tint.copy(alpha = 0.16f),
                spotColor = tint.copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        IconTile(iconRes = iconRes, tint = tint, size = 40.dp, corner = 13.dp)
        Column {
            Text(
                text = title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textTertiary,
            )
        }
    }
}

/** The file that is about to be printed, with preview, copies and print options. */
@Composable
private fun SelectionCard(
    selection: PrintSelection,
    copiesText: String,
    onCopiesChange: (String) -> Unit,
    layout: Boolean,
    onLayoutChange: (Boolean) -> Unit,
    duplex: Boolean,
    onDuplexChange: (Boolean) -> Unit,
    pageRange: String,
    onPageRangeChange: (String) -> Unit,
    openedFromAnotherApp: Boolean,
) {
    val colors = myPrinterColors
    GlassCard(contentPadding = 16.dp) {
        if (selection.isImage) {
            ImagePreview(selection = selection)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = selection.name,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    SelectionMeta(isImage = true, openedFromAnotherApp = openedFromAnotherApp)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(
                    iconRes = R.drawable.ic_select_document,
                    tint = colors.brandEnd,
                    size = 52.dp,
                    corner = 16.dp,
                )
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = selection.name,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    SelectionMeta(isImage = false, openedFromAnotherApp = openedFromAnotherApp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Hairline()
        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "打印份数",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "支持 1–999 份",
                    fontSize = 11.5.sp,
                    color = colors.textTertiary,
                )
            }
            QuantityStepper(
                value = copiesText,
                onValueChange = onCopiesChange,
                modifier = Modifier.width(150.dp),
            )
        }

        Spacer(Modifier.height(14.dp))
        Hairline()
        Spacer(Modifier.height(10.dp))

        ToggleRow(
            title = "横版打印",
            subtitle = "适合宽幅表格与横向海报",
            checked = layout,
            onCheckedChange = onLayoutChange,
        )
        // Duplex and page ranges only make sense for documents.
        if (!selection.isImage) {
            ToggleRow(
                title = "双面打印",
                subtitle = "自动翻面，节省纸张",
                checked = duplex,
                onCheckedChange = onDuplexChange,
            )
            Spacer(Modifier.height(12.dp))
            FieldLabel("指定页码")
            Spacer(Modifier.height(7.dp))
            AppTextField(
                value = pageRange,
                onValueChange = onPageRangeChange,
                placeholder = "如：1-5,8,10-12（留空表示全部）",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            )
        }
    }
}

@Composable
private fun SelectionMeta(isImage: Boolean, openedFromAnotherApp: Boolean) {
    val colors = myPrinterColors
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MetaPill(
            text = if (isImage) "图片" else "文档",
            tint = if (isImage) Primary else colors.brandEnd,
            background = if (isImage) {
                Primary.copy(alpha = 0.10f)
            } else {
                colors.brandEnd.copy(alpha = 0.10f)
            },
        )
        if (openedFromAnotherApp) {
            MetaPill(
                text = "来自其他应用",
                tint = colors.textSecondary,
                background = colors.surfaceMuted,
                borderColor = colors.border,
            )
        }
    }
}

/** The image being printed, with a fade-in once the decoder has produced a bitmap. */
@Composable
private fun ImagePreview(selection: PrintSelection, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = myPrinterColors
    var preview by remember(selection.uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(selection.uri) {
        preview = withContext(Dispatchers.IO) {
            FileIntake.decodePreview(context, selection.uri)?.asImageBitmap()
        }
    }
    val appear by animateFloatAsState(
        targetValue = if (preview != null) 1f else 0f,
        animationSpec = tween(Motion.Slow, easing = Motion.EaseOut),
        label = "previewAppear",
    )
    val shape = RoundedCornerShape(20.dp)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // The frame follows the picture's own aspect ratio (within limits) instead of adding
        // letterboxing on every photo.
        val aspect = preview?.let { it.width.toFloat() / it.height.toFloat() } ?: (4f / 3f)
        val targetHeight by animateDpAsState(
            targetValue = if (preview == null) 190.dp else (maxWidth / aspect).coerceIn(160.dp, 300.dp),
            animationSpec = Motion.settleSpring(),
            label = "previewHeight",
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight)
                .clip(shape)
                .background(colors.previewBackground)
                .border(1.dp, colors.border, shape),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = preview
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "待打印图片预览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .graphicsLayer {
                            alpha = appear
                            scaleX = 0.97f + 0.03f * appear
                            scaleY = 0.97f + 0.03f * appear
                        },
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_select_image),
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "无法预览，不影响打印",
                        fontSize = 12.sp,
                        color = colors.textTertiary,
                    )
                }
            }
        }
    }
}

/** The pinned bottom action: a gradient pill above a soft page-coloured fade. */
@Composable
private fun PrintBar(
    printing: Boolean,
    enabled: Boolean,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.38f to PageBackground.copy(alpha = 0.86f),
                    1f to PageBackground,
                ),
            )
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp),
    ) {
        GradientButton(
            label = "开始打印",
            loading = printing,
            loadingLabel = "正在发送…",
            enabled = enabled,
            iconRes = R.drawable.ic_print,
            onClick = onPrint,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun displayValue(value: String): String = value.ifBlank { "未设置" }

private fun hasCameraApp(context: Context): Boolean =
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(context.packageManager) != null
