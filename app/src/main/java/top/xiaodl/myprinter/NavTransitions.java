package top.xiaodl.myprinter;

import android.app.Activity;
import android.os.Build;

/**
 * 两个设置页共用的滑动转场。
 *
 * <p>Android 14（API 34）起由被打开/关闭的 Activity 自己声明动画；更低版本通过
 * {@link #finish(Activity)} 以及启动方的 {@code overridePendingTransition} 读取同一套资源。
 */
public final class NavTransitions {

    private NavTransitions() {
        // 工具类，禁止实例化。
    }

    /**
     * 声明设置页的进入 / 退出转场。必须在 Activity 的 {@code onCreate} 中调用。
     *
     * @param activity 设置页 Activity
     */
    public static void install(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.nav_enter,
                    R.anim.nav_exit);
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.nav_pop_enter,
                    R.anim.nav_pop_exit);
        }
    }

    /**
     * 低版本系统上为关闭动作补上转场。必须在 {@code super.finish()} 之后调用。
     *
     * @param activity 正在关闭的设置页 Activity
     */
    public static void finish(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overridePendingTransition(R.anim.nav_pop_enter, R.anim.nav_pop_exit);
        }
    }
}
