"""Bounded GET-only collection of linked Mabhas19 pages and assets.

Run from the repository root. Raw captures are local research, not fixtures.
"""
import hashlib
import json
from collections import deque
from datetime import datetime, timezone
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urljoin, urlparse
from urllib.request import Request, urlopen

BASE = "https://mabhas19.ir/"
OUT = Path("research/local/public")
OUT.mkdir(parents=True, exist_ok=True)


class Page(HTMLParser):
    def __init__(self):
        super().__init__()
        self.links, self.assets, self.forms, self.fields, self.text = [], [], [], [], []
        self.hidden = 0

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag in ("script", "style"):
            self.hidden += 1
        if tag == "a" and a.get("href"):
            self.links.append(a["href"])
        if tag in ("script", "link") and (a.get("src") or a.get("href")):
            self.assets.append(a.get("src") or a.get("href"))
        if tag == "form":
            self.forms.append(a)
        if tag in ("input", "select", "textarea", "button"):
            self.fields.append({"tag": tag, **a})

    def handle_endtag(self, tag):
        if tag in ("script", "style"):
            self.hidden = max(0, self.hidden - 1)

    def handle_data(self, text):
        if not self.hidden and text.strip():
            self.text.append(text.strip())


queue = deque([BASE])
seen, assets, records = set(), set(), []
while queue and len(seen) < 45:
    url = queue.popleft()
    if url in seen:
        continue
    seen.add(url)
    record = {"url": url, "captured_at": datetime.now(timezone.utc).isoformat()}
    try:
        with urlopen(Request(url, headers={"User-Agent": "Mabhas19-Android-Discovery/1.0"}), timeout=25) as r:
            data = r.read(6_000_000)
            record.update(status=r.status, final_url=r.url, content_type=r.headers.get("Content-Type"))
        name = hashlib.sha256(url.encode()).hexdigest()[:12] + ".html"
        (OUT / name).write_bytes(data)
        page = Page()
        page.feed(data.decode("utf-8", errors="replace"))
        record.update(file=name, sha256=hashlib.sha256(data).hexdigest(), links=page.links,
                      assets=page.assets, forms=page.forms, fields=page.fields, text=page.text)
        assets.update(urljoin(record["final_url"], a) for a in page.assets)
        for link in page.links:
            child = urljoin(record["final_url"], link).split("#")[0]
            parsed = urlparse(child)
            if (parsed.netloc == "mabhas19.ir" and parsed.path.endswith(".php")
                    and not any(s in child.lower() for s in ("logout", "delete", "remove", "reset", "download", "export", "print"))):
                queue.append(child)
    except Exception as e:
        record["error"] = str(e)
    records.append(record)
    print(url, record.get("status", record.get("error")), flush=True)

asset_records = []
for url in sorted(assets):
    if urlparse(url).netloc != "mabhas19.ir":
        continue
    try:
        with urlopen(url, timeout=25) as r:
            data = r.read(6_000_000)
        name = hashlib.sha256(url.encode()).hexdigest()[:12] + "-" + Path(urlparse(url).path).name
        (OUT / name).write_bytes(data)
        asset_records.append({"url": url, "file": name, "sha256": hashlib.sha256(data).hexdigest()})
    except Exception as e:
        asset_records.append({"url": url, "error": str(e)})
(OUT / "index.json").write_text(json.dumps({"pages": records, "assets": asset_records}, ensure_ascii=False, indent=2))
print("Saved", len(records), "pages and", len(asset_records), "assets", flush=True)
