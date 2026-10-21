#!/usr/bin/env python3
"""Génère les PNG 32×32 des Lucky Blocks (Pillow, pixel art 16×16 upscalé ×2)."""
from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageDraw

from texture_size import OUTPUT_SIZE, to_output_size

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures"
SIZE = 16  # grille de dessin ; export via to_output_size()


def rgb(h: str) -> tuple[int, int, int]:
    h = h.lstrip("#")
    return tuple(int(h[i : i + 2], 16) for i in (0, 2, 4))


def base() -> Image.Image:
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def save(img: Image.Image, rel: str, *, skip_upscale: bool = False) -> None:
    path = OUT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    out = img if skip_upscale else to_output_size(img)
    out.save(path)
    print("wrote", path.relative_to(ROOT), f"({out.size[0]}×{out.size[1]})")


def qmark(draw: ImageDraw.ImageDraw, color: tuple[int, int, int]) -> None:
    draw.rectangle((6, 3, 9, 4), fill=color)
    draw.rectangle((8, 4, 9, 6), fill=color)
    draw.rectangle((7, 7, 8, 8), fill=color)
    draw.rectangle((7, 10, 8, 11), fill=color)


def gen_anachronisme() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 7, 15), fill=rgb("B87333"))
    d.rectangle((8, 0, 15, 15), fill=rgb("111133"))
    for y in range(2, 14, 3):
        d.rectangle((9, y, 14, y), fill=rgb("00FFCC"))
    d.ellipse((2, 2, 6, 6), outline=rgb("5C3317"), width=1)
    qmark(d, rgb("FFD700"))
    return img


def gen_boulangerie() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("C8A040"))
    for x in range(0, 16, 4):
        d.line((x, 0, x, 15), fill=rgb("A08030"))
    d.rectangle((4, 5, 6, 7), fill=rgb("5C3317"))
    d.rectangle((9, 8, 11, 10), fill=rgb("5C3317"))
    d.rectangle((5, 0, 10, 2), fill=rgb("F5F5F5"))
    d.rectangle((5, 1, 10, 1), fill=rgb("2040A0"))
    qmark(d, rgb("FFE8B0"))
    return img


def gen_bsod() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("0000AA"))
    for y in range(3, 12, 2):
        d.rectangle((2, y, 13, y), fill=rgb("FFFFFF"))
    d.rectangle((5, 12, 6, 13), fill=rgb("FFFFFF"))
    d.rectangle((9, 12, 10, 13), fill=rgb("FFFFFF"))
    return img


def gen_volaille() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("F0F0F0"))
    d.rectangle((4, 2, 11, 3), fill=rgb("CC0000"))
    d.rectangle((7, 6, 9, 8), fill=rgb("FFCC00"))
    qmark(d, rgb("333333"))
    return img


def gen_impots() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("808080"))
    d.rectangle((0, 0, 15, 2), fill=rgb("FFFF00"))
    d.rectangle((3, 5, 8, 6), fill=rgb("CC0000"))
    d.rectangle((3, 8, 9, 9), fill=rgb("CC0000"))
    return img


def gen_streamer() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    for y in range(16):
        t = y / 15
        c = (
            int(180 + 60 * t),
            int(40 + 80 * (1 - t)),
            int(200 + 40 * t),
        )
        d.line((0, y, 15, y), fill=c)
    d.rectangle((4, 6, 11, 9), fill=rgb("FFFF00"))
    d.polygon([(2, 2), (2, 6), (5, 4)], fill=rgb("FF0000"))
    return img


def gen_proprete() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("87CEEB"))
    d.ellipse((2, 2, 5, 5), outline=rgb("FFFFFF"))
    d.ellipse((9, 4, 12, 7), outline=rgb("FFFFFF"))
    d.rectangle((11, 10, 14, 13), fill=rgb("FFD700"))
    return img


def gen_gravite_zero() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("120028"))
    for x, y in [(2, 2), (12, 3), (8, 11), (4, 9)]:
        d.point((x, y), fill=rgb("FFFFFF"))
    d.ellipse((6, 5, 10, 9), fill=rgb("9966FF"))
    return img


def gen_fast_food() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("D4A574"))
    d.ellipse((4, 4, 11, 11), fill=rgb("CC3300"))
    d.ellipse((6, 6, 9, 9), fill=rgb("FFCC66"))
    qmark(d, rgb("FFFFFF"))
    return img


def gen_cauchemar_matin() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("2E004F"))
    d.rectangle((4, 4, 11, 9), fill=rgb("CC0000"))
    d.rectangle((5, 6, 6, 7), fill=rgb("FFFF00"))
    d.rectangle((9, 6, 10, 7), fill=rgb("FFFF00"))
    return img


def gen_film_action() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("111111"))
    d.rectangle((2, 2, 13, 13), fill=rgb("FFFFFF"))
    d.rectangle((2, 7, 13, 8), fill=rgb("000000"))
    d.rectangle((7, 2, 8, 13), fill=rgb("000000"))
    return img


def gen_jdr_d20() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (14, 5), (12, 14), (4, 14), (2, 5)], fill=rgb("AA0000"))
    d.polygon([(8, 4), (11, 6), (10, 11), (6, 11), (5, 6)], fill=rgb("FF6666"))
    return img


def gen_penurie_2020() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((4, 0, 11, 15), fill=rgb("FFFFFF"))
    d.rectangle((5, 2, 10, 4), fill=rgb("EEEEEE"))
    d.rectangle((5, 6, 10, 8), fill=rgb("EEEEEE"))
    d.rectangle((3, 1, 4, 14), fill=rgb("CCCCCC"))
    return img


def gen_complot() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A3D1A"))
    d.polygon([(8, 4), (12, 12), (4, 12)], outline=rgb("FFD700"))
    d.rectangle((3, 0, 12, 3), fill=rgb("C0C0C0"))
    return img


def gen_lag_bug() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                img.putpixel((x, y), rgb("FF00FF") + (255,))
            else:
                img.putpixel((x, y), rgb("000000") + (255,))
    d = ImageDraw.Draw(img)
    d.rectangle((3, 7, 12, 8), fill=rgb("00FF00"))
    return img


def gen_disco_80() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    colors = [rgb("FFFF00"), rgb("FF00FF"), rgb("00FFFF"), rgb("FFFFFF")]
    for i in range(4):
        d.rectangle((i * 4, 0, i * 4 + 3, 15), fill=colors[i])
    d.ellipse((5, 5, 10, 10), fill=rgb("CCCCCC"))
    return img


def gen_plomberie() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("228B22"))
    for y in range(0, 16, 4):
        d.rectangle((0, y, 15, y + 1), fill=rgb("AA0000"))
    d.rectangle((10, 8, 14, 10), fill=rgb("FFD700"))
    return img


def gen_fortune_cookie() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.ellipse((2, 4, 13, 13), fill=rgb("E8C872"))
    d.rectangle((7, 2, 9, 8), fill=rgb("FFFFFF"))
    return img


def gen_camping() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.polygon([(8, 2), (14, 14), (2, 14)], fill=rgb("556B2F"))
    d.rectangle((6, 12, 9, 14), fill=rgb("FF6600"))
    return img


def gen_gym_bro() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("CCFF00"))
    d.rectangle((2, 6, 5, 9), fill=rgb("222222"))
    d.rectangle((10, 6, 13, 9), fill=rgb("222222"))
    d.rectangle((5, 7, 10, 8), fill=rgb("222222"))
    return img


def gen_art_moderne() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("FAFAFA"))
    d.rectangle((2, 3, 6, 7), fill=rgb("FF00FF"))
    d.rectangle((9, 2, 13, 6), fill=rgb("00FF00"))
    d.ellipse((4, 9, 11, 14), fill=rgb("00FFFF"))
    return img


def gen_magie_comptoir() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A1A40"))
    d.polygon([(2, 6), (14, 6), (8, 0)], fill=rgb("111111"))
    for x, y in [(3, 3), (10, 4), (12, 8)]:
        d.point((x, y), fill=rgb("FFD700"))
    d.ellipse((6, 10, 9, 13), fill=rgb("FFFFFF"))
    return img


def gen_casino() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("880000"))
    d.rectangle((3, 4, 12, 11), fill=rgb("FFD700"))
    d.rectangle((5, 6, 10, 7), fill=rgb("000000"))
    d.text((4, 5), "777", fill=rgb("FF0000"))
    return img


def gen_invasion_memes() -> Image.Image:
    img = base()
    d = ImageDraw.Draw(img)
    for y in range(16):
        t = y / 15
        d.line((0, y, 15, y), fill=(int(255 * t), int(128 * (1 - t)), 255))
    d.rectangle((2, 4, 7, 10), fill=rgb("D4A520"))
    d.rectangle((9, 5, 13, 9), fill=rgb("88CC88"))
    return img


GENERATORS = {
    "lucky_anachronisme": gen_anachronisme,
    "lucky_boulangerie": gen_boulangerie,
    "lucky_bsod": gen_bsod,
    "lucky_volaille": gen_volaille,
    "lucky_impots": gen_impots,
    "lucky_streamer": gen_streamer,
    "lucky_proprete": gen_proprete,
    "lucky_gravite_zero": gen_gravite_zero,
    "lucky_fast_food": gen_fast_food,
    "lucky_cauchemar_matin": gen_cauchemar_matin,
    "lucky_film_action": gen_film_action,
    "lucky_jdr_d20": gen_jdr_d20,
    "lucky_penurie_2020": gen_penurie_2020,
    "lucky_complot": gen_complot,
    "lucky_lag_bug": gen_lag_bug,
    "lucky_disco_80": gen_disco_80,
    "lucky_plomberie": gen_plomberie,
    "lucky_fortune_cookie": gen_fortune_cookie,
    "lucky_camping": gen_camping,
    "lucky_gym_bro": gen_gym_bro,
    "lucky_art_moderne": gen_art_moderne,
    "lucky_magie_comptoir": gen_magie_comptoir,
    "lucky_casino": gen_casino,
    "lucky_invasion_memes": gen_invasion_memes,
}


def icon() -> None:
    img = to_output_size(gen_invasion_memes()).resize((128, 128), Image.NEAREST)
    save(img, "icon.png", skip_upscale=True)


def main() -> None:
    for name, fn in GENERATORS.items():
        save(fn(), f"block/{name}.png")
    icon()
    print(f"Generated {len(GENERATORS)} block textures ({OUTPUT_SIZE}×{OUTPUT_SIZE}) + icon.")


if __name__ == "__main__":
    main()
