#!/usr/bin/env python3
"""Genere blockstates/models/items, loot, advancements, lang et textures des themes 33–36.

Cote Java (deja fait a la main) : LuckyTheme, ModConfig.ThemeSpawn, StructurePlacer, RocketStyle,
Quests, QuestTriggers, ThemeNewEffects. Apres ce script, relancer :
  python tools/generate_recipes.py && python tools/quest_texts.py && python tools/sync_geckolib_models.py
"""
from __future__ import annotations

import json
import os
import time
from pathlib import Path

from PIL import Image, ImageDraw

import sys
sys.path.insert(0, str(Path(__file__).resolve().parent))
from pixel_canvas import Draw32, lucky_frame, new_canvas, rgb, shade_bottom  # noqa: E402
from texture_size import to_output_size  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets/unpredictableblocks"
DATA = ROOT / "common/src/main/resources/data/unpredictableblocks"


def gen_greve_transports(d: Draw32) -> None:
    d.rectangle((0, 0, 15, 15), fill=rgb("ECEFF1"))
    d.rectangle((2, 9, 13, 10), fill=rgb("6D4C41"))          # rail
    for x in range(3, 13, 3):
        d.rectangle((x, 8, x, 11), fill=rgb("8D6E63"))
    d.rectangle((4, 3, 11, 7), fill=rgb("E53935"))          # pancarte
    d.rectangle((7, 7, 8, 9), fill=rgb("5D4037"))
    d.raw.line((10, 10, 22, 10), fill=rgb("FFFFFF"), width=2)


def gen_meteo_folle(d: Draw32) -> None:
    d.rectangle((0, 0, 7, 15), fill=rgb("4FC3F7"))          # moitie soleil
    d.rectangle((8, 0, 15, 15), fill=rgb("37474F"))         # moitie orage
    d.ellipse((2, 2, 6, 6), fill=rgb("FFEB3B"))
    d.ellipse((9, 2, 14, 6), fill=rgb("B0BEC5"))
    d.polygon([(12, 7), (10, 11), (12, 11), (10, 14)], fill=rgb("FFD600"))


def gen_chat_internet(d: Draw32) -> None:
    d.rectangle((0, 0, 15, 15), fill=rgb("FFE0B2"))
    d.ellipse((3, 5, 12, 13), fill=rgb("FFA726"))           # tete
    d.polygon([(3, 7), (4, 2), (7, 5)], fill=rgb("FFA726"))  # oreilles
    d.polygon([(12, 7), (11, 2), (8, 5)], fill=rgb("FFA726"))
    d.rectangle((5, 8, 6, 9), fill=rgb("212121"))
    d.rectangle((9, 8, 10, 9), fill=rgb("212121"))
    d.rectangle((7, 10, 8, 10), fill=rgb("F06292"))


def gen_crypto_bulle(d: Draw32) -> None:
    d.rectangle((0, 0, 15, 15), fill=rgb("1B1B1B"))
    d.ellipse((3, 3, 12, 12), fill=rgb("F7931A"))           # piece
    d.rectangle((6, 5, 9, 10), fill=rgb("FFFFFF"))
    d.rectangle((7, 6, 8, 9), fill=rgb("F7931A"))
    d.raw.line((2, 28, 10, 20, 16, 24, 28, 6), fill=rgb("00E676"), width=2)  # courbe qui monte


# tid -> (titre FR, titre EN, rarete-frame, cadre (bord, reflet), dessin, quete FR, quete EN, desc FR, desc EN, loot)
THEMES = {
    "greve_transports": dict(
        fr="Grève des Transports", en="Transport Strike", frame="task", colors=("B71C1C", "FFFFFF"), draw=gen_greve_transports,
        quest_fr=("Service Minimum", "Monter dans un wagonnet."), quest_en=("Minimum Service", "Ride a minecart."),
        desc_fr="Aucun train ne circule. Sauf le tien, peut-être.", desc_en="No trains running. Except yours, maybe.",
        master_fr="Syndicaliste en chef", master_en="Union Boss",
        loot_high=("minecraft:powered_rail", 4, 8), loot_neutral=("minecraft:rail", 8, 16), loot_low=("minecraft:stick", 1, 3),
        reward="minecraft:minecart",
    ),
    "meteo_folle": dict(
        fr="Météo Folle", en="Crazy Weather", frame="goal", colors=("263238", "FFEB3B"), draw=gen_meteo_folle,
        quest_fr=("Paratonnerre Humain", "Être frappé par la foudre."), quest_en=("Human Lightning Rod", "Get struck by lightning."),
        desc_fr="Soleil, orage, soleil, orage… en même temps.", desc_en="Sun, storm, sun, storm... all at once.",
        master_fr="Maître des Nuages", master_en="Cloud Master",
        loot_high=("minecraft:lightning_rod", 1, 2), loot_neutral=("minecraft:snowball", 4, 12), loot_low=("minecraft:water_bucket", 1, 1),
        reward="minecraft:trident",
    ),
    "chat_internet": dict(
        fr="Chats d'Internet", en="Internet Cats", frame="task", colors=("E65100", "FFF3E0"), draw=gen_chat_internet,
        quest_fr=("Esclave de Chat", "Apprivoiser 3 chats."), quest_en=("Cat Servant", "Tame 3 cats."),
        desc_fr="100 % de mignon, 100 % de chaos.", desc_en="100% cute, 100% chaos.",
        master_fr="Roi des Chats", master_en="Cat King",
        loot_high=("minecraft:cod", 6, 12), loot_neutral=("minecraft:string", 3, 8), loot_low=("minecraft:cobweb", 1, 2),
        reward="minecraft:name_tag",
    ),
    "crypto_bulle": dict(
        fr="Bulle Crypto", en="Crypto Bubble", frame="challenge", colors=("F7931A", "FFFFFF"), draw=gen_crypto_bulle,
        quest_fr=("HODL", "Avoir 64 émeraudes dans l'inventaire."), quest_en=("HODL", "Hold 64 emeralds."),
        desc_fr="To the moon… ou au fond du trou.", desc_en="To the moon... or down the drain.",
        master_fr="Baleine Crypto", master_en="Crypto Whale",
        loot_high=("minecraft:emerald", 8, 20), loot_neutral=("minecraft:gold_nugget", 6, 18), loot_low=("minecraft:rotten_flesh", 2, 5),
        reward="minecraft:emerald_block",
    ),
}


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def write_atomic(path: Path, text: str) -> None:
    tmp = path.with_suffix(path.suffix + ".tmp")
    with open(tmp, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)
    for attempt in range(10):
        try:
            os.replace(tmp, path)
            return
        except OSError:
            time.sleep(0.3 * (attempt + 1))
    raise OSError(f"Impossible de remplacer {path}")


def loot(item: str, lo: int, hi: int, dust: bool = False) -> dict:
    pools = [{"rolls": 1, "entries": [{
        "type": "minecraft:item", "name": item,
        "functions": [{"function": "minecraft:set_count", "count": {"min": lo, "max": hi}}],
    }]}]
    if dust:
        pools.append({"rolls": 1, "entries": [{
            "type": "minecraft:item", "name": "unpredictableblocks:luck_emerald_dust",
            "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 2}}],
        }]})
    return {"pools": pools}


def criterion_of(tid: str) -> str:
    return {"greve_transports": "minecart", "meteo_folle": "struck", "chat_internet": "tame_cats", "crypto_bulle": "hodl"}[tid]


def main() -> None:
    lang = {"fr_fr.json": {}, "en_us.json": {}, "en_gb.json": {}}
    for tid, t in THEMES.items():
        block = f"lucky_{tid}"
        write_json(ASSETS / f"blockstates/{block}.json", {"variants": {"": {"model": f"unpredictableblocks:block/{block}"}}})
        write_json(ASSETS / f"models/block/{block}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"unpredictableblocks:block/{block}"}})
        write_json(ASSETS / f"models/item/{block}.json", {"parent": f"unpredictableblocks:block/{block}"})
        write_json(ASSETS / f"items/{block}.json", {"model": {"type": "minecraft:model", "model": f"unpredictableblocks:block/{block}"}})

        base = DATA / f"loot_table/gameplay/lucky/{tid}"
        write_json(base / "high.json", loot(*t["loot_high"], dust=True))
        write_json(base / "neutral.json", loot(*t["loot_neutral"]))
        write_json(base / "low.json", loot(*t["loot_low"]))
        write_json(DATA / f"loot_table/rewards/quest_{tid}.json", loot(t["reward"], 1, 1, dust=True))

        crit = criterion_of(tid)
        write_json(DATA / f"advancement/quest/{tid}.json", {
            "display": {
                "icon": {"id": f"unpredictableblocks:{block}"},
                "title": {"translate": f"advancements.unpredictableblocks.{tid}.title", "fallback": t["quest_fr"][0]},
                "description": {"translate": f"advancements.unpredictableblocks.{tid}.description", "fallback": t["quest_fr"][1]},
                "frame": t["frame"], "show_toast": True, "announce_to_chat": True,
            },
            "parent": "unpredictableblocks:root",
            "criteria": {crit: {"trigger": "minecraft:impossible"}},
            "requirements": [[crit]],
            "rewards": {"loot": [f"unpredictableblocks:rewards/quest_{tid}"]},
        })
        write_json(DATA / f"advancement/theme_master/{tid}.json", {
            "display": {
                "icon": {"id": f"unpredictableblocks:{block}"},
                "title": {"translate": f"advancements.unpredictableblocks.theme_master.{tid}.title", "fallback": f"Theme master: {tid}"},
                "description": {"translate": f"advancements.unpredictableblocks.theme_master.{tid}.description",
                                "fallback": "Break 10 blocks and complete the theme quest."},
                "frame": "challenge", "show_toast": True, "announce_to_chat": True,
            },
            "parent": f"unpredictableblocks:quest/{tid}",
            "criteria": {"master": {"trigger": "minecraft:impossible"}},
            "requirements": [["master"]],
        })

        img = new_canvas()
        t["draw"](Draw32(ImageDraw.Draw(img)))
        lucky_frame(Draw32(ImageDraw.Draw(img)), rgb(t["colors"][0]), rgb(t["colors"][1]))
        shade_bottom(img)
        to_output_size(img).save(ASSETS / f"textures/block/{block}.png")

        for file in lang:
            fr = file == "fr_fr.json"
            entries = lang[file]
            entries[f"block.unpredictableblocks.{block}"] = f"Lucky Block {t['fr']}" if fr else f"{t['en']} Lucky Block"
            entries[f"block.unpredictableblocks.{block}.desc"] = t["desc_fr"] if fr else t["desc_en"]
            entries[f"block.unpredictableblocks.{block}.usage"] = "Place dans le monde, puis casse-le." if fr else "Place in the world, then break it."
            entries[f"block.unpredictableblocks.{block}.effect"] = ("Declenche un evenement et du loot selon ton palier de Chance." if fr
                                                                  else "Rolls a themed event and loot based on your Luck tier.")
            entries[f"block.unpredictableblocks.{block}.quest"] = ("Complete la quete du theme apres le cassage pour les recompenses." if fr
                                                                 else "Complete the theme quest after the break for rewards.")
            q = t["quest_fr"] if fr else t["quest_en"]
            entries[f"advancements.unpredictableblocks.{tid}.title"] = q[0]
            entries[f"advancements.unpredictableblocks.{tid}.description"] = q[1]
            master = t["master_fr"] if fr else t["master_en"]
            entries[f"advancements.unpredictableblocks.theme_master.{tid}.title"] = (f"Maître — {master}" if fr else master)
            entries[f"advancements.unpredictableblocks.theme_master.{tid}.description"] = (
                f"Casser 10 blocs {tid} et compléter la quête du thème." if fr else f"10 breaks and complete the {t['en']} quest.")

    for file, entries in lang.items():
        path = ASSETS / "lang" / file
        data = json.loads(path.read_text(encoding="utf-8"))
        data.update(entries)
        write_atomic(path, json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    print(f"OK {len(THEMES)} themes (assets, data, lang, textures)")


if __name__ == "__main__":
    main()
