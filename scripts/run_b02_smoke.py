#!/usr/bin/env python3
"""Run B02/B05 API smoke checks against an authorized test service and dependencies."""

import argparse
import json
import secrets
import socket
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from pathlib import Path

from check_b01_runtime import nacos_config, scalar


def redis_command(sock, *parts):
    payload = b"*" + str(len(parts)).encode() + b"\r\n"
    for part in parts:
        raw = str(part).encode()
        payload += b"$" + str(len(raw)).encode() + b"\r\n" + raw + b"\r\n"
    sock.sendall(payload)
    line = b""
    while not line.endswith(b"\r\n"):
        line += sock.recv(1)
    if line.startswith(b"-"):
        raise RuntimeError("Redis test setup failed")
    return line


def verified_test_captcha(redis_config):
    token = str(uuid.uuid4())
    with socket.create_connection((scalar(redis_config, "host"),
                                   int(scalar(redis_config, "port"))), timeout=10) as sock:
        password = scalar(redis_config, "password")
        if password:
            redis_command(sock, "AUTH", password)
        redis_command(sock, "SET", "SYSTEM:USER:SLIDER:CAPTCHA:VERIFIED:" + token,
                      "true", "EX", "300")
    return token


def request(base_url, method, path, token=None, data=None, content_type="application/json"):
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = token
    if data is not None:
        headers["Content-Type"] = content_type
        if content_type == "application/json":
            data = json.dumps(data, ensure_ascii=False).encode()
    req = urllib.request.Request(base_url.rstrip("/") + path, data=data,
                                 headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            status, body = response.status, response.read()
    except urllib.error.HTTPError as error:
        status, body = error.code, error.read()
    try:
        return status, json.loads(body.decode())
    except (UnicodeDecodeError, json.JSONDecodeError):
        return status, {"code": status, "msg": "non-JSON response", "data": None}


def success(response, label):
    status, body = response
    if status >= 400 or body.get("code") != 0:
        raise RuntimeError(label + " failed: HTTP " + str(status) + ", code="
                           + str(body.get("code")) + ", message=" + str(body.get("msg"))[:100])
    return body.get("data")


def denied(response, label):
    status, body = response
    if status < 400 and body.get("code") == 0:
        raise RuntimeError(label + " unexpectedly allowed")


def login(base_url, redis_config, account):
    captcha = verified_test_captcha(redis_config)
    data = success(request(base_url, "POST", "/api/login", data={
        "userName": account["username"], "passWord": account["password"],
        "sliderCaptchaToken": captcha,
    }), "login " + account["username"])
    return data["token"]


def multipart_pdf(pdf, signature):
    boundary = "study-b02-" + secrets.token_hex(12)
    parts = [
        b"--" + boundary.encode() + b"\r\nContent-Disposition: form-data; name=\"signature\"\r\n\r\n"
        + signature.encode() + b"\r\n",
        b"--" + boundary.encode() + b"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"b02-synthetic-two-page.pdf\"\r\nContent-Type: application/pdf\r\n\r\n"
        + pdf.read_bytes() + b"\r\n",
        b"--" + boundary.encode() + b"--\r\n",
    ]
    return b"".join(parts), "multipart/form-data; boundary=" + boundary


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8091")
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--pdf", type=Path, required=True)
    parser.add_argument("--nacos-url", default="http://127.0.0.1:8848")
    parser.add_argument("--namespace", default="ad55cf5c-9725-4e8a-8e51-f684f1de5211")
    parser.add_argument("--evidence", type=Path, required=True)
    args = parser.parse_args()
    accounts = json.loads(args.credentials.read_text())
    redis_config = nacos_config(args.nacos_url, args.namespace, "redis-config.yaml")
    tokens = {name: login(args.base_url, redis_config, account)
              for name, account in accounts.items()}
    evidence = {"accounts": {name: account["username"] for name, account in accounts.items()},
                "checks": []}

    def checked(label):
        evidence["checks"].append(label)
        print("PASS", label, flush=True)

    try:
        users = {name: success(request(args.base_url, "GET", "/api/user/getCurrentUserInfo",
                                       tokens[name]), "current user " + name)
                 for name in tokens}
        checked("three test accounts login and load profile")
        for name in tokens:
            denied(request(args.base_url, "GET", "/api/role/roleOptions", tokens[name]),
                   name + " admin role options")
        checked("student and guardian roles cannot call admin API")
        student_id = users["student_a"]["id"]
        denied(request(args.base_url, "GET", "/api/guardianOverview/studentOverview?studentUserId="
                       + str(student_id), tokens["guardian"]), "unbound guardian overview")
        checked("unbound guardian cannot read student overview")
        policy = success(request(args.base_url, "GET", "/api/file/policy?uploadType=wrongQuestion",
                                 tokens["student_a"]), "upload policy")
        body, content_type = multipart_pdf(args.pdf, policy["signature"])
        uploaded = success(request(args.base_url, "POST", "/api/file/uploadFile",
                                   tokens["student_a"], body, content_type), "PDF upload")
        file_id = uploaded["id"]
        evidence["file_id"] = file_id
        checked("student uploads synthetic two-page PDF")
        denied(request(args.base_url, "GET", "/api/file/downloadUrl?fileId=" + str(file_id)
                       + "&uploadType=wrongQuestion", tokens["student_b"]), "cross-owner file")
        checked("student B cannot get student A file URL")
        capture = {"grade": "1", "subject": "2", "questionType": "3", "source": "3",
                   "imageFileIds": [file_id], "clientRequestId": "b02-" + str(uuid.uuid4())}
        first = success(request(args.base_url, "POST", "/api/questionCapture/createQuestionCaptureTask",
                                tokens["student_a"], capture), "capture create")
        repeated = success(request(args.base_url, "POST", "/api/questionCapture/createQuestionCaptureTask",
                                   tokens["student_a"], capture), "capture duplicate")
        if first != repeated:
            raise RuntimeError("duplicate clientRequestId created a second capture task")
        evidence["task_id"] = first
        checked("duplicate capture request returns the same task ID")
        denied(request(args.base_url, "GET", "/api/questionCapture/questionCaptureTaskDetail?id="
                       + str(first), tokens["student_b"]), "cross-owner capture task")
        checked("student B cannot read student A capture task")
        for attempt in range(30):
            detail = success(request(args.base_url, "GET", "/api/questionCapture/questionCaptureTaskDetail?id="
                                     + str(first), tokens["student_a"]), "capture detail")
            if detail.get("status") in (2, 3, 4):
                break
            time.sleep(5)
        evidence["capture_status"] = detail.get("status")
        evidence["page_count"] = len(detail.get("pageList") or [])
        if evidence["page_count"] < 2:
            raise RuntimeError("PDF was not split into at least two pages")
        checked("PDF is split into multiple persisted capture pages")
        if detail.get("status") == 4:
            success(request(args.base_url, "POST", "/api/questionCapture/retryQuestionCaptureTask",
                            tokens["student_a"], {"id": first}), "failed capture retry")
            checked("failed OCR task can be retried")
        elif detail.get("status") == 2:
            checked("capture task reaches waiting-confirm state")
        else:
            raise RuntimeError("capture task still processing after the smoke test timeout")
    finally:
        args.evidence.parent.mkdir(parents=True, exist_ok=True)
        args.evidence.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + "\n")
        print("evidence:", args.evidence, flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("FAIL", str(error), file=sys.stderr)
        raise SystemExit(1)
