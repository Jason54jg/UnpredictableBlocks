#!/usr/bin/env python3
"""Genere les mobs animes GeckoLib (modele, texture, animations) : une seule definition par mob.

Sorties (common/src/main/resources/assets/unpredictableblocks/) :
  geckolib/models/entity/<mob>.geo.json        modele Bedrock (UV « box », editable dans Blockbench)
  geckolib/animations/entity/<mob>.animation.json   idle / walk / attack / special
  textures/entity/<mob>.png                    texture 128x128 peinte (faces avant decorees)

Conventions : unites en pixels (16 = 1 bloc), y = 0 au sol, avant du mob = -Z (face « north »).
Rotations d'animation en degres, convention GeckoLib/Bedrock. La tete suit le regard via Molang
(query.head_x_rotation / query.head_y_rotation) dans idle et walk.

Lancer : python tools/generate_mobs.py
"""
from __future__ import annotations

import json
import math
import random
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets/unpredictableblocks"
TEX = 128

Rgb = tuple[int, int, int]
Painter = Callable[[ImageDraw.ImageDraw, int, int, int, int], None]  # (draw, x, y, w, h) d'une face


def rgb(h: str) -> Rgb:
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


@dataclass
class Cube:
    origin: tuple[float, float, float]
    size: tuple[int, int, int]
    color: str
    front: Painter | None = None   # decor de la face avant (north)
    top: Painter | None = None
    pattern: str = "noise"         # noise | checker | stripes
    inflate: float = 0.0
    uv: tuple[int, int] = (0, 0)   # rempli par le packer


@dataclass
class Bone:
    name: str
    pivot: tuple[float, float, float]
    parent: str | None = None
    cubes: list[Cube] = field(default_factory=list)
    rotation: tuple[float, float, float] | None = None


@dataclass
class Mob:
    id: str
    bones: list[Bone]
    animations: dict[str, tuple[float, object, dict]]


# --------------------------------------------------------------------------- peinture
def shade(c: Rgb, k: float) -> Rgb:
    return tuple(max(0, min(255, int(v * k))) for v in c)  # type: ignore[return-value]


def fill_face(img: Image.Image, x: int, y: int, w: int, h: int, base: Rgb, pattern: str, rnd: random.Random, k: float) -> None:
    for j in range(h):
        for i in range(w):
            c = shade(base, k)
            if pattern == "checker" and ((i // 2) + (j // 2)) % 2:
                c = shade((235, 235, 235), k) if sum(base) < 200 else shade((30, 30, 30), k)
            elif pattern == "stripes" and (j % 4 == 0):
                c = shade(base, k * 0.8)
            n = rnd.randint(-10, 10)
            img.putpixel((x + i, y + j), tuple(max(0, min(255, v + n)) for v in c))


def paint_cube(img: Image.Image, cube: Cube, rnd: random.Random) -> None:
    """Depliage UV « box » Bedrock : ligne 1 = dessus/dessous, ligne 2 = est/nord/ouest/sud."""
    u, v = cube.uv
    w, h, d = cube.size
    base = rgb(cube.color)
    faces = {
        "up": (u + d, v, w, d, 1.12), "down": (u + d + w, v, w, d, 0.7),
        "east": (u, v + d, d, h, 0.85), "north": (u + d, v + d, w, h, 1.0),
        "west": (u + d + w, v + d, d, h, 0.85), "south": (u + 2 * d + w, v + d, w, h, 0.8),
    }
    for name, (x, y, fw, fh, k) in faces.items():
        if fw > 0 and fh > 0:
            fill_face(img, x, y, fw, fh, base, cube.pattern, rnd, k)
    draw = ImageDraw.Draw(img)
    if cube.front:
        x, y, fw, fh, _ = faces["north"]
        cube.front(draw, x, y, fw, fh)
    if cube.top:
        x, y, fw, fh, _ = faces["up"]
        cube.top(draw, x, y, fw, fh)


def pack(mob: Mob) -> None:
    """Rangement en etageres des depliages (2*(w+d)) x (d+h) dans la texture."""
    x = y = shelf = 0
    for bone in mob.bones:
        for cube in bone.cubes:
            w, h, d = cube.size
            uw, uh = 2 * (w + d), d + h
            if x + uw > TEX:
                x, y, shelf = 0, y + shelf, 0
            cube.uv = (x, y)
            x += uw
            shelf = max(shelf, uh)
            if y + uh > TEX:
                raise SystemExit(f"{mob.id} : texture {TEX}x{TEX} trop petite")


# --------------------------------------------------------------------------- decors (faces avant)
def px(draw, x, y, c):
    draw.point((x, y), fill=rgb(c) if isinstance(c, str) else c)


def eyes(color="1A1A1A", white=True, y_frac=0.35, gap=0.22):
    def p(draw, x, y, w, h):
        cy = y + int(h * y_frac)
        for ex in (x + int(w * (0.5 - gap)) - 1, x + int(w * (0.5 + gap))):
            if white:
                draw.rectangle((ex - 1, cy - 1, ex + 1, cy), fill=rgb("F5F5F5"))
            draw.rectangle((ex, cy - 1, ex, cy), fill=rgb(color))
    return p


def chef_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 3, y + 4, x + 4, y + 5), fill=rgb("C68B59"))           # nez
    draw.rectangle((x + 1, y + 6, x + w - 2, y + 6), fill=rgb("2B1B0E"))       # moustache
    px(draw, x + 1, y + 7, "2B1B0E"); px(draw, x + w - 2, y + 7, "2B1B0E")


def chef_jacket(draw, x, y, w, h):
    for row in range(2, h - 1, 3):
        px(draw, x + 2, y + row, "202020"); px(draw, x + w - 3, y + row, "202020")
    draw.rectangle((x, y, x + w - 1, y + 1), fill=rgb("C62828"))               # foulard rouge


def dracula_face(draw, x, y, w, h):
    eyes("C62828", white=False)(draw, x, y, w, h)
    draw.rectangle((x + 2, y + 6, x + w - 3, y + 6), fill=rgb("5A1010"))       # bouche
    px(draw, x + 2, y + 7, "FFFFFF"); px(draw, x + w - 3, y + 7, "FFFFFF")     # crocs
    draw.polygon([(x + w // 2, y + 2), (x + w // 2 - 2, y), (x + w // 2 + 2, y)], fill=rgb("111111"))


def dracula_suit(draw, x, y, w, h):
    draw.rectangle((x + 2, y, x + w - 3, y + h - 3), fill=rgb("8B0000"))       # gilet
    draw.rectangle((x + 3, y, x + w - 4, y + 3), fill=rgb("F0F0F0"))           # chemise
    px(draw, x + w // 2, y + 4, "FFD700")                                      # broche


def burger_top_front(draw, x, y, w, h):
    for ex in (x + 3, x + w - 6):                                              # grands yeux cartoon
        draw.ellipse((ex, y + 1, ex + 3, y + 4), fill=rgb("FFFFFF"), outline=rgb("222222"))
        draw.rectangle((ex + 1, y + 2, ex + 2, y + 3), fill=rgb("111111"))


def sesame(draw, x, y, w, h):
    rnd = random.Random(7)
    for _ in range(max(4, w * h // 12)):
        px(draw, x + rnd.randrange(w), y + rnd.randrange(h), "FFF3C4")


def cat_face(draw, x, y, w, h):
    draw.rectangle((x, y + 1, x + w - 1, y + 2), fill=rgb("111111"))           # lunettes de soleil
    px(draw, x + 1, y + 1, "5A5A5A"); px(draw, x + w - 2, y + 1, "5A5A5A")
    px(draw, x + w // 2, y + h - 2, "F06292")                                  # nez rose


def cat_stripes(draw, x, y, w, h):
    for j in range(0, h, 3):
        draw.line((x, y + j, x + w - 1, y + j), fill=rgb("C46A1A"))


def rooster_face(draw, x, y, w, h):
    eyes(white=False, y_frac=0.3, gap=0.35)(draw, x, y, w, h)


def screen_front(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("1E5BD8"))
    # « :( » + lignes de texte du crash
    px(draw, x + 3, y + 3, "FFFFFF"); px(draw, x + 3, y + 5, "FFFFFF")
    for j in range(2, 7):
        px(draw, x + 5 + (1 if j in (2, 6) else 0), y + j, "FFFFFF")
    for row in (8, 10):
        draw.line((x + 3, y + row, x + w - 5, y + row), fill=rgb("AFC8F5"))


# --------------------------------------------------------------------------- animations
Frames = dict[float, list]


def loop(length: float, pts: list[tuple[float, list]]) -> Frames:
    return {round(t, 3): v for t, v in pts}


def swing(length: float, amp: float, axis: int = 0, phase: float = 0.0, steps: int = 8) -> Frames:
    out: Frames = {}
    for i in range(steps + 1):
        t = length * i / steps
        val = [0.0, 0.0, 0.0]
        val[axis] = round(amp * math.sin(2 * math.pi * (i / steps) + phase), 2)
        out[round(t, 3)] = val
    return out


HEAD_LOOK = ["query.head_x_rotation", "query.head_y_rotation", 0]


def humanoid_anims(attack: dict, special: tuple[float, dict] | None = None, walk_arms: float = 28) -> dict:
    anims = {
        "idle": (2.0, True, {
            "head": {"rotation": HEAD_LOOK},
            "right_arm": {"rotation": swing(2.0, 3, axis=2)},
            "left_arm": {"rotation": swing(2.0, -3, axis=2)},
            "body": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1.02, 1.01, 1.02]), (2.0, [1, 1, 1])])},
        }),
        "walk": (1.0, True, {
            "head": {"rotation": HEAD_LOOK},
            "right_leg": {"rotation": swing(1.0, 32)},
            "left_leg": {"rotation": swing(1.0, -32)},
            "right_arm": {"rotation": swing(1.0, -walk_arms)},
            "left_arm": {"rotation": swing(1.0, walk_arms)},
        }),
        "attack": (0.5, False, attack),
    }
    if special:
        anims["special"] = (special[0], False, special[1])
    return anims


# --------------------------------------------------------------------------- mobs
def humanoid(head_color: str, body: Cube, head_front: Painter, arm: str, leg: str, leg_pattern: str = "noise") -> list[Bone]:
    return [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 12, 0), "root", [body]),
        Bone("head", (0, 24, 0), "body", [Cube((-4, 24, -4), (8, 8, 8), head_color, front=head_front)]),
        Bone("right_arm", (-5, 22, 0), "body", [Cube((-8, 12, -2), (4, 12, 4), arm)]),
        Bone("left_arm", (5, 22, 0), "body", [Cube((4, 12, -2), (4, 12, 4), arm)]),
        Bone("right_leg", (-2, 12, 0), "root", [Cube((-4, 0, -2), (4, 12, 4), leg, pattern=leg_pattern)]),
        Bone("left_leg", (2, 12, 0), "root", [Cube((0, 0, -2), (4, 12, 4), leg, pattern=leg_pattern)]),
    ]


def chef() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "F2F2F2", front=chef_jacket), chef_face,
                     "F2F2F2", "EDEDED", leg_pattern="checker")
    bones.append(Bone("hat", (0, 32, 0), "head", [
        Cube((-4, 32, -4), (8, 2, 8), "FFFFFF", inflate=0.3),
        Cube((-5, 34, -5), (10, 6, 10), "FAFAFA"),
    ]))
    bones.append(Bone("pizza", (-6, 12, -3), "right_arm", [
        Cube((-9, 11, -6), (6, 1, 6), "F4C04A", top=lambda d, x, y, w, h: [px(d, x + i, y + j, "C62828")
                                                                       for i, j in ((1, 1), (4, 2), (2, 4))]),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [-160, 0, 10]), (0.32, [40, 0, 0]), (0.5, [0, 0, 0])])},
        "body": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [0, 15, 0]), (0.32, [0, -10, 0]), (0.5, [0, 0, 0])])},
    }
    mamma_mia = {
        "right_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.2, [-150, 0, -20]), (0.4, [-140, 0, -35]), (0.6, [-150, 0, -20]), (0.8, [-140, 0, -35]), (1.0, [0, 0, 0])])},
        "left_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.2, [-150, 0, 20]), (0.4, [-140, 0, 35]), (0.6, [-150, 0, 20]), (0.8, [-140, 0, 35]), (1.0, [0, 0, 0])])},
        "head": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-20, 0, 0]), (0.7, [-20, 0, 0]), (1.0, [0, 0, 0])])},
    }
    return Mob("chef_italien", bones, humanoid_anims(attack, special=(1.0, mamma_mia)))


def dracula() -> Mob:
    bones = humanoid("DADAE8", Cube((-4, 12, -2), (8, 12, 4), "141414", front=dracula_suit), dracula_face,
                     "141414", "1C1C1C")
    bones.append(Bone("hair", (0, 32, 0), "head", [Cube((-4, 31, -4), (8, 2, 8), "0D0D0D", inflate=0.3)]))
    bones.append(Bone("cape", (0, 24, 2), "body", [
        Cube((-6, 3, 2), (12, 21, 1), "1A0000"),
        Cube((-5, 23, 1.5), (10, 7, 1), "8B0000"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-110, 20, 0]), (0.35, [-40, -30, 0]), (0.5, [0, 0, 0])])},
        "left_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-110, -20, 0]), (0.35, [-40, 30, 0]), (0.5, [0, 0, 0])])},
        "cape": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [25, 0, 0]), (0.5, [0, 0, 0])])},
    }
    bats = {
        "root": {"rotation": loop(1.0, [(0, [0, 0, 0]), (1.0, [0, 720, 0])]),
                 "scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.1, 0.6, 1.1]), (0.9, [0, 0, 0]), (1.0, [0, 0, 0])])},
        "cape": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [60, 0, 0]), (1.0, [60, 0, 0])])},
    }
    anims = humanoid_anims(attack, special=(1.0, bats))
    anims["idle"][2]["cape"] = {"rotation": swing(2.0, 4)}
    anims["walk"][2]["cape"] = {"rotation": loop(1.0, [(0, [12, 0, 0]), (0.5, [18, 0, 0]), (1.0, [12, 0, 0])])}
    return Mob("comte_dracula", bones, anims)


def hamburger() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("right_leg", (-3, 6, 0), "root", [Cube((-5, 0, -2), (3, 6, 3), "C62828")]),
        Bone("left_leg", (3, 6, 0), "root", [Cube((2, 0, -2), (3, 6, 3), "C62828")]),
        Bone("body", (0, 6, 0), "root", [
            Cube((-7, 6, -7), (14, 4, 14), "D9A35B"),
            Cube((-7, 10, -7), (14, 3, 14), "5B3A1E", inflate=0.4),
            Cube((-8, 13, -8), (16, 1, 16), "F7C531"),
            Cube((-7, 14, -7), (14, 1, 14), "4CAF50", inflate=0.5),
        ]),
        Bone("top", (0, 15, 0), "body", [Cube((-7, 15, -7), (14, 7, 14), "E0A85C", front=burger_top_front, top=sesame)]),
        Bone("right_arm", (-8, 11, 0), "body", [Cube((-10, 4, -1), (2, 8, 2), "FFFFFF")]),
        Bone("left_arm", (8, 11, 0), "body", [Cube((8, 4, -1), (2, 8, 2), "FFFFFF")]),
    ]
    anims = {
        "idle": (1.6, True, {
            "body": {"scale": loop(1.6, [(0, [1, 1, 1]), (0.8, [1.04, 0.95, 1.04]), (1.6, [1, 1, 1])])},
            "top": {"rotation": swing(1.6, 3, axis=2)},
            "right_arm": {"rotation": swing(1.6, 8, axis=2)},
            "left_arm": {"rotation": swing(1.6, -8, axis=2)},
        }),
        "walk": (0.6, True, {
            "right_leg": {"rotation": swing(0.6, 35)},
            "left_leg": {"rotation": swing(0.6, -35)},
            "body": {"position": loop(0.6, [(0, [0, 0, 0]), (0.15, [0, 2, 0]), (0.3, [0, 0, 0]), (0.45, [0, 2, 0]), (0.6, [0, 0, 0])])},
            "right_arm": {"rotation": swing(0.6, -30)},
            "left_arm": {"rotation": swing(0.6, 30)},
        }),
        "attack": (0.6, False, {
            "root": {"position": loop(0.6, [(0, [0, 0, 0]), (0.25, [0, 8, 0]), (0.4, [0, 0, 0]), (0.6, [0, 0, 0])])},
            "body": {"scale": loop(0.6, [(0, [1, 1, 1]), (0.1, [1.15, 0.8, 1.15]), (0.25, [0.9, 1.15, 0.9]), (0.42, [1.3, 0.65, 1.3]), (0.6, [1, 1, 1])])},
            "top": {"rotation": loop(0.6, [(0, [0, 0, 0]), (0.25, [-20, 0, 0]), (0.42, [10, 0, 0]), (0.6, [0, 0, 0])])},
        }),
    }
    anims["special"] = (0.8, False, {
        "top": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.15, [-40, 0, 0]), (0.3, [0, 0, 0]), (0.45, [-40, 0, 0]), (0.6, [0, 0, 0]), (0.8, [0, 0, 0])])},
    })
    return Mob("mascotte_hamburger", bones, anims)


def cat() -> Mob:
    leg = "F0A040"
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 6, 0), "root", [Cube((-2.5, 4, -6), (5, 5, 12), "F0A040", top=cat_stripes)]),
        Bone("head", (0, 8, -6), "body", [
            Cube((-3, 5, -11), (6, 5, 5), "F0A040", front=cat_face),
            Cube((-3, 10, -9), (2, 2, 1), "F0A040"),
            Cube((1, 10, -9), (2, 2, 1), "F0A040"),
            Cube((-3.5, 7.5, -11.6), (7, 1, 1), "111111"),
        ]),
        Bone("tail", (0, 8, 6), "body", [Cube((-0.5, 7.5, 6), (1, 1, 8), "E08A2A")], rotation=(-35, 0, 0)),
        Bone("front_right_leg", (-1.5, 4, -4.5), "root", [Cube((-2.5, 0, -5.5), (2, 4, 2), leg)]),
        Bone("front_left_leg", (1.5, 4, -4.5), "root", [Cube((0.5, 0, -5.5), (2, 4, 2), leg)]),
        Bone("back_right_leg", (-1.5, 4, 4.5), "root", [Cube((-2.5, 0, 3.5), (2, 4, 2), leg)]),
        Bone("back_left_leg", (1.5, 4, 4.5), "root", [Cube((0.5, 0, 3.5), (2, 4, 2), leg)]),
    ]
    anims = {
        "idle": (2.4, True, {
            "head": {"rotation": HEAD_LOOK},
            "tail": {"rotation": swing(2.4, 18, axis=2)},
        }),
        "walk": (0.6, True, {
            "head": {"rotation": HEAD_LOOK},
            "front_right_leg": {"rotation": swing(0.6, 35)},
            "back_left_leg": {"rotation": swing(0.6, 35)},
            "front_left_leg": {"rotation": swing(0.6, -35)},
            "back_right_leg": {"rotation": swing(0.6, -35)},
            "tail": {"rotation": swing(0.6, 10, axis=2)},
        }),
        "sit": (1.0, True, {
            "head": {"rotation": HEAD_LOOK},
            "body": {"position": {0.0: [0, -2, 1]}, "rotation": {0.0: [-25, 0, 0]}},
            "back_right_leg": {"rotation": {0.0: [-80, 0, 0]}, "position": {0.0: [0, -1, -1]}},
            "back_left_leg": {"rotation": {0.0: [-80, 0, 0]}, "position": {0.0: [0, -1, -1]}},
            "tail": {"rotation": swing(1.0, 15, axis=2)},
        }),
        "attack": (0.4, False, {
            "front_right_leg": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.15, [-75, 0, -15]), (0.28, [-20, 0, 10]), (0.4, [0, 0, 0])])},
            "head": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.15, [-10, 15, 0]), (0.4, [0, 0, 0])])},
        }),
    }
    anims["special"] = (1.2, False, {
        "body": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [0.95, 0.9, 1.2]), (0.8, [0.95, 0.9, 1.2]), (1.2, [1, 1, 1])]),
                 "rotation": loop(1.2, [(0, [0, 0, 0]), (0.4, [10, 0, 0]), (0.8, [10, 0, 0]), (1.2, [0, 0, 0])])},
        "tail": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.4, [-40, 0, 0]), (0.8, [-40, 0, 0]), (1.2, [0, 0, 0])])},
    })
    return Mob("chat_internet", bones, anims)


def rooster() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("right_leg", (-2, 10, 0), "root", [Cube((-3, 0, -1), (2, 10, 2), "F2C230"), Cube((-4, 0, -3), (4, 1, 5), "F2C230")]),
        Bone("left_leg", (2, 10, 0), "root", [Cube((1, 0, -1), (2, 10, 2), "F2C230"), Cube((0, 0, -3), (4, 1, 5), "F2C230")]),
        Bone("body", (0, 10, 0), "root", [Cube((-5, 10, -6), (10, 10, 12), "F5F5F5")]),
        Bone("right_wing", (-5, 18, -2), "body", [Cube((-6, 11, -5), (1, 8, 10), "1E40AF")]),
        Bone("left_wing", (5, 18, -2), "body", [Cube((5, 11, -5), (1, 8, 10), "1E40AF")]),
        Bone("tail", (0, 16, 6), "body", [
            Cube((-4, 14, 5), (2, 11, 2), "1E40AF"), Cube((-1, 15, 5), (2, 13, 2), "FFFFFF"), Cube((2, 14, 5), (2, 11, 2), "D32F2F"),
        ], rotation=(-30, 0, 0)),
        Bone("neck", (0, 18, -5), "body", [Cube((-2, 18, -7), (4, 6, 4), "F5F5F5")]),
        Bone("head", (0, 23, -6), "neck", [
            Cube((-2.5, 23, -8.5), (5, 5, 5), "F5F5F5", front=rooster_face),
            Cube((-0.5, 28, -8), (1, 3, 5), "D32F2F"),
            Cube((-1, 24, -10.5), (2, 2, 2), "F2C230"),
            Cube((-0.5, 21.5, -9), (1, 3, 1), "D32F2F"),
        ]),
    ]
    anims = {
        "idle": (2.0, True, {
            "head": {"rotation": HEAD_LOOK},
            "neck": {"rotation": loop(2.0, [(0, [0, 0, 0]), (0.3, [12, 0, 0]), (0.5, [0, 0, 0]), (2.0, [0, 0, 0])])},
            "tail": {"rotation": swing(2.0, 4, axis=2)},
        }),
        "walk": (0.8, True, {
            "head": {"rotation": HEAD_LOOK},
            "right_leg": {"rotation": swing(0.8, 30)},
            "left_leg": {"rotation": swing(0.8, -30)},
            "neck": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.2, [15, 0, 0]), (0.4, [0, 0, 0]), (0.6, [15, 0, 0]), (0.8, [0, 0, 0])])},
        }),
        "attack": (0.4, False, {
            "neck": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.12, [-20, 0, 0]), (0.22, [60, 0, 0]), (0.4, [0, 0, 0])])},
        }),
        "special": (1.2, False, {
            "right_wing": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.15, [0, 0, 70]), (0.3, [0, 0, 10]), (0.45, [0, 0, 70]), (0.6, [0, 0, 10]), (0.75, [0, 0, 70]), (1.2, [0, 0, 0])])},
            "left_wing": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.15, [0, 0, -70]), (0.3, [0, 0, -10]), (0.45, [0, 0, -70]), (0.6, [0, 0, -10]), (0.75, [0, 0, -70]), (1.2, [0, 0, 0])])},
            "neck": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [-35, 0, 0]), (0.9, [-35, 0, 0]), (1.2, [0, 0, 0])])},
        }),
    }
    return Mob("coq_gaulois", bones, anims)


def screen() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("screen", (0, 14, 0), "root", [
            Cube((-9, 8, -2), (18, 12, 3), "2A2A2A", front=screen_front),
            Cube((-6, 10, 1), (12, 8, 3), "3A3A3A"),
        ]),
        Bone("cable", (0, 10, 3), "screen", [Cube((-0.5, 2, 3), (1, 8, 1), "111111"), Cube((-1, 0, 2.5), (2, 2, 2), "555555")]),
    ]
    steps = [(round(i * 0.05, 2), [0, 0, 0] if i % 2 == 0 else [0, 0, (8 if i % 4 == 1 else -8)]) for i in range(9)]
    anims = {
        "idle": (2.0, True, {
            "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
            "screen": {"rotation": swing(2.0, 4, axis=2)},
            "cable": {"rotation": swing(2.0, 10)},
        }),
        "walk": (1.0, True, {
            "root": {"position": loop(1.0, [(0, [0, 1, 0]), (0.5, [0, 2.5, 0]), (1.0, [0, 1, 0])])},
            "screen": {"rotation": {0.0: [12, 0, 0]}},
            "cable": {"rotation": swing(1.0, 20)},
        }),
        "attack": (0.4, False, {
            "screen": {"scale": loop(0.4, [(0, [1, 1, 1]), (0.1, [1.25, 1.25, 1.25]), (0.2, [0.9, 0.9, 0.9]), (0.4, [1, 1, 1])]),
                       "rotation": loop(0.4, [(0, [0, 0, 0]), (0.1, [-25, 0, 0]), (0.4, [0, 0, 0])])},
        }),
        "special": (0.4, False, {
            "screen": {"rotation": {t: v for t, v in steps},
                       "position": {t: ([0, 0, 0] if i % 2 == 0 else [(-2 if i % 3 else 2), 0, 0]) for i, (t, _) in enumerate(steps)}},
        }),
    }
    return Mob("ecran_bleu", bones, anims)


# --------------------------------------------------------------------------- lot 2
def futur_face(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 2, x + w - 2, y + 5), fill=rgb("00E5FF"))  # visière néon
    px(draw, x + 2, y + 3, "FFFFFF"); px(draw, x + w - 3, y + 3, "FFFFFF")


def futur_chest(draw, x, y, w, h):
    draw.rectangle((x + 2, y + 2, x + w - 3, y + h - 3), fill=rgb("B0BEC5"))
    draw.rectangle((x + 3, y + 4, x + w - 4, y + 6), fill=rgb("00E5FF"))
    px(draw, x + w // 2, y + 8, "FF4081")


def douanier_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 1, y + 6, x + w - 2, y + 6), fill=rgb("5D4037"))  # moustache


def douanier_jacket(draw, x, y, w, h):
    draw.rectangle((x, y, x + w - 1, y + 2), fill=rgb("0D47A1"))
    for row in (4, 7, 10):
        px(draw, x + 2, y + row, "FFD700"); px(draw, x + w - 3, y + row, "FFD700")


def rabbit_face(draw, x, y, w, h):
    eyes(y_frac=0.4, gap=0.28)(draw, x, y, w, h)
    px(draw, x + w // 2, y + h - 2, "F48FB1")


def dealer_face(draw, x, y, w, h):
    draw.rectangle((x, y + 2, x + w - 1, y + 4), fill=rgb("111111"))  # lunettes noires
    px(draw, x + 1, y + 2, "333333"); px(draw, x + w - 2, y + 2, "333333")


def dealer_coat(draw, x, y, w, h):
    draw.rectangle((x + 1, y, x + w - 2, y + h - 1), fill=rgb("37474F"))
    draw.rectangle((x + 3, y + 2, x + w - 4, y + 8), fill=rgb("263238"))


def statue_face(draw, x, y, w, h):
    draw.rectangle((x + 2, y + 2, x + w - 3, y + h - 3), fill=rgb("ECEFF1"))
    px(draw, x + 3, y + 4, "90A4AE"); px(draw, x + w - 4, y + 4, "90A4AE")


def mimic_front(draw, x, y, w, h):
    # faces du dé + dents
    draw.rectangle((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("6A1B9A"))
    for i, j in ((2, 2), (w // 2, 2), (w - 3, 2), (2, h // 2), (w // 2, h // 2),
                 (w - 3, h // 2), (2, h - 3), (w // 2, h - 3), (w - 3, h - 3)):
        if 0 <= i < w and 0 <= j < h:
            px(draw, x + i, y + j, "FFD700")
    for i in range(2, w - 2, 2):
        px(draw, x + i, y + h - 2, "FFFFFF")


def toi_du_futur() -> Mob:
    bones = humanoid("C0C0C0", Cube((-4, 12, -2), (8, 12, 4), "90A4AE", front=futur_chest), futur_face,
                     "78909C", "607D8B")
    bones.append(Bone("visor", (0, 28, -4), "head", [Cube((-4, 26, -5), (8, 3, 1), "00E5FF", inflate=0.2)]))
    bones.append(Bone("jetpack", (0, 18, 3), "body", [
        Cube((-5, 14, 2), (4, 10, 3), "455A64"),
        Cube((1, 14, 2), (4, 10, 3), "455A64"),
        Cube((-4, 12, 3), (2, 3, 2), "FF6D00"),
        Cube((2, 12, 3), (2, 3, 2), "FF6D00"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-90, 0, 0]), (0.35, [-90, 0, 0]), (0.5, [0, 0, 0])])},
        "left_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-80, 0, 20]), (0.5, [0, 0, 0])])},
        "visor": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.2, 1.2, 1.2]), (0.5, [1, 1, 1])])},
    }
    fly = {
        "root": {"position": loop(1.0, [(0, [0, 0, 0]), (0.3, [0, 6, 0]), (0.7, [0, 8, 0]), (1.0, [0, 0, 0])])},
        "jetpack": {"rotation": swing(1.0, 8, axis=2)},
        "right_arm": {"rotation": {0.0: [-20, 0, 25]}},
        "left_arm": {"rotation": {0.0: [-20, 0, -25]}},
    }
    return Mob("toi_du_futur", bones, humanoid_anims(attack, special=(1.0, fly), walk_arms=20))


def douanier() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "1565C0", front=douanier_jacket), douanier_face,
                     "1565C0", "0D47A1")
    bones.append(Bone("cap", (0, 32, 0), "head", [
        Cube((-4, 32, -4), (8, 2, 8), "0D47A1", inflate=0.2),
        Cube((-5, 33, -5), (10, 1, 10), "0D47A1"),
        Cube((-1, 33, -6), (2, 1, 2), "FFD700"),
    ]))
    bones.append(Bone("stamp", (-6, 14, -2), "right_arm", [
        Cube((-8, 12, -4), (4, 4, 4), "C62828"),
        Cube((-7, 16, -3), (2, 4, 2), "5D4037"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [-140, 10, 0]), (0.32, [20, 0, 0]), (0.5, [0, 0, 0])])},
        "stamp": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [-30, 0, 0]), (0.32, [40, 0, 0]), (0.5, [0, 0, 0])])},
    }
    search = {
        "left_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.25, [-60, 0, 20]), (0.5, [-40, 30, 0]), (0.75, [-60, -20, 0]), (1.0, [0, 0, 0])])},
        "head": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [15, 25, 0]), (0.7, [15, -25, 0]), (1.0, [0, 0, 0])])},
        "body": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 10, 0]), (1.0, [0, 0, 0])])},
    }
    return Mob("douanier", bones, humanoid_anims(attack, special=(1.0, search)))


def lapin_extirpateur() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 8, 0), "root", [Cube((-3, 5, -4), (6, 7, 8), "F5F5F5")]),
        Bone("head", (0, 12, -3), "body", [
            Cube((-3.5, 11, -8), (7, 6, 6), "FAFAFA", front=rabbit_face),
            Cube((-3, 17, -6), (2, 6, 1), "FAFAFA"),
            Cube((1, 17, -6), (2, 6, 1), "FAFAFA"),
            Cube((-2.5, 17, -6.5), (1, 4, 1), "F48FB1"),
            Cube((1.5, 17, -6.5), (1, 4, 1), "F48FB1"),
        ]),
        Bone("hat", (0, 18, -5), "head", [
            Cube((-4, 17, -9), (8, 1, 8), "111111"),
            Cube((-2.5, 18, -7.5), (5, 5, 5), "111111"),
            Cube((-3, 18, -8), (6, 1, 6), "C62828"),
        ]),
        Bone("cape", (0, 12, 3), "body", [Cube((-4, 2, 3), (8, 12, 1), "6A1B9A")]),
        Bone("front_right", (-2, 5, -2), "root", [Cube((-3, 0, -3), (2, 5, 2), "F5F5F5")]),
        Bone("front_left", (2, 5, -2), "root", [Cube((1, 0, -3), (2, 5, 2), "F5F5F5")]),
        Bone("back_right", (-2, 5, 3), "root", [Cube((-3, 0, 2), (2, 5, 2), "F5F5F5")]),
        Bone("back_left", (2, 5, 3), "root", [Cube((1, 0, 2), (2, 5, 2), "F5F5F5")]),
    ]
    anims = {
        "idle": (1.2, True, {
            "head": {"rotation": HEAD_LOOK},
            "body": {"position": loop(1.2, [(0, [0, 0, 0]), (0.3, [0, 3, 0]), (0.6, [0, 0, 0]), (0.9, [0, 2, 0]), (1.2, [0, 0, 0])])},
            "cape": {"rotation": swing(1.2, 6)},
        }),
        "walk": (0.5, True, {
            "head": {"rotation": HEAD_LOOK},
            "front_right": {"rotation": swing(0.5, 40)},
            "back_left": {"rotation": swing(0.5, 40)},
            "front_left": {"rotation": swing(0.5, -40)},
            "back_right": {"rotation": swing(0.5, -40)},
            "body": {"position": loop(0.5, [(0, [0, 0, 0]), (0.25, [0, 4, 0]), (0.5, [0, 0, 0])])},
        }),
        "attack": (0.8, False, {
            "hat": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.2, [-50, 0, 0]), (0.45, [-50, 0, 0]), (0.65, [20, 0, 0]), (0.8, [0, 0, 0])])},
            "right_arm_fake": {},
            "head": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.3, [-15, 0, 0]), (0.8, [0, 0, 0])])},
        }),
        "special": (1.0, False, {
            "root": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [0.2, 0.2, 0.2]), (0.6, [0, 0, 0]), (0.85, [0.3, 0.3, 0.3]), (1.0, [1, 1, 1])]),
                     "rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 360, 0]), (1.0, [0, 720, 0])])},
        }),
    }
    # clean empty bone ref from attack
    del anims["attack"][2]["right_arm_fake"]
    return Mob("lapin_extirpateur", bones, anims)


def grossiste_clandestin() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "455A64", front=dealer_coat), dealer_face,
                     "37474F", "263238")
    bones.append(Bone("backpack", (0, 18, 4), "body", [
        Cube((-5, 12, 2), (10, 12, 5), "ECEFF1"),
        Cube((-4, 14, 6), (3, 4, 2), "FAFAFA"),
        Cube((1, 18, 6), (3, 4, 2), "FAFAFA"),
        Cube((-2, 21, 6), (4, 3, 2), "FAFAFA"),
    ]))
    attack = {  # fuite : se penche et sprinte
        "body": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [25, 0, 0]), (0.5, [15, 0, 0])])},
        "right_arm": {"rotation": swing(0.5, 40)},
        "left_arm": {"rotation": swing(0.5, -40)},
    }
    sale = {
        "right_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-80, 0, -20]), (0.6, [-60, 0, 10]), (1.0, [0, 0, 0])])},
        "left_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-70, 0, 30]), (0.6, [-50, 0, 10]), (1.0, [0, 0, 0])])},
        "backpack": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [15, 0, 0]), (1.0, [0, 0, 0])])},
        "head": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [10, 20, 0]), (1.0, [0, 0, 0])])},
    }
    anims = humanoid_anims(attack, special=(1.0, sale), walk_arms=35)
    anims["walk"][2]["body"] = {"rotation": {0.0: [8, 0, 0]}}
    return Mob("grossiste_clandestin", bones, anims)


def statue_vivante() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("core", (0, 12, 0), "root", [Cube((-4, 8, -4), (8, 8, 8), "B0BEC5", front=statue_face, pattern="checker")]),
        Bone("ring_a", (0, 16, 0), "core", [Cube((-7, 15, -1), (14, 2, 2), "ECEFF1"), Cube((-1, 9, -1), (2, 14, 2), "ECEFF1")]),
        Bone("ring_b", (0, 12, 0), "core", [Cube((-9, 11, -1), (18, 2, 2), "90A4AE"), Cube((-1, 3, -1), (2, 18, 2), "90A4AE")],
             rotation=(0, 45, 0)),
        Bone("cube_top", (0, 22, 0), "core", [Cube((-2, 20, -2), (4, 4, 4), "CFD8DC")]),
        Bone("cube_side", (8, 12, 0), "core", [Cube((6, 10, -2), (4, 4, 4), "78909C")]),
        Bone("fist", (0, 6, -6), "root", [Cube((-3, 4, -9), (6, 6, 6), "607D8B")]),
    ]
    anims = {
        "idle": (2.4, True, {
            "root": {"position": loop(2.4, [(0, [0, 0, 0]), (1.2, [0, 3, 0]), (2.4, [0, 0, 0])])},
            "ring_a": {"rotation": swing(2.4, 25, axis=1)},
            "ring_b": {"rotation": swing(2.4, -30, axis=1)},
            "cube_top": {"position": loop(2.4, [(0, [0, 0, 0]), (1.2, [0, 2, 0]), (2.4, [0, 0, 0])])},
            "cube_side": {"position": loop(2.4, [(0, [0, 0, 0]), (1.2, [1, 0, 0]), (2.4, [0, 0, 0])])},
        }),
        "walk": (1.0, True, {
            "root": {"position": loop(1.0, [(0, [0, 1, 0]), (0.5, [0, 3, 0]), (1.0, [0, 1, 0])])},
            "fist": {"position": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 0, -2]), (1.0, [0, 0, 0])])},
            "ring_a": {"rotation": swing(1.0, 40, axis=1)},
            "ring_b": {"rotation": swing(1.0, -40, axis=1)},
        }),
        "attack": (0.6, False, {
            "fist": {"position": loop(0.6, [(0, [0, 0, 0]), (0.2, [0, 4, -8]), (0.35, [0, -2, 4]), (0.6, [0, 0, 0])])},
            "core": {"rotation": loop(0.6, [(0, [0, 0, 0]), (0.2, [-15, 0, 0]), (0.6, [0, 0, 0])])},
        }),
        "special": (1.2, False, {  # assemblage / figé
            "cube_top": {"position": loop(1.2, [(0, [0, 8, 0]), (0.5, [0, 0, 0]), (1.2, [0, 0, 0])])},
            "cube_side": {"position": loop(1.2, [(0, [8, 0, 0]), (0.5, [0, 0, 0]), (1.2, [0, 0, 0])])},
            "ring_a": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.6, [0, 180, 0]), (1.2, [0, 360, 0])])},
            "ring_b": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.6, [0, -180, 0]), (1.2, [0, -360, 0])])},
            "root": {"scale": loop(1.2, [(0, [0.6, 0.6, 0.6]), (0.5, [1.1, 1.1, 1.1]), (1.2, [1, 1, 1])])},
        }),
    }
    return Mob("statue_vivante", bones, anims)


def mimique_d20() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 8, 0), "root", [
            Cube((-7, 0, -7), (14, 10, 14), "4A148C", front=mimic_front),
            Cube((-6, 10, -6), (12, 2, 12), "6A1B9A"),
        ]),
        Bone("lid", (0, 12, 0), "body", [
            Cube((-7, 10, -7), (14, 4, 14), "7B1FA2", front=mimic_front),
            Cube((-1, 14, -8), (2, 2, 2), "FFD700"),
        ]),
        Bone("jaw", (0, 4, -4), "body", [
            Cube((-6, 1, -8), (12, 3, 4), "311B92"),
            Cube((-5, 0, -9), (2, 2, 2), "FFFFFF"),
            Cube((-1, 0, -9), (2, 2, 2), "FFFFFF"),
            Cube((3, 0, -9), (2, 2, 2), "FFFFFF"),
        ]),
        Bone("tongue", (0, 3, -6), "jaw", [Cube((-1, 2, -10), (2, 1, 5), "E91E63")]),
    ]
    anims = {
        "idle": (2.0, True, {
            "body": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1.02, 0.98, 1.02]), (2.0, [1, 1, 1])])},
            "lid": {"rotation": {0.0: [0, 0, 0]}},
        }),
        "walk": (0.7, True, {
            "root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 4, 0]), (0.4, [0, 0, 0]), (0.55, [0, 3, 0]), (0.7, [0, 0, 0])])},
            "body": {"rotation": swing(0.7, 8, axis=2)},
        }),
        "attack": (0.5, False, {
            "lid": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-70, 0, 0]), (0.35, [-40, 0, 0]), (0.5, [0, 0, 0])])},
            "jaw": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [50, 0, 0]), (0.35, [20, 0, 0]), (0.5, [0, 0, 0])])},
            "root": {"position": loop(0.5, [(0, [0, 0, 0]), (0.15, [0, 6, -4]), (0.35, [0, 0, 0]), (0.5, [0, 0, 0])])},
        }),
        "special": (1.0, False, {  # réveil + roulé
            "lid": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-90, 0, 0]), (1.0, [-60, 0, 0])])},
            "jaw": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [40, 0, 0]), (1.0, [25, 0, 0])])},
            "root": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 180, 20]), (1.0, [0, 360, 0])])},
            "tongue": {"position": loop(1.0, [(0, [0, 0, 0]), (0.4, [0, 0, -3]), (1.0, [0, 0, -2])])},
        }),
    }
    return Mob("mimique_d20", bones, anims)


# --------------------------------------------------------------------------- lot 3
def gym_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 2, y + 6, x + w - 3, y + 7), fill=rgb("5D4037"))  # sourire force


def gym_tank(draw, x, y, w, h):
    draw.rectangle((x, y, x + w - 1, y + 2), fill=rgb("C62828"))
    px(draw, x + w // 2, y + 5, "FFFFFF")


def disco_face(draw, x, y, w, h):
    eyes("E91E63", white=True)(draw, x, y, w, h)
    draw.rectangle((x + 1, y + 6, x + w - 2, y + 6), fill=rgb("FFD700"))


def disco_suit(draw, x, y, w, h):
    for j in range(0, h, 2):
        c = "7B1FA2" if (j // 2) % 2 == 0 else "1565C0"
        draw.line((x, y + j, x + w - 1, y + j), fill=rgb(c))
    px(draw, x + w // 2, y + 3, "FFD700")


def hacker_face(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("1B5E20"))
    for row in (3, 5, 7):
        draw.line((x + 2, y + row, x + w - 3, y + row), fill=rgb("69F0AE"))
    px(draw, x + 3, y + 3, "FFFFFF")


def bot_face(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 2, x + w - 2, y + 5), fill=rgb("00E5FF"))
    px(draw, x + 2, y + 3, "FFFFFF"); px(draw, x + w - 3, y + 3, "FFFFFF")
    draw.rectangle((x + 2, y + 6, x + w - 3, y + 7), fill=rgb("263238"))


def barista_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 2, y + 6, x + w - 3, y + 6), fill=rgb("6D4C41"))


def barista_apron(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 2, x + w - 2, y + h - 1), fill=rgb("FAFAFA"))
    draw.rectangle((x + 2, y + 4, x + w - 3, y + 8), fill=rgb("5D4037"))  # tache cafe


def pirate_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x, y + 1, x + w - 1, y + 2), fill=rgb("111111"))  # bandeau
    px(draw, x + 1, y + 1, "C62828")


def pirate_coat(draw, x, y, w, h):
    draw.rectangle((x, y, x + w - 1, y + h - 1), fill=rgb("1A237E"))
    draw.rectangle((x + 2, y + 2, x + w - 3, y + 6), fill=rgb("F5F5F5"))
    px(draw, x + w // 2, y + 4, "FFD700")


def bodybuilder() -> Mob:
    bones = humanoid("E0AC69", Cube((-5, 12, -3), (10, 12, 6), "C62828", front=gym_tank), gym_face,
                     "E0AC69", "1565C0")
    # bras hypertrophies
    bones[3] = Bone("right_arm", (-6, 22, 0), "body", [Cube((-11, 12, -3), (6, 12, 6), "E0AC69")])
    bones[4] = Bone("left_arm", (6, 22, 0), "body", [Cube((5, 12, -3), (6, 12, 6), "E0AC69")])
    bones.append(Bone("dumbbell", (-9, 12, 0), "right_arm", [
        Cube((-12, 10, -1), (2, 2, 2), "37474F"),
        Cube((-14, 9, -2), (2, 4, 4), "78909C"),
        Cube((-10, 9, -2), (2, 4, 4), "78909C"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-120, 0, 20]), (0.35, [30, 0, 0]), (0.5, [0, 0, 0])])},
        "body": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [0, 15, 0]), (0.5, [0, 0, 0])])},
    }
    flex = {
        "right_arm": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [-90, 0, -70]), (0.9, [-90, 0, -70]), (1.2, [0, 0, 0])])},
        "left_arm": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [-90, 0, 70]), (0.9, [-90, 0, 70]), (1.2, [0, 0, 0])])},
        "body": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [1.15, 1.1, 1.15]), (0.9, [1.15, 1.1, 1.15]), (1.2, [1, 1, 1])])},
    }
    return Mob("bodybuilder", bones, humanoid_anims(attack, special=(1.2, flex), walk_arms=15))


def danseur_disco() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "7B1FA2", front=disco_suit), disco_face,
                     "7B1FA2", "111111")
    bones.append(Bone("afro", (0, 32, 0), "head", [Cube((-5, 30, -5), (10, 6, 10), "3E2723", inflate=0.4)]))
    bones.append(Bone("collar", (0, 24, 0), "body", [Cube((-5, 22, -3), (10, 3, 6), "FFD700", inflate=0.2)]))
    attack = {
        "root": {"rotation": loop(0.6, [(0, [0, 0, 0]), (0.3, [0, 180, 0]), (0.6, [0, 360, 0])])},
        "right_arm": {"rotation": swing(0.6, 60)},
        "left_arm": {"rotation": swing(0.6, -60)},
    }
    dance = {
        "body": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [0, 0, 15]), (0.6, [0, 0, -15]), (0.9, [0, 0, 15]), (1.2, [0, 0, 0])]),
                 "position": loop(1.2, [(0, [0, 0, 0]), (0.3, [0, 3, 0]), (0.6, [0, 0, 0]), (0.9, [0, 3, 0]), (1.2, [0, 0, 0])])},
        "right_arm": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [-100, 0, -40]), (0.6, [-40, 0, 20]), (0.9, [-100, 0, -40]), (1.2, [0, 0, 0])])},
        "left_arm": {"rotation": loop(1.2, [(0, [0, 0, 0]), (0.3, [-40, 0, -20]), (0.6, [-100, 0, 40]), (0.9, [-40, 0, -20]), (1.2, [0, 0, 0])])},
        "right_leg": {"rotation": swing(1.2, 25)},
        "left_leg": {"rotation": swing(1.2, -25)},
    }
    return Mob("danseur_disco", bones, humanoid_anims(attack, special=(1.2, dance), walk_arms=40))


def hacker_fantome() -> Mob:
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 14, 0), "root", [
            Cube((-4, 10, -2), (8, 10, 4), "1B5E20", front=hacker_face, pattern="stripes"),
            Cube((-5, 8, -1), (10, 2, 2), "69F0AE"),
        ]),
        Bone("hood", (0, 22, 0), "body", [Cube((-5, 18, -4), (10, 8, 8), "0D1F12", inflate=0.3)]),
        Bone("right_arm", (-5, 18, 0), "body", [Cube((-7, 8, -1), (3, 10, 3), "1B5E20")]),
        Bone("left_arm", (5, 18, 0), "body", [Cube((4, 8, -1), (3, 10, 3), "1B5E20")]),
        Bone("trail", (0, 10, 2), "body", [Cube((-3, 0, 1), (6, 10, 1), "69F0AE", inflate=0.5)]),
    ]
    anims = {
        "idle": (2.0, True, {
            "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
            "trail": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1.1, 0.8, 1]), (2.0, [1, 1, 1])])},
            "hood": {"rotation": swing(2.0, 4, axis=2)},
        }),
        "walk": (0.8, True, {
            "root": {"position": loop(0.8, [(0, [0, 2, 0]), (0.4, [0, 4, 0]), (0.8, [0, 2, 0])])},
            "right_arm": {"rotation": swing(0.8, 20)},
            "left_arm": {"rotation": swing(0.8, -20)},
            "body": {"rotation": {0.0: [8, 0, 0]}},
        }),
        "attack": (0.5, False, {
            "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-110, 0, 0]), (0.5, [0, 0, 0])])},
            "body": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.15, [1.2, 0.9, 1.2]), (0.5, [1, 1, 1])])},
        }),
        "special": (0.6, False, {
            "root": {"scale": loop(0.6, [(0, [1, 1, 1]), (0.15, [0.2, 1.4, 0.2]), (0.3, [1.3, 0.5, 1.3]), (0.45, [0.4, 1.2, 0.4]), (0.6, [1, 1, 1])]),
                     "rotation": loop(0.6, [(0, [0, 0, 0]), (0.3, [0, 180, 0]), (0.6, [0, 360, 0])])},
        }),
    }
    return Mob("hacker_fantome", bones, anims)


def bot_speedrunner() -> Mob:
    bones = humanoid("90A4AE", Cube((-4, 12, -2), (8, 12, 4), "455A64", front=bot_face), bot_face,
                     "607D8B", "37474F")
    bones.append(Bone("visor", (0, 28, -4), "head", [Cube((-4, 26, -5), (8, 3, 1), "00E5FF")]))
    bones.append(Bone("backpack", (0, 18, 3), "body", [
        Cube((-4, 14, 2), (8, 8, 3), "263238"),
        Cube((-2, 16, 5), (4, 3, 2), "FF6D00"),
    ]))
    attack = {
        "root": {"position": loop(0.4, [(0, [0, 0, 0]), (0.1, [0, 0, -6]), (0.25, [0, 2, 2]), (0.4, [0, 0, 0])])},
        "right_arm": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.1, [-90, 0, 0]), (0.4, [0, 0, 0])])},
    }
    dash = {
        "root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.2, [0, 4, -10]), (0.5, [0, 2, -20]), (0.8, [0, 0, 0])]),
                 "scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [0.8, 1.1, 1.4]), (0.8, [1, 1, 1])])},
        "backpack": {"rotation": swing(0.8, 15)},
    }
    anims = humanoid_anims(attack, special=(0.8, dash), walk_arms=45)
    anims["walk"][2]["body"] = {"rotation": {0.0: [12, 0, 0]}}
    return Mob("bot_speedrunner", bones, anims)


def barista() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "5D4037", front=barista_apron), barista_face,
                     "FFFFFF", "3E2723")
    bones.append(Bone("hat", (0, 32, 0), "head", [
        Cube((-3, 32, -3), (6, 2, 6), "FAFAFA"),
        Cube((-2, 34, -2), (4, 3, 4), "FAFAFA"),
    ]))
    bones.append(Bone("cup", (-6, 14, -2), "right_arm", [
        Cube((-8, 12, -4), (4, 5, 4), "FAFAFA"),
        Cube((-7, 16, -3), (2, 2, 2), "5D4037"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [-140, 0, 10]), (0.35, [20, 0, 0]), (0.5, [0, 0, 0])])},
        "cup": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.18, [-40, 0, 0]), (0.5, [0, 0, 0])])},
    }
    pour = {
        "right_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-60, 0, -20]), (0.7, [-50, 0, -30]), (1.0, [0, 0, 0])])},
        "cup": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [50, 0, 0]), (0.8, [50, 0, 0]), (1.0, [0, 0, 0])])},
        "head": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [15, 0, 0]), (1.0, [0, 0, 0])])},
    }
    return Mob("barista", bones, humanoid_anims(attack, special=(1.0, pour)))


def pirate_torrent() -> Mob:
    bones = humanoid("E0AC69", Cube((-4, 12, -2), (8, 12, 4), "1A237E", front=pirate_coat), pirate_face,
                     "1A237E", "212121")
    bones.append(Bone("hat", (0, 32, 0), "head", [
        Cube((-5, 31, -5), (10, 2, 10), "111111"),
        Cube((-3, 33, -3), (6, 4, 6), "111111"),
        Cube((-1, 33, -6), (2, 3, 2), "C62828"),
    ]))
    bones.append(Bone("drive", (-6, 14, -2), "right_arm", [
        Cube((-8, 12, -4), (4, 2, 5), "37474F"),
        Cube((-7, 13, -3), (2, 1, 2), "69F0AE"),
    ]))
    attack = {
        "right_arm": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-100, 20, 0]), (0.35, [20, -10, 0]), (0.5, [0, 0, 0])])},
        "drive": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-30, 0, 0]), (0.5, [0, 0, 0])])},
    }
    download = {
        "drive": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-80, 0, 0]), (0.7, [-80, 0, 0]), (1.0, [0, 0, 0])]),
                  "scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.2, 1.2, 1.2]), (1.0, [1, 1, 1])])},
        "right_arm": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [-70, 0, -20]), (0.7, [-70, 0, -20]), (1.0, [0, 0, 0])])},
        "body": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [10, 15, 0]), (1.0, [0, 0, 0])])},
    }
    return Mob("pirate_torrent", bones, humanoid_anims(attack, special=(1.0, download)))


# --------------------------------------------------------------------------- lot 4 — silhouettes NON-Steve (objets / creatures)
def sign_front(draw, x, y, w, h):
    draw.rectangle((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("C62828"))
    # greve / X
    for i in range(2, min(w, h) - 2):
        px(draw, x + i, y + i, "FFFFFF")
        px(draw, x + w - 1 - i, y + i, "FFFFFF")


def bubble_front(draw, x, y, w, h):
    draw.ellipse((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("FFD700"), outline=rgb("F9A825"))
    px(draw, x + w // 2 - 1, y + h // 2 - 1, "1B5E20")
    px(draw, x + w // 2 + 1, y + h // 2 - 1, "1B5E20")
    draw.rectangle((x + w // 2 - 1, y + h // 2 + 1, x + w // 2 + 1, y + h // 2 + 2), fill=rgb("1B5E20"))


def cloud_face(draw, x, y, w, h):
    eyes("263238", white=True, y_frac=0.45, gap=0.25)(draw, x, y, w, h)


def popit_front(draw, x, y, w, h):
    draw.rectangle((x, y, x + w - 1, y + h - 1), fill=rgb("F48FB1"))
    for row in range(1, h - 1, 3):
        for col in range(1, w - 1, 3):
            draw.ellipse((x + col, y + row, x + col + 1, y + row + 1), fill=rgb("FCE4EC"))


def clock_face(draw, x, y, w, h):
    draw.ellipse((x + 1, y + 1, x + w - 2, y + h - 2), fill=rgb("FAFAFA"), outline=rgb("C62828"))
    cx, cy = x + w // 2, y + h // 2
    draw.line((cx, cy, cx, y + 3), fill=rgb("212121"))
    draw.line((cx, cy, x + w - 4, cy + 2), fill=rgb("C62828"))


def manifestant() -> Mob:
    """Pancarte de greve flottante + megaphone — pas de corps humain."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("pole", (0, 10, 0), "root", [Cube((-1, 0, -1), (2, 18, 2), "5D4037")]),
        Bone("sign", (0, 22, 0), "pole", [Cube((-10, 16, -2), (20, 12, 2), "ECEFF1", front=sign_front)]),
        Bone("megaphone", (8, 14, 0), "root", [
            Cube((6, 12, -2), (4, 4, 4), "FFD700"),
            Cube((10, 11, -3), (4, 6, 6), "FF6F00"),
        ]),
    ]
    anims = {
        "idle": (2.0, True, {
            "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
            "sign": {"rotation": swing(2.0, 6, axis=2)},
            "megaphone": {"rotation": swing(2.0, 8)},
        }),
        "walk": (0.8, True, {
            "root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0, 0]), (0.6, [0, 3, 0]), (0.8, [0, 0, 0])])},
            "sign": {"rotation": swing(0.8, 12, axis=2)},
            "megaphone": {"rotation": swing(0.8, 20)},
        }),
        "attack": (0.5, False, {
            "sign": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-25, 0, 0]), (0.3, [20, 0, 0]), (0.5, [0, 0, 0])])},
            "pole": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.15, [-15, 0, 0]), (0.5, [0, 0, 0])])},
        }),
        "special": (1.0, False, {
            "megaphone": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.4, 1.4, 1.4]), (0.6, [1.1, 1.1, 1.1]), (1.0, [1, 1, 1])]),
                          "rotation": swing(1.0, 25)},
            "sign": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 20, 0]), (1.0, [0, 0, 0])])},
        }),
    }
    return Mob("manifestant", bones, anims)


def crypto_bulle() -> Mob:
    """Bulle crypto flottante (piece / graphique) — pas de Steve."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("bubble", (0, 12, 0), "root", [
            Cube((-8, 4, -8), (16, 16, 16), "FFD700", front=bubble_front, inflate=0.5),
        ]),
        Bone("chart", (0, 20, -8), "bubble", [
            Cube((-6, 18, -9), (12, 6, 1), "1B5E20"),
        ]),
        Bone("spark", (6, 22, 0), "bubble", [Cube((4, 20, -1), (3, 3, 3), "69F0AE")]),
    ]
    anims = {
        "idle": (2.4, True, {
            "root": {"position": loop(2.4, [(0, [0, 0, 0]), (1.2, [0, 4, 0]), (2.4, [0, 0, 0])])},
            "bubble": {"scale": loop(2.4, [(0, [1, 1, 1]), (1.2, [1.08, 1.08, 1.08]), (2.4, [1, 1, 1])])},
            "spark": {"rotation": swing(2.4, 30, axis=1)},
        }),
        "walk": (1.0, True, {
            "root": {"position": loop(1.0, [(0, [0, 2, 0]), (0.5, [0, 5, 0]), (1.0, [0, 2, 0])])},
            "bubble": {"rotation": swing(1.0, 10, axis=2)},
        }),
        "attack": (0.5, False, {
            "bubble": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.3, 0.8, 1.3]), (0.4, [0.9, 1.2, 0.9]), (0.5, [1, 1, 1])])},
        }),
        "special": (1.0, False, {  # pump then dump
            "bubble": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.5, 1.5, 1.5]), (0.7, [0.4, 0.4, 0.4]), (1.0, [1, 1, 1])])},
            "chart": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [0, 0, 20]), (0.7, [0, 0, -40]), (1.0, [0, 0, 0])])},
        }),
    }
    return Mob("crypto_bulle", bones, anims)


def nuage_orage() -> Mob:
    """Nuage orageux avec eclairs — flotte, pas de jambes Steve."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("cloud", (0, 14, 0), "root", [
            Cube((-10, 10, -6), (20, 10, 12), "78909C", front=cloud_face),
            Cube((-7, 16, -5), (14, 6, 10), "90A4AE", inflate=0.4),
            Cube((-12, 8, -4), (8, 6, 8), "607D8B"),
            Cube((4, 8, -4), (8, 6, 8), "607D8B"),
        ]),
        Bone("bolt_a", (-4, 8, 0), "cloud", [Cube((-5, 0, -1), (2, 10, 2), "FFEB3B")]),
        Bone("bolt_b", (4, 8, 0), "cloud", [Cube((3, 2, -1), (2, 8, 2), "FFC107")]),
    ]
    anims = {
        "idle": (2.0, True, {
            "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
            "bolt_a": {"scale": loop(2.0, [(0, [1, 1, 1]), (0.3, [1, 0.3, 1]), (0.5, [1, 1.2, 1]), (2.0, [1, 1, 1])])},
            "bolt_b": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1, 0.4, 1]), (1.3, [1, 1.1, 1]), (2.0, [1, 1, 1])])},
        }),
        "walk": (1.0, True, {
            "root": {"position": loop(1.0, [(0, [0, 2, 0]), (0.5, [0, 4, 0]), (1.0, [0, 2, 0])])},
            "cloud": {"rotation": swing(1.0, 6, axis=2)},
        }),
        "attack": (0.5, False, {
            "bolt_a": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.15, [1.5, 1.8, 1.5]), (0.5, [1, 1, 1])])},
            "bolt_b": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.5, 1.8, 1.5]), (0.5, [1, 1, 1])])},
            "cloud": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.15, [1.15, 0.9, 1.15]), (0.5, [1, 1, 1])])},
        }),
        "special": (0.8, False, {
            "cloud": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.3, 0.7, 1.3]), (0.6, [0.9, 1.3, 0.9]), (0.8, [1, 1, 1])])},
            "bolt_a": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 0, 25]), (0.8, [0, 0, 0])])},
            "bolt_b": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 0, -25]), (0.8, [0, 0, 0])])},
        }),
    }
    return Mob("nuage_orage", bones, anims)


def slider_multivers() -> Mob:
    """Barre slider bleu/orange + curseur — portail vivant."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("rail", (0, 10, 0), "root", [
            Cube((-12, 8, -2), (12, 4, 4), "1565C0"),
            Cube((0, 8, -2), (12, 4, 4), "EF6C00"),
        ]),
        Bone("knob", (0, 12, 0), "rail", [
            Cube((-3, 6, -3), (6, 8, 6), "FAFAFA", front=eyes("212121")),
        ]),
        Bone("portal_a", (-10, 16, 0), "rail", [Cube((-12, 12, -1), (4, 8, 2), "42A5F5", inflate=0.3)]),
        Bone("portal_b", (10, 16, 0), "rail", [Cube((8, 12, -1), (4, 8, 2), "FF9800", inflate=0.3)]),
    ]
    anims = {
        "idle": (2.0, True, {
            "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
            "knob": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [4, 0, 0]), (2.0, [0, 0, 0])])},
            "portal_a": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1, 1.2, 1]), (2.0, [1, 1, 1])])},
            "portal_b": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1, 0.8, 1]), (2.0, [1, 1, 1])])},
        }),
        "walk": (0.8, True, {
            "root": {"position": loop(0.8, [(0, [0, 1, 0]), (0.4, [0, 3, 0]), (0.8, [0, 1, 0])])},
            "knob": {"position": loop(0.8, [(0, [-3, 0, 0]), (0.4, [3, 0, 0]), (0.8, [-3, 0, 0])])},
        }),
        "attack": (0.5, False, {
            "knob": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -6]), (0.5, [0, 0, 0])])},
            "rail": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.2, 1, 0.8]), (0.5, [1, 1, 1])])},
        }),
        "special": (1.0, False, {
            "knob": {"position": loop(1.0, [(0, [0, 0, 0]), (0.3, [-8, 0, 0]), (0.7, [8, 0, 0]), (1.0, [0, 0, 0])])},
            "portal_a": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.5, 1.5, 1.5]), (1.0, [1, 1, 1])])},
            "portal_b": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.7, [1.5, 1.5, 1.5]), (1.0, [1, 1, 1])])},
            "root": {"rotation": loop(1.0, [(0, [0, 0, 0]), (1.0, [0, 360, 0])])},
        }),
    }
    return Mob("slider_multivers", bones, anims)


def pop_it_geant() -> Mob:
    """Pop-it geant rebondissant — ASMR."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("board", (0, 6, 0), "root", [
            Cube((-10, 2, -8), (20, 3, 16), "F48FB1", front=popit_front, top=popit_front),
        ]),
        Bone("bubbles", (0, 8, 0), "board", [
            Cube((-8, 5, -6), (4, 3, 4), "FCE4EC"),
            Cube((-2, 5, -6), (4, 3, 4), "F8BBD0"),
            Cube((4, 5, -6), (4, 3, 4), "FCE4EC"),
            Cube((-8, 5, 0), (4, 3, 4), "F8BBD0"),
            Cube((-2, 5, 0), (4, 3, 4), "FCE4EC"),
            Cube((4, 5, 0), (4, 3, 4), "F8BBD0"),
        ]),
    ]
    anims = {
        "idle": (1.6, True, {
            "board": {"scale": loop(1.6, [(0, [1, 1, 1]), (0.8, [1.04, 0.92, 1.04]), (1.6, [1, 1, 1])])},
            "bubbles": {"position": loop(1.6, [(0, [0, 0, 0]), (0.8, [0, 1, 0]), (1.6, [0, 0, 0])])},
        }),
        "walk": (0.6, True, {
            "root": {"position": loop(0.6, [(0, [0, 0, 0]), (0.15, [0, 4, 0]), (0.3, [0, 0, 0]), (0.45, [0, 4, 0]), (0.6, [0, 0, 0])])},
            "board": {"rotation": swing(0.6, 8, axis=2)},
        }),
        "attack": (0.5, False, {
            "bubbles": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.15, [1, 0.3, 1]), (0.35, [1, 1.4, 1]), (0.5, [1, 1, 1])])},
            "board": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.1, 0.7, 1.1]), (0.5, [1, 1, 1])])},
        }),
        "special": (1.0, False, {
            "bubbles": {"scale": loop(1.0, [
                (0, [1, 1, 1]), (0.15, [1, 0.2, 1]), (0.3, [1, 1, 1]),
                (0.45, [1, 0.2, 1]), (0.6, [1, 1, 1]), (0.75, [1, 0.2, 1]), (1.0, [1, 1, 1]),
            ])},
        }),
    }
    return Mob("pop_it_geant", bones, anims)


def reveil_cauchemar() -> Mob:
    """Reveil-matin a cloches — Cauchemard du matin."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("body", (0, 8, 0), "root", [
            Cube((-7, 2, -4), (14, 12, 8), "C62828", front=clock_face),
            Cube((-6, 3, -5), (12, 10, 1), "FAFAFA"),
        ]),
        Bone("bell_l", (-6, 16, 0), "body", [
            Cube((-9, 14, -3), (5, 5, 5), "FFD700"),
            Cube((-7, 18, -1), (1, 3, 1), "FFD700"),
        ]),
        Bone("bell_r", (6, 16, 0), "body", [
            Cube((4, 14, -3), (5, 5, 5), "FFD700"),
            Cube((6, 18, -1), (1, 3, 1), "FFD700"),
        ]),
        Bone("legs", (0, 2, 0), "root", [
            Cube((-5, 0, -2), (3, 3, 4), "212121"),
            Cube((2, 0, -2), (3, 3, 4), "212121"),
        ]),
        Bone("hammer", (0, 18, 0), "body", [Cube((-1, 16, -1), (2, 6, 2), "90A4AE")]),
    ]
    anims = {
        "idle": (1.2, True, {
            "hammer": {"rotation": loop(1.2, [(0, [0, 0, -20]), (0.3, [0, 0, 20]), (0.6, [0, 0, -20]), (1.2, [0, 0, -20])])},
            "bell_l": {"rotation": swing(1.2, 12, axis=2)},
            "bell_r": {"rotation": swing(1.2, -12, axis=2)},
        }),
        "walk": (0.5, True, {
            "root": {"position": loop(0.5, [(0, [0, 0, 0]), (0.15, [0, 3, 0]), (0.3, [0, 0, 0]), (0.45, [0, 3, 0]), (0.5, [0, 0, 0])])},
            "legs": {"rotation": swing(0.5, 15, axis=2)},
            "hammer": {"rotation": swing(0.5, 25, axis=2)},
        }),
        "attack": (0.4, False, {
            "body": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.15, [-20, 0, 0]), (0.3, [15, 0, 0]), (0.4, [0, 0, 0])])},
            "hammer": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.15, [0, 0, 40]), (0.4, [0, 0, 0])])},
        }),
        "special": (1.0, False, {  # sonnerie folle
            "bell_l": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.1, [0, 0, 30]), (0.2, [0, 0, -30]), (0.3, [0, 0, 30]), (0.4, [0, 0, -30]), (1.0, [0, 0, 0])])},
            "bell_r": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.1, [0, 0, -30]), (0.2, [0, 0, 30]), (0.3, [0, 0, -30]), (0.4, [0, 0, 30]), (1.0, [0, 0, 0])])},
            "body": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.2, [1.1, 1.1, 1.1]), (0.4, [1, 1, 1]), (0.6, [1.1, 1.1, 1.1]), (1.0, [1, 1, 1])])},
            "hammer": {"rotation": swing(1.0, 40, axis=2)},
        }),
    }
    return Mob("reveil_cauchemar", bones, anims)


# --------------------------------------------------------------------------- reshape : plus de silhouettes Steve
# (redefinit les usines humanoïdes — Python garde la derniere def)

def _obj_anims(idle_bones, walk_bones, attack_bones, special_bones, idle_len=2.0, walk_len=0.8, atk_len=0.5, sp_len=1.0):
    return {
        "idle": (idle_len, True, idle_bones),
        "walk": (walk_len, True, walk_bones),
        "attack": (atk_len, False, attack_bones),
        "special": (sp_len, False, special_bones),
    }


def chef() -> Mob:
    """Toque + pizza volante (plus de corps Steve)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("pizza", (0, 8, 0), "root", [
            Cube((-8, 6, -8), (16, 2, 16), "F4C04A",
                 top=lambda d, x, y, w, h: [px(d, x + i, y + j, "C62828")
                                           for i, j in ((2, 2), (8, 3), (4, 8), (11, 10), (6, 12))]),
        ]),
        Bone("toque", (0, 14, 0), "root", [
            Cube((-5, 10, -5), (10, 3, 10), "FFFFFF"),
            Cube((-6, 13, -6), (12, 8, 12), "FAFAFA"),
        ]),
        Bone("eyes", (0, 9, -8), "pizza", [
            Cube((-5, 8, -9), (3, 3, 1), "FFFFFF"), Cube((-4, 9, -9.5), (1, 1, 1), "111111"),
            Cube((2, 8, -9), (3, 3, 1), "FFFFFF"), Cube((3, 9, -9.5), (1, 1, 1), "111111"),
        ]),
    ]
    return Mob("chef_italien", bones, _obj_anims(
        {"toque": {"rotation": swing(2.0, 4, axis=2)}, "pizza": {"rotation": swing(2.0, 3, axis=1)},
         "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0, 0]), (0.6, [0, 3, 0]), (0.8, [0, 0, 0])])},
         "pizza": {"rotation": swing(0.8, 15, axis=1)}},
        {"pizza": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-40, 0, 0]), (0.4, [20, 0, 0]), (0.5, [0, 0, 0])])}},
        {"toque": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.2, 1.4, 1.2]), (0.7, [1.2, 1.4, 1.2]), (1.0, [1, 1, 1])])},
         "pizza": {"rotation": loop(1.0, [(0, [0, 0, 0]), (1.0, [0, 360, 0])])}},
    ))


def dracula() -> Mob:
    """Nuee de chauves-souris + cape flottante + crocs."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("cape", (0, 12, 0), "root", [
            Cube((-8, 2, 0), (16, 18, 1), "1A0000"),
            Cube((-6, 16, -1), (12, 6, 1), "8B0000"),
        ]),
        Bone("fangs", (0, 14, -4), "root", [
            Cube((-3, 12, -5), (2, 4, 2), "FAFAFA"),
            Cube((1, 12, -5), (2, 4, 2), "FAFAFA"),
            Cube((-4, 14, -4), (8, 4, 3), "DADAE8", front=dracula_face),
        ]),
        Bone("bat_a", (-8, 18, 2), "root", [Cube((-10, 16, 0), (4, 3, 6), "111111")]),
        Bone("bat_b", (8, 16, 2), "root", [Cube((6, 14, 0), (4, 3, 6), "111111")]),
        Bone("bat_c", (0, 20, 4), "root", [Cube((-2, 18, 2), (4, 3, 6), "1A0000")]),
    ]
    return Mob("comte_dracula", bones, _obj_anims(
        {"cape": {"rotation": swing(2.0, 8)}, "bat_a": {"rotation": swing(2.0, 20, axis=2)},
         "bat_b": {"rotation": swing(2.0, -20, axis=2)}, "bat_c": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
         "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.8, [(0, [0, 1, 0]), (0.4, [0, 3, 0]), (0.8, [0, 1, 0])])},
         "cape": {"rotation": {0.0: [15, 0, 0]}}, "bat_a": {"rotation": swing(0.8, 35, axis=2)}, "bat_b": {"rotation": swing(0.8, -35, axis=2)}},
        {"fangs": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -4]), (0.5, [0, 0, 0])])},
         "cape": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [30, 0, 0]), (0.5, [0, 0, 0])])}},
        {"root": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.1, 0.5, 1.1]), (0.8, [0, 0, 0]), (1.0, [0, 0, 0])]),
                  "rotation": loop(1.0, [(0, [0, 0, 0]), (1.0, [0, 720, 0])])},
         "cape": {"rotation": {0.0: [60, 0, 0]}}},
    ))


def toi_du_futur() -> Mob:
    """Casque neon + jetpack chrome flottant."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("helmet", (0, 16, 0), "root", [
            Cube((-5, 12, -5), (10, 8, 10), "B0BEC5", front=futur_face),
            Cube((-5, 14, -6), (10, 4, 1), "00E5FF", inflate=0.2),
        ]),
        Bone("jetpack", (0, 12, 4), "root", [
            Cube((-6, 8, 2), (5, 10, 4), "455A64"),
            Cube((1, 8, 2), (5, 10, 4), "455A64"),
            Cube((-5, 6, 4), (3, 3, 2), "FF6D00"),
            Cube((2, 6, 4), (3, 3, 2), "FF6D00"),
        ]),
        Bone("beam", (0, 14, -6), "helmet", [Cube((-1, 13, -14), (2, 2, 8), "00E5FF")]),
    ]
    return Mob("toi_du_futur", bones, _obj_anims(
        {"root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
         "jetpack": {"rotation": swing(2.0, 4, axis=2)}, "helmet": {"rotation": swing(2.0, 3, axis=1)}},
        {"root": {"position": loop(0.8, [(0, [0, 2, 0]), (0.4, [0, 4, 0]), (0.8, [0, 2, 0])])},
         "jetpack": {"rotation": swing(0.8, 10, axis=2)}},
        {"beam": {"scale": loop(0.5, [(0, [1, 1, 0.2]), (0.2, [1, 1, 1.5]), (0.5, [1, 1, 0.2])])},
         "helmet": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.15, 1.15, 1.15]), (0.5, [1, 1, 1])])}},
        {"root": {"position": loop(1.0, [(0, [0, 0, 0]), (0.4, [0, 8, 0]), (1.0, [0, 0, 0])])},
         "jetpack": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.2, 1.3, 1.2]), (1.0, [1, 1, 1])])}},
    ))


def douanier() -> Mob:
    """Tampon geant + barriere douaniere."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("barrier", (0, 10, 0), "root", [
            Cube((-12, 8, -1), (24, 3, 2), "FFD700"),
            Cube((-12, 0, -1), (2, 10, 2), "1565C0"),
            Cube((10, 0, -1), (2, 10, 2), "1565C0"),
        ]),
        Bone("stamp", (0, 16, -4), "root", [
            Cube((-5, 12, -8), (10, 8, 8), "C62828", front=douanier_face),
            Cube((-2, 20, -5), (4, 6, 4), "5D4037"),
        ]),
    ]
    return Mob("douanier", bones, _obj_anims(
        {"stamp": {"rotation": swing(2.0, 5, axis=2)}, "barrier": {"rotation": swing(2.0, 2)}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 2, 0]), (0.8, [0, 0, 0])])},
         "stamp": {"rotation": swing(0.8, 12)}},
        {"stamp": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, -6, -2]), (0.35, [0, 2, 0]), (0.5, [0, 0, 0])])}},
        {"stamp": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.3, [20, 25, 0]), (0.7, [20, -25, 0]), (1.0, [0, 0, 0])])},
         "barrier": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.5, [1.1, 1, 1.1]), (1.0, [1, 1, 1])])}},
    ))


def grossiste_clandestin() -> Mob:
    """Montagne de PQ + lunettes noires (plus de Steve en imper)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("stack", (0, 6, 0), "root", [
            Cube((-6, 0, -6), (12, 6, 12), "FAFAFA"),
            Cube((-5, 6, -5), (10, 6, 10), "ECEFF1"),
            Cube((-4, 12, -4), (8, 6, 8), "FAFAFA"),
            Cube((-3, 18, -3), (6, 4, 6), "F5F5F5"),
        ]),
        Bone("shades", (0, 20, -4), "stack", [
            Cube((-5, 18, -5), (10, 3, 1), "111111"),
        ]),
        Bone("coat", (0, 10, 4), "root", [
            Cube((-7, 2, 2), (14, 16, 3), "37474F"),
        ]),
    ]
    return Mob("grossiste_clandestin", bones, _obj_anims(
        {"stack": {"rotation": swing(2.0, 3, axis=2)}, "shades": {"rotation": swing(2.0, 4, axis=1)}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.2, [0, 2, 0]), (0.4, [0, 0, 0]), (0.6, [0, 2, 0]), (0.8, [0, 0, 0])])},
         "coat": {"rotation": {0.0: [8, 0, 0]}}},
        {"root": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -4]), (0.5, [0, 0, 0])])},
         "coat": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [25, 0, 0]), (0.5, [0, 0, 0])])}},
        {"shades": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [0, 30, 0]), (1.0, [0, 0, 0])])},
         "stack": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.1, 1.15, 1.1]), (1.0, [1, 1, 1])])}},
        idle_len=2.0, walk_len=0.7, atk_len=0.5, sp_len=1.0,
    ))


def bodybuilder() -> Mob:
    """Halteres vivantes (plaques qui flexent)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("bar", (0, 10, 0), "root", [Cube((-12, 9, -1), (24, 2, 2), "78909C")]),
        Bone("plate_l", (-10, 10, 0), "bar", [
            Cube((-14, 6, -4), (4, 8, 8), "C62828"),
            Cube((-16, 7, -3), (2, 6, 6), "B71C1C"),
        ]),
        Bone("plate_r", (10, 10, 0), "bar", [
            Cube((10, 6, -4), (4, 8, 8), "C62828"),
            Cube((14, 7, -3), (2, 6, 6), "B71C1C"),
        ]),
        Bone("face", (0, 12, -2), "bar", [
            Cube((-3, 10, -3), (6, 4, 2), "E0AC69", front=gym_face),
        ]),
    ]
    return Mob("bodybuilder", bones, _obj_anims(
        {"plate_l": {"rotation": swing(2.0, 6, axis=2)}, "plate_r": {"rotation": swing(2.0, -6, axis=2)},
         "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.6, [(0, [0, 0, 0]), (0.15, [0, 3, 0]), (0.3, [0, 0, 0]), (0.45, [0, 3, 0]), (0.6, [0, 0, 0])])},
         "bar": {"rotation": swing(0.6, 10, axis=2)}},
        {"bar": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -40]), (0.4, [0, 0, 20]), (0.5, [0, 0, 0])])}},
        {"plate_l": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [1.3, 1.3, 1.3]), (0.9, [1.3, 1.3, 1.3]), (1.2, [1, 1, 1])])},
         "plate_r": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [1.3, 1.3, 1.3]), (0.9, [1.3, 1.3, 1.3]), (1.2, [1, 1, 1])])},
         "face": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [1.2, 1.2, 1.2]), (1.2, [1, 1, 1])])}},
        walk_len=0.6, sp_len=1.2,
    ))


def danseur_disco() -> Mob:
    """Boule disco + enceinte (plus de danseur Steve)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("speaker", (0, 4, 0), "root", [
            Cube((-6, 0, -4), (12, 8, 8), "212121", front=disco_suit),
            Cube((-4, 2, -5), (8, 4, 1), "7B1FA2"),
        ]),
        Bone("ball", (0, 16, 0), "root", [
            Cube((-5, 11, -5), (10, 10, 10), "E0E0E0", pattern="checker", front=disco_face),
        ]),
        Bone("light", (0, 22, 0), "ball", [Cube((-1, 21, -1), (2, 2, 2), "FFD700")]),
    ]
    return Mob("danseur_disco", bones, _obj_anims(
        {"ball": {"rotation": swing(2.0, 25, axis=1)}, "light": {"rotation": swing(2.0, 40, axis=1)},
         "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0, 0]), (0.6, [0, 3, 0]), (0.7, [0, 0, 0])])},
         "ball": {"rotation": swing(0.7, 40, axis=1)}},
        {"root": {"rotation": loop(0.6, [(0, [0, 0, 0]), (0.3, [0, 180, 0]), (0.6, [0, 360, 0])])},
         "ball": {"scale": loop(0.6, [(0, [1, 1, 1]), (0.3, [1.2, 1.2, 1.2]), (0.6, [1, 1, 1])])}},
        {"ball": {"rotation": loop(1.2, [(0, [0, 0, 0]), (1.2, [0, 720, 0])])},
         "speaker": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.3, [1.1, 0.9, 1.1]), (0.6, [0.95, 1.15, 0.95]), (0.9, [1.1, 0.9, 1.1]), (1.2, [1, 1, 1])])}},
        walk_len=0.7, atk_len=0.6, sp_len=1.2,
    ))


def hacker_fantome() -> Mob:
    """Terminal matrix flottant (plus de hoodie humanoide)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("screen", (0, 12, 0), "root", [
            Cube((-8, 6, -2), (16, 12, 3), "0D1F12", front=hacker_face),
            Cube((-6, 8, 1), (12, 8, 2), "1B5E20"),
        ]),
        Bone("code", (0, 12, -3), "screen", [
            Cube((-5, 8, -3), (10, 1, 1), "69F0AE"),
            Cube((-4, 11, -3), (8, 1, 1), "69F0AE"),
            Cube((-6, 14, -3), (9, 1, 1), "69F0AE"),
        ]),
        Bone("glitch", (6, 18, 0), "screen", [Cube((4, 16, -1), (4, 4, 2), "69F0AE")]),
    ]
    return Mob("hacker_fantome", bones, _obj_anims(
        {"root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
         "code": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, -1, 0]), (2.0, [0, 0, 0])])},
         "glitch": {"rotation": swing(2.0, 15, axis=2)}},
        {"root": {"position": loop(0.8, [(0, [0, 2, 0]), (0.4, [0, 4, 0]), (0.8, [0, 2, 0])])},
         "screen": {"rotation": {0.0: [8, 0, 0]}}},
        {"screen": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.15, [1.2, 0.9, 1.2]), (0.5, [1, 1, 1])])}},
        {"root": {"scale": loop(0.6, [(0, [1, 1, 1]), (0.15, [0.3, 1.4, 0.3]), (0.3, [1.3, 0.5, 1.3]), (0.45, [0.5, 1.2, 0.5]), (0.6, [1, 1, 1])]),
                  "rotation": loop(0.6, [(0, [0, 0, 0]), (0.6, [0, 360, 0])])}},
        sp_len=0.6,
    ))


def bot_speedrunner() -> Mob:
    """Chronometre volant + propulseurs (plus de bot Steve)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("chrono", (0, 12, 0), "root", [
            Cube((-6, 6, -3), (12, 12, 6), "455A64", front=bot_face),
            Cube((-4, 8, -4), (8, 8, 1), "00E5FF"),
        ]),
        Bone("hand", (0, 14, -4), "chrono", [Cube((-1, 12, -4), (2, 5, 1), "212121")]),
        Bone("thruster_l", (-6, 8, 2), "chrono", [
            Cube((-8, 6, 1), (3, 6, 3), "263238"),
            Cube((-7, 4, 2), (2, 2, 2), "FF6D00"),
        ]),
        Bone("thruster_r", (6, 8, 2), "chrono", [
            Cube((5, 6, 1), (3, 6, 3), "263238"),
            Cube((6, 4, 2), (2, 2, 2), "FF6D00"),
        ]),
    ]
    return Mob("bot_speedrunner", bones, _obj_anims(
        {"hand": {"rotation": swing(2.0, 20, axis=2)}, "root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.5, [(0, [0, 1, 0]), (0.25, [0, 3, 0]), (0.5, [0, 1, 0])])},
         "chrono": {"rotation": {0.0: [12, 0, 0]}}, "thruster_l": {"rotation": swing(0.5, 8, axis=2)},
         "thruster_r": {"rotation": swing(0.5, -8, axis=2)}},
        {"root": {"position": loop(0.4, [(0, [0, 0, 0]), (0.15, [0, 0, -8]), (0.4, [0, 0, 0])])}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.3, [0, 4, -12]), (0.8, [0, 0, 0])]),
                  "scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [0.8, 1.1, 1.4]), (0.8, [1, 1, 1])])},
         "thruster_l": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.3, 1.5, 1.3]), (0.8, [1, 1, 1])])},
         "thruster_r": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.3, 1.5, 1.3]), (0.8, [1, 1, 1])])}},
        walk_len=0.5, atk_len=0.4, sp_len=0.8,
    ))


def barista() -> Mob:
    """Tasse de cafe geante + vapeur (plus de barista Steve)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("cup", (0, 6, 0), "root", [
            Cube((-6, 0, -6), (12, 12, 12), "FAFAFA", front=barista_apron),
            Cube((-5, 10, -5), (10, 4, 10), "5D4037"),
        ]),
        Bone("handle", (8, 8, 0), "cup", [Cube((6, 4, -2), (4, 8, 4), "FAFAFA")]),
        Bone("steam_a", (-2, 16, 0), "cup", [Cube((-3, 14, -1), (2, 6, 2), "ECEFF1", inflate=0.5)]),
        Bone("steam_b", (2, 17, 1), "cup", [Cube((1, 15, 0), (2, 5, 2), "ECEFF1", inflate=0.5)]),
    ]
    return Mob("barista", bones, _obj_anims(
        {"steam_a": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
         "steam_b": {"position": loop(2.0, [(0, [0, 1, 0]), (1.0, [0, 3, 0]), (2.0, [0, 1, 0])])},
         "cup": {"rotation": swing(2.0, 3, axis=2)}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 2, 0]), (0.4, [0, 0, 0]), (0.6, [0, 2, 0]), (0.7, [0, 0, 0])])}},
        {"cup": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-30, 0, 20]), (0.5, [0, 0, 0])])}},
        {"cup": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.4, [40, 0, 0]), (0.8, [40, 0, 0]), (1.0, [0, 0, 0])])},
         "steam_a": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.5, [1.4, 1.8, 1.4]), (1.0, [1, 1, 1])])},
         "steam_b": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.5, [1.5, 2.0, 1.5]), (1.0, [1, 1, 1])])}},
        walk_len=0.7,
    ))


def pirate_torrent() -> Mob:
    """Disque dur pirate + barres de download (plus de pirate Steve)."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("drive", (0, 8, 0), "root", [
            Cube((-8, 2, -4), (16, 10, 8), "37474F", front=pirate_face),
            Cube((-6, 4, -5), (4, 3, 1), "69F0AE"),
            Cube((-1, 4, -5), (6, 2, 1), "C62828"),
        ]),
        Bone("flag", (0, 16, 0), "drive", [
            Cube((-1, 12, -1), (2, 10, 2), "5D4037"),
            Cube((1, 16, -1), (8, 5, 1), "111111", front=pirate_coat),
        ]),
        Bone("bar_a", (-4, 0, 0), "drive", [Cube((-6, -1, -2), (4, 2, 4), "00E5FF")]),
        Bone("bar_b", (2, 0, 0), "drive", [Cube((0, -1, -2), (6, 2, 4), "69F0AE")]),
    ]
    return Mob("pirate_torrent", bones, _obj_anims(
        {"flag": {"rotation": swing(2.0, 12, axis=2)}, "bar_a": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1.2, 1, 1]), (2.0, [1, 1, 1])])},
         "bar_b": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [0.8, 1, 1]), (2.0, [1, 1, 1])])}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 2, 0]), (0.4, [0, 0, 0]), (0.6, [0, 2, 0]), (0.7, [0, 0, 0])])},
         "flag": {"rotation": swing(0.7, 20, axis=2)}},
        {"drive": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-25, 0, 0]), (0.5, [0, 0, 0])])}},
        {"bar_a": {"scale": loop(1.0, [(0, [0.2, 1, 1]), (0.5, [1.5, 1, 1]), (1.0, [1, 1, 1])])},
         "bar_b": {"scale": loop(1.0, [(0, [0.2, 1, 1]), (0.7, [1.4, 1, 1]), (1.0, [1, 1, 1])])},
         "flag": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 0, 30]), (1.0, [0, 0, 0])])}},
        walk_len=0.7,
    ))




# --------------------------------------------------------------------------- Lot 5 : themes sans mob custom (silhouettes objets)
def baguette_face(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 2, y + h - 3, x + w - 3, y + h - 2), fill=rgb("8D6E63"))


def baguette_vivante() -> Mob:
    """Baguette geante souriante — Boulangerie."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("loaf", (0, 8, 0), "root", [
            Cube((-12, 4, -3), (24, 6, 6), "E8C07A", front=baguette_face, pattern="noise"),
            Cube((-10, 5, -4), (6, 4, 1), "D4A574"),
            Cube((4, 5, -4), (6, 4, 1), "D4A574"),
        ]),
        Bone("crumb_a", (-8, 14, 0), "loaf", [Cube((-9, 12, -1), (2, 2, 2), "FFE0B2")]),
        Bone("crumb_b", (6, 15, 1), "loaf", [Cube((5, 13, 0), (2, 2, 2), "FFE0B2")]),
        Bone("steam", (0, 16, 0), "loaf", [Cube((-1, 14, -1), (2, 4, 2), "ECEFF1", inflate=0.4)]),
    ]
    return Mob("baguette_vivante", bones, _obj_anims(
        {"loaf": {"rotation": swing(2.0, 4, axis=2)},
         "crumb_a": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
         "crumb_b": {"position": loop(2.0, [(0, [0, 1, 0]), (1.0, [0, 3, 0]), (2.0, [0, 1, 0])])},
         "steam": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0, 0]), (0.6, [0, 3, 0]), (0.7, [0, 0, 0])])},
         "loaf": {"rotation": swing(0.7, 10, axis=2)}},
        {"loaf": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [-35, 0, 0]), (0.5, [0, 0, 0])])}},
        {"crumb_a": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.5, 1.5, 1.5]), (0.6, [0.5, 0.5, 0.5]), (1.0, [1, 1, 1])])},
         "crumb_b": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.6, 1.6, 1.6]), (0.7, [0.4, 0.4, 0.4]), (1.0, [1, 1, 1])])},
         "loaf": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.1, 0.9, 1.1]), (1.0, [1, 1, 1])])}},
        walk_len=0.7,
    ))


def dossier_front(draw, x, y, w, h):
    eyes("C62828")(draw, x, y, w, h)
    draw.rectangle((x + 2, y + int(h * 0.55), x + w - 3, y + int(h * 0.7)), fill=rgb("FFEB3B"))
    for i in range(2, w - 2, 3):
        px(draw, x + i, y + int(h * 0.62), "333333")


def dossier_fiscal() -> Mob:
    """Dossier administratif + tampon — Impots."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("folder", (0, 8, 0), "root", [
            Cube((-8, 2, -2), (16, 14, 4), "F9A825", front=dossier_front),
            Cube((-7, 12, -3), (8, 3, 1), "FFD54F"),
        ]),
        Bone("paper", (0, 10, -3), "folder", [
            Cube((-6, 4, -4), (12, 10, 1), "FAFAFA"),
        ]),
        Bone("stamp", (4, 16, 0), "folder", [
            Cube((2, 14, -2), (6, 5, 4), "C62828"),
            Cube((3, 18, -1), (4, 2, 2), "B71C1C"),
        ]),
    ]
    return Mob("dossier_fiscal", bones, _obj_anims(
        {"folder": {"rotation": swing(2.0, 3, axis=2)},
         "stamp": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 1, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 2, 0]), (0.8, [0, 0, 0])])},
         "paper": {"rotation": swing(0.8, 8, axis=0)}},
        {"stamp": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, -6, -4]), (0.5, [0, 0, 0])])}},
        {"stamp": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.3, [0, 0, 40]), (0.6, [0, 0, -20]), (0.8, [0, 0, 0])])},
         "paper": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.2, 0.8, 1]), (0.8, [1, 1, 1])])}},
        sp_len=0.8,
    ))


def alerte_front(draw, x, y, w, h):
    eyes("E91E63")(draw, x, y, w, h)
    draw.rectangle((x + 2, y + int(h * 0.55), x + w - 3, y + int(h * 0.85)), fill=rgb("AD1457"))
    draw.rectangle((x + 3, y + int(h * 0.65), x + w - 4, y + int(h * 0.75)), fill=rgb("69F0AE"))


def alerte_don() -> Mob:
    """Alerte donation / like flottant — Streamer."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("screen", (0, 10, 0), "root", [
            Cube((-8, 4, -2), (16, 12, 3), "4A148C", front=alerte_front),
            Cube((-7, 5, -3), (14, 10, 1), "1A1A2E"),
        ]),
        Bone("heart", (-6, 18, 0), "screen", [Cube((-8, 16, -1), (4, 4, 2), "E91E63")]),
        Bone("coin", (6, 18, 0), "screen", [Cube((4, 16, -1), (4, 4, 2), "FFD700")]),
        Bone("like", (0, 4, 0), "screen", [Cube((-3, 2, -2), (6, 3, 3), "00E5FF")]),
    ]
    return Mob("alerte_don", bones, _obj_anims(
        {"root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
         "heart": {"scale": loop(2.0, [(0, [1, 1, 1]), (0.5, [1.3, 1.3, 1.3]), (1.0, [1, 1, 1]), (2.0, [1, 1, 1])])},
         "coin": {"rotation": loop(2.0, [(0, [0, 0, 0]), (2.0, [0, 360, 0])])}},
        {"root": {"position": loop(0.6, [(0, [0, 1, 0]), (0.3, [0, 4, 0]), (0.6, [0, 1, 0])])},
         "like": {"scale": loop(0.6, [(0, [1, 1, 1]), (0.3, [1.2, 1.2, 1.2]), (0.6, [1, 1, 1])])}},
        {"screen": {"scale": loop(0.4, [(0, [1, 1, 1]), (0.15, [1.15, 0.9, 1.15]), (0.4, [1, 1, 1])])}},
        {"heart": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.2, [1.8, 1.8, 1.8]), (0.5, [0.5, 0.5, 0.5]), (1.0, [1, 1, 1])])},
         "coin": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.6, 1.6, 1.6]), (0.6, [0.6, 0.6, 0.6]), (1.0, [1, 1, 1])])},
         "screen": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 20, 0]), (1.0, [0, 0, 0])])}},
        walk_len=0.6, atk_len=0.4,
    ))


def eponge_front(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    for i, j in ((2, 2), (w - 4, 3), (4, h - 4), (w // 2, h // 2)):
        px(draw, x + i, y + j, "F9A825")


def eponge_savon() -> Mob:
    """Eponge geante + bulles — Proprete."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("sponge", (0, 6, 0), "root", [
            Cube((-8, 2, -6), (16, 10, 12), "FFEB3B", front=eponge_front, pattern="noise"),
        ]),
        Bone("bubble_a", (-4, 14, 0), "sponge", [Cube((-5, 12, -1), (3, 3, 3), "B3E5FC", inflate=0.5)]),
        Bone("bubble_b", (3, 16, 2), "sponge", [Cube((2, 14, 1), (4, 4, 4), "E1F5FE", inflate=0.5)]),
        Bone("bubble_c", (0, 18, -2), "sponge", [Cube((-1, 17, -3), (2, 2, 2), "B3E5FC", inflate=0.3)]),
    ]
    return Mob("eponge_savon", bones, _obj_anims(
        {"sponge": {"scale": loop(2.0, [(0, [1, 1, 1]), (1.0, [1.05, 0.95, 1.05]), (2.0, [1, 1, 1])])},
         "bubble_a": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
         "bubble_b": {"position": loop(2.0, [(0, [0, 1, 0]), (1.0, [0, 4, 0]), (2.0, [0, 1, 0])])},
         "bubble_c": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 3, 0]), (0.4, [0, 0, 0]), (0.6, [0, 3, 0]), (0.7, [0, 0, 0])])}},
        {"sponge": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.2, 0.7, 1.2]), (0.5, [1, 1, 1])])}},
        {"bubble_a": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.5, 1.5, 1.5]), (0.6, [0.2, 0.2, 0.2]), (1.0, [1, 1, 1])])},
         "bubble_b": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.6, 1.6, 1.6]), (0.7, [0.2, 0.2, 0.2]), (1.0, [1, 1, 1])])},
         "bubble_c": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.5, [1.8, 1.8, 1.8]), (0.8, [0.1, 0.1, 0.1]), (1.0, [1, 1, 1])])}},
        walk_len=0.7,
    ))


def casque_front(draw, x, y, w, h):
    draw.rectangle((x + 2, y + int(h * 0.35), x + w - 3, y + int(h * 0.75)), fill=rgb("4FC3F7"))
    eyes("FFFFFF", white=False, y_frac=0.5)(draw, x, y, w, h)


def casque_orbital() -> Mob:
    """Casque astronaute flottant + planete — Gravite Zero."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("helmet", (0, 12, 0), "root", [
            Cube((-7, 6, -7), (14, 14, 14), "ECEFF1", front=casque_front),
            Cube((-5, 8, -8), (10, 8, 1), "29B6F6"),
        ]),
        Bone("planet", (10, 8, 0), "helmet", [
            Cube((8, 6, -2), (5, 5, 5), "7E57C2"),
            Cube((9, 7, -3), (3, 2, 1), "B39DDB"),
        ]),
        Bone("ring", (0, 10, 0), "helmet", [
            Cube((-9, 11, -1), (18, 1, 2), "90CAF9", inflate=0.2),
        ]),
    ]
    return Mob("casque_orbital", bones, _obj_anims(
        {"root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 4, 0]), (2.0, [0, 0, 0])])},
         "planet": {"rotation": loop(2.0, [(0, [0, 0, 0]), (2.0, [0, 360, 0])])},
         "ring": {"rotation": swing(2.0, 8, axis=2)}},
        {"root": {"position": loop(0.8, [(0, [0, 2, 0]), (0.4, [0, 5, 0]), (0.8, [0, 2, 0])])},
         "helmet": {"rotation": {0.0: [8, 0, 0]}}},
        {"helmet": {"position": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -6]), (0.5, [0, 0, 0])])}},
        {"root": {"rotation": loop(1.2, [(0, [0, 0, 0]), (1.2, [0, 360, 0])])},
         "planet": {"scale": loop(1.2, [(0, [1, 1, 1]), (0.4, [1.4, 1.4, 1.4]), (1.2, [1, 1, 1])])}},
        walk_len=0.8, sp_len=1.2,
    ))


def clap_front(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    for row in range(0, h, 3):
        c = "FAFAFA" if (row // 3) % 2 == 0 else "212121"
        draw.rectangle((x, y + row, x + w - 1, y + min(row + 2, h - 1)), fill=rgb(c))


def clap_cinema() -> Mob:
    """Clap de cinema vivant — Film Action."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("board", (0, 8, 0), "root", [
            Cube((-9, 2, -2), (18, 12, 4), "212121", front=clap_front),
            Cube((-8, 3, -3), (16, 5, 1), "FAFAFA"),
        ]),
        Bone("stick", (0, 16, 0), "board", [
            Cube((-9, 14, -2), (18, 4, 4), "FAFAFA", pattern="stripes"),
        ]),
        Bone("hinge", (0, 14, 0), "board", [Cube((-1, 13, -1), (2, 2, 2), "FFD700")]),
    ]
    return Mob("clap_cinema", bones, _obj_anims(
        {"stick": {"rotation": swing(2.0, 6, axis=0)},
         "board": {"rotation": swing(2.0, 2, axis=2)}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 2, 0]), (0.4, [0, 0, 0]), (0.6, [0, 2, 0]), (0.7, [0, 0, 0])])}},
        {"stick": {"rotation": loop(0.4, [(0, [0, 0, 0]), (0.15, [-50, 0, 0]), (0.3, [0, 0, 0]), (0.4, [0, 0, 0])])}},
        {"stick": {"rotation": loop(0.8, [
            (0, [0, 0, 0]), (0.15, [-60, 0, 0]), (0.3, [0, 0, 0]),
            (0.45, [-60, 0, 0]), (0.6, [0, 0, 0]), (0.8, [0, 0, 0]),
         ])},
         "board": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.1, 0.9, 1.1]), (0.8, [1, 1, 1])])}},
        walk_len=0.7, atk_len=0.4, sp_len=0.8,
    ))




# --------------------------------------------------------------------------- Lot 6 : derniers themes sans mob
def pyramide_front(draw, x, y, w, h):
    eyes("C62828", white=False)(draw, x, y, w, h)
    # oeil central
    cx, cy = x + w // 2, y + h // 2
    draw.ellipse((cx - 3, cy - 2, cx + 3, cy + 2), fill=rgb("FAFAFA"))
    px(draw, cx, cy, "C62828")


def pyramide_oeil() -> Mob:
    """Pyramide + oeil flottant — Complot."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("pyramid", (0, 4, 0), "root", [
            Cube((-8, 0, -8), (16, 4, 16), "C0C0C0"),
            Cube((-6, 4, -6), (12, 4, 12), "B0BEC5"),
            Cube((-4, 8, -4), (8, 4, 8), "90A4AE"),
            Cube((-2, 12, -2), (4, 4, 4), "78909C", front=pyramide_front),
        ]),
        Bone("eye", (0, 18, 0), "pyramid", [
            Cube((-3, 16, -2), (6, 4, 4), "FAFAFA"),
            Cube((-1, 17, -3), (2, 2, 1), "C62828"),
        ]),
        Bone("foil", (6, 10, 0), "pyramid", [Cube((4, 8, -2), (5, 5, 4), "ECEFF1", inflate=0.3)]),
    ]
    return Mob("pyramide_oeil", bones, _obj_anims(
        {"eye": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 2, 0]), (2.0, [0, 0, 0])])},
         "foil": {"rotation": swing(2.0, 10, axis=2)},
         "pyramid": {"rotation": swing(2.0, 2, axis=1)}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 2, 0]), (0.8, [0, 0, 0])])}},
        {"eye": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.4, 1.4, 1.4]), (0.5, [1, 1, 1])])}},
        {"pyramid": {"rotation": loop(1.0, [(0, [0, 0, 0]), (1.0, [0, 360, 0])])},
         "eye": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.4, [1.6, 1.6, 1.6]), (1.0, [1, 1, 1])])}},
    ))


def cle_front(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 2, y + int(h * 0.6), x + w - 3, y + int(h * 0.75)), fill=rgb("FFD700"))


def cle_tuyau() -> Mob:
    """Cle a molette + tuyau — Plomberie."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("wrench", (0, 8, 0), "root", [
            Cube((-10, 6, -2), (20, 4, 4), "78909C", front=cle_front),
            Cube((-12, 4, -3), (6, 8, 6), "607D8B"),
            Cube((6, 5, -2), (5, 6, 4), "546E7A"),
        ]),
        Bone("pipe", (0, 2, 0), "wrench", [
            Cube((-2, 0, -2), (4, 6, 4), "4CAF50"),
            Cube((-2, -2, -2), (4, 2, 4), "388E3C"),
        ]),
        Bone("drip", (0, 0, 0), "pipe", [Cube((-1, -4, -1), (2, 3, 2), "81D4FA")]),
    ]
    return Mob("cle_tuyau", bones, _obj_anims(
        {"drip": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, -3, 0]), (2.0, [0, 0, 0])])},
         "wrench": {"rotation": swing(2.0, 4, axis=2)}},
        {"root": {"position": loop(0.7, [(0, [0, 0, 0]), (0.2, [0, 2, 0]), (0.4, [0, 0, 0]), (0.6, [0, 2, 0]), (0.7, [0, 0, 0])])}},
        {"wrench": {"rotation": loop(0.5, [(0, [0, 0, 0]), (0.2, [0, 0, -40]), (0.5, [0, 0, 0])])}},
        {"pipe": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.2, 1.5, 1.2]), (0.8, [1, 1, 1])])},
         "drip": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.4, [1.5, 2.0, 1.5]), (0.8, [1, 1, 1])])}},
        walk_len=0.7, sp_len=0.8,
    ))


def biscuit_front(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    draw.rectangle((x + 2, y + int(h * 0.7), x + w - 3, y + int(h * 0.8)), fill=rgb("5D4037"))


def biscuit_fortune() -> Mob:
    """Biscuit fortune + papier — Fortune Cookie."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("cookie", (0, 6, 0), "root", [
            Cube((-6, 2, -4), (12, 8, 8), "E0A060", front=biscuit_front, pattern="noise"),
            Cube((-5, 3, -5), (10, 6, 1), "D4934A"),
        ]),
        Bone("paper", (0, 10, -5), "cookie", [
            Cube((-5, 8, -6), (10, 3, 1), "FFFDE7"),
        ]),
        Bone("crumb", (5, 4, 2), "cookie", [Cube((4, 3, 1), (2, 2, 2), "FFCC80")]),
    ]
    return Mob("biscuit_fortune", bones, _obj_anims(
        {"paper": {"rotation": swing(2.0, 8, axis=0)},
         "cookie": {"rotation": swing(2.0, 3, axis=2)}},
        {"root": {"position": loop(0.6, [(0, [0, 0, 0]), (0.15, [0, 3, 0]), (0.3, [0, 0, 0]), (0.45, [0, 3, 0]), (0.6, [0, 0, 0])])}},
        {"cookie": {"scale": loop(0.4, [(0, [1, 1, 1]), (0.15, [1.15, 0.85, 1.15]), (0.4, [1, 1, 1])])}},
        {"paper": {"position": loop(1.0, [(0, [0, 0, 0]), (0.4, [0, 0, -6]), (0.7, [0, 0, -6]), (1.0, [0, 0, 0])])},
         "crumb": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.5, 1.5, 1.5]), (0.6, [0.3, 0.3, 0.3]), (1.0, [1, 1, 1])])}},
        walk_len=0.6, atk_len=0.4,
    ))


def tente_front(draw, x, y, w, h):
    eyes()(draw, x, y, w, h)
    # entree tente
    draw.rectangle((x + w // 2 - 2, y + int(h * 0.4), x + w // 2 + 1, y + h - 1), fill=rgb("3E2723"))


def tente_camp() -> Mob:
    """Tente + feu de camp — Camping."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("tent", (0, 4, 0), "root", [
            Cube((-10, 0, -8), (20, 2, 16), "5D4037"),
            Cube((-8, 2, -6), (16, 10, 12), "2E7D32", front=tente_front),
            Cube((-4, 12, -4), (8, 4, 8), "1B5E20"),
        ]),
        Bone("pole", (0, 14, 0), "tent", [Cube((-1, 10, -1), (2, 8, 2), "8D6E63")]),
        Bone("fire", (0, 2, 10), "root", [
            Cube((-2, 0, 8), (4, 5, 4), "FF6D00"),
            Cube((-1, 4, 9), (2, 3, 2), "FFEB3B"),
        ]),
    ]
    return Mob("tente_camp", bones, _obj_anims(
        {"fire": {"scale": loop(1.5, [(0, [1, 1, 1]), (0.4, [1.1, 1.3, 1.1]), (0.8, [0.9, 0.8, 0.9]), (1.5, [1, 1, 1])])},
         "tent": {"rotation": swing(2.0, 2, axis=2)}},
        {"root": {"position": loop(0.8, [(0, [0, 0, 0]), (0.4, [0, 1, 0]), (0.8, [0, 0, 0])])}},
        {"tent": {"scale": loop(0.5, [(0, [1, 1, 1]), (0.2, [1.1, 0.9, 1.1]), (0.5, [1, 1, 1])])}},
        {"fire": {"scale": loop(1.0, [(0, [1, 1, 1]), (0.3, [1.5, 2.0, 1.5]), (0.6, [0.8, 0.6, 0.8]), (1.0, [1, 1, 1])])},
         "pole": {"rotation": loop(1.0, [(0, [0, 0, 0]), (0.5, [0, 0, 15]), (1.0, [0, 0, 0])])}},
    ))


def meme_front(draw, x, y, w, h):
    eyes("69F0AE")(draw, x, y, w, h)
    # sourire pepe-ish
    draw.rectangle((x + 2, y + int(h * 0.7), x + w - 3, y + int(h * 0.8)), fill=rgb("212121"))
    px(draw, x + 2, y + int(h * 0.65), "212121")
    px(draw, x + w - 3, y + int(h * 0.65), "212121")


def cube_meme() -> Mob:
    """Cube meme arc-en-ciel — Invasion Memes."""
    bones = [
        Bone("root", (0, 0, 0)),
        Bone("cube", (0, 8, 0), "root", [
            Cube((-7, 2, -7), (14, 14, 14), "7CB342", front=meme_front, pattern="checker"),
        ]),
        Bone("nyan_a", (-10, 10, 0), "cube", [Cube((-12, 8, -1), (4, 3, 2), "E91E63")]),
        Bone("nyan_b", (-10, 12, 0), "cube", [Cube((-12, 11, -1), (4, 2, 2), "FFEB3B")]),
        Bone("nyan_c", (-10, 14, 0), "cube", [Cube((-12, 13, -1), (4, 2, 2), "42A5F5")]),
        Bone("glasses", (0, 14, -8), "cube", [Cube((-5, 12, -8), (10, 3, 1), "212121")]),
    ]
    return Mob("cube_meme", bones, _obj_anims(
        {"root": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [0, 3, 0]), (2.0, [0, 0, 0])])},
         "cube": {"rotation": loop(2.0, [(0, [0, 0, 0]), (2.0, [0, 360, 0])])},
         "nyan_a": {"position": loop(2.0, [(0, [0, 0, 0]), (1.0, [-2, 0, 0]), (2.0, [0, 0, 0])])}},
        {"root": {"position": loop(0.5, [(0, [0, 1, 0]), (0.25, [0, 4, 0]), (0.5, [0, 1, 0])])}},
        {"cube": {"scale": loop(0.4, [(0, [1, 1, 1]), (0.15, [1.2, 0.8, 1.2]), (0.4, [1, 1, 1])])}},
        {"cube": {"rotation": loop(0.8, [(0, [0, 0, 0]), (0.8, [0, 720, 0])])},
         "nyan_a": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.3, [1.5, 1.5, 1.5]), (0.8, [1, 1, 1])])},
         "nyan_b": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.4, [1.5, 1.5, 1.5]), (0.8, [1, 1, 1])])},
         "nyan_c": {"scale": loop(0.8, [(0, [1, 1, 1]), (0.5, [1.5, 1.5, 1.5]), (0.8, [1, 1, 1])])}},
        walk_len=0.5, atk_len=0.4, sp_len=0.8,
    ))


MOBS = [chef, dracula, hamburger, cat, rooster, screen,
        toi_du_futur, douanier, lapin_extirpateur, grossiste_clandestin, statue_vivante, mimique_d20,
        bodybuilder, danseur_disco, hacker_fantome, bot_speedrunner, barista, pirate_torrent,
        manifestant, crypto_bulle, nuage_orage, slider_multivers, pop_it_geant, reveil_cauchemar,
        baguette_vivante, dossier_fiscal, alerte_don, eponge_savon, casque_orbital, clap_cinema,
        pyramide_oeil, cle_tuyau, biscuit_fortune, tente_camp, cube_meme]


# --------------------------------------------------------------------------- export
def fmt_time(t: float) -> str:
    return f"{t:.1f}" if float(t).is_integer() else f"{t:g}"


def geo_json(mob: Mob) -> dict:
    bones = []
    for bone in mob.bones:
        out: dict = {"name": bone.name, "pivot": list(bone.pivot)}
        if bone.parent:
            out["parent"] = bone.parent
        if bone.rotation:
            out["rotation"] = list(bone.rotation)
        if bone.cubes:
            out["cubes"] = []
            for c in bone.cubes:
                cube = {"origin": list(c.origin), "size": list(c.size), "uv": list(c.uv)}
                if c.inflate:
                    cube["inflate"] = c.inflate
                out["cubes"].append(cube)
        bones.append(out)
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": f"geometry.unpredictableblocks.{mob.id}",
                "texture_width": TEX, "texture_height": TEX,
                "visible_bounds_width": 3, "visible_bounds_height": 3.5, "visible_bounds_offset": [0, 1.25, 0],
            },
            "bones": bones,
        }],
    }


def anim_json(mob: Mob) -> dict:
    out = {}
    for name, (length, looping, bones) in mob.animations.items():
        anim_bones = {}
        for bone, channels in bones.items():
            anim_bones[bone] = {
                ch: (frames if isinstance(frames, list) else {fmt_time(t): v for t, v in sorted(frames.items())})
                for ch, frames in channels.items()
            }
        out[f"animation.{mob.id}.{name}"] = {
            # Anims ponctuelles : jouees une fois puis rendues (« hold » laisserait p. ex. Dracula invisible)
            "loop": bool(looping),
            "animation_length": length,
            "bones": anim_bones,
        }
    return {"format_version": "1.8.0", "animations": out}


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for factory in MOBS:
        mob = factory()
        pack(mob)
        img = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
        rnd = random.Random(mob.id)
        for bone in mob.bones:
            for cube in bone.cubes:
                paint_cube(img, cube, rnd)
        (ASSETS / "textures/entity").mkdir(parents=True, exist_ok=True)
        img.save(ASSETS / f"textures/entity/{mob.id}.png")
        write_json(ASSETS / f"geckolib/models/entity/{mob.id}.geo.json", geo_json(mob))
        write_json(ASSETS / f"geckolib/animations/entity/{mob.id}.animation.json", anim_json(mob))
        print("OK", mob.id, "-", ", ".join(mob.animations))


if __name__ == "__main__":
    main()
