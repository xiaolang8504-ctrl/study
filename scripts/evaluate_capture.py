#!/usr/bin/env python3
"""Evaluate a fixed, consented OCR sample set from recorded provider outputs."""

import argparse
import json
import math
from collections import defaultdict
from pathlib import Path

FIELDS = ("question_title", "question_content", "wrong_answer", "correct_answer", "analysis")


def overlap(left, right):
    ax1, ay1, ax2, ay2 = left
    bx1, by1, bx2, by2 = right
    area = max(0, min(ax2, bx2) - max(ax1, bx1)) * max(0, min(ay2, by2) - max(ay1, by1))
    union = (ax2 - ax1) * (ay2 - ay1) + (bx2 - bx1) * (by2 - by1) - area
    return area / union if union > 0 else 0


def normalized(value):
    return "".join(str(value or "").split()).lower()


def percentile(values, fraction):
    if not values:
        return None
    ordered = sorted(values)
    return ordered[max(0, math.ceil(len(ordered) * fraction) - 1)]


def evaluate(samples, predictions):
    actual = {sample["sample_id"]: sample for sample in predictions}
    if set(actual) != {sample["sample_id"] for sample in samples}:
        raise ValueError("预测样本 ID 必须与固定真值样本完全一致")
    buckets = defaultdict(list)
    for sample in samples:
        result = actual[sample["sample_id"]]
        tags = sample["strata"]
        for dimension in ("subject", "print_handwriting", "math_diagram", "quality", "page_type"):
            if dimension not in tags:
                raise ValueError(f"{sample['sample_id']} 缺少分层字段 {dimension}")
        metrics = {"truth": len(sample["regions"]), "matched": 0, "fields": 0,
                   "correct_fields": 0, "failed": bool(result.get("failure_reason")),
                   "duration_ms": result.get("duration_ms"),
                   "correction_seconds": result.get("correction_seconds")}
        unused = set(range(len(result.get("regions", []))))
        for truth in sample["regions"]:
            candidates = [(overlap(truth["box"], result["regions"][index]["box"]), index)
                          for index in unused]
            if not candidates:
                continue
            iou, index = max(candidates)
            if iou < 0.5:
                continue
            unused.remove(index)
            metrics["matched"] += 1
            for field in FIELDS:
                if normalized(truth.get(field)):
                    metrics["fields"] += 1
                    metrics["correct_fields"] += int(normalized(truth[field]) ==
                                                     normalized(result["regions"][index].get(field)))
        buckets["overall"].append(metrics)
        for dimension, value in tags.items():
            buckets[f"{dimension}={value}"].append(metrics)
    report = {}
    for name, rows in sorted(buckets.items()):
        truth_count = sum(row["truth"] for row in rows)
        field_count = sum(row["fields"] for row in rows)
        report[name] = {
            "pages": len(rows),
            "region_recall": round(sum(row["matched"] for row in rows) / truth_count, 4)
            if truth_count else None,
            "key_field_accuracy": round(sum(row["correct_fields"] for row in rows) / field_count, 4)
            if field_count else None,
            "failure_rate": round(sum(row["failed"] for row in rows) / len(rows), 4),
            "ocr_p95_ms": percentile([row["duration_ms"] for row in rows
                                      if row["duration_ms"] is not None], 0.95),
            "correction_p95_seconds": percentile([row["correction_seconds"] for row in rows
                                                   if row["correction_seconds"] is not None], 0.95),
        }
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--truth", required=True, type=Path)
    parser.add_argument("--predictions", required=True, type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    truth = json.loads(args.truth.read_text(encoding="utf-8"))
    prediction_file = json.loads(args.predictions.read_text(encoding="utf-8"))
    report = {"sample_version": truth["version"], "provider": prediction_file["provider"],
              "model_version": prediction_file["model_version"],
              "metrics": evaluate(truth["samples"], prediction_file["samples"])}
    output = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(output, encoding="utf-8")
    else:
        print(output, end="")


if __name__ == "__main__":
    main()
