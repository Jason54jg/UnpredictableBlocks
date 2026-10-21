#!/usr/bin/env python3
"""Generate lang/*.json from en_us via MyMemory (en→target). Preserves %s placeholders."""
from __future__ import annotations

import json
import re
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "common/src/main/resources/assets/unpredictableblocks/lang"

LOCALES: dict[str, str] = {
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


def protect(text: str) -> tuple[str, list[str]]:
    placeholders: list[str] = []

    def repl(match: re.Match[str]) -> str:
        placeholders.append(match.group(0))
        return f"__PH{len(placeholders) - 1}__"

    protected = re.sub(r"%(\d+\$)?[sdif]", repl, text)
    return protected, placeholders


def restore(text: str, placeholders: list[str]) -> str:
    for i, ph in enumerate(placeholders):
        text = text.replace(f"__PH{i}__", ph)
    return text


def translate(text: str, target: str) -> str:
    if not text or not text.strip():
        return text
    protected, placeholders = protect(text)
    q = urllib.parse.quote(protected[:480])
    pair = f"en|{target}"
    url = f"https://api.mymemory.translated.net/get?q={q}&langpair={urllib.parse.quote(pair)}"
    for attempt in range(5):
        try:
            with urllib.request.urlopen(url, timeout=45) as resp:
                data = json.load(resp)
            out = data.get("responseData", {}).get("translatedText", protected)
            if "MYMEMORY WARNING" in out.upper():
                time.sleep(2 ** attempt)
                continue
            return restore(out, placeholders)
        except Exception:
            time.sleep(2 ** attempt)
    return text


def main() -> None:
    en_path = LANG_DIR / "en_us.json"
    en: dict[str, str] = json.loads(en_path.read_text(encoding="utf-8"))
    only = sys.argv[1:] if len(sys.argv) > 1 else list(LOCALES.keys())

    for loc in only:
        target = LOCALES.get(loc)
        if not target:
            print("unknown locale", loc)
            continue
        out_path = LANG_DIR / f"{loc}.json"
        if out_path.exists():
            existing = json.loads(out_path.read_text(encoding="utf-8"))
            if len(existing) == len(en) and not any(k not in existing for k in en):
                print(f"skip {loc} (complete)")
                continue
        out: dict[str, str] = {}
        keys = list(en.items())
        print(f"=== {loc} ({target}) {len(keys)} keys ===", flush=True)
        for i, (key, value) in enumerate(keys):
            out[key] = translate(value, target)
            if (i + 1) % 20 == 0:
                print(f"  {i + 1}/{len(keys)}")
            time.sleep(0.22)
        out_path.write_text(json.dumps(out, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print("wrote", out_path)


if __name__ == "__main__":
    main()
