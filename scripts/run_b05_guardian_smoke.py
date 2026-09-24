#!/usr/bin/env python3
"""Check guardian binding, assisted capture, and immediate access revocation."""

import argparse
import json
import sys
from pathlib import Path

from check_b01_runtime import nacos_config
from run_b02_smoke import denied, login, multipart_pdf, request, success


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8091")
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--pdf", type=Path, required=True)
    parser.add_argument("--evidence", type=Path, required=True)
    parser.add_argument("--nacos-url", default="http://127.0.0.1:8848")
    parser.add_argument("--namespace", default="ad55cf5c-9725-4e8a-8e51-f684f1de5211")
    args = parser.parse_args()
    accounts = json.loads(args.credentials.read_text())
    redis_config = nacos_config(args.nacos_url, args.namespace, "redis-config.yaml")
    student = login(args.base_url, redis_config, accounts["student_a"])
    other_student = login(args.base_url, redis_config, accounts["student_b"])
    guardian = login(args.base_url, redis_config, accounts["guardian"])
    student_id = success(request(args.base_url, "GET", "/api/user/getCurrentUserInfo", student),
                         "student profile")["id"]
    evidence = {"student_id": student_id, "checks": []}

    def checked(label):
        evidence["checks"].append(label)
        print("PASS", label, flush=True)

    try:
        invitation = success(request(args.base_url, "POST",
                                     "/api/guardianBinding/createGuardianInvitation", student,
                                     {"relationType": "GUARDIAN"}), "create invitation")
        relation_id = invitation["relationId"]
        evidence["relation_id"] = relation_id
        success(request(args.base_url, "POST", "/api/guardianBinding/acceptGuardianInvitation",
                        guardian, {"invitationCode": invitation["invitationCode"]}),
                "accept invitation")
        denied(request(args.base_url, "GET", "/api/guardianOverview/studentOverview?studentUserId="
                       + str(student_id), guardian), "unconfirmed binding overview")
        checked("guardian cannot see overview before student confirms")
        success(request(args.base_url, "POST", "/api/guardianBinding/confirmGuardianBinding",
                        student, {"relationId": relation_id}), "confirm binding")
        success(request(args.base_url, "GET", "/api/guardianOverview/studentOverview?studentUserId="
                        + str(student_id), guardian), "bound guardian overview")
        denied(request(args.base_url, "GET", "/api/guardianOverview/studentOverview?studentUserId="
                       + str(student_id), other_student), "unbound account overview")
        checked("only confirmed guardian sees student overview")
        policy = success(request(args.base_url, "GET", "/api/file/policy?uploadType=wrongQuestion",
                                 guardian), "guardian upload policy")
        body, content_type = multipart_pdf(args.pdf, policy["signature"])
        uploaded = success(request(args.base_url, "POST", "/api/file/uploadFile",
                                   guardian, body, content_type), "guardian PDF upload")
        assisted = success(request(args.base_url, "POST",
                                   "/api/guardianAssistedCapture/createGuardianAssistedCapture",
                                   guardian, {"studentUserId": student_id, "grade": "1", "subject": "2",
                                              "questionType": "3", "source": "3",
                                              "imageFileIds": [uploaded["id"]]}), "assisted capture create")
        evidence["assisted_capture_id"] = assisted
        visible = success(request(args.base_url, "GET",
                                  "/api/guardianAssistedCapture/guardianAssistedCaptureList", guardian),
                          "guardian capture list")
        if not any(str(item["id"]) == str(assisted) for item in visible):
            raise RuntimeError("active guardian cannot see pending assisted capture")
        checked("bound guardian can prepare assisted capture")
        success(request(args.base_url, "POST", "/api/guardianBinding/revokeGuardianBinding",
                        student, {"relationId": relation_id}), "revoke binding")
        denied(request(args.base_url, "GET", "/api/guardianOverview/studentOverview?studentUserId="
                       + str(student_id), guardian), "revoked guardian overview")
        remaining = success(request(args.base_url, "GET",
                                    "/api/guardianAssistedCapture/guardianAssistedCaptureList", guardian),
                            "revoked guardian capture list")
        if any(str(item["id"]) == str(assisted) for item in remaining):
            raise RuntimeError("revoked guardian still sees assisted capture")
        denied(request(args.base_url, "POST",
                       "/api/guardianAssistedCapture/revokeGuardianAssistedCapture", guardian,
                       {"relationId": assisted}), "revoked guardian capture mutation")
        denied(request(args.base_url, "POST",
                       "/api/guardianAssistedCapture/confirmGuardianAssistedCapture", student,
                       {"relationId": assisted}), "student confirmation after revocation")
        checked("revocation immediately blocks overview, capture list and pending capture actions")
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
