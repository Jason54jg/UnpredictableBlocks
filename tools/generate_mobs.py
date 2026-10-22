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


MOBS = [chef, dracula, hamburger, cat, rooster, screen]


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
