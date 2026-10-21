#!/usr/bin/env python3
"""Génère des structures .nbt minimalistes pour UnpredictableBlocks."""
from __future__ import annotations

from pathlib import Path

import nbtlib
from nbtlib import Compound, File, Int, List, String

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/data/unpredictableblocks/structures"


def palette_index(palette: list[Compound], block_id: str) -> int:
    for i, entry in enumerate(palette):
        if entry["Name"] == block_id:
            return i
    palette.append(Compound({"Name": String(block_id)}))
    return len(palette) - 1


def cuboid(size: tuple[int, int, int], fill: str, hollow: bool = False) -> dict[tuple[int, int, int], str]:
    sx, sy, sz = size
    blocks: dict[tuple[int, int, int], str] = {}
    for x in range(sx):
        for y in range(sy):
            for z in range(sz):
                if hollow and 0 < x < sx - 1 and 0 < y < sy - 1 and 0 < z < sz - 1:
                    continue
                blocks[(x, y, z)] = fill
    return blocks


def merge(*maps: dict[tuple[int, int, int], str]) -> dict[tuple[int, int, int], str]:
    out: dict[tuple[int, int, int], str] = {}
    for m in maps:
        out.update(m)
    return out


def build(blocks: dict[tuple[int, int, int], str]) -> Compound:
    if not blocks:
        blocks = {(0, 0, 0): "minecraft:stone"}
    max_x = max(p[0] for p in blocks) + 1
    max_y = max(p[1] for p in blocks) + 1
    max_z = max(p[2] for p in blocks) + 1
    palette: list[Compound] = []
    block_entries: list[Compound] = []
    for (x, y, z), bid in sorted(blocks.items()):
        idx = palette_index(palette, bid)
        block_entries.append(
            Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(idx)})
        )
    return Compound(
        {
            "size": List[Int]([Int(max_x), Int(max_y), Int(max_z)]),
            "palette": List[Compound](palette),
            "blocks": List[Compound](block_entries),
            "entities": List[Compound]([]),
        }
    )


def save(name: str, compound: Compound) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / f"{name}.nbt"
    File(compound).save(path, gzipped=False)
    print("wrote", path.relative_to(ROOT))


def four_geant() -> Compound:
    base = cuboid((5, 5, 5), "minecraft:bricks", hollow=True)
    base[(2, 1, 2)] = "minecraft:campfire"
    base[(2, 0, 2)] = "minecraft:chest"
    return build(base)


def drive_thru() -> Compound:
    b = merge(
        cuboid((7, 4, 5), "minecraft:yellow_concrete", hollow=True),
    )
    b[(3, 1, 0)] = "minecraft:air"
    b[(3, 0, 0)] = "minecraft:red_concrete"
    return build(b)


def pyramide_pq() -> Compound:
    blocks: dict[tuple[int, int, int], str] = {}
    for layer in range(5):
        s = 5 - layer
        off = layer
        for x in range(s):
            for z in range(s):
                blocks[(off + x, layer, off + z)] = "minecraft:white_wool"
    blocks[(2, 5, 2)] = "minecraft:chest"
    return build(blocks)


def tapis_rouge() -> Compound:
    blocks: dict[tuple[int, int, int], str] = {}
    for x in range(20):
        blocks[(x, 0, 0)] = "minecraft:red_wool"
        blocks[(x, 0, 1)] = "minecraft:red_wool"
        if x % 4 == 0:
            blocks[(x, 1, 0)] = "minecraft:armor_stand"
    return build(blocks)


def bunker_zone51() -> Compound:
    return build(cuboid((6, 4, 6), "minecraft:gray_concrete", hollow=True))


def piste_disco() -> Compound:
    blocks: dict[tuple[int, int, int], str] = {}
    for x in range(7):
        for z in range(7):
            blocks[(x, 0, z)] = "minecraft:glowstone" if (x + z) % 2 == 0 else "minecraft:glass"
    return build(blocks)


def chateau_briques() -> Compound:
    b = cuboid((7, 5, 7), "minecraft:bricks", hollow=True)
    b[(3, 4, 3)] = "minecraft:red_banner"
    b[(3, 1, 3)] = "minecraft:chest"
    return build(b)


def galerie_absurde() -> Compound:
    b = cuboid((8, 4, 6), "minecraft:quartz_block", hollow=True)
    b[(4, 2, 1)] = "minecraft:item_frame"
    b[(2, 1, 4)] = "minecraft:barrel"
    return build(b)


def sanctuaire_doge() -> Compound:
    b = cuboid((5, 6, 5), "minecraft:smooth_stone", hollow=False)
    b[(2, 1, 2)] = "minecraft:gold_block"
    b[(2, 2, 2)] = "minecraft:player_head"
    return build(b)


def pyramide_piege() -> Compound:
    return pyramide_pq()


def scene_spectacle() -> Compound:
    b = merge(cuboid((9, 1, 7), "minecraft:oak_planks"), cuboid((9, 5, 1), "minecraft:red_wool"))
    return build(b)


def table_poker_vip() -> Compound:
    blocks: dict[tuple[int, int, int], str] = {}
    for x in range(5):
        for z in range(3):
            blocks[(x, 0, z)] = "minecraft:green_wool"
    return build(blocks)


def campement_inonde() -> Compound:
    b = cuboid((5, 2, 5), "minecraft:water", hollow=False)
    b[(2, 1, 2)] = "minecraft:tripwire_hook"
    return build(b)


def salle_gym() -> Compound:
    b = cuboid((6, 4, 6), "minecraft:iron_block", hollow=True)
    b[(2, 1, 2)] = "minecraft:anvil"
    return build(b)


def autel_mochi() -> Compound:
    b = cuboid((5, 3, 5), "minecraft:pink_wool", hollow=True)
    b[(2, 1, 2)] = "minecraft:chest"
    return build(b)


STRUCTURES = {
    "four_geant": four_geant,
    "drive_thru": drive_thru,
    "pyramide_pq": pyramide_pq,
    "tapis_rouge": tapis_rouge,
    "bunker_zone51": bunker_zone51,
    "piste_disco": piste_disco,
    "chateau_briques": chateau_briques,
    "galerie_absurde": galerie_absurde,
    "sanctuaire_doge": sanctuaire_doge,
    "pyramide_piege": pyramide_piege,
    "scene_spectacle": scene_spectacle,
    "table_poker_vip": table_poker_vip,
    "campement_inonde": campement_inonde,
    "salle_gym": salle_gym,
    "autel_mochi": autel_mochi,
}


def main() -> None:
    for name, fn in STRUCTURES.items():
        save(name, fn())
    print(f"Generated {len(STRUCTURES)} structures.")


if __name__ == "__main__":
    main()
