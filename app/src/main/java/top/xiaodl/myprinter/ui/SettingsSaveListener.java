package top.xiaodl.myprinter.ui;

/**
 * 打印设置页的保存回调。
 *
 * <p>由 Compose 界面在用户点击「保存配置」时调用，宿主 Activity 负责把结果写入
 * {@code ConfigStore} 并关闭页面。
 */
public interface SettingsSaveListener {

    /**
     * 保存打印配置。
     *
     * @param apiUrl    打印服务上传接口地址
     * @param printer   打印机名称
     * @param paperSize 纸张大小（如 A4）
     * @param code      打印服务验证口令
     * @param color     是否彩色打印
     */
    void onSave(String apiUrl, String printer, String paperSize, String code, boolean color);
}
