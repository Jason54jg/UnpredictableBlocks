#!/usr/bin/env python3
"""
Génère les 31 Lucky Blocks prêts Blockbench (Java Block).

Livrables dans docs/blockbench/ :
  lucky_<id>.bbmodel
  lucky_<id>.png          (spritesheet 32 × 32×N)
  lucky_<id>.png.mcmeta   (animation texture)

Usage:
  python tools/generate_blockbench.py
  python tools/generate_blockbench.py anachronisme boulangerie
"""
from __future__ import annotations

import base64
import io
import json
import math
import sys
import uuid
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "blockbench"
SIZE = 32


# ---------------------------------------------------------------------------
# Utils
# ---------------------------------------------------------------------------

def uid() -> str:
    return str(uuid.uuid4())


def rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


def rgba(h: str, a: int = 255) -> tuple[int, int, int, int]:
    r, g, b = rgb(h)
    return r, g, b, a


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def lerp_rgb(c1: tuple[int, int, int], c2: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return (
        int(lerp(c1[0], c2[0], t)),
        int(lerp(c1[1], c2[1], t)),
        int(lerp(c1[2], c2[2], t)),
    )


def clamp(v: int, lo: int = 0, hi: int = 31) -> int:
    return max(lo, min(hi, v))


def new_frame(fill: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGBA", (SIZE, SIZE), fill + (255,))
    return img


def put(img: Image.Image, x: int, y: int, c: tuple[int, int, int] | tuple[int, int, int, int]) -> None:
    if 0 <= x < SIZE and 0 <= y < SIZE:
        if len(c) == 3:
            img.putpixel((x, y), c + (255,))
        else:
            img.putpixel((x, y), c)


def fill_rect(img: Image.Image, x1: int, y1: int, x2: int, y2: int, c) -> None:
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            put(img, x, y, c)


def hline(img: Image.Image, x1: int, x2: int, y: int, c) -> None:
    for x in range(x1, x2 + 1):
        put(img, x, y, c)


def vline(img: Image.Image, x: int, y1: int, y2: int, c) -> None:
    for y in range(y1, y2 + 1):
        put(img, x, y, c)


def ellipse_fill(img: Image.Image, cx: int, cy: int, rx: int, ry: int, c) -> None:
    for y in range(cy - ry, cy + ry + 1):
        for x in range(cx - rx, cx + rx + 1):
            if rx == 0 or ry == 0:
                continue
            if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0:
                put(img, x, y, c)


def ellipse_outline(img: Image.Image, cx: int, cy: int, rx: int, ry: int, c) -> None:
    for y in range(cy - ry, cy + ry + 1):
        for x in range(cx - rx, cx + rx + 1):
            if rx == 0 or ry == 0:
                continue
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if 0.55 <= d <= 1.15:
                put(img, x, y, c)


def draw_qmark(img: Image.Image, color: tuple[int, int, int], ox: int = 0, oy: int = 0) -> None:
    """Point d'interrogation Lucky Block centré (~colonnes 13–18)."""
    pts = [
        (14, 6), (15, 6), (16, 6), (17, 6),
        (17, 7), (18, 7), (18, 8), (18, 9),
        (17, 10), (16, 10), (15, 11), (15, 12),
        (15, 13), (15, 14),
        (15, 17), (15, 18), (16, 17), (16, 18),
    ]
    for x, y in pts:
        put(img, x + ox, y + oy, color)


def lucky_frame(img: Image.Image, edge: tuple[int, int, int], hi: tuple[int, int, int]) -> None:
    """Cadre Lucky Block stable (ne bouge pas entre frames)."""
    for i in range(SIZE):
        put(img, i, 0, edge)
        put(img, i, SIZE - 1, edge)
        put(img, 0, i, edge)
        put(img, SIZE - 1, i, edge)
    for i in range(1, SIZE - 1):
        put(img, i, 1, hi)
        put(img, 1, i, hi)
    for i in range(2, SIZE - 2):
        put(img, i, SIZE - 2, edge)
        put(img, SIZE - 2, i, edge)


def shade_bottom(img: Image.Image, strength: int = 24) -> None:
    px = img.load()
    for y in range(22, SIZE):
        for x in range(SIZE):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            f = (y - 22) / 10
            s = int(strength * f)
            px[x, y] = (max(0, r - s), max(0, g - s), max(0, b - s), a)


def stack_frames(frames: list[Image.Image]) -> Image.Image:
    sheet = Image.new("RGBA", (SIZE, SIZE * len(frames)), (0, 0, 0, 0))
    for i, fr in enumerate(frames):
        sheet.paste(fr, (0, i * SIZE))
    return sheet


def png_b64(img: Image.Image) -> str:
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("ascii")


# ---------------------------------------------------------------------------
# Theme spec
# ---------------------------------------------------------------------------

@dataclass
class Theme:
    tid: str
    title: str
    concept: str
    edge: str
    hi: str
    bg: str
    frames: int = 8
    frametime: int = 2
    interpolate: bool = False
    geom: str = "none"  # none | d20 | float | paper | bat
    length: float = 1.2
    draw: Callable[[Image.Image, int, int], None] = field(repr=False, default=lambda *_: None)


# ---------------------------------------------------------------------------
# Frame drawers (t = frame index, n = total frames)
# ---------------------------------------------------------------------------

def draw_anachronisme(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 15, 29, rgb("B87333"))
    fill_rect(img, 16, 2, 29, 29, rgb("0A1028"))
    # sablier
    sand_y = 10 + (t % n)
    ellipse_outline(img, 9, 12, 5, 7, rgb("5C3317"))
    fill_rect(img, 7, 12, 11, 12, rgb("5C3317"))
    for y in range(13, min(19, sand_y + 1)):
        hline(img, 8, 10, y, rgb("CD7F32"))
    for y in range(max(13, 20 - (t % 4)), 19):
        hline(img, 8, 10, y, rgb("E8C872"))
    # circuits neon pulse
    pulse = 0.4 + 0.6 * abs(math.sin(t / n * math.pi * 2))
    neon = lerp_rgb(rgb("004466"), rgb("00FFCC"), pulse)
    for i, y in enumerate([6, 10, 14, 18, 22]):
        hline(img, 18, 28, y, neon if (i + t) % 2 == 0 else rgb("003344"))
        put(img, 20 + (t + i) % 7, y + 1, rgb("FFD700"))
    draw_qmark(img, rgb("FFD700"))


def draw_boulangerie(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("E8D5A8"))
    for x in range(4, 28, 5):
        vline(img, x, 3, 28, rgb("C8A040"))
    # baguette
    fill_rect(img, 6, 18, 25, 22, rgb("D4A574"))
    hline(img, 7, 24, 18, rgb("F5E6C8"))
    for x in (9, 14, 19):
        put(img, x, 20, rgb("A07040"))
    # croissant
    ellipse_fill(img, 10, 12, 5, 3, rgb("E8B86D"))
    ellipse_fill(img, 16, 11, 4, 3, rgb("F0C878"))
    # vapeur montante
    for i in range(3):
        sx = 12 + i * 5
        sy = 8 - ((t + i * 2) % 6)
        if sy > 2:
            ellipse_fill(img, sx, sy, 1, 2, rgba("FFFFFF", 140))
    draw_qmark(img, rgb("8B4513"), oy=-1)


def draw_bsod(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("0000AA"))
    # scanlines
    for y in range(3, 29, 2):
        if (y // 2 + t) % 3 != 0:
            hline(img, 3, 28, y, rgb("000088"))
    # sad face :(
    put(img, 11, 10, rgb("FFFFFF"))
    put(img, 12, 10, rgb("FFFFFF"))
    put(img, 18, 10, rgb("FFFFFF"))
    put(img, 19, 10, rgb("FFFFFF"))
    for x in range(12, 19):
        put(img, x, 16 + (1 if 13 <= x <= 17 else 0), rgb("FFFFFF"))
    # code lines glitch
    for i, y in enumerate([20, 22, 24, 26]):
        w = 8 + ((t * 3 + i * 5) % 12)
        hline(img, 5, 5 + w, y, rgb("AAAAFF") if (t + i) % 2 else rgb("FFFFFF"))
    # curseur blink
    if t % 2 == 0:
        fill_rect(img, 6, 27, 8, 28, rgb("FFFFFF"))
    # glitch tear
    if t % 4 == 0:
        hline(img, 3, 28, 14, rgb("00FFFF"))
        hline(img, 3, 28, 15, rgb("FF00FF"))


def draw_volaille(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("FFF8E7"))
    # corps poule
    ellipse_fill(img, 16, 16, 8, 7, rgb("FFFFFF"))
    ellipse_fill(img, 22, 12, 4, 4, rgb("FFFFFF"))
    # crête
    fill_rect(img, 20, 6, 24, 9, rgb("CC0000"))
    put(img, 22, 5, rgb("FF2222"))
    # bec
    fill_rect(img, 25, 12, 28, 14, rgb("FFCC00"))
    # œil cligne
    if t % n < n - 1:
        put(img, 23, 11, rgb("222222"))
    else:
        hline(img, 22, 24, 11, rgb("222222"))
    # plume flottante
    px = 6 + (t % 4)
    py = 8 + int(2 * math.sin(t))
    fill_rect(img, px, py, px + 2, py + 4, rgb("FFE0A0"))
    put(img, px + 1, py - 1, rgb("FFFFFF"))
    draw_qmark(img, rgb("CC0000"), ox=-4, oy=4)


def draw_impots(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("9E9E9E"))
    # dossier
    fill_rect(img, 6, 6, 25, 26, rgb("ECEFF1"))
    fill_rect(img, 6, 6, 25, 9, rgb("FFC107"))
    for y in (12, 15, 18, 21):
        hline(img, 9, 22, y, rgb("B0BEC5"))
    # tampon qui tape
    phase = t / max(1, n - 1)
    if phase < 0.35:
        ty = 8 + int(phase / 0.35 * 8)
    elif phase < 0.55:
        ty = 16
    else:
        ty = 16 - int((phase - 0.55) / 0.45 * 6)
    stamp = rgb("C62828") if phase < 0.6 else rgba("C62828", 200)
    fill_rect(img, 10, ty, 21, ty + 5, stamp)
    # X stamp mark when down
    if 0.35 <= phase <= 0.7:
        for i in range(5):
            put(img, 12 + i, ty + 1 + i, rgb("FFFFFF"))
            put(img, 19 - i, ty + 1 + i, rgb("FFFFFF"))


def draw_streamer(img: Image.Image, t: int, n: int) -> None:
    for y in range(2, 30):
        tt = (y - 2) / 27
        c = lerp_rgb(rgb("E040FB"), rgb("7B1FA2"), tt)
        hline(img, 2, 29, y, c)
    # caméra
    fill_rect(img, 8, 12, 20, 22, rgb("212121"))
    ellipse_fill(img, 14, 17, 4, 4, rgb("455A64"))
    ellipse_fill(img, 14, 17, 2, 2, rgb("00E5FF"))
    fill_rect(img, 20, 15, 24, 19, rgb("37474F"))
    # LIVE blink
    if t % 2 == 0:
        fill_rect(img, 9, 6, 22, 10, rgb("FF1744"))
        for x, bit in enumerate([1, 1, 0, 1, 1]):  # crude L I V E dots
            if bit:
                put(img, 11 + x * 2, 8, rgb("FFFFFF"))
    # confettis
    cols = [rgb("FFEB3B"), rgb("00E676"), rgb("FF4081"), rgb("40C4FF")]
    for i in range(6):
        put(img, 4 + (t * 3 + i * 7) % 24, 4 + (t + i * 5) % 22, cols[i % 4])


def draw_proprete(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("E0F7FA"))
    # éponge
    fill_rect(img, 18, 18, 27, 26, rgb("FFD54F"))
    for y in range(19, 26, 2):
        hline(img, 19, 26, y, rgb("FFECB3"))
    # bulles montantes
    bubbles = [(8, 22), (12, 18), (6, 14), (14, 10), (10, 6), (16, 20)]
    for i, (bx, by) in enumerate(bubbles):
        yy = by - ((t + i * 2) % 18)
        if 3 < yy < 28:
            r = 1 + (i % 3)
            ellipse_outline(img, bx, yy, r, r, rgb("4DD0E1"))
            put(img, bx - 1, yy - 1, rgb("FFFFFF"))
    # shine
    if t % 3 == 0:
        put(img, 22, 16, rgb("FFFFFF"))
        put(img, 24, 15, rgb("FFFFFF"))


def draw_gravite_zero(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("050510"))
    stars = [(5, 5), (10, 8), (22, 6), (26, 12), (8, 20), (18, 24), (14, 4), (28, 22)]
    for i, (sx, sy) in enumerate(stars):
        on = (t + i) % 3 != 0
        put(img, sx, sy, rgb("FFFFFF") if on else rgb("6688AA"))
        if on and i % 2 == 0:
            put(img, sx + 1, sy, rgb("AACCFF"))
    # astronaute float
    ay = 14 + int(2 * math.sin(t / n * math.pi * 2))
    ellipse_fill(img, 16, ay, 5, 5, rgb("ECEFF1"))
    ellipse_fill(img, 16, ay, 3, 3, rgb("81D4FA"))
    fill_rect(img, 14, ay + 5, 18, ay + 10, rgb("CFD8DC"))
    put(img, 12, ay + 6, rgb("FF5252"))
    put(img, 20, ay + 6, rgb("FF5252"))


def draw_fast_food(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("FFEB3B"))
    # burger layers
    fill_rect(img, 7, 10, 24, 13, rgb("F4A460"))
    fill_rect(img, 8, 13, 23, 15, rgb("8BC34A"))
    fill_rect(img, 7, 15, 24, 18, rgb("BF360C"))
    fill_rect(img, 8, 18, 23, 20, rgb("FFF59D"))
    fill_rect(img, 7, 20, 24, 23, rgb("D2691E"))
    # sesame sparkle
    for i, (sx, sy) in enumerate([(10, 11), (14, 10), (18, 11), (21, 12)]):
        if (t + i) % 2 == 0:
            put(img, sx, sy, rgb("FFFFFF"))
    # vapeur
    for i in range(3):
        sx = 12 + i * 4
        sy = 7 - ((t + i) % 5)
        if sy > 2:
            put(img, sx, sy, rgba("FFFFFF", 160))


def draw_cauchemar_matin(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("37474F"))
    # réveil
    shake = (t % 2) * 1 - (1 - t % 2) * 1 if t % 4 < 2 else 0
    cx = 16 + shake
    ellipse_fill(img, cx, 16, 9, 9, rgb("CFD8DC"))
    ellipse_fill(img, cx, 16, 7, 7, rgb("ECEFF1"))
    # aiguilles
    vline(img, cx, 10, 16, rgb("212121"))
    hline(img, cx, cx + 5, 16, rgb("C62828"))
    # pieds
    fill_rect(img, cx - 6, 24, cx - 4, 27, rgb("78909C"))
    fill_rect(img, cx + 4, 24, cx + 6, 27, rgb("78909C"))
    # flash alarme
    if t % 2 == 0:
        fill_rect(img, 3, 3, 5, 5, rgb("FF1744"))
        fill_rect(img, 26, 3, 28, 5, rgb("FF1744"))
        for x in range(3, 29):
            put(img, x, 2, rgb("FF5252"))


def draw_film_action(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("1A1A1A"))
    # clap
    fill_rect(img, 6, 8, 25, 14, rgb("FAFAFA"))
    fill_rect(img, 6, 8, 25, 10, rgb("FF6D00"))
    for x in range(8, 24, 3):
        vline(img, x, 8, 10, rgb("212121"))
    fill_rect(img, 6, 14, 25, 22, rgb("37474F"))
    # boom pulse + sparks
    pulse = abs(math.sin(t / n * math.pi * 2))
    boom = lerp_rgb(rgb("FF6D00"), rgb("FFEB3B"), pulse)
    ellipse_fill(img, 16, 24, 3 + int(pulse * 3), 2 + int(pulse * 2), boom)
    sparks = [(8, 18), (24, 17), (12, 25), (20, 26), (5, 12), (26, 13)]
    for i, (sx, sy) in enumerate(sparks):
        if (t + i) % 3 == 0:
            put(img, sx, sy, rgb("FFEB3B"))
            put(img, sx + 1, sy - 1, rgb("FF6D00"))


def draw_jdr_d20(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("4A148C"))
    # D20 facets (rotation via shifting highlight)
    rot = t % 6
    verts = [(16, 5), (25, 11), (22, 23), (10, 23), (7, 11)]
    # rotate indices
    vv = verts[rot:] + verts[:rot]
    # fill approx
    for y in range(5, 24):
        for x in range(7, 26):
            if abs(x - 16) + abs(y - 14) < 12:
                put(img, x, y, rgb("7B1FA2"))
    # edges
    for i in range(len(vv)):
        x1, y1 = vv[i]
        x2, y2 = vv[(i + 1) % len(vv)]
        steps = max(abs(x2 - x1), abs(y2 - y1), 1)
        for s in range(steps + 1):
            put(img, x1 + (x2 - x1) * s // steps, y1 + (y2 - y1) * s // steps, rgb("FFD700"))
    # number / crit glow
    glow = t >= n - 2
    num_c = rgb("00E676") if glow else rgb("FFFFFF")
    fill_rect(img, 14, 13, 17, 16, num_c)
    if glow:
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            put(img, 15 + dx, 14 + dy, rgba("00E676", 180))


def draw_penurie_2020(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("BBDEFB"))
    # caddie
    fill_rect(img, 8, 12, 24, 22, rgb("90A4AE"))
    hline(img, 8, 24, 12, rgb("546E7A"))
    vline(img, 8, 8, 12, rgb("546E7A"))
    hline(img, 6, 8, 8, rgb("546E7A"))
    ellipse_fill(img, 11, 24, 2, 2, rgb("37474F"))
    ellipse_fill(img, 21, 24, 2, 2, rgb("37474F"))
    # PQ rolls
    ellipse_fill(img, 13, 16, 3, 4, rgb("FFF8E1"))
    ellipse_fill(img, 19, 16, 3, 4, rgb("FFF8E1"))
    put(img, 13, 16, rgb("E0E0E0"))
    put(img, 19, 16, rgb("E0E0E0"))
    # étiquette prix flicker
    if t % 2 == 0:
        fill_rect(img, 10, 6, 21, 10, rgb("FFEB3B"))
        hline(img, 12, 19, 8, rgb("C62828"))
    else:
        fill_rect(img, 10, 6, 21, 10, rgb("FFCDD2"))
        hline(img, 12, 19, 8, rgb("B71C1C"))


def draw_complot(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("263238"))
    # alu top bar
    fill_rect(img, 3, 3, 28, 7, rgb("B0BEC5"))
    for x in range(4, 28, 2):
        put(img, x, 5, rgb("78909C"))
    # pyramide
    for y in range(10, 26):
        w = (y - 10) + 1
        hline(img, 16 - w, 16 + w, y, rgb("455A64"))
        put(img, 16 - w, y, rgb("FFD700"))
        put(img, 16 + w, y, rgb("FFD700"))
    hline(img, 16 - 16, 16 + 16, 25, rgb("FFD700"))
    # œil
    ellipse_fill(img, 16, 16, 4, 3, rgb("FFEB3B"))
    blink = t % n >= n - 1
    if blink:
        hline(img, 13, 19, 16, rgb("C62828"))
    else:
        ellipse_fill(img, 16, 16, 2, 2, rgb("C62828"))
        put(img, 16, 16, rgb("212121"))
    # static
    if t % 3 == 0:
        for i in range(8):
            put(img, 4 + (t * 5 + i * 3) % 24, 8 + (i * 2) % 18, rgb("90A4AE"))


def draw_lag_bug(img: Image.Image, t: int, n: int) -> None:
    # missing texture checker
    for y in range(SIZE):
        for x in range(SIZE):
            if ((x // 4) + (y // 4)) % 2 == 0:
                put(img, x, y, rgb("FF00FF"))
            else:
                put(img, x, y, rgb("000000"))
    lucky_frame(img, rgb("00FF00"), rgb("88FF88"))
    # stutter offset tear
    tear = (t % 4) * 2
    for y in range(10, 20):
        for x in range(8, 24):
            src = img.getpixel(((x + tear) % SIZE, y))
            put(img, x, y + (1 if t % 2 else 0), src[:3])
    # green glitch bars
    if t % 2 == 0:
        hline(img, 3, 28, 12, rgb("00FF00"))
        hline(img, 5, 20, 18, rgb("00FF00"))
    return  # frame already applied


def draw_disco_80(img: Image.Image, t: int, n: int) -> None:
    cols = [rgb("FF00FF"), rgb("00FFFF"), rgb("FFD700"), rgb("FF1744"), rgb("76FF03")]
    bg = cols[t % len(cols)]
    fill_rect(img, 2, 2, 29, 29, lerp_rgb(rgb("120018"), bg, 0.25))
    # boule disco
    ellipse_fill(img, 16, 14, 8, 8, rgb("CFD8DC"))
    for i in range(16):
        ang = (i / 16 + t / n) * math.pi * 2
        px = 16 + int(math.cos(ang) * 6)
        py = 14 + int(math.sin(ang) * 6)
        put(img, px, py, cols[(i + t) % len(cols)])
    # sparkle rays
    for i in range(4):
        ang = (t / n + i / 4) * math.pi * 2
        for r in range(9, 14):
            put(img, 16 + int(math.cos(ang) * r), 14 + int(math.sin(ang) * r), cols[i])


def draw_plomberie(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("607D8B"))
    # pipes
    fill_rect(img, 6, 8, 10, 26, rgb("90A4AE"))
    fill_rect(img, 10, 8, 22, 12, rgb("90A4AE"))
    fill_rect(img, 20, 8, 24, 20, rgb("78909C"))
    # clé à molette
    fill_rect(img, 12, 18, 26, 21, rgb("FFC107"))
    fill_rect(img, 24, 15, 27, 24, rgb("FFA000"))
    # gouttes drip
    for i in range(3):
        dx = 8 + i * 6
        dy = 14 + ((t + i * 3) % 12)
        if dy < 28:
            ellipse_fill(img, dx, dy, 1, 2, rgb("29B6F6"))
            put(img, dx, dy - 2, rgb("81D4FA"))


def draw_fortune_cookie(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("FFF3E0"))
    # cookie
    ellipse_fill(img, 16, 18, 10, 7, rgb("E0A040"))
    ellipse_fill(img, 16, 18, 8, 5, rgb("F0C878"))
    # papier sort/rentre
    phase = t / max(1, n - 1)
    if phase < 0.5:
        plen = int(phase * 2 * 10)
    else:
        plen = int((1 - phase) * 2 * 10)
    if plen > 0:
        fill_rect(img, 15, 18 - plen, 17, 18, rgb("FFEBEE"))
        hline(img, 15, 17, 18 - plen, rgb("C62828"))
        if plen > 3:
            put(img, 16, 18 - plen + 2, rgb("C62828"))


def draw_camping(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("3E4A28"))
    # tente
    for y in range(8, 24):
        w = y - 8
        hline(img, 16 - w, 16 + w, y, rgb("6B8E23"))
        put(img, 16 - w, y, rgb("556B2F"))
        put(img, 16 + w, y, rgb("556B2F"))
    fill_rect(img, 14, 18, 18, 24, rgb("3E2723"))
    # feu flicker
    fx, fy = 8, 24
    flame = [rgb("FF6D00"), rgb("FFAB00"), rgb("FFEB3B"), rgb("FF3D00")]
    for i in range(4):
        h = 2 + ((t + i) % 3)
        fill_rect(img, fx + i, fy - h, fx + i, fy, flame[(t + i) % 4])
    put(img, fx + 1, fy + 1, rgb("5D4037"))
    put(img, fx + 2, fy + 1, rgb("5D4037"))


def draw_gym_bro(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("212121"))
    # haltère bounce
    by = 14 + (0 if t % 4 < 2 else -2)
    fill_rect(img, 6, by, 10, by + 6, rgb("78909C"))
    fill_rect(img, 21, by, 25, by + 6, rgb("78909C"))
    fill_rect(img, 10, by + 2, 21, by + 4, rgb("B0BEC5"))
    # shaker
    fill_rect(img, 13, 22, 18, 28, rgb("C62828"))
    fill_rect(img, 14, 20, 17, 22, rgb("ECEFF1"))
    # pulse
    if t % 2 == 0:
        hline(img, 4, 27, 4, rgb("FF1744"))
        hline(img, 4, 27, 28, rgb("FF1744"))


def draw_art_moderne(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("FAFAFA"))
    # cadre
    for i in range(3, 29):
        put(img, i, 3, rgb("212121"))
        put(img, i, 28, rgb("212121"))
        put(img, 3, i, rgb("212121"))
        put(img, 28, i, rgb("212121"))
    shapes = [
        (rgb("FF1744"), 6, 6, 14, 14),
        (rgb("00E5FF"), 14, 8, 24, 16),
        (rgb("76FF03"), 8, 16, 18, 25),
        (rgb("FFEA00"), 16, 18, 26, 26),
    ]
    # morph: swap sizes with frame
    for i, (c, x1, y1, x2, y2) in enumerate(shapes):
        shift = (t + i) % 4
        fill_rect(img, x1 + shift, y1, x2 - (shift // 2), y2 - shift // 2, c)
    if t % 2:
        ellipse_fill(img, 12, 20, 3, 3, rgb("E040FB"))


def draw_magie_comptoir(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("1A0033"))
    # chapeau
    fill_rect(img, 8, 14, 23, 18, rgb("212121"))
    for y in range(6, 14):
        w = 4 + (y - 6)
        hline(img, 16 - w, 16 + w, y, rgb("111111"))
    fill_rect(img, 6, 18, 25, 20, rgb("212121"))
    put(img, 16, 8, rgb("C62828"))
    # cartes flip
    face = t % 4
    cols = [rgb("FAFAFA"), rgb("C62828"), rgb("FAFAFA"), rgb("FFD700")]
    fill_rect(img, 20, 20, 26, 27, cols[face])
    if face % 2 == 0:
        put(img, 23, 23, rgb("C62828"))
    # étincelles
    for i, (sx, sy) in enumerate([(7, 10), (24, 8), (10, 22), (18, 24), (5, 16)]):
        if (t + i) % 2 == 0:
            put(img, sx, sy, rgb("FFD700"))


def draw_invasion_memes(img: Image.Image, t: int, n: int) -> None:
    for y in range(2, 30):
        tt = (y + t * 2) % 28 / 27
        hline(img, 2, 29, y, lerp_rgb(rgb("FF00AA"), rgb("00E5FF"), tt))
    # troll-ish pixel face (original, not a logo)
    scale = 1 + (1 if t % 4 < 2 else 0)
    cx, cy = 16, 15
    fill_rect(img, cx - 6 * scale // 1, cy - 5, cx + 6, cy + 6, rgb("88CC44"))
    put(img, cx - 3, cy - 2, rgb("212121"))
    put(img, cx + 2, cy - 2, rgb("212121"))
    hline(img, cx - 2, cx + 2, cy + 2, rgb("212121"))
    # bounce pop
    if t % 3 == 0:
        ellipse_outline(img, cx, cy, 10, 9, rgb("FFEB3B"))
    # floating emoji-ish
    put(img, 6, 6 + (t % 3), rgb("FFEB3B"))
    put(img, 25, 8 - (t % 2), rgb("FF4081"))


def draw_pause_cafe(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("5D4037"))
    # tasse
    fill_rect(img, 9, 12, 22, 24, rgb("EFEBE9"))
    fill_rect(img, 11, 14, 20, 22, rgb("3E2723"))
    # anse
    fill_rect(img, 22, 15, 26, 20, rgb("EFEBE9"))
    fill_rect(img, 23, 16, 25, 19, rgb("5D4037"))
    # ripple surface
    phase = t % 4
    ellipse_outline(img, 15, 15, 3 + phase, 1, rgb("6D4C41"))
    # vapeur
    for i in range(3):
        sx = 12 + i * 4
        sy = 10 - ((t + i * 2) % 7)
        if sy > 3:
            ellipse_fill(img, sx, sy, 1, 2, rgba("D7CCC8", 160))


def draw_retard_vol(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("0277BD"))
    # panneau
    fill_rect(img, 4, 8, 27, 22, rgb("263238"))
    fill_rect(img, 5, 9, 26, 21, rgb("37474F"))
    # DELAYED blink
    if t % 2 == 0:
        fill_rect(img, 7, 12, 24, 18, rgb("FF6D00"))
        # D E L A Y rough pixels
        for x in range(9, 23, 2):
            put(img, x, 14, rgb("FFFFFF"))
            put(img, x, 16, rgb("FFFFFF"))
    else:
        fill_rect(img, 7, 12, 24, 18, rgb("455A64"))
    # avion tip
    fill_rect(img, 14, 4, 17, 7, rgb("ECEFF1"))
    put(img, 18, 5, rgb("ECEFF1"))


def draw_ananas_pizza(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("FFCC80"))
    # pizza triangle
    for y in range(8, 26):
        w = (y - 8) // 1
        hline(img, 16 - w // 2 - 4, 16 + w // 2 + 4, y, rgb("D84315"))
        hline(img, 16 - w // 2 - 3, 16 + w // 2 + 3, y, rgb("FFECB3"))
    # fromage blobs
    for x, y in [(12, 14), (18, 16), (14, 20), (17, 12)]:
        put(img, x, y, rgb("FFF59D"))
    # ananas bounce
    ay = 14 + (0 if t % 4 < 2 else -2)
    ellipse_fill(img, 16, ay, 3, 3, rgb("FFEB3B"))
    fill_rect(img, 15, ay - 4, 17, ay - 2, rgb("558B2F"))
    if t % 2 == 0:
        put(img, 20, 10, rgb("FFFFFF"))
        put(img, 11, 18, rgb("FFFFFF"))


def draw_speedrunner(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("111111"))
    # timer
    fill_rect(img, 6, 8, 25, 16, rgb("1B5E20"))
    fill_rect(img, 7, 9, 24, 15, rgb("00E676"))
    # ticking digits (blocks)
    tick = t % 4
    fill_rect(img, 9 + tick, 10, 11 + tick, 14, rgb("FFFFFF"))
    fill_rect(img, 15, 10, 17, 14, rgb("212121"))
    fill_rect(img, 19, 10, 21, 14, rgb("FFFFFF"))
    # perle
    ellipse_fill(img, 16, 22, 3, 3, rgb("81D4FA"))
    put(img, 15, 21, rgb("E1F5FE"))
    # speed lines
    for i in range(4):
        y = 18 + i * 2
        hline(img, 4, 4 + ((t + i) % 5) + 8, y, rgb("76FF03"))


def draw_piraterie_torrent(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("0D47A1"))
    # voile
    for y in range(6, 18):
        w = y - 5
        hline(img, 10, 10 + w, y, rgb("ECEFF1"))
    vline(img, 10, 6, 26, rgb("5D4037"))
    # download bar loop
    fill_rect(img, 6, 22, 25, 26, rgb("212121"))
    progress = int((t / n) * 18)
    fill_rect(img, 7, 23, 7 + progress, 25, rgb("00E676"))
    # arrow
    put(img, 16, 12, rgb("00E676"))
    put(img, 15, 13, rgb("00E676"))
    put(img, 17, 13, rgb("00E676"))
    put(img, 16, 14, rgb("00E676"))


def draw_asmr_fidget(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("F8BBD0"))
    # pop-it grid
    colors = [rgb("F48FB1"), rgb("CE93D8"), rgb("80DEEA"), rgb("FFF59D"), rgb("A5D6A7")]
    idx = 0
    for gy in range(4):
        for gx in range(4):
            cx = 7 + gx * 5
            cy = 7 + gy * 5
            popped = ((gy * 4 + gx) + t) % 8 < 3
            r = 1 if popped else 2
            ellipse_fill(img, cx, cy, r, r, colors[idx % len(colors)])
            if not popped:
                put(img, cx, cy, rgb("FFFFFF"))
            idx += 1


def draw_vampire_gothique(img: Image.Image, t: int, n: int) -> None:
    fill_rect(img, 2, 2, 29, 29, rgb("0D0015"))
    # chauve-souris flap
    wing = 1 if t % 2 == 0 else 3
    # body
    ellipse_fill(img, 16, 14, 3, 4, rgb("4A148C"))
    # wings
    for dx in range(-wing - 4, -2):
        put(img, 16 + dx, 14 + abs(dx + 3) // 2, rgb("6A1B9A"))
        put(img, 16 + dx, 13, rgb("4A148C"))
    for dx in range(3, wing + 5):
        put(img, 16 + dx, 14 + abs(dx - 3) // 2, rgb("6A1B9A"))
        put(img, 16 + dx, 13, rgb("4A148C"))
    put(img, 15, 12, rgb("FF1744"))
    put(img, 17, 12, rgb("FF1744"))
    # drip sang
    for i in range(3):
        dx = 12 + i * 4
        dy = 20 + ((t + i * 2) % 6)
        put(img, dx, dy, rgb("B71C1C"))
        put(img, dx, dy + 1, rgb("C62828"))


def draw_multivers_sliders(img: Image.Image, t: int, n: int) -> None:
    # color shift bg
    phase = t / n
    c1 = lerp_rgb(rgb("00E5FF"), rgb("FF00AA"), phase)
    c2 = lerp_rgb(rgb("FF00AA"), rgb("7C4DFF"), phase)
    for y in range(2, 30):
        hline(img, 2, 29, y, lerp_rgb(c1, c2, (y - 2) / 27))
    # portail rings
    for r in range(4, 12, 2):
        rr = r + (t % 3)
        ellipse_outline(img, 16, 16, rr, rr - 1, rgb("FFFFFF") if r % 4 == 0 else rgb("E1BEE7"))
    # sliders
    fill_rect(img, 6, 24, 25, 27, rgb("212121"))
    knobs = [6 + ((t + i * 3) % 14) for i in range(3)]
    for i, kx in enumerate(knobs):
        fill_rect(img, 7 + i * 6 + (t + i) % 4, 24, 9 + i * 6 + (t + i) % 4, 27, [rgb("00E5FF"), rgb("FF00AA"), rgb("FFD700")][i])


# skip frame for lag_bug which draws its own frame
SKIP_FRAME = {"lucky_lag_bug"}


THEMES: list[Theme] = [
    Theme("lucky_anachronisme", "Anachronisme", "Sablier bronze qui coule + pulse néon cyan", "5C3317", "FFD700", "B87333", geom="none", draw=draw_anachronisme),
    Theme("lucky_boulangerie", "Boulangerie Française", "Baguette/croissant + vapeur montante", "A08030", "FFE8B0", "E8D5A8", draw=draw_boulangerie),
    Theme("lucky_bsod", "BSOD", "Sad face + scanlines + curseur blink", "000088", "AAAAFF", "0000AA", frames=10, draw=draw_bsod),
    Theme("lucky_volaille", "Volaille Suprême", "Poule + plume flottante + clignement", "888888", "FFFFFF", "FFF8E7", draw=draw_volaille),
    Theme("lucky_impots", "Administration & Impôts", "Dossier + tampon qui tape", "666666", "FFEB3B", "9E9E9E", frames=10, draw=draw_impots),
    Theme("lucky_streamer", "Streamer / Clickbait", "Caméra + LIVE blink + confettis", "880088", "FFAAFF", "E040FB", draw=draw_streamer),
    Theme("lucky_proprete", "Propreté Extrême", "Éponge + bulles montantes + shine", "5A9FD4", "FFFFFF", "E0F7FA", draw=draw_proprete),
    Theme("lucky_gravite_zero", "Gravité Zéro", "Astronaute float + étoiles", "220044", "CCAAFF", "050510", geom="float", length=1.4, draw=draw_gravite_zero),
    Theme("lucky_fast_food", "Fast-Food", "Burger + vapeur + sesame sparkle", "C62828", "FFEB3B", "FFEB3B", draw=draw_fast_food),
    Theme("lucky_cauchemar_matin", "Cauchemar de Matinée", "Réveil alarme shake + flash", "1A0033", "FF6666", "37474F", frames=8, draw=draw_cauchemar_matin),
    Theme("lucky_film_action", "Film d'Action", "Clap + sparks + boom pulse", "000000", "FF6D00", "1A1A1A", draw=draw_film_action),
    Theme("lucky_jdr_d20", "JDR & D20", "D20 spin + glow crit", "6A1B9A", "FFD700", "4A148C", geom="d20", length=1.5, frames=12, draw=draw_jdr_d20),
    Theme("lucky_penurie_2020", "Pénurie de 2020", "PQ/caddie + étiquette prix flicker", "90A4AE", "FFFFFF", "BBDEFB", draw=draw_penurie_2020),
    Theme("lucky_complot", "Théorie du Complot", "Pyramide œil cligne + static", "B0BEC5", "FFD700", "263238", draw=draw_complot),
    Theme("lucky_lag_bug", "Lag & Bug", "Missing cube + stutter tear UV", "00FF00", "88FF88", "FF00FF", frames=8, frametime=1, draw=draw_lag_bug),
    Theme("lucky_disco_80", "Soirée Disco 80's", "Boule disco RGB + sparkle", "FF00FF", "00FFFF", "120018", frames=10, draw=draw_disco_80),
    Theme("lucky_plomberie", "Plomberie", "Clé + drip gouttes", "607D8B", "29B6F6", "607D8B", draw=draw_plomberie),
    Theme("lucky_fortune_cookie", "Fortune Cookie", "Papier sort/rentre", "C8A040", "FFF8DC", "FFF3E0", geom="paper", length=1.3, draw=draw_fortune_cookie),
    Theme("lucky_camping", "Camping Catastrophe", "Tente + flammes flicker", "3D4F1F", "FF8833", "3E4A28", draw=draw_camping),
    Theme("lucky_gym_bro", "Go Muscu", "Haltère bounce + pulse", "C62828", "B0BEC5", "212121", geom="none", draw=draw_gym_bro),
    Theme("lucky_art_moderne", "Art Moderne", "Formes qui morphent", "212121", "FFFFFF", "FAFAFA", frames=8, draw=draw_art_moderne),
    Theme("lucky_magie_comptoir", "Magie de Comptoir", "Chapeau + carte flip + étincelles", "0A0A20", "FFD700", "1A0033", draw=draw_magie_comptoir),
    Theme("lucky_invasion_memes", "Invasion de Mèmes", "Troll pixel bounce + pop zoom", "FF00AA", "00E5FF", "FF00AA", draw=draw_invasion_memes),
    Theme("lucky_pause_cafe", "Pause Café / Barista", "Tasse + vapeur + ripple", "3E2723", "D7CCC8", "5D4037", draw=draw_pause_cafe),
    Theme("lucky_retard_vol", "Retard de Vol / Aéroport", "Panneau DELAYED blink", "0277BD", "FF6D00", "0277BD", frames=6, frametime=3, draw=draw_retard_vol),
    Theme("lucky_ananas_pizza", "Ananas sur la Pizza", "Pizza + ananas bounce + shine", "E65100", "FFF9C4", "FFCC80", draw=draw_ananas_pizza),
    Theme("lucky_speedrunner", "Speedrunner Ultra-Tryhard", "Timer ticks + speed lines", "212121", "76FF03", "111111", draw=draw_speedrunner),
    Theme("lucky_piraterie_torrent", "Piraterie & Torrent", "Voile + barre download loop", "0D47A1", "00E676", "0D47A1", frames=10, draw=draw_piraterie_torrent),
    Theme("lucky_asmr_fidget", "ASMR & Fidget Toy", "Pop-it bubbles pop sequence", "F48FB1", "FFFFFF", "F8BBD0", frames=8, draw=draw_asmr_fidget),
    Theme("lucky_vampire_gothique", "Vampire & Gothique", "Chauve-souris flap + drip sang", "4A148C", "FF5252", "0D0015", geom="bat", length=1.2, draw=draw_vampire_gothique),
    Theme("lucky_multivers_sliders", "Multivers / Sliders", "Portail ripples + color shift", "311B92", "FF6E40", "00E5FF", frames=10, draw=draw_multivers_sliders),
]


# ---------------------------------------------------------------------------
# Blockbench model builder
# ---------------------------------------------------------------------------

def face_uv(tex: str = "0") -> dict:
    return {
        "north": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
        "east": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
        "south": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
        "west": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
        "up": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
        "down": {"uv": [0, 0, 32, 32], "texture": f"#{tex}"},
    }


def kf(channel: str, time: float, x, y, z, interp: str = "catmullrom") -> dict:
    return {
        "channel": channel,
        "data_points": [{"x": str(x), "y": str(y), "z": str(z)}],
        "uuid": uid(),
        "time": round(time, 4),
        "color": -1,
        "interpolation": interp,
    }


def build_idle_anim(theme: Theme, groups: dict[str, str]) -> dict:
    """Animation idle = envol flottant (boucle seamless), 8–10 keyframes."""
    length = max(theme.length, 1.6)
    animators: dict = {}

    # --- Envol sur root : decollage, derive, retour doux ---
    root_uuid = groups["root"]
    # fractions du temps -> (y, rot_x, rot_z, scale)
    path = [
        (0.00, 0.0, 0.0, 0.0, 1.00),
        (0.12, 2.5, -6.0, 4.0, 1.04),   # push-off
        (0.28, 7.0, 8.0, -6.0, 1.02),   # monte
        (0.45, 12.0, -4.0, 10.0, 0.98), # sommet derive
        (0.60, 14.0, 5.0, -4.0, 0.96),  # plane
        (0.75, 10.0, -3.0, 6.0, 0.98),  # redescend un peu
        (0.88, 4.0, 4.0, -3.0, 1.01),
        (1.00, 0.0, 0.0, 0.0, 1.00),    # atterrit = seamless
    ]
    pos_kfs, rot_kfs, scl_kfs = [], [], []
    for frac, y, rx, rz, s in path:
        tm = round(length * frac, 4)
        # leger drift X/Z pour "vent"
        x = round(math.sin(frac * math.pi * 2) * 1.5, 3)
        z = round(math.cos(frac * math.pi * 2) * 1.2, 3)
        pos_kfs.append(kf("position", tm, x, y, z))
        rot_kfs.append(kf("rotation", tm, rx, round(frac * 25, 2), rz))
        scl_kfs.append(kf("scale", tm, s, s, s))
    animators[root_uuid] = {
        "name": "root",
        "type": "bone",
        "keyframes": pos_kfs + rot_kfs + scl_kfs,
    }

    # leger "souffle" sur block (squash au decollage)
    block_uuid = groups["block"]
    block_kfs = [
        kf("scale", 0.0, 1.0, 1.0, 1.0),
        kf("scale", round(length * 0.12, 4), 1.06, 0.92, 1.06),
        kf("scale", round(length * 0.28, 4), 0.98, 1.05, 0.98),
        kf("scale", round(length * 0.6, 4), 1.0, 1.0, 1.0),
        kf("scale", length, 1.0, 1.0, 1.0),
    ]
    animators[block_uuid] = {"name": "block", "type": "bone", "keyframes": block_kfs}

    if theme.geom == "d20" and "emblem" in groups:
        ek = []
        for i in range(9):
            tm = length * i / 8
            ek.append(kf("rotation", tm, 0, i * 45, 12 * math.sin(i / 8 * math.pi * 2)))
        ek[-1] = kf("rotation", length, 0, 360, 0)
        animators[groups["emblem"]] = {"name": "emblem", "type": "bone", "keyframes": ek}

    elif theme.geom == "float" and "emblem" in groups:
        ek = []
        for i in range(7):
            tm = length * i / 6
            y = round(math.sin(i / 6 * math.pi * 2) * 1.2, 3)
            ek.append(kf("position", tm, 0, y, 0))
        ek[-1] = kf("position", length, 0, 0, 0)
        animators[groups["emblem"]] = {"name": "emblem", "type": "bone", "keyframes": ek}

    elif theme.geom == "paper" and "emblem" in groups:
        ek = []
        for i in range(7):
            tm = length * i / 6
            y = round(abs(math.sin(i / 6 * math.pi)) * 1.5, 3)
            ek.append(kf("position", tm, 0, y, 0.2))
        ek[-1] = kf("position", length, 0, 0, 0.2)
        animators[groups["emblem"]] = {"name": "emblem", "type": "bone", "keyframes": ek}

    elif theme.geom == "bat" and "emblem" in groups:
        ek = []
        for i in range(7):
            tm = length * i / 6
            zrot = round(math.sin(i / 6 * math.pi * 2) * 18, 2)
            ek.append(kf("rotation", tm, 0, 0, zrot))
        ek[-1] = kf("rotation", length, 0, 0, 0)
        animators[groups["emblem"]] = {"name": "emblem", "type": "bone", "keyframes": ek}

    if "fx" in groups:
        fx_kfs = []
        for i in range(7):
            tm = length * i / 6
            s = 1.0 + 0.04 * abs(math.sin(i / 6 * math.pi * 2))
            fx_kfs.append(kf("scale", tm, s, s, s))
        fx_kfs[-1] = kf("scale", length, 1.0, 1.0, 1.0)
        animators[groups["fx"]] = {"name": "fx", "type": "bone", "keyframes": fx_kfs}

    return {
        "uuid": uid(),
        "name": "idle",
        "loop": "loop",
        "override": False,
        "length": length,
        "snapping": 24,
        "selected": True,
        "saved_name": "idle",
        "anim_time_update": "",
        "blend_weight": "",
        "start_delay": "",
        "loop_delay": "",
        "animators": animators,
    }


def build_bbmodel(theme: Theme, sheet: Image.Image) -> dict:
    tex_uuid = uid()
    elem_uuid = uid()
    root_uuid = uid()
    block_uuid = uid()
    groups = {"root": root_uuid, "block": block_uuid}

    elements = [
        {
            "name": "cube",
            "box_uv": False,
            "rescale": False,
            "locked": False,
            "from": [0, 0, 0],
            "to": [16, 16, 16],
            "autouv": 0,
            "color": 0,
            "origin": [8, 8, 8],
            "faces": face_uv("0"),
            "uuid": elem_uuid,
        }
    ]
    block_children: list = [elem_uuid]

    # Optional emblem (tiny overlay cube for geom gags)
    if theme.geom != "none":
        emblem_uuid = uid()
        emblem_elem = uid()
        groups["emblem"] = emblem_uuid
        elements.append(
            {
                "name": "emblem_mesh",
                "box_uv": False,
                "from": [5, 5, -0.5],
                "to": [11, 11, 0.5],
                "origin": [8, 8, 0],
                "color": 1,
                "faces": face_uv("0"),
                "uuid": emblem_elem,
            }
        )
        block_children.append(
            {
                "name": "emblem",
                "origin": [8, 8, 0],
                "color": 1,
                "uuid": emblem_uuid,
                "export": True,
                "mirror_uv": False,
                "reset": False,
                "children": [emblem_elem],
            }
        )

    # Optional FX group (bone only)
    fx_uuid = uid()
    groups["fx"] = fx_uuid
    block_children.append(
        {
            "name": "fx",
            "origin": [8, 8, 8],
            "color": 2,
            "uuid": fx_uuid,
            "export": True,
            "mirror_uv": False,
            "reset": False,
            "children": [],
        }
    )

    outliner = [
        {
            "name": "root",
            "origin": [8, 8, 8],
            "color": 0,
            "uuid": root_uuid,
            "export": True,
            "mirror_uv": False,
            "reset": False,
            "children": [
                {
                    "name": "block",
                    "origin": [8, 8, 8],
                    "color": 0,
                    "uuid": block_uuid,
                    "export": True,
                    "mirror_uv": False,
                    "reset": False,
                    "children": block_children,
                }
            ],
        }
    ]

    # Embed first frame for Blockbench editing; full sheet stays as external PNG
    # Full spritesheet embedded so Blockbench can scrub frames;
    # UV still maps the first 32x32 frame (resolution below).
    source = "data:image/png;base64," + png_b64(sheet)

    textures = [
        {
            "path": f"{theme.tid}.png",
            "name": f"{theme.tid}.png",
            "folder": "",
            "namespace": "",
            "id": "0",
            "particle": True,
            "render_mode": "default",
            "render_sides": "auto",
            "frame_time": theme.frametime,
            "frame_order_type": "loop",
            "frame_order": "",
            "layers_enabled": False,
            "visible": True,
            "internal": True,
            "saved": True,
            "relative_path": "./" + theme.tid + ".png",
            "uuid": tex_uuid,
            "source": source,
        }
    ]

    return {
        "meta": {
            "format_version": "4.10",
            "model_format": "free",  # Generic: onglet Animate + Play (java_block n a pas Animate)
            "box_uv": False,
        },
        "name": theme.tid,
        "model_identifier": theme.tid,
        "visible_box": [1, 1, 0],
        "variable_placeholders": "",
        "variable_placeholder_buttons": [],
        "resolution": {"width": SIZE, "height": SIZE},
        "elements": elements,
        "outliner": outliner,
        "textures": textures,
        "animations": [build_idle_anim(theme, groups)],
        "animation_variable_placeholders": "",
    }


def write_mcmeta(path: Path, theme: Theme) -> None:
    data = {
        "animation": {
            "frametime": theme.frametime,
            "interpolate": theme.interpolate,
        }
    }
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def generate_theme(theme: Theme) -> None:
    frames: list[Image.Image] = []
    for t in range(theme.frames):
        img = new_frame(rgb(theme.bg))
        theme.draw(img, t, theme.frames)
        if theme.tid not in SKIP_FRAME:
            lucky_frame(img, rgb(theme.edge), rgb(theme.hi))
        shade_bottom(img)
        frames.append(img)

    sheet = stack_frames(frames)
    png_path = OUT / f"{theme.tid}.png"
    sheet.save(png_path)
    write_mcmeta(OUT / f"{theme.tid}.png.mcmeta", theme)

    model = build_bbmodel(theme, sheet)
    # annotate concept in a comment-like key Blockbench ignores gracefully via meta
    model["meta"]["unpredictableblocks_concept"] = theme.concept
    model["meta"]["unpredictableblocks_title"] = theme.title

    bb_path = OUT / f"{theme.tid}.bbmodel"
    bb_path.write_text(json.dumps(model, indent="\t") + "\n", encoding="utf-8")
    print(f"OK  {theme.tid}  ({theme.frames} frames, geom={theme.geom}) — {theme.concept}")


def write_readme(themes: list[Theme]) -> None:
    lines = [
        "# Blockbench — UnpredictableBlocks (32 Lucky Blocks)",
        "",
        "Format : **Java Block** · Texture **32×32** (spritesheet vertical) · Cube **16×16×16**",
        "",
        "## Ouvrir",
        "",
        "1. Installer [Blockbench](https://www.blockbench.net/)",
        "2. File → Open → `lucky_<id>.bbmodel`",
        "3. Animation panel → jouer **idle** (boucle)",
        "4. Les `.png` + `.png.mcmeta` sont à côté pour Minecraft (animation texture)",
        "",
        "## Régénérer",
        "",
        "```bash",
        "python tools/generate_blockbench.py",
        "```",
        "",
        "## Catalogue",
        "",
        "| # | Fichier | Thème | Concept idle | Géométrie |",
        "|---|---------|-------|--------------|-----------|",
    ]
    for i, th in enumerate(themes, 1):
        lines.append(
            f"| {i:02d} | `{th.tid}` | {th.title} | {th.concept} | {th.geom} |"
        )
    lines += [
        "",
        "## Notes",
        "",
        "- Priorité **animation texture** (`.mcmeta`) ; géométrie seulement pour D20 / float / paper / bat.",
        "- Groupes : `root` → `block` → (`emblem`, `fx`).",
        "- Style pixel art humoristique, cadre Lucky Block stable.",
        "",
    ]
    (OUT / "README.md").write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    wanted = {a.lower().removeprefix("lucky_") for a in sys.argv[1:]}
    selected = [
        th
        for th in THEMES
        if not wanted or th.tid.removeprefix("lucky_") in wanted or th.tid in wanted
    ]
    for th in selected:
        generate_theme(th)
    write_readme(THEMES)
    print("Done:", len(selected), "themes in", OUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
