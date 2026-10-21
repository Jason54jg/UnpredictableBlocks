# Fiches thèmes 1–24

Minecraft **26.1.2–26.3**, Mojmap, NeoForge/Fabric. Textures **32×32 natif** (`tools/generate_textures.py`).

**Effets** : pour chaque thème, si `ThemeCatalogEffects.applies(theme, tier)` → événement **catalogue** (directive §4) ; sinon **legacy** (`ThemeEventEffects.applyLegacy`). Pas de double déclenchement.

Légende tiers : **HIGH** ≈ chance +100, **NEUTRAL** ≈ 0, **LOW** ≈ −100 (modulé par `LuckResolver`).

---

## 1. Anachronisme (`lucky_anachronisme`) — Rare

- **Biomes** : plaines, badlands — **Structure** : `pyramide_piege` (HIGH/NEUTRAL)
- **32×32** : split cuivre / néon cyan, rouage, ? or
- **Catalogue HIGH** : « Toi du Futur » (zombie Netherite allié)
- **Legacy** : NEUTRAL ravageur ; LOW inventaire stone+flint ; HIGH résistance
- **Quête** : `quest/anachronisme` — récompense Lucky +100

## 2. Boulangerie (`lucky_boulangerie`) — Commun

- Voir [`GENERATION_BOULANGERIE.md`](../GENERATION_BOULANGERIE.md)
- **Catalogue LOW** : pluie pain rassis (hay blocks)
- **Structure** : `four_geant`

## 3. BSOD (`lucky_bsod`) — Légendaire

- **32×32** : bleu #0000AA, lignes blanches, smiley `:` `(` en 32 px
- **Catalogue NEUTRAL** : Alt+Tab (lenteur + blindness 15s)
- **Legacy** : HIGH speed+haste ; NEUTRAL darkness ; LOW blindness+nausée

## 4. Volaille suprême (`lucky_volaille`) — Commun

- **32×32** : plumes, crête rouge, bec, reflets plume 32 px
- **Catalogue HIGH** : poulets « œuf d’or incubateur »
- **Legacy** : HIGH spawn poulets ; LOW golem fer ; NEUTRAL caquetage

## 5. Administration / Impôts (`lucky_impots`) — Rare

- **32×32** : classeur gris, ruban jaune, tampons REJETÉ rouges
- **Catalogue LOW** : grève générale (mining fatigue + weakness 45s)
- **Legacy** : HIGH émeraudes+or ; LOW weakness

## 6. Streamer (`lucky_streamer`) — Rare

- **32×32** : dégradé, bouton LIKE, flèche subscribe 32 px
- **Catalogue NEUTRAL** : NordVPN (invisibilité, pseudo masqué)
- **Legacy** : LOW horde zombies ; HIGH explosion vide

## 7. Propreté (`lucky_proprete`) — Commun

- **32×32** : bulles savon, éponge or
- **Catalogue HIGH** : douche désinfectante (clear effects + résistance poison 5 min)
- **Legacy** : HIGH clear hostiles ; NEUTRAL laine ; LOW blindness+glace

## 8. Gravité zéro (`lucky_gravite_zero`) — Légendaire

- **32×32** : ciel violet, étoiles, planète, halo 32 px
- **Catalogue LOW** : pression nulle (wither + dégâts « noyade »)
- **Legacy** : HIGH lévitation ; NEUTRAL boost vertical ; LOW void damage

## 9. Fast-Food (`lucky_fast_food`) — Commun

- **Structure** : `drive_thru`
- **Catalogue NEUTRAL** : mascotte hamburger (golem nommé)
- **Legacy** : HIGH/NEUTRAL nourriture ; NEUTRAL slowness+blind ; LOW zombie

## 10. Cauchemar matinée (`lucky_cauchemar_matin`) — Rare (nuit)

- **Catalogue HIGH** : rêve lucide (lévitation 30s)
- **Legacy** : LOW faim 0 + slowness ; HIGH téléport lit

## 11. Film d’action (`lucky_film_action`) — Rare

- **Structure** : `tapis_rouge`
- **Catalogue LOW** : sniper squelette + petite explosion
- **Legacy** : HIGH saut ; NEUTRAL explosion TNT

## 12. JDR & D20 (`lucky_jdr_d20`) — Rare

- **Catalogue NEUTRAL** : jet Perception (Glow 45s)
- **Legacy** : LOW poison + casse arme ; HIGH épée netherite ; tier → `JdrDiceRoll`

## 13. Pénurie 2020 (`lucky_penurie_2020`) — Commun

- **Structure** : `pyramide_pq`
- **Catalogue HIGH** : grossiste clandestin (villageois)
- **Legacy** : HIGH paper stack ; LOW strip paper items

## 14. Théorie du complot (`lucky_complot`) — Légendaire

- **Structure** : `bunker_zone51`
- **Catalogue LOW** : puce 5G (Glow 600s)
- **Legacy** : LOW endermen ; NEUTRAL flatten zone

## 15. Lag & Bug (`lucky_lag_bug`) — Légendaire

- **32×32** : damier magenta/noir plein 32, barres glitch vertes
- **Catalogue NEUTRAL** : desync hotbar (shuffle)
- **Legacy** : LOW hollow slice ; HIGH duplicate animals

## 16. Disco 80's (`lucky_disco_80`) — Rare (nuit)

- **Structure** : `piste_disco`
- **Catalogue HIGH** : stroboscopie (nausée mobs + sea lanterns)
- **Legacy** : HIGH/NEUTRAL glowstone floor

## 17. Plomberie (`lucky_plomberie`) — Commun

- **Structure** : `chateau_briques`
- **Catalogue LOW** : geyser boue (propulsion + mud)
- **Legacy** : HIGH buffs ; NEUTRAL téléport bas ; LOW silverfish

## 18. Fortune Cookie (`lucky_fortune_cookie`) — Rare

- **Structure** : `autel_mochi`
- **Catalogue NEUTRAL** : code fortune 5s → `/say` + 500 XP (`FortuneCookieHandler`)
- **Legacy** : HIGH absorption ; LOW poison+blind

## 19. Camping (`lucky_camping`) — Commun

- **Structure** : `campement_inonde`
- **Catalogue HIGH** : shulker survie
- **Legacy** : LOW polar bear ; HIGH campfire block

## 20. Go muscu (`lucky_gym_bro`) — Rare

- **Structure** : `salle_gym`
- **Catalogue LOW** : crampe (slowness 255, 10s)
- **Legacy** : HIGH strength+haste ; NEUTRAL speed ; LOW slowness+fatigue

## 21. Art moderne (`lucky_art_moderne`) — Rare

- **Structure** : `galerie_absurde`
- **Catalogue NEUTRAL** : statues (slowness 255 + glow entités)
- **Legacy** : NEUTRAL blocs béton blanc

## 22. Magie de comptoir (`lucky_magie_comptoir`) — Commun

- **Structure** : `scene_spectacle`
- **Catalogue HIGH** : lapin extirpateur
- **Legacy** : NEUTRAL rabbits ; LOW wolf

## 23. Casino (`lucky_casino`) — Légendaire

- **Structure** : `table_poker_vip` — **CasinoJackpot** au break
- **Catalogue LOW** : banqueroute (cuir vert, XP reset)
- **Legacy** : HIGH gems ; LOW strip currency

## 24. Invasion de mèmes (`lucky_invasion_memes`) — Légendaire

- **Structure** : `sanctuaire_doge`
- **32×32** : Doge/Pepe, trait Nyan, carré rose
- **Catalogue NEUTRAL** : Emotional Damage (knockback message)
- **Legacy** : HIGH levitation+speed ; NEUTRAL skeletons — **Quête** : 50 coups marteau BONK

---

## Quêtes & récompenses (1–24)

Advancements : `data/unpredictableblocks/advancement/quest/<id>.json`  
Loot récompense : `data/unpredictableblocks/loot_table/rewards/quest_<id>.json` → Lucky Block `{Luck:100}` (posé via `LuckyBlockItem.readLuckFromStack`).

Quêtes **1–24** : logique runtime dans `QuestLegacyHandler.java` (kills, effets, inventaire) + critères `impossible` côté JSON.

Quêtes **25–32** : `QuestExtendedHandler.java` — voir [`THEMES_25_32.md`](THEMES_25_32.md).
