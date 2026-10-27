"""Produce reproducible local summaries and PNGs from retained REA evidence."""
import base64
import json
from pathlib import Path

out = Path("research/local")
for path in sorted(out.glob("rea-*.json")):
    evidence = json.loads(path.read_text())
    result = evidence.get("normalized_result", {})
    if "steps" not in result:
        continue
    last = result["steps"][-1]
    artifacts = last["artifacts"]
    if artifacts["screenshot"]["state"] == "captured":
        (out / (path.stem + ".png")).write_bytes(base64.b64decode(artifacts["screenshot"]["value"]["data_base64"]))
    for key in ("dom", "accessibility"):
        if artifacts[key]["state"] == "captured":
            (out / (path.stem + "-" + key + ".txt")).write_text(artifacts[key]["value"]["text"])
    events = result["events"]["items"]
    api_ids = {e["transaction_id"] for e in events if "/api/" in str(e) and "transaction_id" in e}
    api_events = [dict(e) for e in events if e.get("transaction_id") in api_ids]
    for e in api_events:
        body = e.get("body", {})
        if body.get("state") == "captured" and body.get("encoding") == "base64":
            try:
                e["decoded_json"] = json.loads(base64.b64decode(body["content"]))
            except (ValueError, UnicodeDecodeError):
                pass
    summary = {"evidence_id": evidence["evidence_id"], "browser": result["browser"],
               "steps": [{k: s[k] for k in ("step_id", "status", "error")} for s in result["steps"]],
               "api_events": api_events, "completeness": result["completeness"],
               "limitations": evidence["limitations"] + result["limitations"]}
    (out / (path.stem + "-summary.json")).write_text(json.dumps(summary, ensure_ascii=False, indent=2))
    print(path.name, evidence["evidence_id"], len(api_events), "API events")
