#!/usr/bin/env python3
"""Remplace les loot tables placeholder (count: 0) par du loot thématique."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOOT_DIR = ROOT / "common/src/main/resources/data/unpredictableblocks/loot_table/gameplay/lucky"

NS = "unpredictableblocks"


def entry_item(name: str, weight: int = 1, count: int | None = None, count_min: int | None = None, count_max: int | None = None):
    e: dict = {"type": "minecraft:item", "name": name, "weight": weight}
    if count is not None or count_min is not None:
        fn: dict = {"function": "minecraft:set_count"}
        if count is not None:
            fn["count"] = count
        else:
            fn["count"] = {"min": count_min, "max": count_max}
        e["functions"] = [fn]
    return e


def table(*pools: dict) -> dict:
    return {"pools": list(pools)}


def pool(rolls: int | dict, entries: list[dict]) -> dict:
    p: dict = {"rolls": rolls, "entries": entries}
    return p


def luck_dust(high: bool) -> dict:
    if high:
        return entry_item(f"{NS}:luck_emerald_dust", 2, count_min=2, count_max=5)
    return entry_item(f"{NS}:luck_blaze_curse", 2, count_min=1, count_max=3)


# Tier templates: high = généreux + item mod ; neutral = mix ; low = malus loot léger
THEME_ENTRIES: dict[str, dict[str, list[dict]]] = {
    "anachronisme": {
        "high": [
            pool(1, [entry_item("minecraft:diamond_sword", 1), entry_item("minecraft:chainmail_chestplate", 1)]),
            pool(1, [entry_item("minecraft:clock", 2, count=1), luck_dust(True)]),
        ],
        "neutral": [pool(2, [entry_item("minecraft:iron_sword", 2), entry_item("minecraft:book", 2, count_min=1, count_max=3)])],
        "low": [pool(1, [entry_item("minecraft:cobweb", 3, count_min=4, count_max=8), luck_dust(False)])],
    },
    "art_moderne": {
        "high": [pool(1, [entry_item("minecraft:painting", 1), entry_item("minecraft:lime_dye", 2, count_min=4, count_max=8)])],
        "neutral": [pool(2, [entry_item("minecraft:brush", 1), entry_item("minecraft:clay", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 1, count_min=4, count_max=8), luck_dust(False)])],
    },
    "bsod": {
        "low": [pool(1, [entry_item("minecraft:blue_concrete", 2, count_min=16, count_max=32), entry_item("minecraft:paper", 1, count=1)])],
        "neutral": [pool(1, [entry_item("minecraft:lapis_block", 1), entry_item(f"{NS}:cle_usb_hacker", 1)])],
    },
    "volaille": {
        "high": [pool(1, [entry_item("minecraft:cooked_chicken", 2, count_min=16, count_max=32), entry_item("minecraft:egg", 2, count_min=8, count_max=16)])],
        "neutral": [pool(2, [entry_item("minecraft:chicken", 2), entry_item("minecraft:feather", 2, count_min=4, count_max=12)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 2, count_min=8, count_max=16), luck_dust(False)])],
    },
    "impots": {
        "high": [pool(1, [entry_item("minecraft:gold_ingot", 2, count_min=8, count_max=16), entry_item("minecraft:emerald", 1, count_min=4, count_max=8)])],
        "neutral": [pool(1, [entry_item("minecraft:paper", 3, count_min=8, count_max=16), entry_item("minecraft:iron_ingot", 1, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:poisonous_potato", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "streamer": {
        "high": [
            pool(1, [entry_item(f"{NS}:cle_usb_hacker", 1)]),
            pool(1, [entry_item("minecraft:emerald", 2, count_min=4, count_max=12), luck_dust(True)]),
        ],
        "neutral": [pool(2, [entry_item("minecraft:glowstone_dust", 2, count_min=4, count_max=8), entry_item("minecraft:redstone", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:spider_eye", 1, count_min=2, count_max=4), luck_dust(False)])],
    },
    "proprete": {
        "high": [pool(1, [entry_item("minecraft:sponge", 1), entry_item("minecraft:clay_ball", 2, count_min=16, count_max=32)])],
        "neutral": [pool(2, [entry_item("minecraft:clay_ball", 2, count_min=16, count_max=32), entry_item("minecraft:water_bucket", 1)])],
        "low": [pool(1, [entry_item("minecraft:slime_ball", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "gravite_zero": {
        "high": [pool(1, [entry_item("minecraft:feather", 2, count_min=16, count_max=32), entry_item("minecraft:phantom_membrane", 1, count_min=2, count_max=4)])],
        "neutral": [pool(1, [entry_item("minecraft:leather_boots", 1), entry_item("minecraft:sugar", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:anvil", 1), luck_dust(False)])],
    },
    "fast_food": {
        "high": [pool(1, [entry_item("minecraft:cooked_beef", 2, count_min=32, count_max=64), entry_item("minecraft:golden_carrot", 1, count_min=4, count_max=8)])],
        "neutral": [pool(2, [entry_item("minecraft:bread", 2, count_min=8, count_max=16), entry_item("minecraft:potato", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:poisonous_potato", 3, count_min=4, count_max=8), luck_dust(False)])],
    },
    "cauchemar_matin": {
        "high": [pool(1, [entry_item("minecraft:golden_apple", 1, count_min=2, count_max=4), entry_item("minecraft:cocoa_beans", 2, count_min=8, count_max=16)])],
        "neutral": [pool(1, [entry_item("minecraft:white_bed", 1), entry_item("minecraft:phantom_membrane", 2, count_min=1, count_max=3)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "film_action": {
        "high": [pool(1, [entry_item("minecraft:tnt", 2, count_min=4, count_max=8), entry_item("minecraft:crossbow", 1)])],
        "neutral": [pool(2, [entry_item("minecraft:gunpowder", 2, count_min=8, count_max=16), entry_item("minecraft:arrow", 2, count_min=16, count_max=32)])],
        "low": [pool(1, [entry_item("minecraft:fire_charge", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "jdr_d20": {
        "high": [pool(1, [entry_item("minecraft:enchanted_book", 1), entry_item("minecraft:experience_bottle", 2, count_min=8, count_max=16)])],
        "neutral": [pool(1, [entry_item("minecraft:book", 2, count_min=4, count_max=8), entry_item("minecraft:paper", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:bone", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "penurie_2020": {
        "high": [pool(1, [entry_item("minecraft:paper", 2, count_min=32, count_max=64), entry_item("minecraft:bread", 2, count_min=16, count_max=32)])],
        "neutral": [pool(1, [entry_item("minecraft:paper", 3, count_min=8, count_max=16), entry_item("minecraft:carrot", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "complot": {
        "high": [pool(1, [entry_item(f"{NS}:casque_5g", 1), entry_item("minecraft:ender_pearl", 2, count_min=2, count_max=4)])],
        "neutral": [pool(1, [entry_item("minecraft:amethyst_shard", 2, count_min=4, count_max=8), entry_item("minecraft:spyglass", 1)])],
        "low": [pool(1, [entry_item("minecraft:phantom_membrane", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "lag_bug": {
        "high": [pool(1, [entry_item(f"{NS}:cle_usb_hacker", 1), entry_item("minecraft:redstone_block", 1, count_min=4, count_max=8)])],
        "neutral": [pool(2, [entry_item("minecraft:repeater", 2, count_min=4, count_max=8), entry_item("minecraft:comparator", 2, count_min=2, count_max=4)])],
        "low": [pool(1, [entry_item("minecraft:slime_block", 1, count_min=4, count_max=8), luck_dust(False)])],
    },
    "disco_80": {
        "high": [pool(1, [entry_item("minecraft:music_disc_13", 1), entry_item("minecraft:leather_boots", 1)])],
        "neutral": [pool(2, [entry_item("minecraft:glow_ink_sac", 2, count_min=4, count_max=8), entry_item("minecraft:purple_dye", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:cocoa_beans", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "plomberie": {
        "high": [pool(1, [entry_item(f"{NS}:cle_molette_legendaire", 1), entry_item("minecraft:copper_ingot", 2, count_min=16, count_max=32)])],
        "neutral": [pool(1, [entry_item("minecraft:bucket", 1), entry_item("minecraft:iron_ingot", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:water_bucket", 1), luck_dust(False)])],
    },
    "fortune_cookie": {
        "high": [pool(1, [entry_item("minecraft:cookie", 2, count_min=16, count_max=32), entry_item("minecraft:emerald", 1, count_min=2, count_max=4)])],
        "neutral": [pool(1, [entry_item("minecraft:cookie", 3, count_min=4, count_max=8), entry_item("minecraft:paper", 2, count_min=1, count_max=3)])],
        "low": [pool(1, [entry_item("minecraft:poisonous_potato", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "camping": {
        "high": [pool(1, [entry_item("minecraft:campfire", 2, count_min=2, count_max=4), entry_item("minecraft:cooked_salmon", 2, count_min=8, count_max=16)])],
        "neutral": [pool(1, [entry_item("minecraft:leather", 2, count_min=4, count_max=8), entry_item("minecraft:torch", 2, count_min=16, count_max=32)])],
        "low": [pool(1, [entry_item("minecraft:stick", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "gym_bro": {
        "high": [pool(1, [entry_item(f"{NS}:shaker_prot", 1), entry_item("minecraft:golden_carrot", 2, count_min=8, count_max=16)])],
        "neutral": [pool(2, [entry_item("minecraft:cooked_beef", 2, count_min=8, count_max=16), entry_item("minecraft:iron_ingot", 1, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "magie_comptoir": {
        "high": [pool(1, [entry_item("minecraft:brewing_stand", 1), entry_item("minecraft:blaze_powder", 2, count_min=4, count_max=8)])],
        "neutral": [pool(1, [entry_item("minecraft:glass_bottle", 2, count_min=8, count_max=16), entry_item("minecraft:nether_wart", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:spider_eye", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "invasion_memes": {
        "low": [pool(1, [entry_item(f"{NS}:marteau_bonk", 1), luck_dust(False)])],
        "neutral": [pool(1, [entry_item("minecraft:slime_ball", 2, count_min=8, count_max=16), entry_item("minecraft:egg", 2, count_min=8, count_max=16)])],
    },
    "pause_cafe": {
        "high": [pool(1, [entry_item(f"{NS}:espresso_ultra_dose", 1), luck_dust(True)])],
        "neutral": [pool(1, [entry_item("minecraft:cocoa_beans", 2, count_min=4, count_max=8), entry_item("minecraft:sugar", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:rotten_flesh", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "retard_vol": {
        "high": [pool(1, [entry_item(f"{NS}:valise_perdue", 1), entry_item("minecraft:leather", 2, count_min=4, count_max=8)])],
        "neutral": [pool(1, [entry_item("minecraft:paper", 2, count_min=8, count_max=16), entry_item("minecraft:compass", 1)])],
        "low": [pool(1, [entry_item("minecraft:tripwire_hook", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "ananas_pizza": {
        "high": [pool(1, [entry_item(f"{NS}:tranche_debat", 1), entry_item("minecraft:golden_carrot", 2, count_min=2, count_max=4)])],
        "neutral": [pool(1, [entry_item("minecraft:bread", 2, count_min=4, count_max=8), entry_item("minecraft:melon_slice", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [luck_dust(False)])],
    },
    "speedrunner": {
        "high": [pool(1, [entry_item(f"{NS}:chrono_world_record", 1), entry_item("minecraft:ender_pearl", 2, count_min=2, count_max=4)])],
        "neutral": [pool(1, [entry_item("minecraft:golden_apple", 1), entry_item("minecraft:cooked_beef", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:slime_block", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "piraterie_torrent": {
        "high": [pool(1, [entry_item(f"{NS}:disque_dur_capitaine", 1), entry_item("minecraft:map", 1)])],
        "neutral": [pool(1, [entry_item("minecraft:writable_book", 2, count_min=1, count_max=2), entry_item("minecraft:paper", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [luck_dust(False)])],
    },
    "asmr_fidget": {
        "high": [pool(1, [entry_item(f"{NS}:pop_it_supreme", 1), luck_dust(True)])],
        "neutral": [pool(1, [entry_item("minecraft:white_wool", 2, count_min=8, count_max=16), entry_item("minecraft:honeycomb", 2, count_min=4, count_max=8)])],
        "low": [pool(1, [entry_item("minecraft:slime_ball", 2, count_min=4, count_max=8), luck_dust(False)])],
    },
    "vampire_gothique": {
        "high": [pool(1, [entry_item(f"{NS}:fiole_sang_noble", 1), entry_item("minecraft:golden_apple", 1)])],
        "neutral": [pool(1, [entry_item("minecraft:redstone", 2, count_min=4, count_max=8), entry_item("minecraft:torch", 2, count_min=8, count_max=16)])],
        "low": [pool(1, [entry_item("minecraft:spider_eye", 2, count_min=2, count_max=4), luck_dust(False)])],
    },
    "multivers_sliders": {
        "high": [pool(1, [entry_item(f"{NS}:zapper_dimensions", 1), entry_item("minecraft:ender_eye", 2, count_min=1, count_max=2)])],
        "neutral": [pool(1, [entry_item("minecraft:obsidian", 2, count_min=4, count_max=8), entry_item("minecraft:ender_pearl", 2, count_min=2, count_max=4)])],
        "low": [pool(1, [luck_dust(False)])],
    },
}


def sanitize_pools(pools: list[dict]) -> list[dict]:
    """Retire les entrées avec id minecraft invalides (weight 0 placeholder)."""
    out = []
    for p in pools:
        entries = [e for e in p["entries"] if e.get("weight", 1) > 0]
        if not entries:
            continue
        out.append({**p, "entries": entries})
    return out


def main() -> None:
    written = 0
    for theme, tiers in THEME_ENTRIES.items():
        for tier, pools in tiers.items():
            path = LOOT_DIR / theme / f"{tier}.json"
            if not path.exists():
                print("skip missing", path)
                continue
            text = path.read_text(encoding="utf-8")
            if '"count": 0' not in text and '"count":0' not in text:
                continue
            clean = sanitize_pools(pools)
            if not clean:
                continue
            path.write_text(json.dumps(table(*clean), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            written += 1
            print("updated", path.relative_to(ROOT))
    print(f"Done. {written} files.")


if __name__ == "__main__":
    main()
