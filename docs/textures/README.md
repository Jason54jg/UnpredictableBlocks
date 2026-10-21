# Textures UnpredictableBlocks

## Faut-il un outil externe ?

**Non.** Les PNG du repo sont générés par script Python ([Pillow](https://pypi.org/project/pillow/)) :

```bash
pip install pillow
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
```

Sortie : `textures/block/lucky_*.png` et `textures/item/*.png` en **32×32 natif** (+ `icon.png` 128×128).

## Pipeline 32×32 détaillé

| Fichier | Rôle |
|---------|------|
| `tools/texture_size.py` | Canvas cible **32×32** (plus d’upscale 16→32) |
| `tools/pixel_canvas.py` | Grille logique 16×16 → pixels 32 (`Draw32`), cadre Lucky Block, ombre basse, reflets |
| `tools/generate_textures.py` | 32 blocs thématiques + détails natifs (thèmes 25–32 enrichis) |
| `tools/generate_item_textures.py` | 17 items en pixel art 32×32 |

Chaque Lucky Block reçoit un **cadre** (bordure + highlight), un **assombrissement** en bas de face, et des **détails 32 px** supplémentaires sur les thèmes récents (vapeur café, pistes torrent, bulles pop-it, etc.).

## Qualité artistique

- **32×32** : résolution par défaut du mod (catalogue `CATALOGUE.md`).
- Retouche manuelle (Aseprite, GIMP, etc.) : remplacez les PNG ou étendez les fonctions `gen_*` / `ITEMS` dans les scripts.
- Régénération complète : `python3 tools/upscale_textures.py` (relance les deux générateurs).

## Convention

| Fichier | Usage |
|---------|--------|
| `textures/block/lucky_<theme>.png` | faces du cube (block model `cube_all`) |
| `textures/icon.png` | mod icon Fabric/NeoForge |

Les objets custom : `textures/item/<id>.png` + model JSON `assets/.../models/item/<id>.json`.
