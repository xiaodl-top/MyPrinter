package top.xiaodl.myprinter.ui;

/**
 * 打印页可能弹出的提示框，把文案留在 ViewModel 中，Compose 只负责渲染。
 */
public final class PrintDialog {

    /** 弹窗类型，决定图标颜色与按钮组合。 */
    public enum Kind {
        /** 普通信息提示。 */
        INFO,
        /** 打印配置不完整，提供「去设置」入口。 */
        NEED_CONFIG,
        /** 打印服务已接受请求。 */
        SUCCESS,
        /** 打印服务拒绝请求或上传失败。 */
        ERROR,
    }

    private final Kind kind;
    private final String title;
    private final String message;

    private PrintDialog(Kind kind, String title, String message) {
        this.kind = kind;
        this.title = title;
        this.message = message;
    }

    /**
     * 普通信息弹窗。
     *
     * @param title   标题
     * @param message 正文
     */
    public static PrintDialog info(String title, String message) {
        return new PrintDialog(Kind.INFO, title, message);
    }

    /**
     * 配置不完整提示。
     *
     * @param title   标题
     * @param message 正文
     */
    public static PrintDialog needConfig(String title, String message) {
        return new PrintDialog(Kind.NEED_CONFIG, title, message);
    }

    /**
     * 打印成功弹窗。
     *
     * @param title   标题
     * @param message 服务端返回的提示
     */
    public static PrintDialog success(String title, String message) {
        return new PrintDialog(Kind.SUCCESS, title, message);
    }

    /**
     * 打印失败弹窗。
     *
     * @param title   标题
     * @param message 失败原因
     */
    public static PrintDialog error(String title, String message) {
        return new PrintDialog(Kind.ERROR, title, message);
    }

    /** @return 弹窗类型 */
    public Kind getKind() {
        return kind;
    }

    /** @return 标题 */
    public String getTitle() {
        return title;
    }

    /** @return 正文 */
    public String getMessage() {
        return message;
    }
}
