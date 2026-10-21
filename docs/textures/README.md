# Textures UnpredictableBlocks

## Faut-il un outil externe ?

**Non.** Les PNG du repo sont générés par script Python ([Pillow](https://pypi.org/project/pillow/)) :

```bash
pip install pillow
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
```

Sortie : `textures/block/lucky_*.png` et `textures/item/*.png` en **32×32** (+ `icon.png` 128×128).

Les scripts dessinent sur une grille **16×16** puis exportent en **32×32** (nearest-neighbor, pixel art net) — voir `tools/texture_size.py`.

## Qualité artistique

- **32×32** : résolution par défaut du mod (placeholders procéduraux, catalogue `CATALOGUE.md`).
- Retouche manuelle (Aseprite, GIMP, etc.) : remplacez les PNG, gardez les mêmes chemins et la taille **32×32** pour rester cohérent.
- Régénération rapide : `python3 tools/upscale_textures.py` relance les deux générateurs.

## Convention

| Fichier | Usage |
|---------|--------|
| `textures/block/lucky_<theme>.png` | faces du cube (block model `cube_all`) |
| `textures/icon.png` | mod icon Fabric/NeoForge |

Les objets custom peuvent réutiliser une texture de bloc ou ajouter `textures/item/<id>.png` + model JSON.
