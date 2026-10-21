# Textures UnpredictableBlocks

## Faut-il un outil externe ?

**Non.** Les PNG du repo sont générés par script Python ([Pillow](https://pypi.org/project/pillow/)) :

```bash
pip install pillow
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
```

Sortie : `common/src/main/resources/assets/unpredictableblocks/textures/block/lucky_*.png` (+ `icon.png`).

## Qualité artistique

- **16×16** : placeholders lisibles en jeu, fidèles au catalogue (`CATALOGUE.md`).
- **32×32** ou retouche manuelle : optionnel (Aseprite, GIMP, Photoshop) — remplacez les PNG générés, gardez les mêmes noms de fichiers.
- **Upscale** : `python3 tools/upscale_textures.py` (à venir) ou nearest-neighbor ×2 dans votre outil préféré.

## Convention

| Fichier | Usage |
|---------|--------|
| `textures/block/lucky_<theme>.png` | faces du cube (block model `cube_all`) |
| `textures/icon.png` | mod icon Fabric/NeoForge |

Les objets custom peuvent réutiliser une texture de bloc ou ajouter `textures/item/<id>.png` + model JSON.
