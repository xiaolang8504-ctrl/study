#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

python3 scripts/verify_managed_sql.py
python3 scripts/evaluate_capture.py \
  --truth docs/ocr-samples/fixture-truth.json \
  --predictions docs/ocr-samples/fixture-predictions.json > /dev/null
mvn -pl study-module-system -am test
(cd frontend && npm ci && npm run build)
git diff --check
