#!/usr/bin/env python3
"""Fill or create all lang/*.json from en_us using Google Translate (deep-translator)."""
from __future__ import annotations

import json
import re
import sys
import time
from pathlib import Path

from deep_translator import GoogleTranslator

LANG_DIR = Path(__file__).resolve().parents[1] / "common/src/main/resources/assets/unpredictableblocks/lang"

LOCALES: dict[str, str] = {
    "en_gb": "en",
    "fr_fr": "fr",
    "de_de": "de",
    "es_es": "es",
    "es_mx": "es",
    "pt_br": "pt",
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
    "he_il": "iw",
    "ar_sa": "ar",
}


def protect(text: str) -> tuple[str, list[str]]:
    placeholders: list[str] = []

    def repl(match: re.Match[str]) -> str:
        placeholders.append(match.group(0))
        return f"__PH{len(placeholders) - 1}__"

    return re.sub(r"%(\d+\$)?[sdif]", repl, text), placeholders


def restore(text: str, placeholders: list[str]) -> str:
    for i, ph in enumerate(placeholders):
        text = text.replace(f"__PH{i}__", ph)
    return text


def main() -> None:
    en: dict[str, str] = json.loads((LANG_DIR / "en_us.json").read_text(encoding="utf-8"))
    only = sys.argv[1:] if len(sys.argv) > 1 else list(LOCALES.keys())

    for loc in only:
        target = LOCALES.get(loc)
        if not target:
            print("unknown", loc)
            continue
        path = LANG_DIR / f"{loc}.json"
        data: dict[str, str] = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}
        missing = [k for k in en if k not in data]
        if not missing:
            print("skip", loc, len(data))
            continue
        print(loc, "missing", len(missing), flush=True)
        tr = GoogleTranslator(source="en", target=target)
        for i, key in enumerate(missing):
            text = en[key]
            if loc == "en_gb" or target == "en":
                data[key] = text
                continue
            prot, ph = protect(text)
            try:
                out = tr.translate(prot[:4500]) if prot.strip() else text
            except Exception:
                out = text
                time.sleep(1)
            data[key] = restore(out, ph)
            if (i + 1) % 25 == 0:
                print(" ", i + 1, flush=True)
            time.sleep(0.05)
        ordered = {k: data[k] for k in sorted(data)}
        path.write_text(json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print("wrote", loc, len(ordered))


if __name__ == "__main__":
    main()
