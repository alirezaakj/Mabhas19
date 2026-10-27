"""Publish a narrow reviewed subset of public research; never copy cookies/CSRF."""
import base64
import hashlib
import json
from pathlib import Path

local = Path("research/local")
dest = Path("research/evidence")
dest.mkdir(parents=True, exist_ok=True)
index = json.loads((local / "public/index.json").read_text())
manifest = {"captured_date": "2026-10-27", "scope": "public guest edition 1399-v4",
            "pages": [{k: p[k] for k in ("url", "status", "error", "sha256") if k in p} for p in index["pages"]],
            "assets": [{k: a[k] for k in ("url", "sha256", "error") if k in a} for a in index["assets"]],
            "rea": []}
api_index = local / "api/index.json"
if api_index.exists():
    manifest["api_reads"] = [{k: r[k] for k in ("url", "status", "content_type", "keys")}
                             for r in json.loads(api_index.read_text())]
skill = Path(".agents/skills/reverse-engineer-anything/SKILL.md")
manifest["skill"] = {"source": "https://github.com/morluto/rea/tree/main/.agents/skills/reverse-engineer-anything",
                     "skill_sha256": hashlib.sha256(skill.read_bytes()).hexdigest(),
                     "cli_version_used": "6.8.0", "node_version_used": "24.22.1"}
for name in ("home-mobile", "architecture-desktop", "airtightness-sample"):
    path = local / ("rea-" + name + ".json")
    e = json.loads(path.read_text())
    n = e["normalized_result"]
    manifest["rea"].append({"scenario": name, "evidence_id": e["evidence_id"],
                            "capture_sha256": hashlib.sha256(path.read_bytes()).hexdigest(),
                            "completeness": n["completeness"], "browser": n["browser"],
                            "limitations": e["limitations"] + n["limitations"]})
    screenshot = n["steps"][-1]["artifacts"]["screenshot"]
    if screenshot["state"] == "captured":
        folder = dest / "screenshots"
        folder.mkdir(exist_ok=True)
        (folder / (name + ".png")).write_bytes(base64.b64decode(screenshot["value"]["data_base64"]))
    if name == "airtightness-sample":
        events = n["events"]["items"]
        ids = {x["transaction_id"] for x in events if x["kind"] == "request" and x["url"]["url"].endswith("/api/arch_airtightness.php")}
        fixture = {"source_evidence_id": e["evidence_id"], "method": "POST", "path": "/api/arch_airtightness.php",
                   "scenario": "site-provided sample in isolated guest browser", "status": 200}
        for event in events:
            if event["kind"] == "network-content" and event["transaction_id"] in ids:
                body = event["body"]
                if body.get("state") == "captured":
                    fixture[event["phase"]] = json.loads(base64.b64decode(body["content"]))
        (dest / "airtightness-sample.json").write_text(json.dumps(fixture, ensure_ascii=False, indent=2) + "\n")
(dest / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
print("Published source manifest, three guest screenshots, and one public calculation fixture.")
