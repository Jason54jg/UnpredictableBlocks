#!/usr/bin/env python3
"""Textures d'objets 32×32 (grille 16×16 upscalée ×2)."""
from pathlib import Path
from PIL import Image, ImageDraw

from texture_size import OUTPUT_SIZE, to_output_size

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures/item"


def save(img: Image.Image, name: str) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    p = OUT / name
    to_output_size(img).save(p)
    print("wrote", p.relative_to(ROOT), f"({OUTPUT_SIZE}×{OUTPUT_SIZE})")


def item_base(color: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((4, 3, 11, 12), fill=color)
    d.rectangle((5, 2, 10, 3), fill=tuple(min(255, c + 30) for c in color))
    return img


def main() -> None:
    save(item_base((200, 160, 80)), "baguette_magique.png")
    save(item_base((40, 40, 200)), "cle_usb_hacker.png")
    img = item_base((120, 80, 40))
    ImageDraw.Draw(img).rectangle((6, 2, 9, 5), fill=(180, 180, 180))
    save(img, "marteau_bonk.png")
    save(item_base((192, 192, 192)), "casque_5g.png")
    save(item_base((255, 100, 200)), "shaker_prot.png")
    save(item_base((255, 215, 0)), "cle_molette_legendaire.png")
    save(item_base((255, 200, 0)), "jeton_or_vip.png")
    save(item_base((80, 200, 120)), "luck_emerald_dust.png")
    save(item_base((200, 60, 20)), "luck_blaze_curse.png")
    print("item textures ok")


if __name__ == "__main__":
    main()
