#!/usr/bin/env python3
"""Generates the MyPrinter launcher icons.

The launcher icon is the same mark as the in-app header logo: the brand gradient
(azure -> indigo -> violet) with the white printer glyph. Legacy icons (API 24/25)
are rendered here as WebP; the adaptive icon layers are plain vector drawables in
res/drawable, so they stay crisp on every density.

Usage:
    python tools/generate_icons.py
"""

from __future__ import annotations

import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
PREVIEW = os.path.join(ROOT, "screenshots")

# Same stops as brandGradient() in ui/Theme.kt.
STOPS = [
    (0.00, (0x2F, 0x6B, 0xFF)),
    (0.55, (0x5B, 0x4B, 0xFF)),
    (1.00, (0x9A, 0x5C, 0xFF)),
]

MASTER = 1024
SUPERSAMPLE = 4
# Legacy icon densities: folder -> size in px (48dp .. 192dp).
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def gradient(size: int) -> np.ndarray:
    """Diagonal (135 deg) three-stop gradient as an RGB float array."""
    axis = np.linspace(0.0, 1.0, size)
    t = (axis[None, :] + axis[:, None]) / 2.0
    channels = [
        np.interp(t, [stop for stop, _ in STOPS], [color[channel] for _, color in STOPS])
        for channel in range(3)
    ]
    return np.stack(channels, axis=-1)


def add_highlight(rgb: np.ndarray) -> np.ndarray:
    """Soft light source in the upper-left corner, matching the hero card."""
    size = rgb.shape[0]
    axis = np.linspace(0.0, 1.0, size)
    dx = axis[None, :] - 0.16
    dy = axis[:, None] - 0.10
    distance = np.sqrt(dx * dx + dy * dy) / 0.95
    alpha = np.clip(1.0 - distance, 0.0, 1.0) ** 2 * 0.12
    alpha = alpha[:, :, None]
    return rgb * (1.0 - alpha) + 255.0 * alpha


def glyph_mask(size: int, ink_ratio: float) -> Image.Image:
    """Alpha mask of the Material 'print' glyph, in 24x24 coordinates.

    The glyph is the same path as drawable/ic_print.xml: a top sheet, a body with
    rounded top corners, a front sheet, plus the two cut-outs (paper slot and LED).
    """
    big = size * SUPERSAMPLE
    mask = Image.new("L", (big, big), 0)
    draw = ImageDraw.Draw(mask)
    # Ink is 20 units wide (x 2..22); centre it so the mark sits optically centred.
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
    # Cut-outs: the paper slot and the status LED.
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

    # Reference renders for review (not shipped).
    render(512, circular=False, ink_ratio=0.52).save(
        os.path.join(PREVIEW, "icon-rounded-square.png")
    )
    render(512, circular=True, ink_ratio=0.50).save(os.path.join(PREVIEW, "icon-circle.png"))

    # Adaptive icon preview: 108dp canvas, glyph at 48/108, masked to the 72dp circle.
    canvas = render(432, circular=False, ink_ratio=48.0 / 108.0, shape_alpha=False)
    mask = Image.new("L", (432, 432), 0)
    ImageDraw.Draw(mask).ellipse([24, 24, 408, 408], fill=255)
    preview = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    preview.paste(canvas, (0, 0), mask)
    preview.save(os.path.join(PREVIEW, "icon-adaptive-preview.png"))
    print("previews written to screenshots/")


if __name__ == "__main__":
    main()
