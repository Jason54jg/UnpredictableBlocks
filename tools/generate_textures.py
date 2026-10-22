#!/usr/bin/env python3
"""Génère les PNG 32×32 natifs des Lucky Blocks (grille logique 16 → pixels 32)."""
from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageDraw

from pixel_canvas import Draw32, lucky_frame, new_canvas, qmark, rgb, shade_bottom, sparkle
from texture_size import OUTPUT_SIZE, to_output_size

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures"

FRAME: dict[str, tuple[str, str]] = {
    "lucky_anachronisme": ("5C3317", "FFD700"),
    "lucky_boulangerie": ("A08030", "FFE8B0"),
    "lucky_bsod": ("000088", "AAAAFF"),
    "lucky_volaille": ("888888", "FFFFFF"),
    "lucky_impots": ("666666", "FFFF00"),
    "lucky_streamer": ("880088", "FFAAFF"),
    "lucky_proprete": ("5A9FD4", "FFFFFF"),
    "lucky_gravite_zero": ("220044", "CCAAFF"),
    "lucky_fast_food": ("AA6622", "FFEEAA"),
    "lucky_cauchemar_matin": ("1A0033", "FF6666"),
    "lucky_film_action": ("000000", "FFFFFF"),
    "lucky_jdr_d20": ("660000", "FFCCCC"),
    "lucky_penurie_2020": ("AAAAAA", "FFFFFF"),
    "lucky_complot": ("0F2F0F", "FFD700"),
    "lucky_lag_bug": ("440044", "00FF00"),
    "lucky_disco_80": ("FF00FF", "FFFF00"),
    "lucky_plomberie": ("1A5A1A", "FFDD00"),
    "lucky_fortune_cookie": ("C8A040", "FFF8DC"),
    "lucky_camping": ("3D4F1F", "FF8833"),
    "lucky_gym_bro": ("88AA00", "222222"),
    "lucky_art_moderne": ("CCCCCC", "FFFFFF"),
    "lucky_magie_comptoir": ("0A0A20", "FFD700"),
    "lucky_invasion_memes": ("6622AA", "FFEE88"),
    "lucky_pause_cafe": ("3E2723", "D7CCC8"),
    "lucky_retard_vol": ("0288D1", "ECEFF1"),
    "lucky_ananas_pizza": ("E65100", "FFF9C4"),
    "lucky_speedrunner": ("212121", "76FF03"),
    "lucky_piraterie_torrent": ("0D47A1", "B0BEC5"),
    "lucky_asmr_fidget": ("F48FB1", "FFFFFF"),
    "lucky_vampire_gothique": ("4A148C", "FF5252"),
    "lucky_multivers_sliders": ("311B92", "FF6E40"),
}


def base() -> Image.Image:
    return new_canvas()


def draw(img: Image.Image) -> Draw32:
    return Draw32(ImageDraw.Draw(img))


def finish(img: Image.Image, name: str, extra_sparkles: list[tuple[int, int]] | None = None) -> Image.Image:
    edge, hi = FRAME.get(name, ("333333", "DDDDDD"))
    lucky_frame(draw(img), rgb(edge), rgb(hi))
    if extra_sparkles:
        sparkle(img, extra_sparkles, (255, 255, 255, 220))
    shade_bottom(img)
    return img


def save(img: Image.Image, rel: str, name: str, *, skip_upscale: bool = False) -> None:
    path = OUT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    out = img if skip_upscale else to_output_size(finish(img, name))
    out.save(path)
    print("wrote", path.relative_to(ROOT), f"({out.size[0]}×{out.size[1]})")


def gen_anachronisme() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 7, 15), fill=rgb("B87333"))
    d.rectangle((8, 0, 15, 15), fill=rgb("111133"))
    for y in range(2, 14, 3):
        d.rectangle((9, y, 14, y), fill=rgb("00FFCC"))
    d.ellipse((2, 2, 6, 6), outline=rgb("5C3317"), width=1)
    qmark(d, rgb("FFD700"))
    d.raw.rectangle((4, 8, 10, 10), fill=rgb("CD7F32"))
    d.raw.rectangle((20, 6, 28, 7), fill=rgb("00FFCC"))
    d.raw.point((24, 12), fill=rgb("FFD700"))
    return img


def gen_boulangerie() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("C8A040"))
    for x in range(0, 16, 4):
        d.line((x, 0, x, 15), fill=rgb("A08030"))
    d.rectangle((4, 5, 6, 7), fill=rgb("5C3317"))
    d.rectangle((9, 8, 11, 10), fill=rgb("5C3317"))
    d.rectangle((5, 0, 10, 2), fill=rgb("F5F5F5"))
    d.rectangle((5, 1, 10, 1), fill=rgb("2040A0"))
    qmark(d, rgb("FFE8B0"))
    d.raw.line((12, 4, 22, 4), fill=rgb("E8C872"))
    d.raw.ellipse((14, 0, 20, 6), fill=(255, 255, 255, 100))
    d.raw.rectangle((10, 2, 11, 5), fill=(255, 255, 255, 80))
    d.raw.rectangle((23, 2, 24, 6), fill=(255, 255, 255, 80))
    return img


def gen_bsod() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("0000AA"))
    for y in range(3, 12, 2):
        d.rectangle((2, y, 13, y), fill=rgb("FFFFFF"))
    d.rectangle((5, 12, 6, 13), fill=rgb("FFFFFF"))
    d.rectangle((9, 12, 10, 13), fill=rgb("FFFFFF"))
    d.raw.rectangle((13, 22, 14, 24), fill=rgb("FFFFFF"))
    d.raw.rectangle((17, 24, 18, 26), fill=rgb("FFFFFF"))
    d.raw.point((15, 26), fill=rgb("FFFFFF"))
    return img


def gen_volaille() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("F0F0F0"))
    d.rectangle((4, 2, 11, 3), fill=rgb("CC0000"))
    d.rectangle((7, 6, 9, 8), fill=rgb("FFCC00"))
    qmark(d, rgb("333333"))
    for x in (6, 10, 20, 24):
        d.raw.line((x, 18, x + 2, 22), fill=rgb("EEEEEE"))
    return img


def gen_impots() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("808080"))
    d.rectangle((0, 0, 15, 2), fill=rgb("FFFF00"))
    d.rectangle((3, 5, 8, 6), fill=rgb("CC0000"))
    d.rectangle((3, 8, 9, 9), fill=rgb("CC0000"))
    d.raw.text((8, 20), "REJETE", fill=rgb("CC0000"))
    return img


def gen_streamer() -> Image.Image:
    img = base()
    d = draw(img)
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
    d.raw.text((10, 20), "LIKE", fill=rgb("FFFFFF"))
    d.raw.polygon([(26, 8), (30, 12), (26, 16)], fill=rgb("FF0000"))
    return img


def gen_proprete() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("87CEEB"))
    d.ellipse((2, 2, 5, 5), outline=rgb("FFFFFF"))
    d.ellipse((9, 4, 12, 7), outline=rgb("FFFFFF"))
    d.rectangle((11, 10, 14, 13), fill=rgb("FFD700"))
    d.raw.ellipse((18, 8, 26, 16), outline=rgb("FFFFFF"))
    d.raw.ellipse((6, 20, 14, 28), outline=rgb("FFFFFF"))
    return img


def gen_gravite_zero() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("120028"))
    for x, y in [(2, 2), (12, 3), (8, 11), (4, 9)]:
        d.point((x, y), fill=rgb("FFFFFF"))
    d.ellipse((6, 5, 10, 9), fill=rgb("9966FF"))
    d.raw.ellipse((14, 14, 22, 22), outline=rgb("9966FF"))
    for x, y in [(6, 6), (22, 8), (18, 24), (8, 26)]:
        d.raw.point((x, y), fill=rgb("FFFFFF"))
    return img


def gen_fast_food() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("D4A574"))
    d.ellipse((4, 4, 11, 11), fill=rgb("CC3300"))
    d.ellipse((6, 6, 9, 9), fill=rgb("FFCC66"))
    qmark(d, rgb("FFFFFF"))
    d.raw.ellipse((14, 14, 18, 18), fill=rgb("CC3300"))
    return img


def gen_cauchemar_matin() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("2E004F"))
    d.rectangle((4, 4, 11, 9), fill=rgb("CC0000"))
    d.rectangle((5, 6, 6, 7), fill=rgb("FFFF00"))
    d.rectangle((9, 6, 10, 7), fill=rgb("FFFF00"))
    d.raw.line((12, 24, 20, 24), fill=rgb("FF0000"))
    return img


def gen_film_action() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("111111"))
    d.rectangle((2, 2, 13, 13), fill=rgb("FFFFFF"))
    d.rectangle((2, 7, 13, 8), fill=rgb("000000"))
    d.rectangle((7, 2, 8, 13), fill=rgb("000000"))
    d.raw.rectangle((20, 20, 28, 26), fill=rgb("FFFF00"))
    return img


def gen_jdr_d20() -> Image.Image:
    img = base()
    d = draw(img)
    d.polygon([(8, 1), (14, 5), (12, 14), (4, 14), (2, 5)], fill=rgb("AA0000"))
    d.polygon([(8, 4), (11, 6), (10, 11), (6, 11), (5, 6)], fill=rgb("FF6666"))
    d.raw.text((11, 20), "20", fill=rgb("FFFFFF"))
    return img


def gen_penurie_2020() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((4, 0, 11, 15), fill=rgb("FFFFFF"))
    d.rectangle((5, 2, 10, 4), fill=rgb("EEEEEE"))
    d.rectangle((5, 6, 10, 8), fill=rgb("EEEEEE"))
    d.rectangle((3, 1, 4, 14), fill=rgb("CCCCCC"))
    d.raw.text((9, 18), "PQ", fill=rgb("888888"))
    return img


def gen_complot() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A3D1A"))
    d.polygon([(8, 4), (12, 12), (4, 12)], outline=rgb("FFD700"))
    d.rectangle((3, 0, 12, 3), fill=rgb("C0C0C0"))
    d.raw.ellipse((14, 14, 18, 18), fill=rgb("FFD700"))
    return img


def gen_lag_bug() -> Image.Image:
    img = base()
    for y in range(32):
        for x in range(32):
            if (x + y) % 2 == 0:
                img.putpixel((x, y), rgb("FF00FF") + (255,))
            else:
                img.putpixel((x, y), rgb("000000") + (255,))
    d = draw(img)
    d.rectangle((3, 7, 12, 8), fill=rgb("00FF00"))
    d.raw.rectangle((8, 14, 23, 17), fill=rgb("00FF00"))
    return img


def gen_disco_80() -> Image.Image:
    img = base()
    d = draw(img)
    colors = [rgb("FFFF00"), rgb("FF00FF"), rgb("00FFFF"), rgb("FFFFFF")]
    for i in range(4):
        d.rectangle((i * 4, 0, i * 4 + 3, 15), fill=colors[i])
    d.ellipse((5, 5, 10, 10), fill=rgb("CCCCCC"))
    for i in range(4):
        d.raw.line((6 + i * 6, 22, 8 + i * 6, 28), fill=rgb("FF00FF"))
    return img


def gen_plomberie() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("228B22"))
    for y in range(0, 16, 4):
        d.rectangle((0, y, 15, y + 1), fill=rgb("AA0000"))
    d.rectangle((10, 8, 14, 10), fill=rgb("FFD700"))
    d.raw.line((20, 20, 28, 28), fill=rgb("AA0000"), width=2)
    return img


def gen_fortune_cookie() -> Image.Image:
    img = base()
    d = draw(img)
    d.ellipse((2, 4, 13, 13), fill=rgb("E8C872"))
    d.rectangle((7, 2, 9, 8), fill=rgb("FFFFFF"))
    d.raw.line((16, 8, 16, 14), fill=rgb("333333"))
    return img


def gen_camping() -> Image.Image:
    img = base()
    d = draw(img)
    d.polygon([(8, 2), (14, 14), (2, 14)], fill=rgb("556B2F"))
    d.rectangle((6, 12, 9, 14), fill=rgb("FF6600"))
    d.raw.polygon([(16, 8), (26, 24), (6, 24)], fill=rgb("556B2F"))
    return img


def gen_gym_bro() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("CCFF00"))
    d.rectangle((2, 6, 5, 9), fill=rgb("222222"))
    d.rectangle((10, 6, 13, 9), fill=rgb("222222"))
    d.rectangle((5, 7, 10, 8), fill=rgb("222222"))
    d.raw.text((12, 22), "PR", fill=rgb("222222"))
    return img


def gen_art_moderne() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("FAFAFA"))
    d.rectangle((2, 3, 6, 7), fill=rgb("FF00FF"))
    d.rectangle((9, 2, 13, 6), fill=rgb("00FF00"))
    d.ellipse((4, 9, 11, 14), fill=rgb("00FFFF"))
    d.raw.rectangle((20, 20, 28, 28), fill=rgb("FF00FF"))
    return img


def gen_magie_comptoir() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A1A40"))
    d.polygon([(2, 6), (14, 6), (8, 0)], fill=rgb("111111"))
    for x, y in [(3, 3), (10, 4), (12, 8)]:
        d.point((x, y), fill=rgb("FFD700"))
    d.ellipse((6, 10, 9, 13), fill=rgb("FFFFFF"))
    d.raw.ellipse((20, 20, 28, 28), fill=rgb("FFFFFF"))
    return img


def gen_pause_cafe() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("4A2C1A"))
    d.rectangle((4, 3, 11, 10), fill=rgb("FFFFFF"))
    d.rectangle((6, 5, 9, 8), fill=rgb("2F1810"))
    d.raw.ellipse((10, 4, 22, 10), fill=(255, 255, 255, 90))
    d.raw.rectangle((14, 20, 18, 24), fill=rgb("795548"))
    qmark(d, rgb("D7CCC8"))
    return img


def gen_retard_vol() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("87CEEB"))
    d.rectangle((2, 8, 13, 14), fill=rgb("C0C0C0"))
    d.polygon([(8, 2), (12, 8), (4, 8)], fill=rgb("FFFFFF"))
    d.raw.line((4, 18, 28, 18), fill=rgb("37474F"), width=2)
    d.raw.rectangle((20, 14, 26, 20), fill=rgb("FFD54F"))
    return img


def gen_ananas_pizza() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("E8C040"))
    d.ellipse((3, 3, 12, 12), fill=rgb("FFCC00"))
    for x, y in [(5, 5), (9, 6), (7, 9), (6, 8), (10, 9)]:
        d.rectangle((x, y, x + 1, y + 1), fill=rgb("FFD700"))
    d.raw.arc((8, 8, 24, 24), 20, 160, fill=rgb("D84315"), width=2)
    return img


def gen_speedrunner() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("111111"))
    d.rectangle((3, 5, 12, 9), fill=rgb("00FF00"))
    d.text((4, 4), "WR", fill=rgb("FFFFFF"))
    d.raw.rectangle((6, 20, 26, 22), fill=rgb("76FF03"))
    d.raw.rectangle((8, 22, 10, 26), fill=rgb("FFFFFF"))
    d.raw.rectangle((22, 22, 24, 26), fill=rgb("FFFFFF"))
    return img


def gen_piraterie_torrent() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A3A5A"))
    d.rectangle((2, 6, 13, 12), fill=rgb("333333"))
    d.ellipse((6, 3, 9, 6), fill=rgb("FFFFFF"))
    d.raw.line((10, 24, 22, 24), fill=rgb("00E676"), width=2)
    d.raw.line((10, 26, 18, 26), fill=rgb("00E676"), width=2)
    return img


def gen_asmr_fidget() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("FFB6C1"))
    for x in range(3, 13, 3):
        for y in range(3, 13, 3):
            d.ellipse((x, y, x + 1, y + 1), fill=rgb("FF69B4"))
    for x in range(6, 26, 5):
        for y in range(6, 26, 5):
            d.raw.ellipse((x, y, x + 3, y + 3), outline=rgb("F06292"))
    return img


def gen_vampire_gothique() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 15, 15), fill=rgb("1A001A"))
    d.polygon([(8, 2), (12, 10), (4, 10)], fill=rgb("8B0000"))
    d.ellipse((6, 6, 9, 9), fill=rgb("FF0000"))
    d.raw.polygon([(16, 4), (26, 14), (6, 14)], outline=rgb("4A148C"))
    d.raw.line((12, 18, 20, 26), fill=rgb("B71C1C"), width=2)
    return img


def gen_multivers_sliders() -> Image.Image:
    img = base()
    d = draw(img)
    d.rectangle((0, 0, 7, 15), fill=rgb("4400FF"))
    d.rectangle((8, 0, 15, 15), fill=rgb("FF4400"))
    d.rectangle((6, 0, 9, 15), fill=rgb("FFFFFF"))
    d.raw.rectangle((13, 13, 19, 19), fill=rgb("FFFFFF"))
    d.raw.line((16, 10, 16, 22), fill=rgb("212121"), width=2)
    return img


def gen_invasion_memes() -> Image.Image:
    img = base()
    d = draw(img)
    for y in range(16):
        t = y / 15
        d.line((0, y, 15, y), fill=(int(255 * t), int(128 * (1 - t)), 255))
    d.rectangle((2, 4, 7, 10), fill=rgb("D4A520"))
    d.rectangle((9, 5, 13, 9), fill=rgb("88CC88"))
    d.raw.rectangle((18, 18, 26, 26), fill=rgb("FF69B4"))
    d.raw.line((4, 28, 28, 4), fill=rgb("00FFFF"), width=1)
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
    "lucky_invasion_memes": gen_invasion_memes,
    "lucky_pause_cafe": gen_pause_cafe,
    "lucky_retard_vol": gen_retard_vol,
    "lucky_ananas_pizza": gen_ananas_pizza,
    "lucky_speedrunner": gen_speedrunner,
    "lucky_piraterie_torrent": gen_piraterie_torrent,
    "lucky_asmr_fidget": gen_asmr_fidget,
    "lucky_vampire_gothique": gen_vampire_gothique,
    "lucky_multivers_sliders": gen_multivers_sliders,
}


def icon() -> None:
    """Icône du pack : logo branding (docs/branding/logo.jpg|.png), sinon fallback pixel."""
    brand = ROOT / "docs/branding/logo.png"
    if not brand.is_file():
        brand = ROOT / "docs/branding/logo.jpg"
    pack_icon = ROOT / "common/src/main/resources/assets/unpredictableblocks/icon.png"
    tex_icon = OUT / "icon.png"
    if brand.is_file():
        img = Image.open(brand).convert("RGBA")
        img512 = img.resize((512, 512), Image.LANCZOS)
        img128 = img.resize((128, 128), Image.LANCZOS)
        pack_icon.parent.mkdir(parents=True, exist_ok=True)
        img512.save(pack_icon)
        img128.save(tex_icon)
        print("wrote", pack_icon.relative_to(ROOT), "(512×512 depuis branding)")
        print("wrote", tex_icon.relative_to(ROOT), "(128×128 depuis branding)")
        return
    img = finish(gen_invasion_memes(), "lucky_invasion_memes")
    img = img.resize((128, 128), Image.NEAREST)
    save(img, "icon.png", "lucky_invasion_memes", skip_upscale=True)
    pack_icon.parent.mkdir(parents=True, exist_ok=True)
    img.resize((512, 512), Image.NEAREST).save(pack_icon)
    print("wrote", pack_icon.relative_to(ROOT), "(512×512 fallback)")


def main() -> None:
    for name, fn in GENERATORS.items():
        save(fn(), f"block/{name}.png", name)
    icon()
    print(f"Generated {len(GENERATORS)} block textures ({OUTPUT_SIZE}×{OUTPUT_SIZE} natif) + icon.")


if __name__ == "__main__":
    main()
