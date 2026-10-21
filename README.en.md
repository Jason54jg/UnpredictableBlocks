# UnpredictableBlocks

**NeoForge + Fabric** mod (Architectury + Stonecutter) for Minecraft **26.1.2 → 26.3**, **Mojmap** mappings.

Chaotic themed Lucky Blocks, **luck (-100…+100)**, quests (advancements), and natural world spawn.

## Features (v0.1.0-SNAPSHOT)

- **32 Lucky Blocks** (`LuckyTheme`) — common / rare / legendary. **One block = one fixed theme** (texture, loot, structure, quest, effects). Randomness is the **tier** (HIGH / NEUTRAL / LOW) from luck, not the theme on break. Exception: **Mystery Lucky** item (random among 32).
- **Luck stat** + craft modifiers ±10 (`luck_emerald_dust`, `luck_blaze_curse`).
- **Spawn**: `config/unpredictableblocks.json`, Architectury `CHUNK_LOAD` hooks.
- **Events**: loot tables + `ThemeEventEffects` + NBT structures (`StructurePlacer`).
- **Quests**: advancements under `data/unpredictableblocks/advancement/quest/` + **Quest Journal** item.
- **Luck HUD** (action bar); optional config in `unpredictableblocks.json`.
- **Quest milestones** 8 / 16 / 24 / **32**; **theme master** per theme; **card album**; combo / near miss / V.I.P. token; chunk aura; **luck bet** (sneak + dust off-hand).
- **Languages** (30 locales, 246 keys each): `en_us`, `en_gb`, `fr_fr`, `de_de`, `es_es`, `es_mx`, `pt_br`, `it_it`, `nl_nl`, `pl_pl`, `ru_ru`, `uk_ua`, `cs_cz`, `sv_se`, `da_dk`, `nb_no`, `fi_fi`, `hu_hu`, `tr_tr`, `ja_jp`, `ko_kr`, `zh_cn`, `zh_tw`, `vi_vn`, `id_id`, `th_th`, `ro_ro`, `el_gr`, `he_il`, `ar_sa` — gameplay messages use `message.unpredictableblocks.*`. Regenerate: `python3 tools/generate_lang_files.py`.

## Build

**JDK 25** required. See `docs/BUILD.md`.

```bash
./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew chiseledBuild
```

## Textures

```bash
pip install -r tools/requirements.txt
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
python3 tools/upscale_textures.py
python3 tools/generate_structures.py
```

See `docs/textures/README.md`.

## Docs

- Example sheet: `docs/GENERATION_BOULANGERIE.md`
- Themes 1–24: `docs/generation/THEMES_01_24.md`
- Themes 25–32: `docs/generation/THEMES_25_32.md`
- French README: `README.md`

Mod ID: `unpredictableblocks` — display name: **UnpredictableBlocks**.
