#!/usr/bin/env python3
"""Génère JSON blockstates/models/loot/advancements pour les thèmes 25–32."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets/unpredictableblocks"
DATA = ROOT / "common/src/main/resources/data/unpredictableblocks"

THEMES = [
    ("pause_cafe", "Pause Café / Barista", "task"),
    ("retard_vol", "Retard de Vol / Aéroport", "goal"),
    ("ananas_pizza", "Ananas sur la Pizza", "goal"),
    ("speedrunner", "Speedrunner Ultra-Tryhard", "challenge"),
    ("piraterie_torrent", "Piraterie & Torrent", "goal"),
    ("asmr_fidget", "ASMR & Fidget Toy", "task"),
    ("vampire_gothique", "Vampire & Gothique", "goal"),
    ("multivers_sliders", "Multivers / Sliders", "challenge"),
]

ITEM_BY_THEME = {
    "pause_cafe": "espresso_ultra_dose",
    "retard_vol": "valise_perdue",
    "ananas_pizza": "tranche_debat",
    "speedrunner": "chrono_world_record",
    "piraterie_torrent": "disque_dur_capitaine",
    "asmr_fidget": "pop_it_supreme",
    "vampire_gothique": "fiole_sang_noble",
    "multivers_sliders": "zapper_dimensions",
}


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def loot_high(theme: str) -> dict:
    item = ITEM_BY_THEME[theme]
    return {
        "pools": [
            {
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": f"unpredictableblocks:{item}"}],
            },
            {
                "rolls": 2,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "unpredictableblocks:luck_emerald_dust",
                        "weight": 2,
                        "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 3}}],
                    }
                ],
            },
        ]
    }


def loot_neutral(theme: str) -> dict:
    return {
        "pools": [
            {
                "rolls": 2,
                "entries": [
                    {"type": "minecraft:item", "name": "minecraft:emerald", "weight": 1, "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 4}}]},
                    {"type": "minecraft:item", "name": "minecraft:cookie", "weight": 1, "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 8}}]},
                ],
            }
        ]
    }


def loot_low(theme: str) -> dict:
    return {
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:item",
                        "name": "unpredictableblocks:luck_blaze_curse",
                        "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 2}}],
                    }
                ],
            }
        ]
    }


def advancement(theme: str, title: str, frame: str) -> dict:
    return {
        "display": {
            "icon": {"id": f"unpredictableblocks:lucky_{theme}"},
            "title": {"translate": f"advancements.unpredictableblocks.{theme}.title", "fallback": title},
            "description": {
                "translate": f"advancements.unpredictableblocks.{theme}.description",
                "fallback": f"Quête thématique — {title}",
            },
            "frame": frame,
            "show_toast": True,
            "announce_to_chat": True,
        },
        "parent": "unpredictableblocks:root",
        "criteria": {
            "trigger_block": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": f"unpredictableblocks:lucky_{theme}"}]},
            }
        },
        "requirements": [["trigger_block"]],
    }


def main() -> None:
    for theme, title, frame in THEMES:
        write_json(
            ASSETS / f"blockstates/lucky_{theme}.json",
            {"variants": {"": {"model": f"unpredictableblocks:block/lucky_{theme}"}}},
        )
        write_json(
            ASSETS / f"models/block/lucky_{theme}.json",
            {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": f"unpredictableblocks:block/lucky_{theme}"},
            },
        )
        write_json(
            ASSETS / f"models/item/lucky_{theme}.json",
            {"parent": f"unpredictableblocks:block/lucky_{theme}"},
        )
        # MC 26+ client item definition (required or inventory shows missing texture)
        write_json(
            ASSETS / f"items/lucky_{theme}.json",
            {
                "model": {
                    "type": "minecraft:model",
                    "model": f"unpredictableblocks:block/lucky_{theme}",
                }
            },
        )
        for tier, builder in [("high", loot_high), ("neutral", loot_neutral), ("low", loot_low)]:
            write_json(
                DATA / f"loot_table/gameplay/lucky/{theme}/{tier}.json",
                builder(theme),
            )
        write_json(DATA / f"advancement/quest/{theme}.json", advancement(theme, title, frame))
        print("scaffold", theme)


if __name__ == "__main__":
    main()
