"""Canvas 32×32 pour pixel art natif (UnpredictableBlocks)."""
from __future__ import annotations

from PIL import Image, ImageDraw


CANVAS = 32


def rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip("#")
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4))


def new_canvas() -> Image.Image:
    return Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))


class Draw32:
    """ImageDraw avec coordonnées logiques 16×16 → pixels 32×32."""

    def __init__(self, draw: ImageDraw.ImageDraw, scale: int = 2):
        self._d = draw
        self._s = scale

    def _x(self, v: int) -> int:
        return v * self._s

    def _box(self, x1: int, y1: int, x2: int, y2: int) -> tuple[int, int, int, int]:
        s = self._s
        return (x1 * s, y1 * s, (x2 + 1) * s - 1, (y2 + 1) * s - 1)

    def rectangle(self, xy: tuple[int, int, int, int], **kwargs) -> None:
        self._d.rectangle(self._box(*xy), **kwargs)

    def line(self, xy: tuple[int, int, int, int], **kwargs) -> None:
        s = self._s
        x1, y1, x2, y2 = xy
        self._d.line((x1 * s, y1 * s, x2 * s, y2 * s), **kwargs)

    def ellipse(self, xy: tuple[int, int, int, int], **kwargs) -> None:
        self._d.ellipse(self._box(*xy), **kwargs)

    def polygon(self, xy: list[tuple[int, int]], **kwargs) -> None:
        s = self._s
        self._d.polygon([(x * s, y * s) for x, y in xy], **kwargs)

    def point(self, xy: tuple[int, int], fill) -> None:
        x, y = xy
        self._d.point((x * self._s, y * self._s), fill=fill)

    def text(self, xy: tuple[int, int], text: str, fill) -> None:
        x, y = xy
        self._d.text((x * self._s, y * self._s), text, fill=fill)

    @property
    def raw(self) -> ImageDraw.ImageDraw:
        return self._d


def qmark(d: Draw32, color: tuple[int, int, int]) -> None:
    d.rectangle((6, 3, 9, 4), fill=color)
    d.rectangle((8, 4, 9, 6), fill=color)
    d.rectangle((7, 7, 8, 8), fill=color)
    d.rectangle((7, 10, 8, 11), fill=color)


def lucky_frame(d: Draw32, edge: tuple[int, int, int], highlight: tuple[int, int, int]) -> None:
    """Cadre Lucky Block commun (32×32 natif)."""
    d.raw.rectangle((0, 0, 31, 31), outline=edge, width=1)
    d.raw.rectangle((1, 1, 30, 30), outline=highlight, width=1)
    d.raw.line((2, 2, 28, 2), fill=highlight)
    d.raw.line((2, 2, 2, 28), fill=highlight)
    d.raw.line((29, 29, 3, 29), fill=edge)
    d.raw.line((29, 29, 29, 3), fill=edge)


def shade_bottom(img: Image.Image, strength: int = 28) -> None:
    px = img.load()
    for y in range(22, 32):
        for x in range(32):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            px[x, y] = (
                max(0, r - strength),
                max(0, g - strength),
                max(0, b - strength),
                a,
            )


def sparkle(img: Image.Image, points: list[tuple[int, int]], color: tuple[int, int, int, int]) -> None:
    px = img.load()
    for x, y in points:
        if 0 <= x < 32 and 0 <= y < 32:
            px[x, y] = color
