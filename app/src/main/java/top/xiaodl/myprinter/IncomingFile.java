package top.xiaodl.myprinter;

import android.net.Uri;

/**
 * 由其它应用交给本应用打印的文件。
 *
 * <p>例如从微信、钉钉或文件管理器「分享到 / 打开方式」进入应用时，由 {@link FileIntake}
 * 解析出的 URI、显示名与类型。
 */
public final class IncomingFile {

    /** 文件的 content:// 或 file:// 地址。 */
    private final Uri uri;

    /** 展示给用户的文件名。 */
    private final String name;

    /** 是否为图片（图片可以直接预览，且不支持双面与页码）。 */
    private final boolean image;

    /**
     * @param uri   文件地址
     * @param name  显示名
     * @param image 是否为图片
     */
    public IncomingFile(Uri uri, String name, boolean image) {
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
