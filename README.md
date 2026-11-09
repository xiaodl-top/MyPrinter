# MyPrinter · 我的打印机

OnePrinter 云打印服务的 Android 客户端。选一张图片或一份文档，设置份数与版式，一键发送到打印服务；
其它 App "分享到" / "打开方式" 送进来的文件也能直接打印。

| | |
|---|---|
| 应用 ID / 包名 | `top.xiaodl.myprinter` |
| 版本 | v1.1（versionCode 2） |
| 最低系统 | Android 7.0（API 24） |
| 目标系统 | Android 15（API 35） |
| 技术栈 | Kotlin + Jetpack Compose（Material 3），少量 Java（Activity、网络与文件层） |


---

## 功能

### 选择文件

- **图片**：调用系统相册选择器（Photo Picker），或直接打开系统相机拍一张。
- **文档**：PDF、Word（doc/docx）、PowerPoint（ppt/pptx）、Excel（xls/xlsx）等。
- **外部文件**：支持 `ACTION_VIEW`（打开方式）、`ACTION_SEND` / `ACTION_SEND_MULTIPLE`（分享），
  从微信、钉钉、文件管理器等直接送入应用；界面会标注"来自其他应用"。
- 支持格式提示：`pdf、jpg、jpeg、png、gif、webp、bmp、cad、dwg、doc、docx、ppt、pptx、xls、xlsx`。

### 打印设置

- **份数**：1–999，输入自动过滤非数字。
- **横版打印**：`layout=1`。
- **双面打印**：`duplex=1`，仅文档可选（图片固定单面）。
- **指定页码**：如 `1-5,8,10-12`，留空表示全部，仅文档可选。

### 打印配置

设置页可修改并保存在本机：

| 配置项 | 说明 |
| --- | --- |
| 打印服务 API | 上传接口地址，如 `http://host:port/upload/single` |
| 验证口令 | 发送给打印服务的口令 |
| 打印机名称 | 打印服务端的打印机标识 |
| 纸张大小 | 如 `A4` |
| 彩色打印 | 关闭时 `color=0` |

配置缺失（API / 打印机名称 / 纸张为空）时点击打印会提示并引导前往设置页。

---

## 构建

环境要求：

- JDK 17
- Android SDK Platform 35 + Build-Tools 35
- Gradle 9.0-milestone-1（随 wrapper 提供，首次会下载）

```bash
# 调试包
./gradlew :app:assembleDebug
# 发布包（使用工程内的 yshaw.jks 签名配置）
./gradlew :app:assembleRelease
# 静态检查
./gradlew :app:lintDebug
```

产物路径（文件名带版本与构建时间）：

```
app/build/outputs/apk/debug/MyPrinter-v1.1-2-debug-<yyyyMMddHHmm>.apk
app/build/outputs/apk/release/MyPrinter-v1.1-2-release-<yyyyMMddHHmm>.apk
```

### 私有默认值（不提交）

`DEFAULT_API_URL`、`DEFAULT_PRINTER`、`DEFAULT_CODE` **不写在源码里**，也不会进入仓库。
构建时按以下优先级读取，并注入到 `BuildConfig`：

1. Gradle 属性：`-PDEFAULT_API_URL=...`，或用户级 `~/.gradle/gradle.properties`
2. 项目根目录的 `local.properties`（`.gitignore` 已忽略）
3. 同名环境变量

把 [`local.properties.example`](local.properties.example) 复制为 `local.properties` 后填写：

```properties
sdk.dir=D\:\\Android\\Sdk
DEFAULT_API_URL=http://192.168.1.10:8080/upload/single
DEFAULT_PRINTER=Office-Printer
DEFAULT_CODE=your-access-code
```

缺少这些值时工程仍可正常编译，`BuildConfig` 中为**空字符串**，App 首次启动会提示前往设置页填写。
注入结果可查看：

```
app/build/generated/source/buildConfig/debug/top/xiaodl/myprinter/BuildConfig.java
```

> 注意：默认值会被编译进 APK（App 运行时需要它们发送请求），因此请不要把发布包分发给不可信的人；
> 仓库本身不含这些值。

安装与启动：

```bash
adb install -r app/build/outputs/apk/debug/MyPrinter-v1.1-2-debug-*.apk
adb shell am start -n top.xiaodl.myprinter/.MainActivity
```

日志（统一 TAG `MyPrinter`，覆盖每一步用户操作与请求结果）：

```bash
adb logcat -s MyPrinter
```

## 打印服务协议

客户端向设置页中的 **打印服务 API** 发送 `POST`，`multipart/form-data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `file` | 文件 | 文件本体，文件名保留扩展名（服务端据此选择转换器） |
| `code` | 文本 | 验证口令 |
| `printer` | 文本 | 打印机名称 |
| `copies` | 文本 | 份数 |
| `pageRange` | 文本 | 页码，空字符串表示全部 |
| `paperSize` | 文本 | 纸张大小 |
| `duplex` | `1` / `0` | 双面 |
| `layout` | `1` / `0` | 横版 |
| `color` | `1` / `0` | 彩色 |

成功响应示例：

```json
{ "code": 1, "info": "打印任务已提交" }
```

`code == 1` 视为成功；`info` / `message` / `msg`，以及嵌套在 `data` 内的同名字段都会被展示给用户。
`code != 1` 或 HTTP 非 2xx 时，把服务端文本原样提示给用户。

---

## 配置存储

使用 `SharedPreferences`（文件名 `oneprinter_settings`）保存在本机，键名：
`api_url`、`printer`、`paper_size`、`code`、`color`（`1` / `0`）。

其中的初始默认值来自 [`ConfigStore.java`](app/src/main/java/top/xiaodl/myprinter/ConfigStore.java)：
服务地址、打印机名称、验证口令三项由构建期注入的 `BuildConfig` 提供（见「私有默认值（不提交）」），
纸张大小固定为 `A4`。首次安装后可在设置页修改，修改结果只保存到本机。

---

## 项目结构

```
app/src/main/
├── AndroidManifest.xml             权限、入口 Activity、外部文件 Intent 过滤器、相机 <queries>
├── java/top/xiaodl/myprinter/
│   ├── MainActivity.java           入口：接收/选择文件，承载主界面
│   ├── SettingsActivity.java       设置页宿主，负责保存与页面转场
│   ├── PrintApi.java               OkHttp multipart 上传与响应解析
│   ├── FileIntake.java             Intent/URI 解析、文件名、预览解码、相机输出
│   ├── ConfigStore.java            配置读写
│   ├── PrinterConfig.java          配置快照（不可变）
│   ├── IncomingFile.java           外部来源文件
│   ├── CameraTarget.java           相机输出文件 + content URI
│   ├── CameraFileProvider.java     仅暴露相机缓存目录的 ContentProvider
│   ├── AppLog.java                 统一日志
│   └── ui/
│       ├── ComposeBridge.kt        Java ↔ Compose 的桥接（状态、页面、系统栏）
│       ├── Theme.kt                设计令牌：配色、字体、形状、品牌渐变
│       ├── PrintScreen.kt          主界面
│       ├── SettingsScreen.kt       设置界面
│       ├── PrintViewModel.java     打印页状态 + 上传任务
│       ├── PrintDialog.java        弹窗模型（信息 / 未配置 / 成功 / 失败）
│       ├── PrintSelection.java     待打印文件
│       ├── SettingsSaveListener.java
│       └── components/             设计系统：动效、背景、卡片、输入、弹层、标题栏
└── res/
    ├── anim/                       设置页进入/退出转场
    ├── drawable/                   矢量图标 + 自适应图标图层
    ├── mipmap-*/                   旧版启动图标（WebP，API 24/25）
    └── values*/                    主题、颜色、字符串（浅色 / 深色）
```

---

## 设计与动效

- **视觉**：`azure → indigo → violet` 品牌渐变 + 青绿点缀，26–30dp 大圆角、分层阴影、
  极细描边；浅色与深色各一套令牌，跟随系统夜间模式。
- **背景**：两团缓慢漂移的环境光晕（约 42 秒一圈），只触发绘制阶段，不引起重组。
- **动效**：
  - 页面内容自上而下错峰淡入上浮；
  - 所有可点击元素带弹簧按压缩放，主按钮另有缓速流光与触感反馈；
  - 主题卡与主按钮的高光缓慢扫过；
  - 份数步进器、渐变开关（果冻回弹）；
  - 图片预览按原图比例自适应高度并淡入；
  - 图片/文档切换用 `AnimatedContent` 交叉淡入；
  - 底部弹层滑入、可下拉关闭；弹窗缩放进场；
  - 主页与设置页之间的滑动 + 淡入淡出窗口转场（Android 14+ 使用
    `overrideActivityTransition`，更低版本回退到 `overridePendingTransition`）。
- **系统栏**：全面屏（edge-to-edge），图标颜色跟随深浅色主题。

---

## 权限与隐私

- 仅申请 `INTERNET`；读取文件走 SAF / Photo Picker / 自有 `ContentProvider`，**不需要**存储权限。
- 允许明文 HTTP（`usesCleartextTraffic="true"`），因为打印服务常部署在局域网内。
- 配置只保存在本机 `SharedPreferences`，日志不会输出文件内容或验证口令。

---

## 应用图标

- 自适应图标（API 26+）：`@drawable/ic_launcher_background`（品牌渐变）+ `@drawable/ic_launcher_foreground`
  （白色打印机字形），并提供 `monochrome` 图层以支持系统主题图标。
- 旧版图标（API 24/25）：`mipmap-*/ic_launcher.webp` 与 `mipmap-*/ic_launcher_round.webp`。
- 重新生成位图图标（需要 Python 3 + Pillow）：

```bash
python tools/generate_icons.py
```

---

## 版本

| 版本 | 说明 |
| --- | --- |
| v1.1 | 应用 ID 迁移为 `top.xiaodl.myprinter`；UI 全量重绘（新设计系统、动效、深浅色）；新应用图标 |
| v1.0 | 首个版本：图片/文档打印、配置管理、外部文件接收 |
