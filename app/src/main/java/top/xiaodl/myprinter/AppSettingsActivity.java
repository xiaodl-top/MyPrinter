package top.xiaodl.myprinter;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import top.xiaodl.myprinter.databinding.ActivityAppSettingsBinding;
import top.xiaodl.myprinter.ui.ComposeBridgeKt;

/**
 * 应用设置页：外观（浅色 / 深色 / 跟随系统）与「关于」信息。
 *
 * <p>从首页右上角的齿轮按钮进入；打印相关配置不在此页，而在 {@link SettingsActivity}。
 */
public final class AppSettingsActivity extends ComponentActivity {

    /** 创建页面：初始化系统栏与转场，并把 Compose 界面挂到布局中的 ComposeView 上。 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化主题模式，并让系统栏图标颜色与当前调色板一致。
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

    /** 关闭页面时播放与进入动画对称的退出转场。 */
    @Override
    public void finish() {
        super.finish();
        NavTransitions.finish(this);
    }
}
