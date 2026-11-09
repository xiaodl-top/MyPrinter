package top.xiaodl.myprinter;

import androidx.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 把文件上传到 OnePrinter 打印服务。
 *
 * <p>请求为 POST 到设置页配置的打印服务地址，使用 multipart/form-data，字段包括
 * {@code file}、{@code code}、{@code printer}、{@code copies}、{@code pageRange}、
 * {@code paperSize}、{@code duplex}、{@code layout} 与 {@code color}。
 * 服务成功时返回 {@code {"code":1,"info":"..."}}，其中 {@code info} 就是要展示给用户的文案。
 */
public final class PrintApi {

    /** 无法从扩展名推断时使用的兜底 MIME 类型。 */
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /** 扩展名 → MIME 类型映射，供 multipart 的 file 部分使用。 */
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }

    /** 全局共享的 HTTP 客户端：连接 15 秒、读写 60 秒，允许连接失败重试。 */
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private PrintApi() {
        // 工具类，禁止实例化。
    }

    /**
     * 把文件作为 multipart 的 file 部分上传，并请求打印。阻塞调用，必须在后台线程执行。
     *
     * @param config    打印配置（服务地址、口令、打印机、纸张、彩色）
     * @param fileName  上传文件名（保留扩展名，服务端据此选择转换器）
     * @param content   文件内容
     * @param copies    份数
     * @param pageRange 页码，空字符串表示全部
     * @param duplex    是否双面
     * @param layout    是否横版
     * @return 服务端返回结果
     * @throws IOException  网络错误或 HTTP 非 2xx
     * @throws JSONException 响应不是合法 JSON
     */
    public static PrintReply print(
            PrinterConfig config,
            String fileName,
            byte[] content,
            long copies,
            String pageRange,
            boolean duplex,
            boolean layout
    ) throws IOException, JSONException {
        MediaType mediaType = MediaType.parse(mediaTypeName(fileName));
        RequestBody fileBody = mediaType == null
                ? RequestBody.create(content, null)
                : RequestBody.create(content, mediaType);
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .addFormDataPart("code", config.getCode())
                .addFormDataPart("printer", config.getPrinter())
                .addFormDataPart("copies", Long.toString(copies))
                .addFormDataPart("pageRange", pageRange == null ? "" : pageRange)
                .addFormDataPart("paperSize", config.getPaperSize())
                .addFormDataPart("duplex", flag(duplex))
                .addFormDataPart("layout", flag(layout))
                .addFormDataPart("color", flag(config.getColor()))
                .build();

        long startedAt = System.currentTimeMillis();
        AppLog.event("发送打印请求",
                "URL=" + config.getApiUrl()
                        + ", 文件名=" + fileName
                        + ", 大小=" + (content.length / 1024) + "KB"
                        + ", 份数=" + copies
                        + ", 页码=" + (pageRange == null || pageRange.trim().isEmpty() ? "全部" : pageRange)
                        + ", 打印机=" + config.getPrinter()
                        + ", 纸张=" + config.getPaperSize()
                        + ", 双面=" + flag(duplex)
                        + ", 横版=" + flag(layout)
                        + ", 彩色=" + flag(config.getColor()));

        Request request = new Request.Builder()
                .url(config.getApiUrl())
                .header("Accept", "application/json")
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            String text = response.body() == null ? "" : response.body().string();
            AppLog.event("收到打印服务响应",
                    "HTTP=" + response.code()
                            + ", 耗时=" + (System.currentTimeMillis() - startedAt) + "ms"
                            + ", 响应长度=" + text.length() + "字符");
            if (!response.isSuccessful()) {
                AppLog.warn("打印服务返回错误状态", "HTTP=" + response.code() + ", 响应=" + snippet(text));
                throw new IOException("打印服务返回 HTTP " + response.code() + describe(text));
            }
            return parseReply(text);
        }
    }

    /**
     * 解析服务端响应：{@code code == 1} 视为成功，否则返回失败并带上提示文本。
     *
     * @param response 响应正文
     * @return 解析结果
     * @throws JSONException 响应不是合法 JSON
     */
    private static PrintReply parseReply(String response) throws JSONException {
        if (response == null || response.trim().isEmpty()) {
            AppLog.warn("打印服务返回空响应");
            return new PrintReply(false, "打印服务未返回结果。");
        }
        JSONObject root;
        try {
            root = new JSONObject(response);
        } catch (JSONException error) {
            AppLog.failure("解析打印服务响应失败", error, "响应=" + snippet(response));
            throw error;
        }
        int code = root.optInt("code", 0);
        String message = findMessage(root);
        if (code == 1) {
            return new PrintReply(true, message.isEmpty() ? "打印服务已接收请求。" : message);
        }
        if (!message.isEmpty()) {
            AppLog.warn("打印服务返回失败代码", "code=" + code + ", 提示=" + message);
            return new PrintReply(false, message);
        }
        AppLog.warn("打印服务返回失败代码", "code=" + code + ", 响应=" + snippet(response));
        return new PrintReply(false, "打印服务拒绝了请求（错误代码 " + code + "）。");
    }

    /**
     * 取出面向用户的提示文本：优先 {@code info}，兼容 {@code message} / {@code msg}；
     * 顶层没有时再查 {@code data} 对象。
     *
     * @param root 响应根对象
     * @return 提示文本，可能为空字符串
     */
    private static String findMessage(JSONObject root) {
        String direct = firstNotEmpty(
                root.optString("info", ""),
                root.optString("message", ""),
                root.optString("msg", ""));
        if (!direct.isEmpty()) {
            return direct;
        }
        JSONObject data = root.optJSONObject("data");
        if (data == null) {
            return "";
        }
        return firstNotEmpty(
                data.optString("info", ""),
                data.optString("message", ""),
                data.optString("msg", ""));
    }

    /** 返回第一个非空白字符串，全为空时返回空字符串。 */
    private static String firstNotEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    /**
     * 根据文件名扩展名推断 MIME 类型。
     *
     * @param fileName 文件名
     * @return MIME 类型；未知扩展名时返回 {@link #DEFAULT_MIME_TYPE}
     */
    private static String mediaTypeName(String fileName) {
        String extension = "";
        int dot = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            extension = fileName.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
        }
        String mimeType = MIME_TYPES.get(extension);
        return mimeType == null ? DEFAULT_MIME_TYPE : mimeType;
    }

    /** 布尔值转协议要求的 {@code "1"} / {@code "0"}。 */
    private static String flag(boolean value) {
        return value ? "1" : "0";
    }

    /** 截断响应文本，避免日志过长。 */
    private static String snippet(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "";
        }
        String compact = response.trim().replaceAll("\\s+", " ");
        return compact.length() > 160 ? compact.substring(0, 160) + "…" : compact;
    }

    /** 把响应片段包装成「：片段」，没有内容时返回空字符串。 */
    private static String describe(String response) {
        String compact = snippet(response);
        return compact.isEmpty() ? "" : "：" + compact;
    }

    /** 一次打印请求的结果：{@code message} 为服务端返回的提示文本。 */
    public static final class PrintReply {

        /** 是否成功。 */
        private final boolean success;

        /** 服务端提示文本。 */
        private final String message;

        /**
         * @param success 是否成功
         * @param message 服务端提示文本
         */
        public PrintReply(boolean success, @NonNull String message) {
            this.success = success;
            this.message = message;
        }

        /** @return 是否成功 */
        public boolean isSuccess() {
            return success;
        }

        /** @return 服务端提示文本 */
        public String getMessage() {
            return message;
        }
    }
}
