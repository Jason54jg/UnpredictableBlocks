# Textes Modrinth — UnpredictableBlocks

Copier-coller dans la fiche projet. **Modrinth exige une description en anglais** ; le français est fourni en complément.

---

## Summary (court, sans formatage, ~1–2 phrases)

Chaotic themed Lucky Blocks with a luck system, quests, collectible cards, and dozens of custom animated mobs. Break a block, get blessed… or absolutely ruined.

---

## Body (description longue) — English

# UnpredictableBlocks

**UnpredictableBlocks** is a themed Lucky Block mod for modern Minecraft: break a block, trigger a chaotic event, loot, structure, or custom mob — then chase the theme quest if you dare.

Each Lucky Block has a **fixed theme** (texture, loot table, structures, quest, effects). What changes when you break it is the **luck tier**: High, Neutral, or Low. High is generous. Low is personal.

## What you get

- **35 themed Lucky Blocks** (common / rare / legendary) — bakery, BSOD, taxes, streamer clickbait, zero-G, disco, vampires, crypto crash, internet cats, and more
- **Luck system (−100 to +100)** with a HUD on the action bar
- **Custom GeckoLib mobs** per theme (object / creature silhouettes — no plain Steve clones)
- **Quests** (advancements) + **Quest Journal** item + inventory button
- **Collectible cards**, theme-master titles, and quest milestones
- **Natural world spawn**, optional world presets (Dense / Flat / Skyblock)
- **Craftable gear**, luck dust / curse, mystery Lucky Block, and themed consumables

## How to play

1. Find a Lucky Block in the world (or craft / creative / commands).
2. Check your **Luck** on the action bar.
3. Break the block.
4. Survive the event, grab the loot, talk to or fight any spawned mob.
5. Open the **Quest Journal** and finish the theme objective for rewards and cards.

### Improving (or ruining) your luck

- Craft and use **Emerald Luck Dust** (+10) or **Blaze Curse** (−10).
- **Luck bet**: sneak while breaking a Lucky Block with Emerald Dust in the off-hand for a temporary luck boost (consumes the dust).
- Some near-miss Low rolls still drop a consolation dust.
- Chunk **auras** can briefly push luck up or down after exploring.

### World types (create world)

| Preset | Idea |
|--------|------|
| Default | Vanilla generation + natural Lucky Block spawn |
| Lucky Dense | Same world, many more Lucky Blocks |
| Lucky Flat | Flat world with a Lucky Block grid |
| Lucky Skyblock | Void island with a regenerating Lucky Block |

### Density gamerules

```
/gamerule unpredictableblocks:lucky_density <0–1000>
/gamerule unpredictableblocks:lucky_shrines <true|false>
```

- `lucky_density`: `0` = none, `100` = normal, `1000` = ×10  
- `lucky_shrines`: pedestals / altars around natural Lucky Blocks

## Themes (overview)

Bakery, BSOD, poultry, taxes, streamer, cleanliness, zero gravity, fast food, morning nightmare, action movie, D20 / TTRPG, 2020 shortage, conspiracy, lag & bugs, disco 80s, plumbing, fortune cookie, camping, gym bro, modern art, countertop magic, meme invasion, coffee break, delayed flight, pineapple pizza, speedrunner, torrent piracy, ASMR fidget, gothic vampire, multiverse sliders, transport strike, crazy weather, internet cats, crypto bubble, anachronism / future-you…

Breaking a themed block can spawn its signature mob (living baguette, tax folder, donation alert, orbital helmet, meme cube, Count Dracula’s bat cloud, giant pop-it, and many more).

## Quests & collection

- One **quest per theme** (tracked as advancements).
- **Quest Journal** item (also a button in the inventory / creative screen).
- **Collectible cards** can drop on strong rolls; fill the album for an advancement.
- Milestones at roughly 8 / 16 / 24 / all themes completed.
- Become **theme master** by breaking enough of a given theme.

## Dependencies

| Loader | Required |
|--------|----------|
| **Fabric** | Fabric API + **GeckoLib** + Architectury API (bundled / listed on the version page) |
| **NeoForge** | **GeckoLib** + Architectury API |

Install the matching Minecraft version build (see version page). Client and server both need the mod for multiplayer.

## Important notes before download

- Events can **damage the player**, spawn hostile mobs, place temporary blocks, or cause small explosions / terrain changes. Not a “safe loot only” mod.
- Designed for **Minecraft 26.1.x–26.3** (see the version you download).
- Early development (`0.1.x`): expect balance tweaks and new content.
- Config file: `config/unpredictableblocks.json` (spawn weights / gameplay knobs). Reload with `/unpredictableblocks reload` (op).

## Admin / debug commands (op)

```
/unpredictableblocks place <theme> [luck]
/unpredictableblocks event <theme> <HIGH|NEUTRAL|LOW>
/unpredictableblocks givekit
/unpredictableblocks reload
/unpredictableblocks gameplay
/unpredictableblocks stats [theme]
```

## Why download this?

If you want Lucky Blocks that feel like **comedy sketches with teeth** — fixed themes, readable luck, real quests, and animated mascots — this is the chaos kit.

---

## Body — Français (optionnel, sous l’anglais)

# UnpredictableBlocks

**UnpredictableBlocks**, c’est des Lucky Blocks thématiques pour Minecraft moderne : tu casses un bloc, tu déclenches un événement absurde (loot, structure, mob custom)… puis tu enchaînes la quête du thème.

Chaque Lucky Block a un **thème fixe**. Ce qui change au cassage, c’est le **palier de chance** : High / Neutral / Low.

## Contenu

- **35 Lucky Blocks** thématiques (commun / rare / légendaire)
- **Système de Chance (−100 à +100)** + HUD
- **Mobs GeckoLib** custom (silhouettes objets / créatures)
- **Quêtes**, Journal des quêtes, **cartes à collectionner**
- Spawn naturel + presets Dense / Flat / Skyblock
- Objets de chance, Lucky mystère, équipements thématiques

## Comment jouer

1. Trouve (ou craft) un Lucky Block.
2. Regarde ta **Chance** dans la barre d’action.
3. Casse le bloc.
4. Survive, loot, interagir / combattre les mobs.
5. Ouvre le **Journal des quêtes** et termine l’objectif du thème.

### Booster (ou saboter) la chance

- **Poussière d’émeraude** (+10) / **Malédiction Blaze** (−10)
- **Pari Luck** : sneak + poussière en main secondaire au cassage
- Auras de chunk, near-miss, combos…

### Types de monde

- **Par défaut** — spawn naturel  
- **Lucky Dense** — beaucoup plus de blocs  
- **Lucky Flat** — grille sur monde plat  
- **Lucky Skyblock** — île + Lucky qui régénère  

```
/gamerule unpredictableblocks:lucky_density <0–1000>
/gamerule unpredictableblocks:lucky_shrines <true|false>
```

## Dépendances

Fabric API + **GeckoLib** (Fabric) · **GeckoLib** (NeoForge) · Architectury selon la version publiée.

## À savoir avant de télécharger

- Les événements peuvent **blesser**, spawner des hostiles, poser des blocs ou provoquer de petites explosions.
- Minecraft **26.1.x–26.3** selon le fichier choisi.
- Version early (`0.1.x`) : l’équilibrage évolue.
- Config : `config/unpredictableblocks.json`

## Commandes (op)

```
/unpredictableblocks place <theme> [luck]
/unpredictableblocks event <theme> <HIGH|NEUTRAL|LOW>
/unpredictableblocks givekit
/unpredictableblocks reload
```

---

## Champs metadata suggérés (Modrinth)

| Champ | Valeur |
|-------|--------|
| Title | UnpredictableBlocks |
| Categories | Adventure, Game Mechanics, Mobs, World Generation |
| Loaders | Fabric, NeoForge |
| Environment | Client & Server |
| Dependencies (version) | GeckoLib (required), Fabric API (Fabric), Architectury API |
| Content disclosures | Peut altérer le terrain / spawner des hostiles ; cocher selon ta checklist Modrinth |
| License | (celle du dépôt) |

### Tags utiles

`lucky-blocks`, `quests`, `mobs`, `adventure`, `fabric`, `neoforge`, `geckolib`
