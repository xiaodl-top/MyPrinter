package top.xiaodl.myprinter;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import top.xiaodl.myprinter.databinding.ActivityAppSettingsBinding;
import top.xiaodl.myprinter.ui.ComposeBridgeKt;

/** App-wide preferences: appearance (light / dark / system) and the about block. */
public final class AppSettingsActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComposeBridgeKt.configureSystemBars(this);
        NavTransitions.install(this);
        AppLog.user("打开设置页");

        ActivityAppSettingsBinding binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ComposeBridgeKt.showAppSettingsScreen(binding.appSettingsComposeView, () -> {
            AppLog.user("点击返回，关闭设置页");
            finish();
        });
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.finish(this);
    }
}
