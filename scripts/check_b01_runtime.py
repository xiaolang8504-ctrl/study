#!/usr/bin/env python3
"""Read-only B01 preflight: Nacos security and managed SQL history (no secrets printed)."""

import argparse
import json
import os
import re
import subprocess
import tempfile
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL_DIR = ROOT / "study-module-system/src/main/resources/sql/generated"
PROTECTED_PREFIXES = (
    "/api/wrongQuestion/",
    "/api/questionCapture/",
    "/api/review/",
    "/api/guardian/",
    "/api/user/createUser",
    "/api/role/",
    "/api/menu/",
    "/api/resource/",
    "/api/dict/",
    "/api/file/policy",
    "/api/file/uploadFile",
)


def nacos_config(base_url, namespace, data_id):
    query = urllib.parse.urlencode({
        "dataId": data_id, "group": "DEFAULT_GROUP", "tenant": namespace,
    })
    with urllib.request.urlopen(base_url.rstrip("/") + "/nacos/v1/cs/configs?" + query,
                                timeout=10) as response:
        return response.read().decode("utf-8")


def scalar(config, key):
    match = re.search(r"^\s*" + re.escape(key) + r":\s*['\"]?([^\s'\"]+)",
                      config, re.MULTILINE)
    if match is None:
        raise ValueError("Nacos database config is missing " + key)
    return match.group(1)


def mysql_rows(config, query):
    jdbc = re.search(r"jdbc:mysql://([^/:?]+)(?::(\d+))?/([^?\s]+)", config)
    if jdbc is None:
        raise ValueError("Nacos database config has no MySQL JDBC URL")
    with tempfile.NamedTemporaryFile(mode="w", prefix="study-b01-mysql-", delete=False) as handle:
        os.chmod(handle.name, 0o600)
        handle.write("[client]\n")
        handle.write("host=" + jdbc.group(1) + "\n")
        handle.write("port=" + (jdbc.group(2) or "3306") + "\n")
        handle.write("user=" + scalar(config, "username") + "\n")
        handle.write("password=" + scalar(config, "password") + "\n")
        handle.write("database=" + jdbc.group(3) + "\n")
        config_path = handle.name
    try:
        completed = subprocess.run(
            ["mysql", "--defaults-extra-file=" + config_path, "-N", "-B", "-e", query],
            check=True, capture_output=True, text=True, timeout=30,
        )
        return [line.split("\t") for line in completed.stdout.splitlines() if line]
    finally:
        os.unlink(config_path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--nacos-url", default="http://127.0.0.1:8848")
    parser.add_argument("--namespace", default="ad55cf5c-9725-4e8a-8e51-f684f1de5211")
    args = parser.parse_args()
    security = nacos_config(args.nacos_url, args.namespace, "security-config.yaml")
    ignored = [line.strip()[2:].strip() for line in security.splitlines()
               if line.strip().startswith("- ")]
    exposed = sorted({path for path in ignored
                      if path.startswith(PROTECTED_PREFIXES)})
    database = nacos_config(args.nacos_url, args.namespace, "database-config.yaml")
    history = mysql_rows(database, "SELECT script_name,script_checksum FROM "
                         "sys_generated_sql_migration ORDER BY script_name")
    audits = mysql_rows(database, "SELECT status,COUNT(*) FROM "
                        "sys_generated_sql_migration_audit GROUP BY status")
    expected = {path.name for path in SQL_DIR.glob("*.sql")}
    applied = {row[0] for row in history}
    result = {
        "managed_sql_total": len(expected),
        "applied_total": len(applied),
        "missing_migrations": sorted(expected - applied),
        "unexpected_history": sorted(applied - expected),
        "audit_counts_all_time": {status: int(count) for status, count in audits},
        "protected_paths_in_ignore_list": exposed,
    }
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if result["missing_migrations"] or result["unexpected_history"] or exposed:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
