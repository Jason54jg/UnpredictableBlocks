"""Taille cible des textures PNG du mod — pixel art natif 32×32."""
from __future__ import annotations

from PIL import Image

DRAW_SIZE = 32
OUTPUT_SIZE = 32


def to_output_size(img: Image.Image) -> Image.Image:
    w, h = img.size
    if (w, h) == (OUTPUT_SIZE, OUTPUT_SIZE):
        return img
    if w == h and w < OUTPUT_SIZE:
        return img.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.NEAREST)
    if w == h and w > OUTPUT_SIZE:
        return img.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.NEAREST)
    return img
