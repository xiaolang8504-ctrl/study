#!/usr/bin/env python3
"""Create isolated B02 student/guardian accounts in an authorized test database."""

import argparse
import json
import os
import re
import secrets
import subprocess
import tempfile
from pathlib import Path

from check_b01_runtime import nacos_config, scalar


def sql_quote(value):
    if not re.fullmatch(r"[A-Za-z0-9_.$/]+", value):
        raise ValueError("unexpected character in generated test value")
    return "'" + value + "'"


def mysql_config(config):
    jdbc = re.search(r"jdbc:mysql://([^/:?]+)(?::(\d+))?/([^?\s]+)", config)
    if jdbc is None:
        raise ValueError("Nacos database config has no MySQL JDBC URL")
    with tempfile.NamedTemporaryFile(mode="w", prefix="study-b02-mysql-", delete=False) as handle:
        os.chmod(handle.name, 0o600)
        handle.write("[client]\n")
        handle.write("host=" + jdbc.group(1) + "\n")
        handle.write("port=" + (jdbc.group(2) or "3306") + "\n")
        handle.write("user=" + scalar(config, "username") + "\n")
        handle.write("password=" + scalar(config, "password") + "\n")
        handle.write("database=" + jdbc.group(3) + "\n")
        return handle.name


def bcrypt(password):
    result = subprocess.run(["htpasswd", "-niBC", "10", "b02"], input=password + "\n",
                            text=True, capture_output=True, check=True)
    return result.stdout.strip().split(":", 1)[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--test-environment", action="store_true", required=True)
    parser.add_argument("--credentials-output", type=Path, required=True)
    parser.add_argument("--nacos-url", default="http://127.0.0.1:8848")
    parser.add_argument("--namespace", default="ad55cf5c-9725-4e8a-8e51-f684f1de5211")
    args = parser.parse_args()
    config = nacos_config(args.nacos_url, args.namespace, "database-config.yaml")
    client_file = mysql_config(config)
    suffix = secrets.token_hex(4)
    users = {name: {"username": "b02_" + name + "_" + suffix,
                    "password": secrets.token_urlsafe(12)[:18]}
             for name in ("student_a", "student_b", "guardian")}
    role_names = {"student_a": "智错本学生", "student_b": "智错本学生", "guardian": "智错本家长"}
    statements = ["START TRANSACTION;"]
    for name, account in users.items():
        statements.append("INSERT INTO sys_user (user_name,pass_word,real_name,status,remark,create_time) "
                          "VALUES (" + sql_quote(account["username"]) + ","
                          + sql_quote(bcrypt(account["password"])) + ","
                          + sql_quote("B02_" + name) + ",1,'B02 synthetic test',NOW());")
        statements.append("INSERT INTO sys_user_role (user_id,role_id,create_time) "
                          "SELECT u.id,r.id,NOW() FROM sys_user u JOIN sys_role r ON r.role_name="
                          + "'" + role_names[name] + "' WHERE u.user_name="
                          + sql_quote(account["username"]) + " AND r.is_system=0;")
    statements.append("COMMIT;")
    try:
        check = subprocess.run(["mysql", "--defaults-extra-file=" + client_file, "-N", "-B",
                                "-e", "SELECT role_name FROM sys_role WHERE role_name IN "
                                "('智错本学生','智错本家长') AND is_system=0"],
                               capture_output=True, text=True, check=True, timeout=30)
        if set(check.stdout.splitlines()) != {"智错本学生", "智错本家长"}:
            raise RuntimeError("B05 test roles are missing; no accounts created")
        subprocess.run(["mysql", "--defaults-extra-file=" + client_file],
                       input="\n".join(statements) + "\n", text=True,
                       capture_output=True, check=True, timeout=30)
        args.credentials_output.parent.mkdir(parents=True, exist_ok=True)
        fd = os.open(args.credentials_output, os.O_CREAT | os.O_EXCL | os.O_WRONLY, 0o600)
        with os.fdopen(fd, "w", encoding="utf-8") as output:
            json.dump(users, output, indent=2)
            output.write("\n")
        print("test account names:", ", ".join(account["username"] for account in users.values()))
        print("credentials file:", args.credentials_output)
    finally:
        os.unlink(client_file)


if __name__ == "__main__":
    main()
