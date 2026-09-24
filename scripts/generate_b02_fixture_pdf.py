#!/usr/bin/env python3
"""Generate a two-page, synthetic math worksheet for B02 capture smoke tests."""

import argparse
from pathlib import Path


def stream(lines):
    commands = ["BT", "/F1 19 Tf", "72 735 Td"]
    for index, line in enumerate(lines):
        escaped = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
        if index:
            commands.append("0 -55 Td")
        commands.append("(" + escaped + ") Tj")
    commands.append("ET")
    body = ("\n".join(commands) + "\n").encode("ascii")
    return b"<< /Length " + str(len(body)).encode() + b" >>\nstream\n" + body + b"endstream"


def generate():
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 6 0 R >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 7 0 R >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        stream(["B02 Synthetic Worksheet - Page 1", "1. Solve: 2x + 5 = 17", "2. Compute: 3/4 + 1/8 = ?"]),
        stream(["B02 Synthetic Worksheet - Page 2", "3. Solve: x^2 - 9 = 0", "4. Triangle sides: 3, 4, 5. Find area."]),
    ]
    output = bytearray(b"%PDF-1.4\n")
    offsets = [0]
    for index, obj in enumerate(objects, 1):
        offsets.append(len(output))
        output.extend(str(index).encode() + b" 0 obj\n" + obj + b"\nendobj\n")
    xref = len(output)
    output.extend(b"xref\n0 8\n0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode())
    output.extend(b"trailer\n<< /Size 8 /Root 1 0 R >>\nstartxref\n")
    output.extend(str(xref).encode() + b"\n%%EOF\n")
    return bytes(output)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(generate())
    print(args.output)


if __name__ == "__main__":
    main()
