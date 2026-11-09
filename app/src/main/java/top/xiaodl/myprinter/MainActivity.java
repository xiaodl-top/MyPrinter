package top.xiaodl.myprinter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import top.xiaodl.myprinter.databinding.ActivityMainBinding;
import top.xiaodl.myprinter.ui.ComposeBridgeKt;
import top.xiaodl.myprinter.ui.PrintViewModel;

/**
 * 首页：选择要打印的文件并提交到 OnePrinter 打印服务。
 *
 * <p>其它应用（微信、钉钉、文件管理器等）通过「打开方式 / 分享」送来的文件也会进入本页，
 * 文件只做暂存，由用户点击「开始打印」后才真正发送。
 *
 * <p>页面本身由 Jetpack Compose 绘制，本类负责：承载 ComposeView、接收 Intent、启动两个设置页。
 */
public final class MainActivity extends ComponentActivity {

    /** 布局绑定，提供 ComposeView。 */
    private ActivityMainBinding binding;

    /** 打印页状态与上传逻辑。 */
    private PrintViewModel viewModel;

    /** 创建首页：加载布局、绑定 ViewModel 与两个设置页入口，并处理首次进入携带的文件。 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化主题模式并配置系统栏；必须在 setContent 之前完成。
        ComposeBridgeKt.configureSystemBars(this);
        AppLog.user("打开主页面",
                "启动方式=" + (getIntent() == null ? "未知" : String.valueOf(getIntent().getAction())));

        // ViewBinding 加载宿主布局，Compose 界面挂到其中的 ComposeView 上。
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(PrintViewModel.class);
        ComposeBridgeKt.showPrintScreen(
                binding.printComposeView,
                viewModel,
                () -> {
                    // 右上角齿轮：应用设置（外观 / 关于）。
                    AppLog.user("点击设置按钮");
                    open(AppSettingsActivity.class);
                },
                () -> {
                    // 打印机卡片右上角「修改」：打印设置。
                    AppLog.user("点击修改打印配置");
                    open(SettingsActivity.class);
                });

        if (savedInstanceState == null) {
            consumeIncomingFile(getIntent());
        }
    }

    /** 复用当前实例接收新的外部文件 Intent。 */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // singleTask + 外部文件再次进入时，复用当前实例并处理新 Intent。
        consumeIncomingFile(intent);
    }

    /**
     * 以统一的滑动转场打开某个设置页。
     *
     * @param target 目标 Activity（{@link AppSettingsActivity} 或 {@link SettingsActivity}）
     */
    private void open(Class<? extends ComponentActivity> target) {
        startActivity(new Intent(this, target));
        // Android 14+ 由被打开的 Activity 自己声明动画。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
        }
    }

    /** 返回前台时刷新打印配置，保证卡片展示的是设置页里的最新值。 */
    @Override
    protected void onResume() {
        super.onResume();
        // 从设置页返回后刷新配置，保证首页卡片展示最新值。
        viewModel.refreshConfig();
        PrinterConfig config = viewModel.config.getValue();
        AppLog.event("刷新打印配置",
                "打印机=" + config.getPrinter() + ", 纸张=" + config.getPaperSize());
    }

    /**
     * 暂存其它应用交给本应用的文件，等待用户点击打印。
     *
     * <p>从最近任务恢复时不重复处理，避免用户返回应用时被旧文件覆盖当前选择。
     *
     * @param intent 进入本页的 Intent
     */
    private void consumeIncomingFile(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean fromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        if (fromHistory) {
            return;
        }
        IncomingFile incoming = FileIntake.resolve(this, intent);
        if (incoming == null) {
            if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
                AppLog.event("Intent 中没有可打印的文件",
                        "action=" + intent.getAction()
                                + ", type=" + intent.getType()
                                + ", data=" + intent.getData());
            }
            return;
        }
        AppLog.user("接收到其他应用的文件",
                "action=" + intent.getAction()
                        + ", 文件名=" + incoming.getName()
                        + ", 类型=" + (incoming.isImage() ? "图片" : "文档")
                        + ", uri=" + incoming.getUri());
        FileIntake.persistReadPermission(this, intent, incoming.getUri());
        viewModel.openIncomingFile(incoming);
    }
}
