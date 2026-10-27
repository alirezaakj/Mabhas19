"""Validate the planning deliverable without networking or an Android SDK."""
import ast
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
errors = []
markdown = [ROOT / "README.md", ROOT / "AGENTS.md", ROOT / "research/README.md"]
markdown += list((ROOT / "docs").rglob("*.md")) + list((ROOT / "android").rglob("*.md"))
for path in markdown:
    text = path.read_text()
    for target in re.findall(r"!?\[[^\]]*\]\(([^)]+)\)", text):
        target = target.strip("<>").split("#")[0]
        if not target or re.match(r"^[a-z]+://", target):
            continue
        if not (path.parent / target).exists():
            errors.append(f"Broken link in {path.relative_to(ROOT)}: {target}")
for path in (ROOT / "tools/research").glob("*.py"):
    ast.parse(path.read_text(), filename=str(path))
for path in (ROOT / "research/evidence").rglob("*.json"):
    obj = json.loads(path.read_text())
    def inspect(value):
        if isinstance(value, dict):
            for key, item in value.items():
                if key.lower() in {"_csrf", "csrf", "password", "authorization", "cookie", "set-cookie"}:
                    errors.append(f"Review sensitive field in {path.relative_to(ROOT)}: {key}")
                inspect(item)
        elif isinstance(value, list):
            for item in value:
                inspect(item)
    inspect(obj)
fixture = json.loads((ROOT / "research/evidence/airtightness-sample.json").read_text())
result = fixture["response"]["result"]
assert fixture["request"]["test"]["airflow_m3h"] == 6750
assert result["test"]["ach_at_test_pressure"] == 2.25
assert result["normative_precheck"]["strict_comparison"] == "<"
assert result["normative_precheck"]["regulatory_approval"] is False
assert result["overall"] == "review_required"
assert result["building_energy_grade_approved"] is False
manifest = json.loads((ROOT / "research/evidence/manifest.json").read_text())
assert len(manifest["pages"]) == 45
assert len(manifest["assets"]) == 134
assert all(x["browser"]["cleanup"] == "terminated-owned-process" for x in manifest["rea"])
if errors:
    raise SystemExit("\n".join(errors))
print(f"Validated {len(markdown)} Markdown files, Python syntax, public JSON/provenance and air-test fixture semantics.")
