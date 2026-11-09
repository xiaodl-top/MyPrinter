package top.xiaodl.myprinter.ui;

import android.net.Uri;

/**
 * 用户即将打印的文件。
 *
 * <p>与 {@code IncomingFile} 的区别：这里表示「已经进入打印页、正在被配置与提交」的文件状态，
 * 仅由 {@link PrintViewModel} 持有。
 */
public final class PrintSelection {

    /** 文件地址。 */
    private final Uri uri;

    /** 展示给用户的文件名。 */
    private final String name;

    /** 是否为图片。 */
    private final boolean image;

    /**
     * @param uri   文件地址
     * @param name  显示名
     * @param image 是否为图片
     */
    public PrintSelection(Uri uri, String name, boolean image) {
        this.uri = uri;
        this.name = name;
        this.image = image;
    }

    /** @return 文件地址 */
    public Uri getUri() {
        return uri;
    }

    /** @return 展示给用户的文件名 */
    public String getName() {
        return name;
    }

    /** @return 是否为图片 */
    public boolean isImage() {
        return image;
    }
}
