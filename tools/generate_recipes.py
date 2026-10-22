#!/usr/bin/env python3
"""Genere les recettes de craft des Lucky Blocks + l'item « Noyau de chance » (lucky_core).

Equilibrage (pas trop facile) :
- lucky_core      : diamant + 4 lingots d'or + 2 poussieres d'emeraude (emeraude + glowstone)
- COMMON          : 1 noyau + 6 lingots d'or + 1 objet du theme
- RARE            : 1 noyau + 4 blocs d'or + 2 lingots + 1 objet du theme
- LEGENDARY       : 2 noyaux + 4 blocs d'or + 1 debris de netherite + 2 objets du theme

Lancer : python tools/generate_recipes.py
"""
from __future__ import annotations

import json
import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "common/src/main/resources"
DATA = RES / "data/unpredictableblocks"
ASSETS = RES / "assets/unpredictableblocks"
THEME_JAVA = ROOT / "common/src/main/java/com/jason54/unpredictableblocks/theme/LuckyTheme.java"

# Objet vanilla qui « signe » chaque theme dans sa recette
THEME_ITEM = {
    "anachronisme": "minecraft:clock",
    "boulangerie": "minecraft:bread",
    "bsod": "minecraft:lapis_block",
    "volaille": "minecraft:feather",
    "impots": "minecraft:paper",
    "streamer": "minecraft:spyglass",
    "proprete": "minecraft:sponge",
    "gravite_zero": "minecraft:phantom_membrane",
    "fast_food": "minecraft:cooked_beef",
    "cauchemar_matin": "minecraft:white_bed",
    "film_action": "minecraft:tnt",
    "jdr_d20": "minecraft:writable_book",
    "penurie_2020": "minecraft:white_wool",
    "complot": "minecraft:ender_eye",
    "lag_bug": "minecraft:repeater",
    "disco_80": "minecraft:note_block",
    "plomberie": "minecraft:bucket",
    "fortune_cookie": "minecraft:cookie",
    "camping": "minecraft:campfire",
    "gym_bro": "minecraft:iron_block",
    "art_moderne": "minecraft:painting",
    "magie_comptoir": "minecraft:rabbit_foot",
    "invasion_memes": "minecraft:goat_horn",
    "pause_cafe": "minecraft:cocoa_beans",
    "retard_vol": "minecraft:compass",
    "ananas_pizza": "minecraft:baked_potato",
    "speedrunner": "minecraft:blaze_rod",
    "piraterie_torrent": "minecraft:map",
    "asmr_fidget": "minecraft:slime_ball",
    "vampire_gothique": "minecraft:crying_obsidian",
    "multivers_sliders": "minecraft:chorus_fruit",
    "greve_transports": "minecraft:rail",
    "meteo_folle": "minecraft:lightning_rod",
    "chat_internet": "minecraft:string",
    "crypto_bulle": "minecraft:emerald_block",
}

PATTERNS = {
    "COMMON": (["GTG", "GCG", "GGG"], {"G": "minecraft:gold_ingot"}),
    "RARE": (["BTB", "GCG", "BGB"], {"G": "minecraft:gold_ingot", "B": "minecraft:gold_block"}),
    "LEGENDARY": (["BTB", "CNC", "BTB"], {"B": "minecraft:gold_block", "N": "minecraft:netherite_scrap"}),
}


def themes() -> list[tuple[str, str]]:
    src = THEME_JAVA.read_text(encoding="utf-8")
    return re.findall(r'^\s+[A-Z0-9_]+\("([a-z0-9_]+)", LuckyRarity\.([A-Z]+)\)', src, re.M)


def shaped(pattern: list[str], key: dict[str, str], result: str, count: int = 1) -> dict:
    return {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": pattern,
        "key": key,
        "result": {"id": result, "count": count},
    }


def write(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def core_texture() -> Image.Image:
    """16x16 : gemme doree avec coeur emeraude."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d <= 7:
                shade = int(255 - d * 14)
                img.putpixel((x, y), (shade, int(shade * 0.78), 40, 255))
            if d <= 3.5:
                img.putpixel((x, y), (40, int(230 - d * 30), 90, 255))
            if d > 6 and d <= 7:
                img.putpixel((x, y), (120, 80, 10, 255))
    img.putpixel((6, 5), (255, 255, 220, 255))
    img.putpixel((5, 6), (255, 255, 220, 255))
    return img


def main() -> None:
    write(DATA / "recipe/lucky_core.json", shaped(
        ["EGE", "GDG", "EGE"],
        {"E": "unpredictableblocks:luck_emerald_dust", "G": "minecraft:gold_ingot", "D": "minecraft:diamond"},
        "unpredictableblocks:lucky_core",
    ))
    found = themes()
    for tid, rarity in found:
        pattern, key = PATTERNS[rarity]
        key = dict(key, T=THEME_ITEM[tid], C="unpredictableblocks:lucky_core")
        write(DATA / f"recipe/lucky_{tid}.json", shaped(pattern, key, f"unpredictableblocks:lucky_{tid}"))

    # Tags #unpredictableblocks:lucky_blocks (bloc + item) : commandes /execute if block, datapacks
    lucky_ids = [f"unpredictableblocks:lucky_{tid}" for tid, _ in found]
    write(DATA / "tags/block/lucky_blocks.json", {"values": lucky_ids})
    write(DATA / "tags/item/lucky_blocks.json", {"values": lucky_ids})

    # Advancement cache : debloque toutes les recettes dans le livre des le premier lingot d'or
    recipes = ["unpredictableblocks:lucky_core"] + [f"unpredictableblocks:lucky_{tid}" for tid, _ in found]
    write(DATA / "advancement/recipes/lucky_blocks.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_gold": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": "minecraft:gold_ingot"}]},
            }
        },
        "requirements": [["has_gold"]],
        "rewards": {"recipes": recipes},
    })

    write(ASSETS / "items/lucky_core.json", {"model": {"type": "minecraft:model", "model": "unpredictableblocks:item/lucky_core"}})
    write(ASSETS / "models/item/lucky_core.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "unpredictableblocks:item/lucky_core"}})
    core_texture().save(ASSETS / "textures/item/lucky_core.png")
    missing = [t for t, _ in found if t not in THEME_ITEM]
    if missing:
        raise SystemExit(f"Objet de theme manquant pour : {missing}")
    print(f"OK lucky_core + {len(found)} recettes de Lucky Blocks")


if __name__ == "__main__":
    main()
