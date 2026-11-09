package top.xiaodl.myprinter;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 在本机保存打印机参数，保证每次打印都使用最新的配置。
 *
 * <p>存储位置为 {@code SharedPreferences}（文件名 {@code oneprinter_settings}），只保存在本机，
 * 不会上传。修改配置的唯一入口是「打印设置」页面。
 */
public final class ConfigStore {

    /** SharedPreferences 文件名。 */
    private static final String PREFERENCES = "oneprinter_settings";

    /** 打印服务地址。 */
    private static final String KEY_API_URL = "api_url";

    /** 打印机名称。 */
    private static final String KEY_PRINTER = "printer";

    /** 纸张大小。 */
    private static final String KEY_PAPER_SIZE = "paper_size";

    /** 验证口令。 */
    private static final String KEY_CODE = "code";

    /** 是否彩色打印（1 表示彩色，0 表示黑白）。 */
    private static final String KEY_COLOR = "color";

    /**
     * 首次启动时展示的默认值。
     *
     * <p>其中服务地址、打印机名称、验证口令三项属于敏感配置：由 Gradle 在构建时从本机的
     * {@code local.properties}（或环境变量）注入 {@code BuildConfig}，因此<b>不会出现在仓库源码中</b>。
     * 没有注入时为空字符串，应用会以「未配置」状态启动，并引导用户前往打印设置页填写。
     */
    public static final String DEFAULT_API_URL = BuildConfig.DEFAULT_API_URL;

    /** 默认打印机名称（同上，由构建期注入）。 */
    public static final String DEFAULT_PRINTER = BuildConfig.DEFAULT_PRINTER;

    /** 默认纸张大小。 */
    public static final String DEFAULT_PAPER_SIZE = "A4";

    /** 默认验证口令（同上，由构建期注入）。 */
    public static final String DEFAULT_CODE = BuildConfig.DEFAULT_CODE;

    private ConfigStore() {
        // 工具类，禁止实例化。
    }

    /**
     * 读取本机配置；未保存过的项回退到默认值。
     *
     * @param context 任意 Context（内部使用 SharedPreferences）
     * @return 配置快照
     */
    public static PrinterConfig load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return new PrinterConfig(
                preferences.getString(KEY_API_URL, DEFAULT_API_URL),
                preferences.getString(KEY_PRINTER, DEFAULT_PRINTER),
                preferences.getString(KEY_PAPER_SIZE, DEFAULT_PAPER_SIZE),
                preferences.getString(KEY_CODE, DEFAULT_CODE),
                preferences.getInt(KEY_COLOR, 0) == 1
        );
    }

    /**
     * 保存配置。字符串会去掉首尾空白；{@code null} 视为空字符串。
     *
     * @param context   任意 Context
     * @param apiUrl    打印服务地址
     * @param printer   打印机名称
     * @param paperSize 纸张大小
     * @param code      验证口令
     * @param color     是否彩色打印
     */
    public static void save(
            Context context,
            String apiUrl,
            String printer,
            String paperSize,
            String code,
            boolean color
    ) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_URL, clean(apiUrl))
                .putString(KEY_PRINTER, clean(printer))
                .putString(KEY_PAPER_SIZE, clean(paperSize))
                .putString(KEY_CODE, clean(code))
                .putInt(KEY_COLOR, color ? 1 : 0)
                .apply();
    }

    /** 去掉首尾空白，{@code null} 转为空字符串。 */
    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
