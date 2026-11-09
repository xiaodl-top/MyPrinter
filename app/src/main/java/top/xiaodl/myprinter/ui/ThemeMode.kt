package top.xiaodl.myprinter.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf

/** 应用的配色模式。 */
enum class ThemeMode {
    /** 跟随系统夜间模式（默认）。 */
    SYSTEM,

    /** 始终使用浅色配色。 */
    LIGHT,

    /** 始终使用深色配色。 */
    DARK;

    /**
     * 结合系统状态计算最终是否使用深色。
     *
     * @param systemDark 系统当前是否处于夜间模式
     * @return 应使用深色时为 true
     */
    fun isDark(systemDark: Boolean): Boolean = when (this) {
        LIGHT -> false
        DARK -> true
        SYSTEM -> systemDark
    }
}

/**
 * 应用级外观状态。
 *
 * <p>[mode] 是 Compose 状态：在设置页切换后，所有已组合的界面（包括停留在设置页后面的首页）
 * 会立即重绘，同时选择会写入本机 SharedPreferences。
 */
object AppTheme {

    /** 外观偏好的 SharedPreferences 文件名。 */
    private const val PREFERENCES = "oneprinter_ui"

    /** 主题模式的键名。 */
    private const val KEY_THEME_MODE = "theme_mode"

    /** 当前模式，界面直接读取即可自动跟随。 */
    val mode = mutableStateOf(ThemeMode.SYSTEM)

    /**
     * 从本机读取已保存的模式。请在 Activity 首次组合之前调用。
     *
     * @param context 任意 Context
     */
    fun init(context: Context) {
        mode.value = load(context)
    }

    /**
     * 切换模式并持久化。
     *
     * @param context 任意 Context
     * @param value   新的主题模式
     */
    fun select(context: Context, value: ThemeMode) {
        mode.value = value
        context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, value.name)
            .apply()
    }

    /**
     * 读取系统当前是否处于夜间模式。
     *
     * @param context 任意 Context
     * @return 系统为深色时为 true
     */
    fun systemIsDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    /** 读取已保存的模式；没有记录或值非法时回退到「跟随系统」。 */
    private fun load(context: Context): ThemeMode {
        val stored = context.applicationContext
            .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
    }
}
