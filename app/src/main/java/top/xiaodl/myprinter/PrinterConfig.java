package top.xiaodl.myprinter;

/**
 * 打印机连接配置的不可变快照。
 *
 * <p>由 {@link ConfigStore} 从本机 {@code SharedPreferences} 读出后构造，打印页与打印请求
 * 都只读取该对象，避免在一次打印过程中配置被修改。
 */
public final class PrinterConfig {

    /** 打印服务上传接口地址。 */
    private final String apiUrl;

    /** 打印机名称（打印服务端的打印机标识）。 */
    private final String printer;

    /** 纸张大小，例如 A4。 */
    private final String paperSize;

    /** 打印服务验证口令。 */
    private final String code;

    /** 是否彩色打印。 */
    private final boolean color;

    /**
     * 构造配置快照。所有字符串参数为 {@code null} 时会被替换为空字符串，避免调用方判空。
     *
     * @param apiUrl    上传接口地址
     * @param printer   打印机名称
     * @param paperSize 纸张大小
     * @param code      验证口令
     * @param color     是否彩色打印
     */
    public PrinterConfig(String apiUrl, String printer, String paperSize, String code, boolean color) {
        this.apiUrl = apiUrl == null ? "" : apiUrl;
        this.printer = printer == null ? "" : printer;
        this.paperSize = paperSize == null ? "" : paperSize;
        this.code = code == null ? "" : code;
        this.color = color;
    }

    /** @return 打印服务上传接口地址 */
    public String getApiUrl() {
        return apiUrl;
    }

    /** @return 打印机名称 */
    public String getPrinter() {
        return printer;
    }

    /** @return 纸张大小 */
    public String getPaperSize() {
        return paperSize;
    }

    /** @return 打印服务验证口令 */
    public String getCode() {
        return code;
    }

    /** @return 是否彩色打印 */
    public boolean getColor() {
        return color;
    }

    /**
     * 判断配置是否足以发起一次打印。
     *
     * <p>接口地址、打印机名称、纸张大小是必填项；验证口令允许为空（部分服务不校验）。
     *
     * @return 三项必填内容均非空白时为 {@code true}
     */
    public boolean isComplete() {
        return !apiUrl.trim().isEmpty() && !printer.trim().isEmpty() && !paperSize.trim().isEmpty();
    }
}
