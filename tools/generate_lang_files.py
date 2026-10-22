#!/usr/bin/env python3
"""Generate lang/*.json from en_us. Prefer Argos (offline); MyMemory as optional fallback."""
from __future__ import annotations

import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "common/src/main/resources/assets/unpredictableblocks/lang"

# Minecraft locale -> Argos to_code (en->X). None = MyMemory only.
LOCALES: dict[str, str] = {
    "de_de": "de",
    "es_es": "es",
    "es_mx": "es",
    "pt_br": "pb",  # Argos Brazilian Portuguese
    "it_it": "it",
    "nl_nl": "nl",
    "pl_pl": "pl",
    "ru_ru": "ru",
    "uk_ua": "uk",
    "cs_cz": "cs",
    "sv_se": "sv",
    "da_dk": "da",
    "nb_no": "nb",
    "fi_fi": "fi",
    "hu_hu": "hu",
    "tr_tr": "tr",
    "ja_jp": "ja",
    "ko_kr": "ko",
    "zh_cn": "zh",
    "zh_tw": "zt",
    "vi_vn": "vi",
    "id_id": "id",
    "th_th": "th",
    "ro_ro": "ro",
    "el_gr": "el",
    "he_il": "he",
    "ar_sa": "ar",
}

# MyMemory langpair codes (when --mymemory)
MYMEMORY: dict[str, str] = {
    "de_de": "de",
    "es_es": "es",
    "es_mx": "es",
    "pt_br": "pt-BR",
    "it_it": "it",
    "nl_nl": "nl",
    "pl_pl": "pl",
    "ru_ru": "ru",
    "uk_ua": "uk",
    "cs_cz": "cs",
    "sv_se": "sv",
    "da_dk": "da",
    "nb_no": "no",
    "fi_fi": "fi",
    "hu_hu": "hu",
    "tr_tr": "tr",
    "ja_jp": "ja",
    "ko_kr": "ko",
    "zh_cn": "zh-CN",
    "zh_tw": "zh-TW",
    "vi_vn": "vi",
    "id_id": "id",
    "th_th": "th",
    "ro_ro": "ro",
    "el_gr": "el",
    "he_il": "he",
    "ar_sa": "ar",
}

_cooldown_until = 0.0
_argos_ready: set[str] = set()


# Termes jamais traduits (noms propres du mod / du jeu)
GLOSSARY = r"Lucky Blocks?|Lucky|Skyblock|BONK|HODL|BSOD|D20|Minecraft"
TOKEN = re.compile(r"%(\d+\$)?[sdif]|§[0-9a-fk-or]|[★✔○·−×]|" + GLOSSARY)


def protect(text: str) -> tuple[str, list[str]]:
    """Remplace placeholders, codes couleur, symboles et glossaire par des marqueurs [[0]], [[1]]…"""
    placeholders: list[str] = []

    def repl(match: re.Match[str]) -> str:
        placeholders.append(match.group(0))
        marker = f"[[{len(placeholders) - 1}]]"
        # Les mots du glossaire gardent leurs espaces (le traducteur les colle sinon aux voisins)
        return f" {marker} " if match.group(0)[0].isalpha() else marker

    return TOKEN.sub(repl, text), placeholders


def restore(text: str, placeholders: list[str]) -> str:
    """Remet les marqueurs, même abîmés par le traducteur ([[ 0 ]], [0], __PH0__, PH 0…)."""
    def repl(match: re.Match[str]) -> str:
        i = int(match.group(1))
        return placeholders[i] if i < len(placeholders) else match.group(0)

    text = re.sub(r"\[\s*\[?\s*(\d+)\s*\]?\s*\]", repl, text)   # [[0]], [[ 0 ]], [0]
    text = re.sub(r"_*\s*PH\s*(\d+)\s*_*", repl, text)           # ancien format __PH0__ abime
    # Le traducteur colle souvent les termes du glossaire aux mots voisins : on remet l'espace
    text = re.sub(rf"(?<=[^\W\d_])({GLOSSARY})", r" \1", text)
    text = re.sub(rf"((?>{GLOSSARY}))(?=[^\W\d_])", r"\1 ", text)  # atomique : jamais « Lucky Block s »
    return re.sub(r" {2,}", " ", text).strip()


def is_valid(source: str, translated: str) -> bool:
    """Traduction utilisable : memes placeholders / codes couleur, aucun crochet ou marqueur invente."""
    if not tokens_match(source, translated):
        return False
    if re.search(r"[\[\]]", translated) and not re.search(r"[\[\]]", source):
        return False
    return not re.search(r"PH\s*\d|WEB", translated) or "WEB" in source


def tokens_match(source: str, translated: str) -> bool:
    """Memes placeholders / codes couleur dans la source et la traduction."""
    pat = r"%(?:\d+\$)?[sdif]|§[0-9a-fk-or]"
    return sorted(re.findall(pat, source)) == sorted(re.findall(pat, translated))


def ensure_argos(to_code: str) -> None:
    if to_code in _argos_ready:
        return
    import argostranslate.package as pkg
    import argostranslate.translate  # noqa: F401

    installed = {(p.from_code, p.to_code) for p in pkg.get_installed_packages()}
    if ("en", to_code) not in installed:
        print(f"  installing Argos en->{to_code}…", flush=True)
        pkg.update_package_index()
        available = pkg.get_available_packages()
        match = next((p for p in available if p.from_code == "en" and p.to_code == to_code), None)
        if match is None:
            raise RuntimeError(f"no Argos package en->{to_code}")
        pkg.install_from_path(match.download())
    _argos_ready.add(to_code)


def translate_argos(text: str, to_code: str) -> str:
    import argostranslate.translate as tr

    ensure_argos(to_code)
    protected, placeholders = protect(text)
    out = tr.translate(protected, "en", to_code)
    return restore(out, placeholders)


def translate_mymemory(text: str, target: str) -> str:
    global _cooldown_until
    if not text or not text.strip():
        return text
    protected, placeholders = protect(text)
    q = urllib.parse.quote(protected[:480])
    pair = f"en|{target}"
    url = f"https://api.mymemory.translated.net/get?q={q}&langpair={urllib.parse.quote(pair)}"
    for attempt in range(8):
        now = time.time()
        if now < _cooldown_until:
            time.sleep(_cooldown_until - now)
        try:
            with urllib.request.urlopen(url, timeout=45) as resp:
                data = json.load(resp)
            out = data.get("responseData", {}).get("translatedText", protected)
            if "MYMEMORY WARNING" in out.upper():
                _cooldown_until = time.time() + min(120, 15 * (2**attempt))
                continue
            return restore(out, placeholders)
        except urllib.error.HTTPError as e:
            if e.code == 429:
                _cooldown_until = time.time() + min(180, 30 * (2**attempt))
                continue
            time.sleep(min(60, 2**attempt))
        except Exception:
            time.sleep(min(60, 2**attempt))
    return text


def save(path: Path, data: dict[str, str]) -> None:
    """Fichier temporaire + remplacement : Windows refuse parfois l'ouverture directe (Errno 22) si l'IDE lit le fichier."""
    tmp = path.with_suffix(path.suffix + ".tmp")
    tmp.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for attempt in range(10):
        try:
            os.replace(tmp, path)
            return
        except OSError:
            time.sleep(0.3 * (attempt + 1))
    raise OSError(f"Impossible de remplacer {path}")


def main() -> None:
    use_mymemory = "--mymemory" in sys.argv
    args = [a for a in sys.argv[1:] if a != "--mymemory"]
    en_path = LANG_DIR / "en_us.json"
    en: dict[str, str] = json.loads(en_path.read_text(encoding="utf-8"))
    only = args if args else list(LOCALES.keys())

    for loc in only:
        argos_code = LOCALES.get(loc)
        if not argos_code:
            print("unknown locale", loc)
            continue
        out_path = LANG_DIR / f"{loc}.json"
        out: dict[str, str] = {}
        if out_path.exists():
            out = json.loads(out_path.read_text(encoding="utf-8"))
            if len(out) == len(en) and not any(k not in out for k in en):
                print(f"skip {loc} (complete)")
                continue
        keys = list(en.items())
        pending = [(k, v) for k, v in keys if k not in out]
        backend = "mymemory" if use_mymemory else "argos"
        print(f"=== {loc} ({backend}:{argos_code}) {len(pending)} pending / {len(keys)} ===", flush=True)
        if not use_mymemory:
            ensure_argos(argos_code)
        for i, (key, value) in enumerate(pending):
            if use_mymemory:
                out[key] = translate_mymemory(value, MYMEMORY[loc])
                time.sleep(0.35)
            else:
                out[key] = translate_argos(value, argos_code)
            if not is_valid(value, out[key]):
                out[key] = value  # traduction cassee : l'anglais vaut mieux qu'un texte illisible
            done = len(out)
            if done % 40 == 0 or (i + 1) == len(pending):
                print(f"  {done}/{len(keys)}", flush=True)
                save(out_path, {k: out[k] for k, _ in keys if k in out})
        ordered = {k: out[k] for k, _ in keys}
        save(out_path, ordered)
        print("wrote", out_path, flush=True)


if __name__ == "__main__":
    main()
