package top.xiaodl.myprinter;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import top.xiaodl.myprinter.databinding.ActivitySettingsBinding;
import top.xiaodl.myprinter.ui.ComposeBridgeKt;

/**
 * 打印设置页：编辑打印服务地址、验证口令、打印机名称、纸张与彩色打印。
 *
 * <p>从首页打印机卡片右上角的「修改」按钮进入；保存成功后写入 {@link ConfigStore} 并关闭页面。
 */
public final class SettingsActivity extends ComponentActivity {

    /** 创建页面：初始化系统栏与转场，并把打印设置界面挂到 ComposeView 上。 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComposeBridgeKt.configureSystemBars(this);
        NavTransitions.install(this);
        AppLog.user("打开打印设置页");

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ComposeBridgeKt.showSettingsScreen(
                binding.settingsComposeView,
                () -> {
                    AppLog.user("点击返回，关闭打印设置页");
                    finish();
                },
                (apiUrl, printer, paperSize, code, color) -> {
                    // 保存到本机后提示并关闭页面；首页 onResume 会重新读取配置。
                    ConfigStore.save(this, apiUrl, printer, paperSize, code, color);
                    AppLog.user("保存打印配置",
                            "API=" + apiUrl
                                    + ", 打印机=" + printer
                                    + ", 纸张=" + paperSize
                                    + ", 彩色=" + (color ? 1 : 0)
                                    + ", 口令长度=" + (code == null ? 0 : code.length()));
                    Toast.makeText(this, "打印配置已保存", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
    }

    /** 保存成功或用户返回时关闭页面，并播放对称的退出转场。 */
    @Override
    public void finish() {
        super.finish();
        NavTransitions.finish(this);
    }
}
