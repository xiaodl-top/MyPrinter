#!/usr/bin/env python3
"""生成 MyPrinter 的启动图标。

启动图标与应用内标题栏的标识一致：品牌渐变（天蓝 -> 靛蓝 -> 紫罗兰）
配上白色打印机字形。旧版图标（API 24/25）在这里渲染成 WebP；自适应图标的两层
则放在 res/drawable 中，用矢量绘制，任何密度下都保持清晰。

用法：
    python tools/generate_icons.py
"""

from __future__ import annotations

import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
PREVIEW = os.path.join(ROOT, "screenshots")

# 与 ui/Theme.kt 中 brandGradient() 相同的三个色标。
STOPS = [
    (0.00, (0x2F, 0x6B, 0xFF)),
    (0.55, (0x5B, 0x4B, 0xFF)),
    (1.00, (0x9A, 0x5C, 0xFF)),
]

MASTER = 1024
SUPERSAMPLE = 4
# 旧版图标各密度：目录 -> 像素尺寸（48dp .. 192dp）。
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def gradient(size: int) -> np.ndarray:
    """生成 135° 对角方向的三段渐变，返回 RGB 浮点数组。"""
    axis = np.linspace(0.0, 1.0, size)
    t = (axis[None, :] + axis[:, None]) / 2.0
    channels = [
        np.interp(t, [stop for stop, _ in STOPS], [color[channel] for _, color in STOPS])
        for channel in range(3)
    ]
    return np.stack(channels, axis=-1)


def add_highlight(rgb: np.ndarray) -> np.ndarray:
    """左上角的柔光，与首页打印机卡片的光效一致。"""
    size = rgb.shape[0]
    axis = np.linspace(0.0, 1.0, size)
    dx = axis[None, :] - 0.16
    dy = axis[:, None] - 0.10
    distance = np.sqrt(dx * dx + dy * dy) / 0.95
    alpha = np.clip(1.0 - distance, 0.0, 1.0) ** 2 * 0.12
    alpha = alpha[:, :, None]
    return rgb * (1.0 - alpha) + 255.0 * alpha


def glyph_mask(size: int, ink_ratio: float) -> Image.Image:
    """Material「print」字形的透明度蒙版，使用 24x24 坐标系。

    字形与应用内的 drawable/ic_print.xml 相同：上方出纸、顶部圆角的机身、
    前方出纸，以及两个镂空（出纸口与状态灯）。
    """
    big = size * SUPERSAMPLE
    mask = Image.new("L", (big, big), 0)
    draw = ImageDraw.Draw(mask)
    # 墨迹宽 20 个单位（x 2..22）；居中放置，让标识在视觉上保持居中。
    scale = ink_ratio * big / 20.0
    offset = big / 2.0 - 12.0 * scale

    def x(value: float) -> float:
        return offset + value * scale

    def y(value: float) -> float:
        return offset + value * scale

    draw.rectangle([x(6), y(3), x(18), y(7)], fill=255)
    draw.rounded_rectangle(
        [x(2), y(8), x(22), y(17)],
        radius=3.0 * scale,
        fill=255,
        corners=(True, True, False, False),
    )
    draw.rectangle([x(6), y(17), x(18), y(21)], fill=255)
    # 镂空：出纸口与状态灯。
    draw.rectangle([x(8), y(14), x(16), y(19)], fill=0)
    draw.ellipse([x(18), y(10), x(20), y(12)], fill=0)
    return mask.resize((size, size), Image.LANCZOS)


def render(size: int, circular: bool, ink_ratio: float, shape_alpha: bool = True) -> Image.Image:
    rgb = add_highlight(gradient(MASTER)).round().clip(0, 255).astype(np.uint8)
    icon = Image.fromarray(rgb, "RGB").convert("RGBA")

    if shape_alpha:
        big = MASTER * SUPERSAMPLE
        shape = Image.new("L", (big, big), 0)
        shape_draw = ImageDraw.Draw(shape)
        if circular:
            shape_draw.ellipse([0, 0, big - 1, big - 1], fill=255)
        else:
            radius = int(0.22 * big)
            shape_draw.rounded_rectangle([0, 0, big - 1, big - 1], radius=radius, fill=255)
        icon.putalpha(shape.resize((MASTER, MASTER), Image.LANCZOS))

    white = Image.new("RGBA", (MASTER, MASTER), (255, 255, 255, 0))
    white.putalpha(glyph_mask(MASTER, ink_ratio))
    return Image.alpha_composite(icon, white).resize((size, size), Image.LANCZOS)


def save_webp(image: Image.Image, folder: str, name: str) -> None:
    directory = os.path.join(RES, folder)
    os.makedirs(directory, exist_ok=True)
    path = os.path.join(directory, name + ".webp")
    image.save(path, "WEBP", lossless=True, quality=100, method=6)
    print(f"  {folder}/{name}.webp  {image.width}x{image.height}")


def main() -> None:
    os.makedirs(PREVIEW, exist_ok=True)

    for density, size in DENSITIES.items():
        save_webp(render(size, circular=False, ink_ratio=0.52), f"mipmap-{density}", "ic_launcher")
        save_webp(render(size, circular=True, ink_ratio=0.50), f"mipmap-{density}", "ic_launcher_round")

    # 供自查的预览图（不随应用发布）。
    render(512, circular=False, ink_ratio=0.52).save(
        os.path.join(PREVIEW, "icon-rounded-square.png")
    )
    render(512, circular=True, ink_ratio=0.50).save(os.path.join(PREVIEW, "icon-circle.png"))

    # 自适应图标预览：108dp 画布，字形占 48/108，并按 72dp 圆形裁切。
    canvas = render(432, circular=False, ink_ratio=48.0 / 108.0, shape_alpha=False)
    mask = Image.new("L", (432, 432), 0)
    ImageDraw.Draw(mask).ellipse([24, 24, 408, 408], fill=255)
    preview = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    preview.paste(canvas, (0, 0), mask)
    preview.save(os.path.join(PREVIEW, "icon-adaptive-preview.png"))
    print("previews written to screenshots/")


if __name__ == "__main__":
    main()
