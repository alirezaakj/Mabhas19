"""Inspect explicitly discovered read endpoints without account/project mutation."""
import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen

OUT = Path("research/local/api")
OUT.mkdir(parents=True, exist_ok=True)
paths = ["reference.php", "projects.php", "team.php", "arch_general.php",
         "arch_walls.php", "architecture_drafts.php?project_id=guest&section=arch_daylight",
         "architecture_assemblies.php?project_id=guest", "architecture_dossier.php?project_id=guest"]
records = []
for path in paths:
    url = "https://mabhas19.ir/api/" + path
    try:
        response = urlopen(Request(url, headers={"Accept": "application/json"}), timeout=25)
    except HTTPError as e:
        response = e
    with response as r:
        data = r.read()
        name = path.split("?")[0] + ".json"
        (OUT / name).write_bytes(data)
        try:
            obj = json.loads(data)
        except ValueError:
            obj = None
        record = {"url": url, "status": r.code, "content_type": r.headers.get("Content-Type"),
                  "keys": list(obj) if isinstance(obj, dict) else None, "file": name}
        records.append(record)
        print(record, flush=True)
(OUT / "index.json").write_text(json.dumps(records, ensure_ascii=False, indent=2))
