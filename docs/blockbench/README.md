# Blockbench — UnpredictableBlocks (32 Lucky Blocks)

Format : **Java Block** · Texture **32×32** (spritesheet vertical) · Cube **16×16×16**

## Ouvrir

1. Installer [Blockbench](https://www.blockbench.net/)
2. File → Open → `lucky_<id>.bbmodel`
3. Animation panel → jouer **idle** (boucle)
4. Les `.png` + `.png.mcmeta` sont à côté pour Minecraft (animation texture)

## Régénérer

```bash
python tools/generate_blockbench.py
```

## Catalogue

| # | Fichier | Thème | Concept idle | Géométrie |
|---|---------|-------|--------------|-----------|
| 01 | `lucky_anachronisme` | Anachronisme | Sablier bronze qui coule + pulse néon cyan | none |
| 02 | `lucky_boulangerie` | Boulangerie Française | Baguette/croissant + vapeur montante | none |
| 03 | `lucky_bsod` | BSOD | Sad face + scanlines + curseur blink | none |
| 04 | `lucky_volaille` | Volaille Suprême | Poule + plume flottante + clignement | none |
| 05 | `lucky_impots` | Administration & Impôts | Dossier + tampon qui tape | none |
| 06 | `lucky_streamer` | Streamer / Clickbait | Caméra + LIVE blink + confettis | none |
| 07 | `lucky_proprete` | Propreté Extrême | Éponge + bulles montantes + shine | none |
| 08 | `lucky_gravite_zero` | Gravité Zéro | Astronaute float + étoiles | float |
| 09 | `lucky_fast_food` | Fast-Food | Burger + vapeur + sesame sparkle | none |
| 10 | `lucky_cauchemar_matin` | Cauchemar de Matinée | Réveil alarme shake + flash | none |
| 11 | `lucky_film_action` | Film d'Action | Clap + sparks + boom pulse | none |
| 12 | `lucky_jdr_d20` | JDR & D20 | D20 spin + glow crit | d20 |
| 13 | `lucky_penurie_2020` | Pénurie de 2020 | PQ/caddie + étiquette prix flicker | none |
| 14 | `lucky_complot` | Théorie du Complot | Pyramide œil cligne + static | none |
| 15 | `lucky_lag_bug` | Lag & Bug | Missing cube + stutter tear UV | none |
| 16 | `lucky_disco_80` | Soirée Disco 80's | Boule disco RGB + sparkle | none |
| 17 | `lucky_plomberie` | Plomberie | Clé + drip gouttes | none |
| 18 | `lucky_fortune_cookie` | Fortune Cookie | Papier sort/rentre | paper |
| 19 | `lucky_camping` | Camping Catastrophe | Tente + flammes flicker | none |
| 20 | `lucky_gym_bro` | Go Muscu | Haltère bounce + pulse | none |
| 21 | `lucky_art_moderne` | Art Moderne | Formes qui morphent | none |
| 22 | `lucky_magie_comptoir` | Magie de Comptoir | Chapeau + carte flip + étincelles | none |
| 23 | `lucky_casino` | Casino & Jackpot | Rouleaux slots + flash jackpot | casino |
| 24 | `lucky_invasion_memes` | Invasion de Mèmes | Troll pixel bounce + pop zoom | none |
| 25 | `lucky_pause_cafe` | Pause Café / Barista | Tasse + vapeur + ripple | none |
| 26 | `lucky_retard_vol` | Retard de Vol / Aéroport | Panneau DELAYED blink | none |
| 27 | `lucky_ananas_pizza` | Ananas sur la Pizza | Pizza + ananas bounce + shine | none |
| 28 | `lucky_speedrunner` | Speedrunner Ultra-Tryhard | Timer ticks + speed lines | none |
| 29 | `lucky_piraterie_torrent` | Piraterie & Torrent | Voile + barre download loop | none |
| 30 | `lucky_asmr_fidget` | ASMR & Fidget Toy | Pop-it bubbles pop sequence | none |
| 31 | `lucky_vampire_gothique` | Vampire & Gothique | Chauve-souris flap + drip sang | bat |
| 32 | `lucky_multivers_sliders` | Multivers / Sliders | Portail ripples + color shift | none |

## Notes

- Priorité **animation texture** (`.mcmeta`) ; géométrie seulement pour D20 / casino / float / paper / bat.
- Groupes : `root` → `block` → (`emblem`, `fx`).
- Style pixel art humoristique, cadre Lucky Block stable.
