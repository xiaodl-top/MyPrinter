package top.xiaodl.myprinter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * 一个刻意保持极简的 ContentProvider，只用于把应用私有的相机缓存文件交给系统相机写入。
 *
 * <p>相机应用没有权限直接写本应用缓存目录，通过本 Provider 暴露 {@code /camera/<文件名>.jpg}
 * 这一个受控路径，就能拍出全分辨率照片，而无需申请存储权限。
 */
public final class CameraFileProvider extends ContentProvider {

    /** 缓存目录下的相机子目录，Provider 只允许访问这里。 */
    private File cameraDirectory;

    @Override
    public boolean onCreate() {
        if (getContext() == null) {
            return false;
        }
        cameraDirectory = new File(getContext().getCacheDir(), "camera");
        return cameraDirectory.exists() || cameraDirectory.mkdirs();
    }

    @Override
    public String getType(Uri uri) {
        // 相机输出固定为 JPEG。
        return "image/jpeg";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        // 只接受读、读写、写三种模式，避免 "rwt" 之类的特殊模式。
        if (!"r".equals(mode) && !"rw".equals(mode) && !"w".equals(mode)) {
            throw new FileNotFoundException("Unsupported access mode");
        }
        File image = resolveCameraFile(uri);
        int flags = "r".equals(mode)
                ? ParcelFileDescriptor.MODE_READ_ONLY
                : ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
        return ParcelFileDescriptor.open(image, flags);
    }

    /**
     * 把 URI 解析为缓存目录中的文件，并做严格的路径校验。
     *
     * <p>只接受 {@code camera/<name>.jpg} 两段路径，且规范化后必须仍位于缓存目录内，
     * 防止 {@code ../} 之类的路径穿越。
     *
     * @param uri 相机传入的 content URI
     * @return 缓存目录中的目标文件
     * @throws FileNotFoundException 路径非法或越界时抛出
     */
    private File resolveCameraFile(Uri uri) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        if (segments.size() != 2 || !"camera".equals(segments.get(0))) {
            throw new FileNotFoundException("Unknown camera file");
        }
        String name = segments.get(1);
        if (!name.endsWith(".jpg") || !name.equals(new File(name).getName())) {
            throw new FileNotFoundException("Invalid camera file name");
        }
        try {
            File root = cameraDirectory.getCanonicalFile();
            File image = new File(root, name).getCanonicalFile();
            if (!image.getPath().startsWith(root.getPath() + File.separator)) {
                throw new FileNotFoundException("Camera file is outside of cache");
            }
            return image;
        } catch (IOException exception) {
            throw new FileNotFoundException(exception.getMessage());
        }
    }

    /** 本 Provider 不支持查询，返回 {@code null}。 */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    /** 本 Provider 不支持插入，返回 {@code null}。 */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    /** 本 Provider 不支持删除，返回 0。 */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /** 本 Provider 不支持更新，返回 0。 */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
