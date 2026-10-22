#!/usr/bin/env python3
"""Merge missing keys from en_us.json into every locale; translate only new keys."""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_DIR = ROOT / "common/src/main/resources/assets/unpredictableblocks/lang"

# Same targets as generate_lang_files.py
LOCALES: dict[str, str] = {
    "en_gb": "en",
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
    "fr_fr": "fr",
}

sys.path.insert(0, str(ROOT / "tools"))
from generate_lang_files import translate  # noqa: E402


def main() -> None:
    en: dict[str, str] = json.loads((LANG_DIR / "en_us.json").read_text(encoding="utf-8"))
    only = sys.argv[1:] if len(sys.argv) > 1 else list(LOCALES.keys())

    for loc in only:
        target = LOCALES.get(loc)
        if not target:
            print("unknown", loc)
            continue
        out_path = LANG_DIR / f"{loc}.json"
        existing: dict[str, str] = {}
        if out_path.exists():
            existing = json.loads(out_path.read_text(encoding="utf-8"))
        missing = [k for k in en if k not in existing]
        if not missing:
            print(f"skip {loc} (complete {len(existing)} keys)")
            continue
        print(f"=== {loc}: +{len(missing)} keys ===", flush=True)
        for i, key in enumerate(missing):
            value = en[key]
            if loc in ("en_gb",) or target == "en":
                existing[key] = value
            elif loc == "fr_fr":
                # keep manual fr if present elsewhere; translate missing only
                existing[key] = translate(value, target)
            else:
                existing[key] = translate(value, target)
            if (i + 1) % 10 == 0:
                print(f"  {i + 1}/{len(missing)}", flush=True)
            time.sleep(0.2)
        ordered = {k: existing[k] for k in sorted(existing.keys(), key=lambda x: (x.split(".")[0], x))}
        out_path.write_text(json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print("wrote", out_path, len(ordered), "keys")


if __name__ == "__main__":
    main()
