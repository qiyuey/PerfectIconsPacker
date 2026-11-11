"""Require JVM 27 outputs and complete, clean lint analysis after a full app build."""
from pathlib import Path
import argparse
import json
import re
import struct
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    log = args.log.read_text()
    assert "BUILD SUCCESSFUL" in log, "Gradle build did not succeed"
    assert not re.search(r"Error processing|Unsupported class file|LintError|^e:", log, re.M), "Build or lint skipped analysis; inspect the log"
    counts = {}
    for directory in ["built_in_kotlinc", "javac"]:
        files = list((root / "app/build/intermediates" / directory).glob("**/classes/**/*.class"))
        counts[directory] = len(files)
        for file in files:
            magic, _, major = struct.unpack(">IHH", file.read_bytes()[:8])
            assert magic == 0xCAFEBABE and major == 71, f"Incorrect JVM target: {file}: major={major}"
    assert counts["built_in_kotlinc"], "No Kotlin compiler outputs found"
    for variant in ["debug", "release"]:
        report = root / f"app/build/reports/lint-results-{variant}.xml"
        issues = ET.parse(report).getroot().findall("issue")
        assert not issues, f"Lint {variant} has {len(issues)} issues"
    print(json.dumps({"jvm_target": 27, "class_major": 71, "classes": counts, "lint_issues": 0}))


if __name__ == "__main__":
    main()
