package top.xiaodl.myprinter.ui

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat

/**
 * The only place where the Java side needs Kotlin: Compose state and Compose content can only be
 * created from Kotlin, so these small helpers are called from the Java activities and the Java
 * [PrintViewModel].
 */

/** Java-friendly `mutableStateOf`. */
fun <T> composeState(value: T): MutableState<T> = mutableStateOf(value)

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

/** App-wide preferences: appearance and about. */
fun ComposeView.showAppSettingsScreen(onBack: Runnable) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        MyPrinterTheme {
            AppSettingsScreen(onBack = { onBack.run() })
        }
    }
}

/** Printer connection settings, opened from the printer card on the home screen. */
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
 * Edge-to-edge system bars. The icon colour follows the app's own palette (light / dark / system
 * from [AppTheme]), not just the system night mode, so a forced theme also gets readable bars.
 */
fun configureSystemBars(activity: ComponentActivity) {
    AppTheme.init(activity)
    activity.enableEdgeToEdge()
    val dark = AppTheme.mode.value.isDark(AppTheme.systemIsDark(activity))
    val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
    controller.isAppearanceLightStatusBars = !dark
    controller.isAppearanceLightNavigationBars = !dark
}
