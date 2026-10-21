# Documentation de génération (UnpredictableBlocks)

Index des fiches alignées sur le cahier des charges (chance −100…+100, quêtes, assets 32×32).

| Document | Contenu |
|----------|---------|
| [`GENERATION_BOULANGERIE.md`](../GENERATION_BOULANGERIE.md) | Fiche complète exemple (thème 2) |
| [`THEMES_01_24.md`](THEMES_01_24.md) | Thèmes 1–24 : visuel, spawn, événements catalogue §4, structures, quêtes |
| [`THEMES_25_32.md`](THEMES_25_32.md) | Thèmes 25–32 : extension directive |
| [`../textures/CATALOGUE.md`](../textures/CATALOGUE.md) | Table PNG 32×32 + items |
| [`../textures/README.md`](../textures/README.md) | Pipeline `pixel_canvas.py`, regen |

## Regénération rapide

```bash
pip install -r tools/requirements.txt
python3 tools/upscale_textures.py
python3 tools/generate_structures.py
python3 tools/enrich_loot_tables.py
```

## Code de référence

| Système | Classe / chemin |
|---------|------------------|
| Thèmes | `LuckyTheme.java` |
| Spawn | `ModConfig.ThemeSpawn` |
| Effets legacy | `ThemeEventEffects.java` |
| Effets catalogue | `ThemeCatalogEffects.java` |
| Thèmes 25–32 | `ThemeNewEffects.java` |
| Structures | `StructurePlacer.java`, `data/.../structures/*.nbt` |
| Quêtes | `data/.../advancement/quest/*.json`, `QuestExtendedHandler.java` |
