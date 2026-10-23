# Mobs — Blockbench + GeckoLib

Un `.bbmodel` par mob, généré par `tools/generate_mobs.py` depuis la même définition que le jeu :

| Fichier | Rôle |
|---|---|
| `geckolib/models/entity/<mob>.geo.json` | Modèle Bedrock chargé par GeckoLib |
| `geckolib/animations/entity/<mob>.animation.json` | `idle`, `walk`, `attack`, `special` (+ `sit` pour le chat) |
| `textures/entity/<mob>.png` | Texture 128×128 (UV « box ») |
| `docs/blockbench/mobs/<mob>.bbmodel` | Même hiérarchie + mêmes keyframes, texture intégrée |

## Blockbench
Fichier → Ouvrir `<mob>.bbmodel` → onglet **Animate** → `animation.<mob>.idle` / `walk` / `attack` / `special` → **Play**.

## Modifier un mob
Ne pas exporter depuis Blockbench : éditer la fonction du mob dans `tools/generate_mobs.py`
(cubes, bones, peinture des faces, keyframes), puis :
```bash
python tools/generate_mobs.py
```
