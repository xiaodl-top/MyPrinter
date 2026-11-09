package top.xiaodl.myprinter.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf

/** How the app picks its palette. */
enum class ThemeMode {
    /** Follow the system night mode. This is the default. */
    SYSTEM,

    /** Always use the light palette. */
    LIGHT,

    /** Always use the dark palette. */
    DARK;

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> systemDark
    }
}

/**
 * App-wide appearance state.
 *
 * [mode] is a Compose state, so changing it in the settings screen repaints every composed
 * screen at once — including the main screen waiting behind the settings activity — and the
 * preference is written through to disk immediately.
 */
object AppTheme {
    private const val PREFERENCES = "oneprinter_ui"
    private const val KEY_THEME_MODE = "theme_mode"

    val mode = mutableStateOf(ThemeMode.SYSTEM)

    /** Reads the stored preference; call once per activity before the first composition. */
    fun init(context: Context) {
        mode.value = load(context)
    }

    fun select(context: Context, value: ThemeMode) {
        mode.value = value
        context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, value.name)
            .apply()
    }

    fun systemIsDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun load(context: Context): ThemeMode {
        val stored = context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
    }
}
