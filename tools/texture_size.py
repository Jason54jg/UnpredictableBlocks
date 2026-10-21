"""Taille cible des textures PNG du mod (pixel art, upscale nearest-neighbor)."""
from __future__ import annotations

from PIL import Image

# Dessin logique 16×16 → export 32×32 (style Lucky Block lisible, 2× en jeu)
DRAW_SIZE = 16
OUTPUT_SIZE = 32


def to_output_size(img: Image.Image) -> Image.Image:
    w, h = img.size
    if (w, h) == (DRAW_SIZE, DRAW_SIZE):
        return img.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.NEAREST)
    if (w, h) == (OUTPUT_SIZE, OUTPUT_SIZE):
        return img
    if w == h and w < OUTPUT_SIZE:
        return img.resize((OUTPUT_SIZE, OUTPUT_SIZE), Image.NEAREST)
    return img
