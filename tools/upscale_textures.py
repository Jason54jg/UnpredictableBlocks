#!/usr/bin/env python3
"""Exporte des variantes 32×32 (nearest neighbor) à partir des PNG 16×16."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures/block"
DST = ROOT / "common/src/main/resources/assets/unpredictableblocks/textures/block_hd"

def main() -> None:
    DST.mkdir(parents=True, exist_ok=True)
    for path in sorted(SRC.glob("lucky_*.png")):
        img = Image.open(path).convert("RGBA")
        out = img.resize((32, 32), Image.NEAREST)
        target = DST / path.name
        out.save(target)
        print("wrote", target.relative_to(ROOT))
    print("Done. Point block models to block_hd/ if you switch to 32×32.")

if __name__ == "__main__":
    main()
