package top.xiaodl.myprinter;

import android.net.Uri;

import java.io.File;

/**
 * 系统相机的输出目标：一个应用私有缓存文件，以及相机可写入的 content URI。
 *
 * <p>相机应用没有权限直接写应用缓存目录，因此由 {@link CameraFileProvider} 通过
 * {@link #getUri()} 暴露一个可写地址；拍照完成后应用再回读 {@link #getFile()}。
 */
public final class CameraTarget {

    /** 应用缓存目录中的目标文件。 */
    private final File file;

    /** 交给相机应用的 content URI。 */
    private final Uri uri;

    /**
     * @param file 缓存目录中的目标文件
     * @param uri  相机可写入的 content URI
     */
    public CameraTarget(File file, Uri uri) {
        this.file = file;
        this.uri = uri;
    }

    /** @return 缓存目录中的目标文件 */
    public File getFile() {
        return file;
    }

    /** @return 相机可写入的 content URI */
    public Uri getUri() {
        return uri;
    }
}
