#!/usr/bin/env python3
"""Grille de review : 32 textures Lucky Block (32×32 → cellules 64×64)."""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
BLOCKS = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures/block"
OUT = ROOT / "docs/textures/contact_sheet.png"

COLS = 8
CELL = 64
LABEL_H = 14
PAD = 4


def main() -> None:
    files = sorted(BLOCKS.glob("lucky_*.png"))
    if not files:
        raise SystemExit("no block textures")
    rows = (len(files) + COLS - 1) // COLS
    w = COLS * CELL + PAD * 2
    h = rows * (CELL + LABEL_H) + PAD * 2
    sheet = Image.new("RGBA", (w, h), (32, 32, 32, 255))
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.load_default(size=10)
    except TypeError:
        font = ImageFont.load_default()

    for i, path in enumerate(files):
        col = i % COLS
        row = i // COLS
        x0 = PAD + col * CELL
        y0 = PAD + row * (CELL + LABEL_H)
        img = Image.open(path).convert("RGBA")
        img = img.resize((CELL - 4, CELL - 4), Image.NEAREST)
        sheet.paste(img, (x0 + 2, y0 + 2), img)
        label = path.stem.replace("lucky_", "")
        draw.text((x0 + 2, y0 + CELL - 2), label[:12], fill=(220, 220, 220), font=font)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print("wrote", OUT.relative_to(ROOT), sheet.size)


if __name__ == "__main__":
    main()
