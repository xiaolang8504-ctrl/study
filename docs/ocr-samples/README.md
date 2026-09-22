# 采集评测样本

`scripts/evaluate_capture.py` 使用固定真值和某次 OCR 结果计算题块召回、关键字段准确率、失败率、OCR P95 与人工修正 P95，并按学科、印刷/手写、公式/图形、清晰度和单双页分桶。题块按归一化坐标框的 IoU ≥ 0.5 匹配；字段忽略空白并转小写后严格比较。每次运行保留提供方、模型版本和样本版本。

`fixture-truth.json` 和 `fixture-predictions.json` 仅验证计算脚本，不代表真实 OCR 质量。真实样本须经授权并去标识化，图像与真值保存在受控存储；仓库只提交样本 ID、分类、文件 SHA256 和不含个人信息的评测报告。每个提供方/版本应导出一份预测 JSON，并执行：

```bash
python3 scripts/evaluate_capture.py --truth /secure/ocr-truth-v1.json --predictions /secure/provider-model-v1.json --output /secure/report-v1.json
```

每页的 `regions` 元素含归一化 `box=[left,top,right,bottom]`（0–10000）和可选关键字段。预测记录另含 `duration_ms`、`correction_seconds`、`failure_reason`。一个 PDF 的各页使用独立样本 ID，并在 `strata.page_type` 标注 `multi`。真实样本集在学科、字迹、公式/图形、模糊/斜拍及单/多页各层至少有代表样本后，才能设上线阈值。
