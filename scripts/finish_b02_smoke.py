#!/usr/bin/env python3
"""Continue a B02 capture task through confirmation, correction, review and export."""

import argparse
import datetime
import json
import sys
import time
import uuid
from pathlib import Path

from check_b01_runtime import nacos_config
from run_b02_smoke import denied, login, request, success


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://127.0.0.1:8091")
    parser.add_argument("--credentials", type=Path, required=True)
    parser.add_argument("--evidence", type=Path, required=True)
    parser.add_argument("--wait-seconds", type=int, default=900)
    parser.add_argument("--nacos-url", default="http://127.0.0.1:8848")
    parser.add_argument("--namespace", default="ad55cf5c-9725-4e8a-8e51-f684f1de5211")
    args = parser.parse_args()
    evidence = json.loads(args.evidence.read_text())
    accounts = json.loads(args.credentials.read_text())
    redis_config = nacos_config(args.nacos_url, args.namespace, "redis-config.yaml")
    student = login(args.base_url, redis_config, accounts["student_a"])
    other = login(args.base_url, redis_config, accounts["student_b"])
    task_id = evidence["task_id"]
    checks = evidence.setdefault("checks", [])

    def checked(label):
        checks.append(label)
        print("PASS", label, flush=True)

    try:
        deadline = time.monotonic() + args.wait_seconds
        previous = None
        retried = False
        while True:
            detail = success(request(args.base_url, "GET",
                                     "/api/questionCapture/questionCaptureTaskDetail?id="
                                     + str(task_id), student), "capture detail")
            state = (detail["status"], tuple((page["pageNo"], page["status"])
                                             for page in detail.get("pageList") or []))
            if state != previous:
                print("OCR state:", state, flush=True)
                previous = state
            if detail["status"] == 2:
                break
            if detail["status"] == 4 and not retried:
                success(request(args.base_url, "POST",
                                "/api/questionCapture/retryQuestionCaptureTask", student,
                                {"id": task_id}), "failed capture retry")
                retried = True
                checked("failed OCR task accepts retry")
            elif detail["status"] == 4:
                raise RuntimeError("capture failed again after retry")
            if time.monotonic() >= deadline:
                raise RuntimeError("OCR did not reach waiting-confirm before timeout")
            time.sleep(5)
        evidence["capture_status"] = detail["status"]
        checked("both PDF pages finish OCR and task waits for confirmation")
        regions = detail.get("regionList") or []
        if not regions:
            page_id = detail["pageList"][0]["id"]
            success(request(args.base_url, "POST",
                            "/api/questionCapture/createQuestionCaptureRegion", student,
                            {"taskId": task_id, "pageId": page_id,
                             "leftPosition": 20, "topPosition": 20,
                             "width": 400, "height": 160}), "manual region")
            detail = success(request(args.base_url, "GET",
                                     "/api/questionCapture/questionCaptureTaskDetail?id="
                                     + str(task_id), student), "capture after manual region")
            regions = detail.get("regionList") or []
        region = next((item for item in regions if item["status"] == 0), None)
        if region is None:
            raise RuntimeError("no region available for confirmation")
        region_id = region["id"]
        success(request(args.base_url, "POST",
                        "/api/questionCapture/updateQuestionCaptureRegion", student,
                        {"id": region_id, "questionTitle": "B02 synthetic equation",
                         "questionContent": "Solve 2x + 5 = 17",
                         "wrongAnswer": "x=5", "correctAnswer": "x=6",
                         "analysis": "Subtract 5, then divide by 2."}), "region correction")
        checked("student adjusts OCR region and corrects content")
        created = success(request(args.base_url, "POST",
                                  "/api/questionCapture/confirmQuestionCapture", student,
                                  {"taskId": task_id, "regionIds": [region_id]}),
                          "region confirmation")
        if created != 1:
            raise RuntimeError("expected exactly one wrong question from region")
        denied(request(args.base_url, "POST",
                       "/api/questionCapture/confirmQuestionCapture", student,
                       {"taskId": task_id, "regionIds": [region_id]}), "duplicate confirmation")
        detail = success(request(args.base_url, "GET",
                                 "/api/questionCapture/questionCaptureTaskDetail?id="
                                 + str(task_id), student), "capture after confirmation")
        question_id = next(item["wrongQuestionId"] for item in detail["regionList"]
                           if str(item["id"]) == str(region_id))
        evidence["wrong_question_id"] = question_id
        checked("repeat confirmation does not create another wrong question")
        success(request(args.base_url, "GET", "/api/wrongQuestion/wrongQuestionDetail?id="
                        + str(question_id), student), "wrong question detail")
        denied(request(args.base_url, "GET", "/api/wrongQuestion/wrongQuestionDetail?id="
                       + str(question_id), other), "cross-owner wrong question")
        checked("student B cannot read student A wrong question")
        success(request(args.base_url, "POST", "/api/wrongQuestion/submitCorrectionRecord",
                        student, {"wrongQuestionId": question_id,
                                  "correctionAnswer": "x=6",
                                  "correctionAnalysis": "2x=12, x=6"}), "correction")
        denied(request(args.base_url, "POST", "/api/wrongQuestion/submitCorrectionRecord",
                       other, {"wrongQuestionId": question_id,
                               "correctionAnswer": "foreign"}), "cross-owner correction")
        checked("student correction succeeds and cross-owner correction is denied")
        success(request(args.base_url, "POST", "/api/review/initializeReviewHome",
                        student, b"", "application/x-www-form-urlencoded"), "review home")
        today = success(request(args.base_url, "GET", "/api/review/todayReviewHome", student),
                        "today review")
        item = next((item for item in today.get("taskList") or []
                     if str(item["wrongQuestionId"]) == str(question_id)), None)
        if item is None:
            raise RuntimeError("corrected question is missing from today's review")
        review_id = item["reviewItemId"]
        success(request(args.base_url, "POST", "/api/review/saveReviewAnswerDraft",
                        student, {"reviewItemId": review_id, "studentAnswer": "x=6"}),
                "independent review draft")
        answer = success(request(args.base_url, "POST", "/api/review/reviewAnswer",
                                 student, {"reviewItemId": review_id}), "answer reveal")
        feedback = {"requestId": "b02-review-" + str(uuid.uuid4()),
                    "reviewItemId": review_id,
                    "startTime": (datetime.datetime.now() - datetime.timedelta(minutes=1))
                    .strftime("%Y-%m-%d %H:%M:%S"),
                    "revealToken": answer["revealToken"], "studentAnswer": "x=6",
                    "selfCorrect": 1, "feedback": 2}
        first_feedback = success(request(args.base_url, "POST", "/api/review/submitReviewFeedback",
                                         student, feedback), "review feedback")
        repeated_feedback = success(request(args.base_url, "POST", "/api/review/submitReviewFeedback",
                                            student, feedback), "duplicate review feedback")
        if first_feedback != repeated_feedback:
            raise RuntimeError("duplicate feedback returned a different result")
        checked("review draft, answer reveal and duplicate feedback are consistent")
        success(request(args.base_url, "GET", "/api/review/learningReport", student),
                "learning report")
        checked("learning report loads after review")
        practice = success(request(args.base_url, "POST", "/api/review/createPracticeSession",
                                   student, {"practiceType": "TYPICAL", "questionSource": "WRONG_QUESTION",
                                             "questionCount": 1}), "practice creation")
        session_id = practice["sessionId"]
        evidence["practice_session_id"] = session_id
        success(request(args.base_url, "GET", "/api/review/practicePaperDetail?sessionId="
                        + str(session_id), student), "practice paper detail")
        denied(request(args.base_url, "GET", "/api/review/practicePaperDetail?sessionId="
                       + str(session_id), other), "cross-owner practice paper")
        checked("practice session and answer sheet are scoped to student A")
        export_id = success(request(args.base_url, "POST",
                                    "/api/review/createPracticePaperExportTask", student,
                                    {"sessionId": session_id, "format": "DOCX", "answerMode": 0}),
                            "practice DOCX export")
        evidence["export_task_id"] = export_id
        exports = success(request(args.base_url, "GET", "/api/review/practicePaperExportTaskList",
                                  student), "export history")
        if not any(str(item["id"]) == str(export_id) for item in exports):
            raise RuntimeError("new export task missing from student's history")
        other_exports = success(request(args.base_url, "GET", "/api/review/practicePaperExportTaskList",
                                        other), "other student's export history")
        if any(str(item["id"]) == str(export_id) for item in other_exports):
            raise RuntimeError("export task leaked into another student's history")
        checked("DOCX export task is created and visible only to its owner")
    finally:
        args.evidence.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + "\n")
        print("evidence:", args.evidence, flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print("FAIL", str(error), file=sys.stderr)
        raise SystemExit(1)
