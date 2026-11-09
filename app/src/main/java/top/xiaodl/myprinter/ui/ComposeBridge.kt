package top.xiaodl.myprinter.ui

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat

/**
 * Java 侧唯一需要 Kotlin 的地方：Compose 状态与 Compose 内容只能由 Kotlin 创建，
 * 因此这里提供几个小工具方法，供 Java 的 Activity 与 [PrintViewModel] 调用。
 */

/**
 * 供 Java 使用的 `mutableStateOf`。
 *
 * @param value 初始值
 * @return 可被 Compose 观察的可变状态
 */
fun <T> composeState(value: T): MutableState<T> = mutableStateOf(value)

/**
 * 在宿主 ComposeView 上渲染首页。
 *
 * @param viewModel           打印页状态
 * @param onOpenAppSettings   打开「设置」页（外观 / 关于）的回调
 * @param onOpenPrintSettings 打开「打印设置」页的回调
 */
fun ComposeView.showPrintScreen(
    viewModel: PrintViewModel,
    onOpenAppSettings: Runnable,
    onOpenPrintSettings: Runnable,
) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        MyPrinterTheme {
            PrintScreen(
                viewModel = viewModel,
                onOpenAppSettings = { onOpenAppSettings.run() },
                onOpenPrintSettings = { onOpenPrintSettings.run() },
            )
        }
    }
}

/**
 * 应用设置页：外观（主题模式）与「关于」。
 *
 * @param onBack 返回回调
 */
fun ComposeView.showAppSettingsScreen(onBack: Runnable) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        MyPrinterTheme {
            AppSettingsScreen(onBack = { onBack.run() })
        }
    }
}

/**
 * 打印设置页：服务地址、口令、打印机、纸张等连接参数。
 *
 * @param onBack 返回回调
 * @param onSave 保存回调
 */
fun ComposeView.showSettingsScreen(onBack: Runnable, onSave: SettingsSaveListener) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        MyPrinterTheme {
            SettingsScreen(
                onBack = { onBack.run() },
                onSave = { apiUrl, printer, paperSize, code, color ->
                    onSave.onSave(apiUrl, printer, paperSize, code, color)
                },
            )
        }
    }
}

/**
 * 配置全面屏（edge-to-edge）系统栏。
 *
 * <p>图标颜色跟随**应用当前调色板**（浅色 / 深色 / 跟随系统由 [AppTheme] 决定），
 * 因此强制主题时系统栏依然可读。同时会初始化 [AppTheme]，请在 `setContent` 之前调用。
 *
 * @param activity 宿主 Activity
 */
fun configureSystemBars(activity: ComponentActivity) {
    AppTheme.init(activity)
    activity.enableEdgeToEdge()
    val dark = AppTheme.mode.value.isDark(AppTheme.systemIsDark(activity))
    val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    controller.isAppearanceLightStatusBars = !dark
    controller.isAppearanceLightNavigationBars = !dark
}
