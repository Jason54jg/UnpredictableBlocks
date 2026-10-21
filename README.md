# UnpredictableBlocks

Mod multi-loader **NeoForge + Fabric** (Architectury + Stonecutter) pour Minecraft **26.1.2 → 26.3**, mappings **Mojmap**.

Lucky Blocks chaotiques à thèmes, système de **chance (-100…+100)**, quêtes (advancements) et spawn naturel en monde.

## Fonctionnalités (v0.1.0-SNAPSHOT)

- **24 Lucky Blocks** (`LuckyTheme`) — communs / rares / légendaires (60/30/10).
- **Luck Stat** + modificateurs craft ±10 (`luck_emerald_dust`, `luck_blaze_curse`).
- **Spawn** : `config/unpredictableblocks.json`, hook Architectury `CHUNK_LOAD` + marqueurs par chunk.
- **Événements** : loot tables + `ThemeEventEffects` + structures NBT (`StructurePlacer`).
- **Quêtes** : advancements sous `data/unpredictableblocks/advancement/quest/`.

## Build

**JDK 25** requis (Minecraft 26.1.2). Voir `docs/BUILD.md`.

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew chiseledBuild
```

## Textures (sans Photoshop / Aseprite obligatoire)

```bash
pip install -r tools/requirements.txt
python3 tools/generate_textures.py      # 16×16 + icon
python3 tools/upscale_textures.py       # optionnel : 32×32 dans textures/block_hd/
python3 tools/generate_item_textures.py
python3 tools/generate_structures.py    # 15 structures .nbt
```

Détails : `docs/textures/README.md`

## Documentation

- Fiche génération exemple : `docs/GENERATION_BOULANGERIE.md`
- Textures : `docs/textures/CATALOGUE.md`
- Structures : `docs/structures/STRUCTURES.md`

Mod ID : `unpredictableblocks` — nom affiché : **UnpredictableBlocks**.
