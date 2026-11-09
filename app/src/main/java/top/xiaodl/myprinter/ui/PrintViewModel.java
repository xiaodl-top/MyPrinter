package top.xiaodl.myprinter.ui;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.compose.runtime.MutableState;
import androidx.lifecycle.AndroidViewModel;

import top.xiaodl.myprinter.AppLog;
import top.xiaodl.myprinter.ConfigStore;
import top.xiaodl.myprinter.FileIntake;
import top.xiaodl.myprinter.IncomingFile;
import top.xiaodl.myprinter.PrinterConfig;
import top.xiaodl.myprinter.PrintApi;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 打印页的状态容器：持有待打印文件、份数与版式，并执行 OkHttp 上传。
 *
 * <p>所有字段都是 Compose 的 {@link MutableState}，界面直接读取即可自动重组；
 * 上传在单线程池中执行，结果通过主线程 Handler 回投到状态里。
 */
public final class PrintViewModel extends AndroidViewModel {

    /** 当前打印配置（从本机读取，进入页面时刷新）。 */
    public final MutableState<PrinterConfig> config;

    /** 当前待打印文件，{@code null} 表示尚未选择。 */
    public final MutableState<PrintSelection> selection;

    /** 份数输入框的文本（已过滤为非数字之外的内容，最多 3 位）。 */
    public final MutableState<String> copiesText;

    /** 横版打印；发送给打印服务时 layout=1/0。 */
    public final MutableState<Boolean> layout;

    /** 双面打印；发送给打印服务时 duplex=1/0。 */
    public final MutableState<Boolean> duplex;

    /** 指定页码，例如 "1-5,8,10-12"；空字符串表示全部。 */
    public final MutableState<String> pageRange;

    /** 是否正在上传（按钮显示「正在发送…」）。 */
    public final MutableState<Boolean> printing;

    /** 当前需要展示的弹窗，{@code null} 表示没有弹窗。 */
    public final MutableState<PrintDialog> dialog;

    /** 当前文件是否由其它应用传入。 */
    public final MutableState<Boolean> openedFromAnotherApp;

    /** 单线程执行器：保证同一时间只有一个上传任务。 */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** 主线程 Handler，用于把后台结果写回 Compose 状态。 */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * @param application 应用上下文（AndroidViewModel 自动注入）
     */
    public PrintViewModel(Application application) {
        super(application);
        config = ComposeBridgeKt.composeState(ConfigStore.load(application));
        selection = ComposeBridgeKt.composeState((PrintSelection) null);
        copiesText = ComposeBridgeKt.composeState("1");
        layout = ComposeBridgeKt.composeState(false);
        duplex = ComposeBridgeKt.composeState(false);
        pageRange = ComposeBridgeKt.composeState("");
        printing = ComposeBridgeKt.composeState(false);
        dialog = ComposeBridgeKt.composeState((PrintDialog) null);
        openedFromAnotherApp = ComposeBridgeKt.composeState(false);
    }

    /** 重新从本机读取配置（从设置页返回时调用）。 */
    public void refreshConfig() {
        config.setValue(ConfigStore.load(getApplication()));
    }

    /**
     * 在应用内选择文件（相册 / 相机 / 文档选择器）。
     *
     * @param uri     文件地址
     * @param name    文件名
     * @param isImage 是否为图片
     */
    public void selectFile(Uri uri, String name, boolean isImage) {
        selection.setValue(new PrintSelection(uri, name, isImage));
        // 应用内选择的文件不再算「来自其他应用」。
        openedFromAnotherApp.setValue(false);
        dialog.setValue(null);
        AppLog.user("选择文件完成",
                "文件名=" + name + ", 类型=" + (isImage ? "图片" : "文档") + ", uri=" + uri);
    }

    /**
     * 接收其它应用（微信、分享面板、打开方式）传入的文件。
     *
     * <p>只做暂存，是否发送由用户点击「开始打印」决定。
     *
     * @param file 外部来源文件
     */
    public void openIncomingFile(IncomingFile file) {
        openedFromAnotherApp.setValue(true);
        selection.setValue(new PrintSelection(file.getUri(), file.getName(), file.isImage()));
        dialog.setValue(null);
        AppLog.user("外部文件已载入，等待点击打印",
                "文件名=" + file.getName() + ", 类型=" + (file.isImage() ? "图片" : "文档"));
    }

    /**
     * 更新份数：只保留数字，最多 3 位（上限 999）。
     *
     * @param value 输入框原始文本
     */
    public void updateCopies(String value) {
        String sanitized = digitsOnly(value, 3);
        if (!sanitized.equals(copiesText.getValue())) {
            AppLog.event("修改打印份数", "份数=" + (sanitized.isEmpty() ? "1" : sanitized));
            copiesText.setValue(sanitized);
        }
    }

    /** 切换横版打印。 */
    public void updateLayout(boolean value) {
        layout.setValue(value);
        AppLog.user("修改横版打印", "横版=" + (value ? 1 : 0));
    }

    /** 切换双面打印（仅文档生效）。 */
    public void updateDuplex(boolean value) {
        duplex.setValue(value);
        AppLog.user("修改双面打印", "双面=" + (value ? 1 : 0));
    }

    /** 更新指定页码，{@code null} 视为空（全部页码）。 */
    public void updatePageRange(String value) {
        pageRange.setValue(value == null ? "" : value);
        AppLog.user("修改指定页码",
                "页码=" + (value == null || value.trim().isEmpty() ? "全部" : value));
    }

    /** 关闭当前弹窗。 */
    public void dismissDialog() {
        dialog.setValue(null);
    }

    /**
     * 解析份数：非法或空输入按 1 处理，最小为 1。
     *
     * @return 实际份数
     */
    public long copies() {
        try {
            return Math.max(1L, Long.parseLong(copiesText.getValue()));
        } catch (NumberFormatException error) {
            return 1L;
        }
    }

    /**
     * 发起打印：校验配置与文件，读取内容并在后台线程上传。
     *
     * <p>成功后弹出「打印请求已发送」，失败时把服务端/异常文本原样提示给用户。
     * 双面与页码只对文档生效，图片固定单面、全部页码。
     */
    public void printNow() {
        PrintSelection selected = selection.getValue();
        if (selected == null) {
            AppLog.warn("点击打印按钮，但尚未选择文件");
            return;
        }
        Application application = getApplication();
        PrinterConfig currentConfig = ConfigStore.load(application);
        config.setValue(currentConfig);
        if (!currentConfig.isComplete()) {
            AppLog.warn("打印被拦截：打印配置不完整",
                    "API=" + currentConfig.getApiUrl()
                            + ", 打印机=" + currentConfig.getPrinter()
                            + ", 纸张=" + currentConfig.getPaperSize());
            dialog.setValue(PrintDialog.needConfig(
                    "请先完成打印设置", "打印机 API、打印机名称和纸张大小均不能为空。"));
            return;
        }

        long copies = copies();
        copiesText.setValue(Long.toString(copies));
        // 双面与页码只对文档有意义；图片始终单面打印。
        boolean imageFile = selected.isImage();
        String requestPageRange = imageFile ? "" : pageRange.getValue();
        boolean requestDuplex = !imageFile && Boolean.TRUE.equals(duplex.getValue());
        boolean requestLayout = Boolean.TRUE.equals(layout.getValue());
        boolean fromAnotherApp = Boolean.TRUE.equals(openedFromAnotherApp.getValue());
        long startedAt = System.currentTimeMillis();
        AppLog.user("点击打印按钮，开始打印",
                "文件名=" + selected.getName()
                        + ", 份数=" + copies
                        + ", 打印机=" + currentConfig.getPrinter()
                        + ", 纸张=" + currentConfig.getPaperSize()
                        + ", 页码=" + (requestPageRange == null || requestPageRange.trim().isEmpty()
                        ? "全部" : requestPageRange)
                        + ", 双面=" + (requestDuplex ? 1 : 0)
                        + ", 横版=" + (requestLayout ? 1 : 0)
                        + ", 彩色=" + (currentConfig.getColor() ? 1 : 0)
                        + ", 来源=" + (fromAnotherApp ? "其他应用" : "应用内"));

        printing.setValue(true);
        executor.execute(() -> {
            try {
                byte[] content = FileIntake.readAllBytes(application, selected.getUri());
                if (content.length == 0) {
                    throw new IOException("所选文件为空");
                }
                PrintApi.PrintReply reply = PrintApi.print(
                        currentConfig,
                        FileIntake.uploadFileName(application, selected.getUri(), selected.getName()),
                        content,
                        copies,
                        requestPageRange,
                        requestDuplex,
                        requestLayout);
                long elapsed = System.currentTimeMillis() - startedAt;
                mainHandler.post(() -> {
                    printing.setValue(false);
                    if (reply.isSuccess()) {
                        AppLog.user("打印请求成功",
                                "耗时=" + elapsed + "ms, 服务端消息=" + reply.getMessage());
                        dialog.setValue(PrintDialog.success("打印请求已发送", reply.getMessage()));
                    } else {
                        AppLog.warn("打印请求被服务端拒绝",
                                "耗时=" + elapsed + "ms, 服务端消息=" + reply.getMessage());
                        dialog.setValue(PrintDialog.error("打印失败", reply.getMessage()));
                    }
                });
            } catch (Exception error) {
                AppLog.failure("打印请求异常", error,
                        "文件名=" + selected.getName()
                                + ", 耗时=" + (System.currentTimeMillis() - startedAt) + "ms");
                String message = error.getMessage();
                String text = message == null || message.trim().isEmpty()
                        ? "无法连接到打印服务，请检查配置和网络。"
                        : message;
                mainHandler.post(() -> {
                    printing.setValue(false);
                    dialog.setValue(PrintDialog.error("打印请求失败", text));
                });
            }
        });
    }

    @Override
    protected void onCleared() {
        // ViewModel 销毁时中断上传，避免泄漏线程。
        executor.shutdownNow();
        super.onCleared();
    }

    /**
     * 只保留数字字符，并限制长度。
     *
     * @param value 原始文本，可为 {@code null}
     * @param limit 最大保留位数
     * @return 过滤后的数字串
     */
    private static String digitsOnly(String value, int limit) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length() && builder.length() < limit; index++) {
            char character = value.charAt(index);
            if (Character.isDigit(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
