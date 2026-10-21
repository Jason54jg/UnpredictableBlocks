# Fiches de génération — thèmes 25 à 32

Référence alignée sur le cahier des charges UnpredictableBlocks (Minecraft 26.1.x, NeoForge/Fabric, Mojmap). Textures : **32×32 natif** (`tools/generate_textures.py`, `pixel_canvas.py`).

---

## 25. Pause Café / Barista (`lucky_pause_cafe`)

| | |
|---|---|
| **Rareté / biomes** | Commun — plaines, villages |
| **Palette** | #4A2C1A bois, tasse #FFFFFF, espresso #2F1810, vapeur rgba blanc |
| **32×32** | Vapeur elliptique au-dessus tasse, manche porte-filtre, `?` crème |
| **+100** | Overdose énergie pratique (Haste IV + Speed III, 2 min) |
| **0** | (legacy si palier non catalogue) |
| **-100** | — |
| **Item** | `espresso_ultra_dose` |
| **Quête** | *Le Coup de Barre* — 5 espressos, faim > 0 → advancement + Lucky +100 |
| **Structure** | `barista_corner.nbt` |

---

## 26. Retard de vol / Aéroport (`lucky_retard_vol`)

| | |
|---|---|
| **Rareté / biomes** | Rare — plaines, déserts |
| **Palette** | ciel #87CEEB, terminal #C0C0C0, avion blanc, bande piste #37474F |
| **32×32** | Piste horizontale, valise jaune sur tapis |
| **+100** | Loot high + valise |
| **-100** | Cage verre + PNJ Douanier |
| **Item** | `valise_perdue` (shulker + cuir) |
| **Quête** | *Passage de Douane* — 3 valises utilisées |
| **Structure** | `tour_controle.nbt` |

---

## 27. Ananas sur la pizza (`lucky_ananas_pizza`)

| | |
|---|---|
| **Rareté / biomes** | Rare — jungles, savanes |
| **Palette** | croûte #E8C040 / #FFCC00, ananas #FFD700, sauce #D84315 (arc) |
| **32×32** | 5 pépites ananas, arc pepperoni |
| **0** | 10 Chefs Italiens (villagers nommés) |
| **Item** | `tranche_debat` (chat public) |
| **Quête** | *La Guerre Italienne* — 10 chefs tués sans manger la tranche |
| **Structure** | `pizzeria_debat.nbt` |

---

## 28. Speedrunner ultra-tryhard (`lucky_speedrunner`)

| | |
|---|---|
| **Rareté / biomes** | Légendaire — Nether, strongholds |
| **Palette** | fond #111111, chrono #00FF00, texte WR |
| **32×32** | Barre timing + « pieds » stick figure |
| **+100** | Téléport skip donjon (approx.) |
| **Item** | `chrono_world_record` (TP 15 blocs) |
| **Quête** | *Any% Glitchless* — chrono au break → End < 3 min |
| **Structure** | `stronghold_skip.nbt` |

---

## 29. Piraterie & Torrent (`lucky_piraterie_torrent`)

| | |
|---|---|
| **Rareté / biomes** | Rare — océans, épaves |
| **Palette** | mer #1A3A5A, HDD #333333, LED #00E676 |
| **32×32** | Barres progression « download » vertes |
| **-100** | Ransomware coffres/fours → fer |
| **Item** | `disque_dur_capitaine` |
| **Quête** | *Seed & Peer* — use disque → carte + livre |
| **Structure** | `epave_pirate.nbt` |

---

## 30. ASMR & Fidget (`lucky_asmr_fidget`)

| | |
|---|---|
| **Rareté / biomes** | Commun — tous biomes |
| **Palette** | #FFB6C1, bulles #FF69B4 |
| **32×32** | Grille pop-it 32 px (cercles outline) |
| **0** | Bulle savon (verre + lévitation) |
| **Item** | `pop_it_supreme` |
| **Quête** | *Instant Réduction du Stress* — 50 laine / honeycomb cassés |
| **Structure** | `bulle_asmr.nbt` |

---

## 31. Vampire & Gothique (`lucky_vampire_gothique`)

| | |
|---|---|
| **Rareté / biomes** | Rare — forêts sombres, manoirs |
| **Palette** | #1A001A, sang #8B0000 / #FF0000, cape #4A148C |
| **32×32** | Double cape, ligne morsure diagonale |
| **+100** | Pacte ténèbres (invis + speed + force) |
| **NEUTRAL** | Spawn Comte Dracula (quête) |
| **Item** | `fiole_sang_noble` |
| **Quête** | *Créature de la Nuit* — Dracula sans armure métal |
| **Structure** | `manoir_gothique.nbt` |

---

## 32. Multivers / Sliders (`lucky_multivers_sliders`)

| | |
|---|---|
| **Rareté / biomes** | Légendaire — Nether, End |
| **Palette** | split #4400FF / #FF4400, slider blanc #FFFFFF |
| **32×32** | Poignée slider + trait central |
| **-100** | Inversion gravité + quête 3 biomes / 2 min |
| **+100** | Zapper entité (TP vertical) |
| **Item** | `zapper_dimensions` |
| **Quête** | *Raccordement Temporel* |
| **Structure** | `portail_sliders.nbt` |

---

## Régénération assets

```bash
python3 tools/scaffold_themes_25_32.py   # loot/advancements si besoin
python3 tools/generate_textures.py
python3 tools/generate_item_textures.py
```
