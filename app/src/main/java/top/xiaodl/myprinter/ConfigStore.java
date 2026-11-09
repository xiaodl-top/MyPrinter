package top.xiaodl.myprinter;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the printer parameters locally so every print uses the latest saved values. */
public final class ConfigStore {
    private static final String PREFERENCES = "oneprinter_settings";
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_PRINTER = "printer";
    private static final String KEY_PAPER_SIZE = "paper_size";
    private static final String KEY_CODE = "code";
    private static final String KEY_COLOR = "color";

    /**
     * Built-in defaults shown on first launch.
     *
     * <p>The sensitive values (service URL, printer name, access code) are injected by Gradle
     * from the machine-local {@code local.properties} (or environment) at build time and are
     * therefore never part of this repository. Without them the app starts unconfigured and the
     * settings screen asks the user to fill the values in.
     */
    public static final String DEFAULT_API_URL = BuildConfig.DEFAULT_API_URL;
    public static final String DEFAULT_PRINTER = BuildConfig.DEFAULT_PRINTER;
    public static final String DEFAULT_PAPER_SIZE = "A4";
    public static final String DEFAULT_CODE = BuildConfig.DEFAULT_CODE;

    private ConfigStore() {
    }

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

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
