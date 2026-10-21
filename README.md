# UnpredictableBlocks

Mod multi-loader **NeoForge + Fabric** (Architectury + Stonecutter) pour Minecraft **26.1.2 → 26.3**, mappings **Mojmap**.

Lucky Blocks chaotiques à thèmes, système de **chance (-100…+100)**, quêtes (advancements) et spawn naturel en monde.

## Fonctionnalités (v0.1.0-SNAPSHOT)

- **32 Lucky Blocks** (`LuckyTheme`, thèmes 1–24 + 25–32) — communs / rares / légendaires.
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
python3 tools/generate_textures.py      # blocs 32×32 + icon 128×128
python3 tools/generate_item_textures.py # objets 32×32
python3 tools/upscale_textures.py       # relance les deux générateurs ci-dessus
python3 tools/generate_structures.py    # structures .nbt (32 thèmes)
```

Détails : `docs/textures/README.md`

## Documentation

- Fiche génération exemple : `docs/GENERATION_BOULANGERIE.md`
- Thèmes 1–24 : `docs/generation/THEMES_01_24.md`
- Thèmes 25–32 : `docs/generation/THEMES_25_32.md`
- Index génération : `docs/generation/README.md`
- Textures 32×32 : `docs/textures/CATALOGUE.md`, `docs/textures/README.md`
- Structures : `docs/structures/STRUCTURES.md`

Mod ID : `unpredictableblocks` — nom affiché : **UnpredictableBlocks**.
