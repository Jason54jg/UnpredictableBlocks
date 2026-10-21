#!/usr/bin/env python3
"""Compatibilité : les générateurs produisent déjà du 32×32. Re-génère si besoin."""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    scripts = ["generate_textures.py", "generate_item_textures.py"]
    for name in scripts:
        path = ROOT / "tools" / name
        print("→", name)
        subprocess.check_call([sys.executable, str(path)], cwd=ROOT)
    print("Done (32×32).")


if __name__ == "__main__":
    main()
