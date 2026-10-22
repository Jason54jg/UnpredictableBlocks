#!/usr/bin/env python3
"""Regenere les assets GeckoLib + bbmodels Blockbench depuis UNE seule definition.

- Bloc pose  : geckolib/models/block/lucky_block.geo.json (cube seul, idle statique)
- Fusee      : geckolib/models/entity/lucky_rocket.geo.json + animations/entity/lucky_rocket.animation.json
               (cube + flamme + 3 bouffees de fumee, anim « launch » jouee au cassage)
- Textures   : textures/entity/lucky_rocket/lucky_<id>.png = texture du bloc (32x32) + bande fumee/flamme (32x16)
- Blockbench : docs/blockbench/lucky_<id>.bbmodel (meme hierarchie, memes keyframes que le jeu)

Bones et keyframes sont ecrits en convention GeckoLib/Bedrock, puis convertis pour Blockbench
(axe X miroir : position [-x, y, z], rotation [-x, -y, z]) — Blockbench et le jeu restent alignes.
"""
from __future__ import annotations

import json
import random
import re
from pathlib import Path

from PIL import Image

import sys
sys.path.insert(0, str(Path(__file__).resolve().parent))
from generate_blockbench import (  # noqa: E402
    THEMES,
    SIZE,
    OUT,
    new_frame,
    lucky_frame,
    shade_bottom,
    stack_frames,
    rgb,
    SKIP_FRAME,
    write_mcmeta,
    png_b64,
    uid,
    kf,
)

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets/unpredictableblocks"
GEO_DIR = ASSETS / "geckolib/models"
ANIM_DIR = ASSETS / "geckolib/animations"
BLOCK_TEX_DIR = ASSETS / "textures/block"
ROCKET_TEX_DIR = ASSETS / "textures/entity/lucky_rocket"

TEX_W, TEX_H = 32, 48          # texture fusee : bloc 32x32 + bande 32x16
LAUNCH_LENGTH = 2.0            # = LuckyRocketEntity.LIFETIME (40 ticks)
WINDUP = 0.5                   # = LuckyRocketEntity.WINDUP_TICKS (10 ticks)

# --------------------------------------------------------------------------- geometrie
# UV par face : (u, v, largeur, hauteur) dans la texture fusee 32x48
UV_BLOCK = (0, 0, 32, 32)
UV_SMOKE = (0, 32, 16, 16)
UV_FLAME = (16, 32, 16, 16)

# (name, parent, pivot, [(origin, size, uv)]) — coordonnees GeckoLib (x/z centres, y=0 au sol)
BLOCK_BONES = [
    ("root", None, [0, 8, 0], []),
    ("block", "root", [0, 8, 0], [([-8, 0, -8], [16, 16, 16], UV_BLOCK)]),
]
ROCKET_BONES = BLOCK_BONES + [
    ("exhaust", "root", [0, 0, 0], []),
    ("flame", "exhaust", [0, 0, 0], [([-3, -6, -3], [6, 6, 6], UV_FLAME)]),
    ("smoke1", "exhaust", [0, -4, 0], [([-2, -6, -2], [4, 4, 4], UV_SMOKE)]),
    ("smoke2", "exhaust", [3, -3, 2], [([1, -5, 0], [4, 4, 4], UV_SMOKE)]),
    ("smoke3", "exhaust", [-3, -3, -2], [([-5, -5, -4], [4, 4, 4], UV_SMOKE)]),
]


def geo_json(identifier: str, bones: list, tex_h: int) -> dict:
    out = []
    for name, parent, pivot, cubes in bones:
        bone = {"name": name, "pivot": pivot}
        if parent:
            bone["parent"] = parent
        if cubes:
            bone["cubes"] = [
                {
                    "origin": origin,
                    "size": size,
                    "uv": {
                        side: {"uv": [uv[0], uv[1]], "uv_size": [uv[2], uv[3]]}
                        for side in ("north", "east", "south", "west", "up", "down")
                    },
                }
                for origin, size, uv in cubes
            ]
        out.append(bone)
    return {
        "format_version": "1.12.0",
        "minecraft:geometry": [
            {
                "description": {
                    "identifier": f"geometry.unpredictableblocks.{identifier}",
                    "texture_width": TEX_W,
                    "texture_height": tex_h,
                    "visible_bounds_width": 3,
                    "visible_bounds_height": 3,
                    "visible_bounds_offset": [0, 0.5, 0],
                },
                "bones": out,
            }
        ],
    }


# --------------------------------------------------------------------------- animation
STYLE_JAVA = ROOT / "common/src/main/java/com/jason54/unpredictableblocks/entity/RocketStyle.java"
STYLES = ["spin", "hop", "zigzag", "spiral", "tumble", "glitch", "float"]
# Animations de cassage autres que la fusee (BreakAnimation.java) : nom -> duree (= lifetime / 20)
BREAK_KINDS = {"pop": 1.1, "sink": 1.6, "vanish": 1.2, "bounce": 2.0}

Frames = list[tuple[float, list[float]]]


def style_by_theme() -> dict[str, str]:
    """lucky_<id> -> style, lu dans RocketStyle.java (meme table que le jeu)."""
    src = STYLE_JAVA.read_text(encoding="utf-8")
    pairs = re.findall(r"entry\(LuckyTheme\.([A-Z0-9_]+), ([A-Z]+),", src)
    return {f"lucky_{tid.lower()}": style.lower() for tid, style in pairs}


def steps(times_values: Frames) -> Frames:
    """Valeurs « en escalier » (pas d'interpolation visible) : duplique chaque cle juste avant la suivante."""
    out: Frames = []
    for i, (tm, v) in enumerate(times_values):
        out.append((tm, v))
        if i + 1 < len(times_values):
            out.append((round(times_values[i + 1][0] - 0.01, 2), v))
    return out


def shake() -> tuple[Frames, Frames]:
    rot: Frames = [(0.0, [0, 0, 0])]
    for i, (rx, rz) in enumerate([(3, -4), (-3, 4), (4, -3), (-4, 4), (3, -4), (-2, 2)]):
        rot.append((round(0.07 * (i + 1), 2), [rx, 0, rz]))
    rot.append((WINDUP, [0, 0, 0]))
    pos: Frames = [(0.0, [0, 0, 0]), (0.14, [0.3, 0.2, 0]), (0.28, [-0.3, 0, 0.3]), (0.42, [0.2, 0.3, -0.2]), (WINDUP, [0, 0, 0])]
    return rot, pos


def squash() -> Frames:
    return [(0.0, [1, 1, 1]), (0.4, [1.12, 0.82, 1.12]), (WINDUP, [0.88, 1.2, 0.88]), (0.75, [1, 1, 1]), (LAUNCH_LENGTH, [1, 1, 1])]


def flame(style: str) -> Frames:
    if style == "float":
        return [(0.0, [0, 0, 0]), (LAUNCH_LENGTH, [0, 0, 0])]
    frames: Frames = [(0.0, [0, 0, 0]), (0.45, [0, 0, 0]), (WINDUP, [1, 1.4, 1])]
    t, big = WINDUP, True
    while t < LAUNCH_LENGTH - 1e-6:
        t = round(t + 0.1, 2)
        if style == "glitch":  # flamme qui saute on/off
            frames += [(round(t - 0.01, 2), frames[-1][1]), (t, [1.2, 1.8, 1.2] if big else [0, 0, 0])]
        else:
            frames.append((t, [1.1, 1.7, 1.1] if big else [0.85, 1.1, 0.85]))
        big = not big
    return frames


def puffs(bones: dict, style: str) -> None:
    """Bouffees : apparait sous le bloc, grossit en tombant, disparait, reboucle."""
    period = 1.0 if style == "float" else 0.6
    fall = -6 if style == "float" else -18
    for name, start, drift in (("smoke1", 0.35, (0, 0)), ("smoke2", 0.55, (2, 1)), ("smoke3", 0.75, (-2, -1))):
        pos: Frames = [(0.0, [0, 0, 0])]
        scl: Frames = [(0.0, [0, 0, 0]), (round(start - 0.01, 2), [0, 0, 0])]
        t0 = start
        while t0 < LAUNCH_LENGTH - 1e-6:
            mid, end = round(t0 + period / 2, 2), round(t0 + period - 0.02, 2)
            pos += [(round(t0, 2), [0, 0, 0]), (mid, [drift[0], fall / 2, drift[1]]), (end, [drift[0] * 2, fall, drift[1] * 2])]
            scl += [(round(t0, 2), [0.4, 0.4, 0.4]), (mid, [1.6, 1.6, 1.6]), (end, [0, 0, 0])]
            t0 = round(t0 + period, 2)
        bones[name] = {
            "position": [p for p in pos if p[0] <= LAUNCH_LENGTH],
            "scale": [s for s in scl if s[0] <= LAUNCH_LENGTH],
        }


def launch_keyframes(style: str = "spin") -> dict[str, dict[str, Frames]]:
    """bone -> channel -> [(temps, [x, y, z])] en convention GeckoLib, pour un style de decollage.

    0.0–0.5 s : preparation au sol (tremblement, sauts, levitation...).
    0.5 s     : l'entite commence a bouger cote Java (trajectoire dans RocketStyle.offset).
    0.5–2.0 s : rotation propre au style, flamme, bouffees de fumee.
    """
    rot, pos = shake()
    block_scale = squash()
    if style == "spin":
        rot = rot + [(LAUNCH_LENGTH, [0, 1080, 0])]
    elif style == "hop":
        pos = [(0.0, [0, 0, 0]), (0.08, [0, 4, 0]), (0.16, [0, 0, 0]), (0.25, [0, 5, 0]),
               (0.33, [0, 0, 0]), (0.42, [0, 6, 0]), (WINDUP, [0, 0, 0])]
        rot = [(0.0, [0, 0, 0]), (WINDUP, [0, 0, 0]), (LAUNCH_LENGTH, [-720, 0, 0])]
        block_scale = [(0.0, [1, 1, 1]), (0.16, [1.15, 0.8, 1.15]), (0.2, [1, 1, 1]), (0.33, [1.15, 0.8, 1.15]),
                       (0.37, [1, 1, 1]), (WINDUP, [0.85, 1.25, 0.85]), (0.75, [1, 1, 1]), (LAUNCH_LENGTH, [1, 1, 1])]
    elif style == "zigzag":
        rot = [(0.0, [0, 0, 0])] + [(round(0.1 * i, 2), [0, 0, 6 if i % 2 else -6]) for i in range(1, 5)]
        rot.append((WINDUP, [0, 0, 0]))
        t, sign = WINDUP, 1
        while t < LAUNCH_LENGTH - 1e-6:
            t = round(t + 0.25, 2)
            rot.append((t, [0, 15 * sign, 25 * sign]))
            sign = -sign
    elif style == "spiral":
        rot = [(0.0, [0, 0, 0]), (WINDUP, [0, 90, 0]), (0.65, [0, 150, 20]), (LAUNCH_LENGTH, [0, 810, 20])]
    elif style == "tumble":
        rot = rot + [(1.0, [200, 120, 90]), (LAUNCH_LENGTH, [540, 360, 270])]
    elif style == "glitch":
        snaps = [[1, 1, 1], [1.3, 0.7, 1], [0.8, 1.25, 1.1], [1, 1, 1], [1.2, 1.2, 0.7], [0.75, 1, 1.3]]
        block_scale = steps([(round(0.1 * i, 2), snaps[i % len(snaps)]) for i in range(int(LAUNCH_LENGTH * 10) + 1)])
        rot = steps([(0.0, [0, 0, 0]), (0.6, [0, 90, 0]), (0.9, [0, 90, 90]), (1.2, [90, 180, 90]),
                     (1.5, [90, 270, 0]), (1.8, [0, 360, 0]), (LAUNCH_LENGTH, [0, 360, 0])])
        pos = steps([(0.0, [0, 0, 0]), (0.15, [1, 0, 0]), (0.25, [0, 0, -1]), (0.35, [-1, 1, 0]), (WINDUP, [0, 0, 0])])
    elif style == "float":
        rot = [(0.0, [0, 0, 0]), (0.5, [5, 30, -8]), (1.0, [-5, 90, 8]), (1.5, [5, 150, -8]), (LAUNCH_LENGTH, [0, 180, 0])]
        pos = [(0.0, [0, 0, 0]), (WINDUP, [0, 1, 0])]
        block_scale = [(0.0, [1, 1, 1]), (0.5, [1.05, 0.95, 1.05]), (1.0, [0.95, 1.05, 0.95]),
                       (1.5, [1.05, 0.95, 1.05]), (LAUNCH_LENGTH, [1, 1, 1])]
    else:
        raise ValueError(f"style inconnu : {style}")

    bones: dict[str, dict] = {
        "root": {"rotation": rot, "position": pos},
        "block": {"scale": block_scale},
        "flame": {"scale": flame(style)},
    }
    puffs(bones, style)
    return bones


def break_keyframes(kind: str) -> dict[str, dict[str, Frames]]:
    """Animations sans fusee : flamme et fumee cachees, seul le bloc bouge (trajet gere en Java)."""
    length = BREAK_KINDS[kind]
    hidden = {"scale": [(0.0, [0, 0, 0]), (length, [0, 0, 0])]}
    if kind == "pop":
        rot = [(round(0.1 * i, 2), [0, 0, (2 + i) * (1 if i % 2 else -1)]) for i in range(11)]
        scale = [(0.0, [1, 1, 1]), (0.3, [1.15, 1.15, 1.15]), (0.5, [1.1, 1.3, 1.1]), (0.7, [1.4, 1.3, 1.4]),
                 (0.9, [1.55, 1.6, 1.55]), (1.05, [1.8, 1.8, 1.8]), (1.1, [0, 0, 0])]
    elif kind == "sink":
        rot = [(round(0.1 * i, 2), [3 if i % 2 else -3, 0, -3 if i % 2 else 3]) for i in range(17)]
        scale = [(0.0, [1, 1, 1]), (0.2, [1.08, 0.92, 1.08]), (length, [0.95, 0.95, 0.95])]
    elif kind == "vanish":
        rot = [(0.0, [0, 0, 0]), (0.4, [0, 180, 0]), (0.8, [0, 720, 0]), (length, [0, 1440, 0])]
        scale = [(0.0, [1, 1, 1]), (0.3, [1.1, 1.1, 1.1]), (0.8, [0.6, 0.6, 0.6]), (length, [0, 0, 0])]
    elif kind == "bounce":
        hop = 0.65
        rot = [(0.0, [0, 0, 0]), (length, [0, 540, 0])]
        scale = [(0.0, [1, 1, 1])]
        for i in range(1, 4):
            land = round(hop * i, 2)
            scale += [(round(land - hop / 2, 2), [0.85, 1.2, 0.85]), (land, [1.25, 0.75, 1.25])]
        scale += [(round(length - 0.15, 2), [1, 1, 1]), (length, [0, 0, 0])]
    else:
        raise ValueError(kind)
    bones: dict[str, dict] = {"root": {"rotation": rot}, "block": {"scale": [s for s in scale if s[0] <= length]}}
    for name in ("flame", "smoke1", "smoke2", "smoke3"):
        bones[name] = hidden
    return bones


def dedupe(frames: Frames) -> Frames:
    """Une seule keyframe par instant (la derniere gagne) — evite les doublons qui cassent Blockbench."""
    by_time: dict[float, list[float]] = {}
    for tm, v in frames:
        by_time[round(tm, 2)] = v
    return sorted(by_time.items())


def anim_json(animations: dict[str, tuple[float, object, dict]]) -> dict:
    """{nom: (duree, loop, bones)} -> fichier .animation.json GeckoLib."""
    return {
        "format_version": "1.8.0",
        "animations": {
            name: {
                "loop": loop,
                "animation_length": length,
                "bones": {
                    bone: {
                        # Format Bedrock standard "t": [x, y, z] — Blockbench ignore {"vector": ...} (valeurs a 0)
                        channel: {f"{tm:g}" if tm % 1 else f"{tm:.1f}": v for tm, v in dedupe(frames)}
                        for channel, frames in channels.items()
                    }
                    for bone, channels in bones.items()
                },
            }
            for name, (length, loop, bones) in animations.items()
        },
    }


# --------------------------------------------------------------------------- textures
def exhaust_strip() -> Image.Image:
    """32x16 : fumee grise (gauche) + flamme (droite). Deterministe."""
    rnd = random.Random(54)
    strip = Image.new("RGBA", (32, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5 / 10.6
            g = int(200 - 70 * d + rnd.randint(-14, 14))
            strip.putpixel((x, y), (g, g, g + 4, 255))
            # flamme : coeur jaune pale -> orange -> rouge en bas
            f = y / 15
            r = 255
            gg = int(235 - 160 * f + rnd.randint(-12, 12))
            b = int(120 - 110 * f) if f < 0.6 else 10
            strip.putpixel((16 + x, y), (r, max(40, gg), max(0, b), 255))
    return strip


def rocket_texture(block_tex: Image.Image, strip: Image.Image) -> Image.Image:
    img = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    img.paste(block_tex.convert("RGBA").crop((0, 0, 32, 32)), (0, 0))
    img.paste(strip, (0, 32))
    return img


# --------------------------------------------------------------------------- Blockbench
def bb_faces(uv) -> dict:
    u, v, w, h = uv
    return {s: {"uv": [u, v, u + w, v + h], "texture": 0} for s in ("north", "east", "south", "west", "up", "down")}


def bb_vec(channel: str, v: list[float]) -> tuple[float, float, float]:
    x, y, z = v
    if channel == "position":
        return -x, y, z
    if channel == "rotation":
        return -x, -y, z
    return x, y, z


def build_bbmodel(theme, texture: Image.Image, style: str) -> dict:
    elements, groups = [], {}
    nodes: dict[str, dict] = {}
    for name, parent, pivot, cubes in ROCKET_BONES:
        bb_pivot = [8 - pivot[0], pivot[1], 8 + pivot[2]]
        node = {"name": name, "origin": bb_pivot, "uuid": uid(), "export": True, "children": []}
        groups[name] = node["uuid"]
        for i, (origin, size, uv) in enumerate(cubes):
            frm = [8 - (origin[0] + size[0]), origin[1], 8 + origin[2]]
            to = [8 - origin[0], origin[1] + size[1], 8 + origin[2] + size[2]]
            el = {
                "name": f"{name}_cube{i}" if name != "block" else "cube",
                "box_uv": False,
                "from": frm,
                "to": to,
                "origin": bb_pivot,
                "color": len(elements) % 8,
                "faces": bb_faces(uv),
                "uuid": uid(),
            }
            elements.append(el)
            node["children"].append(el["uuid"])
        nodes[name] = node
        if parent:
            nodes[parent]["children"].append(node)

    def bb_animation(name: str, length: float, bones: dict, selected: bool) -> dict:
        animators = {}
        for bone, channels in bones.items():
            kfs = []
            for channel, frames in channels.items():
                for tm, v in dedupe(frames):
                    kfs.append(kf(channel, tm, *bb_vec(channel, v), interp="linear"))
            animators[groups[bone]] = {"name": bone, "type": "bone", "keyframes": kfs}
        return {
            "uuid": uid(),
            "name": name,
            # Boucle pour l'apercu ; in-game = hold_on_last_frame (l'entite disparait a la fin)
            "loop": "loop",
            "override": False,
            "length": length,
            "snapping": 100,
            "selected": selected,
            "saved_name": name,
            "anim_time_update": "",
            "blend_weight": "",
            "start_delay": "",
            "loop_delay": "",
            "animators": animators,
        }

    return {
        "meta": {
            "format_version": "4.10",
            "model_format": "free",
            "box_uv": False,
            "unpredictableblocks_note": "Genere par tools/sync_geckolib_models.py — meme geo/anim que lucky_rocket in-game",
        },
        "name": theme.tid,
        "model_identifier": "lucky_rocket",
        "resolution": {"width": TEX_W, "height": TEX_H},
        "elements": elements,
        "outliner": [nodes["root"]],
        "textures": [
            {
                "path": f"{theme.tid}.png",
                "name": f"{theme.tid}.png",
                "id": "0",
                "particle": True,
                "visible": True,
                "internal": True,
                "saved": True,
                "uuid": uid(),
                "width": TEX_W,
                "height": TEX_H,
                "uv_width": TEX_W,
                "uv_height": TEX_H,
                "source": "data:image/png;base64," + png_b64(texture),
            }
        ],
        "animations": [bb_animation(f"launch_{style}", LAUNCH_LENGTH, launch_keyframes(style), True)]
        + [bb_animation(kind, length, break_keyframes(kind), False) for kind, length in BREAK_KINDS.items()],
    }


# --------------------------------------------------------------------------- main
def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def write_geckolib_assets() -> None:
    write_json(GEO_DIR / "block/lucky_block.geo.json", geo_json("lucky_block", BLOCK_BONES, 32))
    write_json(
        ANIM_DIR / "block/lucky_block.animation.json",
        anim_json({"animation.lucky_block.idle": (1.0, True, {"root": {"position": [(0.0, [0, 0, 0])]}})}),
    )
    write_json(GEO_DIR / "entity/lucky_rocket.geo.json", geo_json("lucky_rocket", ROCKET_BONES, TEX_H))
    write_json(
        ANIM_DIR / "entity/lucky_rocket.animation.json",
        anim_json({
            **{f"animation.lucky_rocket.{style}": (LAUNCH_LENGTH, "hold_on_last_frame", launch_keyframes(style))
               for style in STYLES},
            **{f"animation.lucky_rocket.{kind}": (length, "hold_on_last_frame", break_keyframes(kind))
               for kind, length in BREAK_KINDS.items()},
        }),
    )
    print("OK geckolib geo+anim (block + rocket)")


def write_rocket_textures() -> dict[str, Image.Image]:
    strip = exhaust_strip()
    ROCKET_TEX_DIR.mkdir(parents=True, exist_ok=True)
    out = {}
    for src in sorted(BLOCK_TEX_DIR.glob("lucky_*.png")):
        img = rocket_texture(Image.open(src), strip)
        img.save(ROCKET_TEX_DIR / src.name)
        out[src.stem] = img
    print(f"OK {len(out)} textures fusee")
    return out


def generate_theme(theme, rocket_tex: dict[str, Image.Image]) -> None:
    frames = []
    for t in range(theme.frames):
        img = new_frame(rgb(theme.bg))
        theme.draw(img, t, theme.frames)
        if theme.tid not in SKIP_FRAME:
            lucky_frame(img, rgb(theme.edge), rgb(theme.hi))
        shade_bottom(img)
        frames.append(img)
    sheet = stack_frames(frames)
    OUT.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT / f"{theme.tid}.png")
    write_mcmeta(OUT / f"{theme.tid}.png.mcmeta", theme)
    texture = rocket_tex.get(theme.tid) or rocket_texture(sheet.crop((0, 0, SIZE, SIZE)), exhaust_strip())
    model = build_bbmodel(theme, texture, style_by_theme().get(theme.tid, "spin"))
    model["meta"]["unpredictableblocks_concept"] = theme.concept
    model["meta"]["unpredictableblocks_title"] = theme.title
    (OUT / f"{theme.tid}.bbmodel").write_text(json.dumps(model, indent="\t") + "\n", encoding="utf-8")
    print("OK", theme.tid)


def main() -> None:
    write_geckolib_assets()
    rocket_tex = write_rocket_textures()
    for th in THEMES:
        generate_theme(th, rocket_tex)
    # Themes sans dessin anime dans generate_blockbench (33+) : bbmodel depuis la texture en jeu
    known = {th.tid for th in THEMES}
    for tid, texture in rocket_tex.items():
        if tid not in known:
            stub = type("Stub", (), {"tid": tid})()
            model = build_bbmodel(stub, texture, style_by_theme().get(tid, "spin"))
            (OUT / f"{tid}.bbmodel").write_text(json.dumps(model, indent="\t") + "\n", encoding="utf-8")
            print("OK", tid, "(texture en jeu)")
    (OUT / "README.md").write_text(
        """# Lucky Blocks — Blockbench + GeckoLib

## Ce qui est genere
| Fichier | Role |
|---|---|
| `geckolib/models/block/lucky_block.geo.json` | Bloc pose (cube seul, statique) |
| `geckolib/models/entity/lucky_rocket.geo.json` | Fusee : `root` → `block` + `exhaust` → `flame`, `smoke1-3` |
| `geckolib/animations/entity/lucky_rocket.animation.json` | 7 styles `animation.lucky_rocket.<style>` (spin, hop, zigzag, spiral, tumble, glitch, float — 2 s, hold) |
| `textures/entity/lucky_rocket/lucky_<id>.png` | 32×48 : texture du bloc + bande fumee (gauche) / flamme (droite) |
| `docs/blockbench/lucky_<id>.bbmodel` | Meme hierarchie + memes keyframes, pour previsualiser |

## Deroule in-game (cassage)
1. Le joueur casse le Lucky Block → l'event part, et une entite `lucky_rocket` apparait a sa place.
2. 0–0.5 s : tremblement + compression, fumee au sol.
3. 0.5 s : son de fusee, flamme, l'entite accelere vers le haut en vrillant, trainee de particules.
4. 2 s : nuage de fumee + etincelles, l'entite disparait.

## Blockbench
- **Le plus simple** : Fichier → Ouvrir `lucky_<id>.bbmodel` → onglet **Animate** → **launch_<style>** → **Play**.
- **Depuis les fichiers GeckoLib** : Fichier → Importer `lucky_rocket.geo.json`, puis onglet **Animate** →
  Importer une animation → `lucky_rocket.animation.json` (la geo ne contient pas les anims).

La montee elle-meme est faite en Java (l'entite bouge) : dans Blockbench le bloc vrille sur place.
Le `.bbmodel` joue `launch` en boucle ; in-game elle est en `hold_on_last_frame` mais l'entite est retiree a 2 s.
Si tu importes `lucky_rocket.animation.json` a la main, Blockbench la met en « hold » (arret a 2 s) :
regle Loop Mode sur « Loop » dans les proprietes de l'anim pour l'apercu.
Keyframes au format Bedrock `"t": [x, y, z]` : l'ancien format `{"vector": [...]}` s'importe a 0 dans Blockbench.

## Modifier l'anim
Style par theme : table `entry(...)` dans `RocketStyle.java` (trajectoire + particules en Java).
Ne pas exporter depuis Blockbench : edite `launch_keyframes(style)` / `ROCKET_BONES`
dans `tools/sync_geckolib_models.py` puis :
```bash
python tools/sync_geckolib_models.py
```
""",
        encoding="utf-8",
        newline="\n",
    )
    print(f"Done {len(THEMES)} themes + geckolib assets")


if __name__ == "__main__":
    main()
