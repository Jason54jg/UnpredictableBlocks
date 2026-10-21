#!/usr/bin/env python3
"""Textures d'objets 32×32 natifs (détail pixel art)."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

from pixel_canvas import CANVAS, Draw32, new_canvas, rgb, shade_bottom
from texture_size import OUTPUT_SIZE, to_output_size

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures/item"


def save(img: Image.Image, name: str) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    shade_bottom(img, 18)
    p = OUT / name
    to_output_size(img).save(p)
    print("wrote", p.relative_to(ROOT), f"({OUTPUT_SIZE}×{OUTPUT_SIZE})")


def d32(img: Image.Image) -> Draw32:
    return Draw32(ImageDraw.Draw(img))


def gen_baguette() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((8, 4, 24, 10), fill=rgb("C8A040"))
    d.raw.rectangle((20, 6, 28, 9), fill=rgb("E8C872"))
    d.raw.line((8, 7, 27, 7), fill=rgb("A08030"))
    d.raw.rectangle((6, 12, 26, 26), fill=rgb("D4A574"))
    return img


def gen_usb() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((10, 8, 22, 20), fill=rgb("283593"))
    d.raw.rectangle((18, 10, 26, 16), fill=rgb("B0BEC5"))
    d.raw.rectangle((12, 12, 16, 16), fill=rgb("00E676"))
    return img


def gen_marteau() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((14, 4, 22, 12), fill=rgb("78909C"))
    d.raw.rectangle((6, 10, 28, 14), fill=rgb("8D6E63"))
    d.raw.rectangle((15, 14, 17, 28), fill=rgb("5D4037"))
    return img


def gen_casque() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.pieslice((6, 6, 26, 24), 180, 0, fill=rgb("CFD8DC"))
    d.raw.rectangle((8, 14, 24, 18), fill=rgb("90A4AE"))
    d.raw.line((10, 10, 22, 10), fill=rgb("ECEFF1"))
    return img


def gen_shaker() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((11, 6, 21, 24), fill=rgb("F06292"))
    d.raw.ellipse((10, 4, 22, 10), fill=rgb("EC407A"))
    d.raw.rectangle((13, 12, 19, 14), fill=rgb("FFFFFF"))
    return img


def gen_molette() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.ellipse((8, 8, 24, 24), fill=rgb("FFD54F"))
    d.raw.ellipse((12, 12, 20, 20), fill=rgb("FFA000"))
    d.raw.rectangle((4, 14, 28, 18), fill=rgb("B0BEC5"))
    return img


def gen_jeton() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.ellipse((6, 6, 26, 26), fill=rgb("FFC107"))
    d.raw.ellipse((10, 10, 22, 22), fill=rgb("FF8F00"))
    d.raw.text((13, 11), "VIP", fill=rgb("FFF8E1"))
    return img


def gen_emerald_dust() -> Image.Image:
    img = new_canvas()
    for x, y, c in [(10, 12, "66BB6A"), (16, 10, "43A047"), (14, 18, "2E7D32"), (20, 16, "81C784")]:
        d32(img).raw.rectangle((x, y, x + 4, y + 4), fill=rgb(c))
    return img


def gen_blaze_curse() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.polygon([(16, 6), (24, 20), (8, 20)], fill=rgb("E65100"))
    d.raw.polygon([(16, 10), (20, 18), (12, 18)], fill=rgb("FF6D00"))
    return img


def gen_espresso() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((10, 14, 22, 24), fill=rgb("FFFFFF"))
    d.raw.rectangle((12, 8, 20, 14), fill=rgb("5D4037"))
    d.raw.ellipse((11, 6, 21, 10), fill=(255, 255, 255, 120))
    d.raw.rectangle((14, 4, 18, 6), fill=rgb("795548"))
    return img


def gen_valise() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((8, 10, 24, 24), fill=rgb("8D6E63"))
    d.raw.rectangle((8, 10, 24, 14), fill=rgb("6D4C41"))
    d.raw.rectangle((14, 12, 18, 14), fill=rgb("FFD54F"))
    d.raw.line((8, 18, 24, 18), fill=rgb("5D4037"))
    return img


def gen_tranche() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.pieslice((6, 8, 26, 26), 30, 210, fill=rgb("FFB74D"))
    d.raw.pieslice((8, 10, 24, 24), 30, 210, fill=rgb("FFA726"))
    for x, y in [(14, 14), (18, 16), (16, 20)]:
        d.raw.rectangle((x, y, x + 2, y + 2), fill=rgb("FDD835"))
    return img


def gen_chrono() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.ellipse((6, 6, 26, 26), fill=rgb("37474F"))
    d.raw.ellipse((9, 9, 23, 23), fill=rgb("263238"))
    d.raw.line((16, 16, 16, 10), fill=rgb("76FF03"), width=2)
    d.raw.line((16, 16, 22, 16), fill=rgb("76FF03"), width=2)
    return img


def gen_disque() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((6, 12, 26, 22), fill=rgb("424242"))
    d.raw.ellipse((10, 8, 22, 14), fill=rgb("616161"))
    d.raw.rectangle((22, 14, 28, 18), fill=rgb("212121"))
    return img


def gen_popit() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rounded_rectangle((6, 8, 26, 24), radius=4, fill=rgb("F48FB1"))
    for x in range(9, 24, 5):
        for y in range(11, 22, 5):
            d.raw.ellipse((x, y, x + 3, y + 3), fill=rgb("EC407A"))
    return img


def gen_fiole() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((14, 6, 18, 10), fill=rgb("B0BEC5"))
    d.raw.polygon([(12, 10), (20, 10), (22, 24), (10, 24)], fill=rgb("880E4F"))
    d.raw.rectangle((11, 16, 21, 20), fill=(255, 0, 0, 180))
    return img


def gen_zapper() -> Image.Image:
    img = new_canvas()
    d = d32(img)
    d.raw.rectangle((8, 10, 22, 22), fill=rgb("0288D1"))
    d.raw.polygon([(16, 4), (24, 12), (18, 12), (22, 20), (10, 14), (15, 14), (12, 8)], fill=rgb("FFEB3B"))
    return img


ITEMS = {
    "baguette_magique.png": gen_baguette,
    "cle_usb_hacker.png": gen_usb,
    "marteau_bonk.png": gen_marteau,
    "casque_5g.png": gen_casque,
    "shaker_prot.png": gen_shaker,
    "cle_molette_legendaire.png": gen_molette,
    "jeton_or_vip.png": gen_jeton,
    "luck_emerald_dust.png": gen_emerald_dust,
    "luck_blaze_curse.png": gen_blaze_curse,
    "espresso_ultra_dose.png": gen_espresso,
    "valise_perdue.png": gen_valise,
    "tranche_debat.png": gen_tranche,
    "chrono_world_record.png": gen_chrono,
    "disque_dur_capitaine.png": gen_disque,
    "pop_it_supreme.png": gen_popit,
    "fiole_sang_noble.png": gen_fiole,
    "zapper_dimensions.png": gen_zapper,
}


def main() -> None:
    for name, fn in ITEMS.items():
        save(fn(), name)
    print(f"{len(ITEMS)} item textures @ {CANVAS}×{CANVAS}")


if __name__ == "__main__":
    main()
