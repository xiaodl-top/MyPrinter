package top.xiaodl.myprinter;

import android.app.Activity;
import android.os.Build;

/**
 * Slide transitions shared by the two settings screens.
 *
 * <p>Android 14+ plays the animations declared on the activity that opens or closes; older
 * releases read the same resources through {@link #finish(Activity)} and the launcher's
 * {@code overridePendingTransition} call.
 */
public final class NavTransitions {

    private NavTransitions() {
    }

    /** Must be called from the activity's {@code onCreate}. */
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

    /** Must be called from the activity's {@code finish()}, after {@code super.finish()}. */
    public static void finish(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overridePendingTransition(R.anim.nav_pop_enter, R.anim.nav_pop_exit);
        }
    }
}
