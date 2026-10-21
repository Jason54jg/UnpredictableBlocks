# WTF Lucky Blocks (UnpredictableBlocks)

Mod multi-loader **NeoForge + Fabric** (Architectury + Stonecutter) pour Minecraft **26.1.2 → 26.3**, mappings **Mojmap**.

## Fonctionnalités implémentées (v0.1.0-SNAPSHOT)

- **24 Lucky Blocks** thématiques (`LuckyTheme`) avec rareté (60/30/10) et config de spawn (`config/wtf_luckyblocks.json`).
- **Luck Stat** `-100…+100` : résolution high / neutral / low à la casse (`LuckResolver`).
- **Modificateurs craft** : `luck_emerald_dust` (+10), `luck_blaze_curse` (-10).
- **Quêtes** : 24 advancements Vanilla (`data/wtf_luckyblocks/advancement/quest/*.json`) + loot de récompense Chance +100.
- **Loot tables** par thème et tier (`data/wtf_luckyblocks/loot_table/gameplay/lucky/<theme>/{high,neutral,low}.json`).
- **Structures NBT** : hooks `StructurePlacer` (IDs `wtf_luckyblocks:<nom>` — assets à placer dans `data/wtf_luckyblocks/structures/`).

## Build

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
```

Stonecutter active la version MC via `gradle.properties` / tâches Stonecutter (`26.1.2`, `26.2`, `26.3`).

## Documentation de génération

Voir `docs/GENERATION_BOULANGERIE.md` pour la fiche complète (texture, événements ±100, quête, JSON, items/NBT) au format demandé.

## Textures

Descriptions visuelles : `docs/textures/CATALOGUE.md`. PNG 16×16 / 32×32 à déposer dans `assets/wtf_luckyblocks/textures/block/`.
