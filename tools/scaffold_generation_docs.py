#!/usr/bin/env python3
"""Génère docs/generation/GENERATION_<theme>.md (fiches courtes, 32 thèmes)."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/generation"

THEMES: list[tuple[int, str, str, str]] = [
    (1, "anachronisme", "Anachronisme", "docs/generation/THEMES_01_24.md#1-anachronisme-lucky_anachronisme--rare"),
    (2, "boulangerie", "Boulangerie française", "../GENERATION_BOULANGERIE.md"),
    (3, "bsod", "BSOD", "THEMES_01_24.md#3-bsod-lucky_bsod--légendaire"),
    (4, "volaille", "Volaille suprême", "THEMES_01_24.md#4-volaille-suprême-lucky_volaille--commun"),
    (5, "impots", "Administration & impôts", "THEMES_01_24.md#5-administration--impôts-lucky_impots--rare"),
    (6, "streamer", "Streamer / clickbait", "THEMES_01_24.md#6-streamer-lucky_streamer--rare"),
    (7, "proprete", "Propreté extrême", "THEMES_01_24.md#7-propreté-lucky_proprete--commun"),
    (8, "gravite_zero", "Gravité zéro", "THEMES_01_24.md#8-gravité-zéro-lucky_gravite_zero--légendaire"),
    (9, "fast_food", "Fast-food", "THEMES_01_24.md#9-fast-food-lucky_fast_food--commun"),
    (10, "cauchemar_matin", "Cauchemar de matinée", "THEMES_01_24.md#10-cauchemar-matinée-lucky_cauchemar_matin--rare"),
    (11, "film_action", "Film d'action", "THEMES_01_24.md#11-film-daction-lucky_film_action--rare"),
    (12, "jdr_d20", "JDR & D20", "THEMES_01_24.md#12-jdr--d20-lucky_jdr_d20--rare"),
    (13, "penurie_2020", "Pénurie 2020", "THEMES_01_24.md#13-pénurie-2020-lucky_penurie_2020--commun"),
    (14, "complot", "Théorie du complot", "THEMES_01_24.md#14-théorie-du-complot-lucky_complot--légendaire"),
    (15, "lag_bug", "Lag & bug", "THEMES_01_24.md#15-lag--bug-lucky_lag_bug--légendaire"),
    (16, "disco_80", "Soirée disco 80's", "THEMES_01_24.md#16-disco-80s-lucky_disco_80--rare"),
    (17, "plomberie", "Plomberie", "THEMES_01_24.md#17-plomberie-lucky_plomberie--commun"),
    (18, "fortune_cookie", "Fortune cookie", "THEMES_01_24.md#18-fortune-cookie-lucky_fortune_cookie--rare"),
    (19, "camping", "Camping catastrophe", "THEMES_01_24.md#19-camping-lucky_camping--commun"),
    (20, "gym_bro", "Go muscu", "THEMES_01_24.md#20-go-muscu-lucky_gym_bro--rare"),
    (21, "art_moderne", "Art moderne", "THEMES_01_24.md#21-art-moderne-lucky_art_moderne--rare"),
    (22, "magie_comptoir", "Magie de comptoir", "THEMES_01_24.md#22-magie-de-comptoir-lucky_magie_comptoir--commun"),
    (23, "casino", "Casino & jackpot", "THEMES_01_24.md#23-casino-lucky_casino--légendaire"),
    (24, "invasion_memes", "Invasion de mèmes", "THEMES_01_24.md#24-invasion-de-mèmes-lucky_invasion_memes--légendaire"),
    (25, "pause_cafe", "Pause café / barista", "THEMES_25_32.md#25-pause-café--barista-lucky_pause_cafe"),
    (26, "retard_vol", "Retard de vol", "THEMES_25_32.md#26-retard-de-vol--aéroport-lucky_retard_vol"),
    (27, "ananas_pizza", "Ananas sur la pizza", "THEMES_25_32.md#27-ananas-sur-la-pizza-lucky_ananas_pizza"),
    (28, "speedrunner", "Speedrunner", "THEMES_25_32.md#28-speedrunner-ultra-tryhard-lucky_speedrunner"),
    (29, "piraterie_torrent", "Piraterie & torrent", "THEMES_25_32.md#29-piraterie--torrent-lucky_piraterie_torrent"),
    (30, "asmr_fidget", "ASMR & fidget", "THEMES_25_32.md#30-asmr--fidget-lucky_asmr_fidget"),
    (31, "vampire_gothique", "Vampire & gothique", "THEMES_25_32.md#31-vampire--gothique-lucky_vampire_gothique"),
    (32, "multivers_sliders", "Multivers / sliders", "THEMES_25_32.md#32-multivers--sliders-lucky_multivers_sliders"),
]

TEMPLATE = """# Lucky Block — {title} (thème {num})

- **ID bloc** : `unpredictableblocks:lucky_{id}`
- **Texture** : `assets/unpredictableblocks/textures/block/lucky_{id}.png` (32×32 natif)
- **Fiche détaillée** : [{anchor}]({link})

## Fichiers data

| Type | Chemin |
|------|--------|
| Loot HIGH/NEUTRAL/LOW | `data/unpredictableblocks/loot_table/gameplay/lucky/{id}/` |
| Quête | `data/unpredictableblocks/advancement/quest/{id}.json` |
| Récompense quête | `data/unpredictableblocks/loot_table/rewards/quest_{id}.json` |

## Regénération texture

```bash
python3 tools/generate_textures.py
```

Voir aussi [`docs/textures/CATALOGUE.md`](../textures/CATALOGUE.md).
"""


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for num, tid, title, link in THEMES:
        if tid == "boulangerie":
            continue  # fiche complète existante
        anchor = title
        path = OUT / f"GENERATION_{tid}.md"
        body = TEMPLATE.format(num=num, id=tid, title=title, anchor=anchor, link=link)
        path.write_text(body, encoding="utf-8")
        print("wrote", path.relative_to(ROOT))
    print(f"Done ({len(THEMES) - 1} fiches; boulangerie → docs/GENERATION_BOULANGERIE.md).")


if __name__ == "__main__":
    main()
