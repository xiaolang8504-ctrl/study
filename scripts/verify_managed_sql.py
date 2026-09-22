#!/usr/bin/env python3
"""Reject edits to frozen managed SQL and unregistered migrations."""

import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL_DIR = ROOT / "study-module-system/src/main/resources/sql/generated"
MANIFEST = ROOT / "docs/releases/p0-managed-sql-sha256.json"


def main():
    expected = json.loads(MANIFEST.read_text(encoding="utf-8"))
    actual = {path.name: hashlib.sha256(path.read_bytes()).hexdigest()
              for path in sorted(SQL_DIR.glob("*.sql"))}
    if actual != expected:
        print("受管 SQL 与校验清单不一致；新增迁移或修改文件后请审查并更新清单。", file=sys.stderr)
        for name in sorted(set(actual) | set(expected)):
            if actual.get(name) != expected.get(name):
                print(f"  {name}: expected={expected.get(name)} actual={actual.get(name)}", file=sys.stderr)
        return 1
    for path in SQL_DIR.glob("*.sql"):
        contents = path.read_text(encoding="utf-8").upper()
        if "DROP TABLE" in contents and "-- DROP TABLE" not in contents:
            print(f"受管迁移含 DROP TABLE：{path.name}", file=sys.stderr)
            return 1
    print(f"受管 SQL 校验通过：{len(actual)} 个脚本")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
