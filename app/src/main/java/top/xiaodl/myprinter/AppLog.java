package top.xiaodl.myprinter;

import android.util.Log;

/**
 * 应用唯一的日志入口，便于用一条过滤命令跟踪所有用户操作：
 *
 * <pre>adb logcat -s MyPrinter</pre>
 *
 * <p>日志只记录文件名、大小、配置与结果，<b>绝不</b>输出文件内容或验证口令。
 */
public final class AppLog {

    /** 全局日志 TAG。 */
    public static final String TAG = "MyPrinter";

    /** 日志总开关；发布异常排查时可临时关闭。 */
    private static final boolean ENABLED = true;

    private AppLog() {
        // 工具类，禁止实例化。
    }

    /** 记录一次用户操作（INFO 级别）。 */
    public static void user(String action) {
        user(action, null);
    }

    /**
     * 记录一次用户操作（INFO 级别）。
     *
     * @param action 动作描述，例如「点击打印按钮」
     * @param detail 补充信息，可为 {@code null}
     */
    public static void user(String action, String detail) {
        write(Log.INFO, action, detail);
    }

    /** 记录一条操作细节（DEBUG 级别）。 */
    public static void event(String action) {
        event(action, null);
    }

    /**
     * 记录一条操作细节（DEBUG 级别）。
     *
     * @param action 动作描述
     * @param detail 补充信息，可为 {@code null}
     */
    public static void event(String action, String detail) {
        write(Log.DEBUG, action, detail);
    }

    /** 记录一个可恢复的问题（WARN 级别）。 */
    public static void warn(String action) {
        warn(action, null);
    }

    /**
     * 记录一个可恢复的问题（WARN 级别）。
     *
     * @param action 问题描述
     * @param detail 补充信息，可为 {@code null}
     */
    public static void warn(String action, String detail) {
        write(Log.WARN, action, detail);
    }

    /**
     * 记录一次失败（ERROR 级别，带堆栈）。
     *
     * @param action 失败描述
     * @param error  异常对象
     * @param detail 补充信息，可为 {@code null}
     */
    public static void failure(String action, Throwable error, String detail) {
        if (ENABLED) {
            Log.e(TAG, line(action, detail), error);
        }
    }

    /**
     * 写出日志。
     *
     * @param priority 日志级别
     * @param action   动作描述
     * @param detail   补充信息
     */
    private static void write(int priority, String action, String detail) {
        if (ENABLED) {
            Log.println(priority, TAG, line(action, detail));
        }
    }

    /** 把动作与细节拼成「动作 | 细节」，细节为空时只输出动作。 */
    private static String line(String action, String detail) {
        if (detail == null || detail.trim().isEmpty()) {
            return action;
        }
        return action + " | " + detail;
    }
}
