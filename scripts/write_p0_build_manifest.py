#!/usr/bin/env python3
"""Write a reproducible P0 build identity without embedding credentials."""

import argparse
import hashlib
import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(["git", *args], cwd=ROOT, text=True).strip()


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    sql_dir = ROOT / "study-module-system/src/main/resources/sql/generated"
    manifest = {
        "commit": git("rev-parse", "HEAD"),
        "dirty_worktree": bool(git("status", "--porcelain")),
        "migrations": {path.name: digest(path) for path in sorted(sql_dir.glob("*.sql"))},
        "frontend_lock_sha256": digest(ROOT / "frontend/package-lock.json"),
        "maven_pom_sha256": digest(ROOT / "pom.xml"),
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(args.output)


if __name__ == "__main__":
    main()
