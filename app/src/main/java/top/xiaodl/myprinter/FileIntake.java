package top.xiaodl.myprinter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Size;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 外部世界进入应用的桥梁：解析「打开方式 / 分享」里的文件（微信、钉钉、文件管理器、浏览器等），
 * 并提供打印流程需要的少量 URI 辅助方法。
 */
public final class FileIntake {

    /** 预览图的边长上限（像素），超过时等比缩放，避免大图占用过多内存。 */
    private static final int PREVIEW_MAX_SIZE = 1080;

    /** 可识别的图片扩展名，用于在 MIME 类型不可靠时兜底判断。 */
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "bmp", "webp", "gif", "heic", "heif", "tif", "tiff"
    ));

    private FileIntake() {
        // 工具类，禁止实例化。
    }

    /**
     * 解析 Intent 中携带的可打印文件。
     *
     * <p>支持 {@code ACTION_VIEW}（打开方式）、{@code ACTION_SEND} 与 {@code ACTION_SEND_MULTIPLE}
     * （分享，取第一个文件），且只接受 {@code content://} 与 {@code file://}；
     * {@code http(s)://} 链接不是可打印文件，会被忽略。
     *
     * @param context 上下文
     * @param intent  进入应用的 Intent
     * @return 解析出的文件；不是文件传递时返回 {@code null}
     */
    public static IncomingFile resolve(Context context, Intent intent) {
        if (intent == null) {
            return null;
        }
        String action = intent.getAction();
        if (!Intent.ACTION_VIEW.equals(action)
                && !Intent.ACTION_SEND.equals(action)
                && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            return null;
        }
        Uri uri = firstUri(intent);
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"content".equals(scheme) && !"file".equals(scheme)) {
            // 网络链接（http/https）不是可打印文件，按设计忽略。
            AppLog.warn("忽略非文件链接", "scheme=" + scheme + ", uri=" + uri);
            return null;
        }
        String name = displayName(context, uri);
        return new IncomingFile(uri, name, isImage(context, intent, uri, name));
    }

    /**
     * 尽可能持久化 URI 的读权限；发送方不允许时静默忽略。
     *
     * <p>部分应用只授予一次性读权限，此时立即读取仍然可用，只是重启后需要重新获取。
     *
     * @param context 上下文
     * @param intent  来源 Intent，可为 {@code null}
     * @param uri     目标文件
     */
    @SuppressLint("WrongConstant")
    public static void persistReadPermission(Context context, Intent intent, Uri uri) {
        int flags = intent == null
                ? Intent.FLAG_GRANT_READ_URI_PERMISSION
                : intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            context.getContentResolver().takePersistableUriPermission(uri, flags);
        } catch (Exception ignored) {
            // 很多发送方只给一次性授权，当前这次读取仍然有效，无需处理。
        }
    }

    /**
     * 读取文件的显示名。
     *
     * <p>优先查询 {@link OpenableColumns#DISPLAY_NAME}，失败或为空时退回 URI 的最后一段；
     * 仍为空则使用「已选择的文件」。
     *
     * @param context 上下文
     * @param uri     文件地址
     * @return 展示给用户的文件名
     */
    public static String displayName(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (column >= 0) {
                    String name = cursor.getString(column);
                    if (name != null && !name.trim().isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
            // 查询失败时走下面的兜底逻辑。
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        String segment = uri.getLastPathSegment();
        return segment == null || segment.isEmpty() ? "已选择的文件" : segment;
    }

    /**
     * 生成 multipart 中 {@code file} 字段使用的文件名。
     *
     * <p>打印服务依据扩展名选择文档转换器，因此当 Provider 给出的名字没有扩展名时，
     * 依次尝试从 URI 路径与 Content-Type 推断并补全。
     *
     * @param context     上下文
     * @param uri         文件地址
     * @param displayName 原始显示名
     * @return 带扩展名的上传文件名
     */
    public static String uploadFileName(Context context, Uri uri, String displayName) {
        if (!extensionOf(displayName).isEmpty()) {
            return displayName;
        }
        String segment = uri.getLastPathSegment();
        String fromUri = segment == null ? "" : extensionOf(segment);
        if (!fromUri.isEmpty()) {
            return displayName + "." + fromUri;
        }
        String mimeType = null;
        try {
            mimeType = context.getContentResolver().getType(uri);
        } catch (Exception ignored) {
            // 取不到类型时使用原始显示名。
        }
        String fromMimeType = mimeType == null
                ? null
                : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        return fromMimeType == null || fromMimeType.isEmpty()
                ? displayName
                : displayName + "." + fromMimeType;
    }

    /**
     * 一次性读取文件全部字节，用于上传。
     *
     * @param context 上下文
     * @param uri     文件地址
     * @return 文件内容
     * @throws IOException 无法打开或读取文件时抛出
     */
    public static byte[] readAllBytes(Context context, Uri uri) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        if (input == null) {
            throw new IOException("无法读取所选文件");
        }
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[16 * 1024];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            byte[] bytes = output.toByteArray();
            AppLog.event("读取文件完成", "大小=" + bytes.length + " 字节");
            return bytes;
        }
    }

    /**
     * 解码一张等比缩小的预览图，避免大照片拖慢绘制。
     *
     * <p>不同 Provider、不同图片格式的兼容性差异很大，因此按「能力最强 → 兼容性最好」的顺序
     * 依次尝试：系统缩略图（API 29+）、ImageDecoder（API 28+）、BitmapFactory。
     *
     * @param context 上下文
     * @param uri     图片地址
     * @return 解码结果；全部失败时返回 {@code null}
     */
    public static Bitmap decodePreview(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && "content".equals(uri.getScheme())) {
            Bitmap thumbnail = thumbnailOrNull(context, uri);
            if (thumbnail != null) {
                AppLog.event("图片预览解码完成",
                        "方式=系统缩略图, " + thumbnail.getWidth() + "x" + thumbnail.getHeight());
                return thumbnail;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Bitmap decoded = decoderBitmapOrNull(context, uri);
            if (decoded != null) {
                AppLog.event("图片预览解码完成",
                        "方式=ImageDecoder, " + decoded.getWidth() + "x" + decoded.getHeight());
                return decoded;
            }
        }
        Bitmap bitmap = factoryBitmapOrNull(context, uri);
        if (bitmap == null) {
            AppLog.warn("图片预览解码失败", "uri=" + uri);
        } else {
            AppLog.event("图片预览解码完成",
                    "方式=BitmapFactory, " + bitmap.getWidth() + "x" + bitmap.getHeight());
        }
        return bitmap;
    }

    /**
     * 创建系统相机的拍照输出目标（缓存文件 + content URI）。
     *
     * @param context 上下文
     * @return 拍照输出目标
     */
    public static CameraTarget newCameraTarget(Context context) {
        File directory = new File(context.getCacheDir(), "camera");
        if (!directory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        File file = new File(directory, "myprinter_" + System.currentTimeMillis() + ".jpg");
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(context.getPackageName() + ".camera-fileprovider")
                .appendPath("camera")
                .appendPath(file.getName())
                .build();
        AppLog.event("创建拍照输出文件", "文件=" + file.getName());
        return new CameraTarget(file, uri);
    }

    /**
     * 从 Intent 中取出第一个文件 URI：依次尝试 data、ClipData 与 {@code EXTRA_STREAM}。
     *
     * @param intent 来源 Intent
     * @return 第一个 URI，找不到时返回 {@code null}
     */
    private static Uri firstUri(Intent intent) {
        if (intent.getData() != null) {
            return intent.getData();
        }
        if (intent.getClipData() != null) {
            for (int index = 0; index < intent.getClipData().getItemCount(); index++) {
                Uri uri = intent.getClipData().getItemAt(index).getUri();
                if (uri != null) {
                    return uri;
                }
            }
        }
        Object extra = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        return extra instanceof Uri ? (Uri) extra : null;
    }

    /**
     * 判断文件是否为图片：优先看 Intent 类型，其次看 Content-Type，最后看扩展名。
     *
     * @param context 上下文
     * @param intent  来源 Intent
     * @param uri     文件地址
     * @param name    文件名
     * @return 是图片时返回 {@code true}
     */
    private static boolean isImage(Context context, Intent intent, Uri uri, String name) {
        String type = intent.getType();
        if (type != null && type.startsWith("image/")) {
            return true;
        }
        String resolved = null;
        try {
            resolved = context.getContentResolver().getType(uri);
        } catch (Exception ignored) {
            // 取不到类型时按扩展名判断。
        }
        if (resolved != null && resolved.startsWith("image/")) {
            return true;
        }
        String extension = extensionOf(name);
        return !extension.isEmpty() && IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 取小写扩展名（不含点）。
     *
     * @param value 文件路径或名称
     * @return 扩展名；没有扩展名时返回空字符串
     */
    private static String extensionOf(String value) {
        if (value == null) {
            return "";
        }
        int dot = value.lastIndexOf('.');
        if (dot < 0 || dot == value.length() - 1) {
            return "";
        }
        return value.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 用系统缩略图接口解码，仅 API 29+ 可用。
     *
     * @param context 上下文
     * @param uri     图片地址
     * @return 缩略图；失败时返回 {@code null}
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private static Bitmap thumbnailOrNull(Context context, Uri uri) {
        try {
            return context.getContentResolver().loadThumbnail(
                    uri, new Size(PREVIEW_MAX_SIZE, PREVIEW_MAX_SIZE), null);
        } catch (Exception error) {
            return null;
        }
    }

    /**
     * 用 {@link ImageDecoder} 解码并按长边限制尺寸，仅 API 28+ 可用。
     *
     * @param context 上下文
     * @param uri     图片地址
     * @return 解码结果；失败时返回 {@code null}
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private static Bitmap decoderBitmapOrNull(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ImageDecoder.Source source = ImageDecoder.createSource(resolver, uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, ignored) -> {
                // 强制软件解码，保证 Bitmap 可安全用于绘制。
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                int width = info.getSize().getWidth();
                int height = info.getSize().getHeight();
                int longestSide = Math.max(width, height);
                if (longestSide > PREVIEW_MAX_SIZE) {
                    float scale = (float) PREVIEW_MAX_SIZE / longestSide;
                    decoder.setTargetSize(
                            Math.max(1, (int) (width * scale)),
                            Math.max(1, (int) (height * scale)));
                }
            });
        } catch (Exception error) {
            return null;
        }
    }

    /**
     * 用 {@link BitmapFactory} 解码：先读边界，再用 2 的幂采样率缩放，兼容性最好。
     *
     * @param context 上下文
     * @param uri     图片地址
     * @return 解码结果；失败时返回 {@code null}
     */
    private static Bitmap factoryBitmapOrNull(Context context, Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream boundsStream = context.getContentResolver().openInputStream(uri);
            if (boundsStream != null) {
                try (InputStream stream = boundsStream) {
                    // 这一趟只填充宽高信息，返回的 Bitmap 始终为 null。
                    BitmapFactory.decodeStream(stream, null, bounds);
                }
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }
            int sampleSize = 1;
            while (bounds.outWidth / sampleSize > PREVIEW_MAX_SIZE
                    || bounds.outHeight / sampleSize > PREVIEW_MAX_SIZE) {
                sampleSize *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            InputStream stream = context.getContentResolver().openInputStream(uri);
            if (stream == null) {
                return null;
            }
            try (InputStream input = stream) {
                return BitmapFactory.decodeStream(input, null, options);
            }
        } catch (Exception error) {
            return null;
        }
    }
}
