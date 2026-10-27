"""Generate/run explicit REA scenarios; no authenticated project writes.

Usage: python3 tools/research/capture_scenarios.py /absolute/path/to/node /absolute/path/to/rea
"""
import json
import subprocess
import sys
from pathlib import Path

OUT = Path("research/local")
OUT.mkdir(parents=True, exist_ok=True)
browser = {"mode": "launch", "executable_path": "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"}
scenarios = {
    "home-mobile": ("/", 390, [{"step_id": "settle", "action": "wait_for_timeout", "duration_ms": 1800}]),
    "architecture-desktop": ("/architecture/arch_general.php", 1440, [{"step_id": "settle", "action": "wait_for_timeout", "duration_ms": 1800}]),
    "airtightness-sample": ("/supervision/airtightness.php", 1280, [
        {"step_id": "settle", "action": "wait_for_timeout", "duration_ms": 1000},
        {"step_id": "sample", "action": "click", "locator": {"kind": "css", "selector": "#btnLoadSamplePass"}, "timeout_ms": 10000},
        {"step_id": "calculate", "action": "click", "locator": {"kind": "css", "selector": "#airRun"}, "timeout_ms": 10000},
        {"step_id": "result", "action": "wait_for_timeout", "duration_ms": 1200},
    ]),
}
for name, (path, width, actions) in scenarios.items():
    spec = {"browser": browser, "start_url": {"url": "https://mabhas19.ir" + path},
            "environment": {"viewport": {"width": width, "height": 900}, "locale": "fa-IR", "timezone": "Asia/Tehran"},
            "actions": actions, "capture": {"at_end": ["url", "dom", "accessibility", "screenshot"],
            "events": ["network"], "network": {"request_body": True, "response_body": True, "header_values": False}}}
    spec_file = OUT / (name + "-input.json")
    spec_file.write_text(json.dumps(spec, ensure_ascii=False, indent=2))
    with (OUT / ("rea-" + name + ".json")).open("w") as f:
        result = subprocess.run([*sys.argv[1:3], "capture-browser-scenario", str(spec_file.resolve()), "--format", "json"], stdout=f, timeout=100)
    print(name, result.returncode, flush=True)
