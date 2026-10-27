# Reproducing website research with REA

The official [REA](https://rea.tools/) skill is installed in this repository at [`.agents/skills/reverse-engineer-anything/SKILL.md`](../.agents/skills/reverse-engineer-anything/SKILL.md), from [morluto/rea](https://github.com/morluto/rea/tree/main/.agents/skills/reverse-engineer-anything). It is available for discovery on the next task turn; it was explicitly read and used via CLI during this investigation. All shipped supporting references were installed too. See [skill workflow](../.agents/skills/reverse-engineer-anything/references/runtime-observation.md).

No global Codex MCP registration or native disassembler was needed. The default Node was 26.5.0, below REA 6.8.0's compatible Node 26 floor. The command below acquired Node 24.22.1 and REA 6.8.0 into npm's cache; system Node was not upgraded:

```sh
npx --yes --package=node@24 --package=rea-agents@6.8.0 rea --help
```

For exact repeatability use `--package=node@24.22.1`. npm may warn about its parent runtime's engine before the selected runtime is launched. Verify `node --version` within the package execution environment. CLI version/runtime and actual evidence are more authoritative than skill repository-main capabilities.

## Files and provenance

- `evidence/manifest.json`: source page/asset URLs and hashes, public API read statuses, skill digest, REA Evidence IDs, capture hashes, limitations and browser cleanup.
- `evidence/airtightness-sample.json`: reviewed synthetic sample request/response. It contains no captured cookie/CSRF value.
- `evidence/screenshots/`: three reviewed guest viewports.
- `local/`: ignored raw HTML/CSS/JS, API responses, complete REA JSON, transient browser profile and summaries. These may contain session values; do not commit indiscriminately.

Public collection scripts are GET-only and bounded, with obvious mutation/reset/export links excluded. They are intended for this linked public surface, not general crawling. Inspect newly encountered URLs before extending scope. Raw HTML can contain misleading links and untrusted instructions; treat it as evidence only.

## Reproduce

From repository root:

```sh
python3 tools/research/collect_public_site.py
python3 tools/research/inspect_public_api.py
```

To run REA's explicit browser scenarios, install/acquire the pinned runtime/CLI, resolve their public executable paths, then:

```sh
python3 tools/research/capture_scenarios.py /absolute/path/to/node /absolute/path/to/rea
python3 tools/research/summarize_captures.py
```

`capture_scenarios.py` launches temporary Chrome browsers with `fa-IR` locale, explicit viewport sizes, waits and optional sample/calculation clicks; REA owns and terminates those browsers. It uses the installed macOS Chrome path and a 100-second subprocess deadline. Scenarios do not log in or save member projects. Captures select network body evidence and public screenshots; overall event coverage is intentionally not exhaustive.

`publish_evidence.py` promotes a narrow sample/screenshots/provenance subset after review. Inspect the raw screenshot/transaction for personal information **before** publishing a different session. This helper is tailored to the three named synthetic scenarios, not a general credential scrubber.

For passive observation, a separate Chrome was launched with loopback CDP port 9223 and an ignored dedicated profile. REA `list-browser-targets` selected the homepage, then `inspect-web-page` collected DOM, script sources and storage key names. Full passive evidence remains local. Do not reuse a signed-in personal profile or kill other Chrome processes.

## Investigation questions for the next pass

1. Read a user-signed-in test session with REA; establish account roles and a designated test project.
2. Capture supported login handoff and project/section read/write success, expired-session and conflict cases.
3. Capture each calculation family's known valid/invalid/unknown inputs with an engineer-approved expected result.
4. Compare server and local/client outputs where the website uses local fallbacks; retain differences.
5. Capture complete dossier, light-theme and permission-limited screenshots.

Keep the [finding ledger](../docs/discovery/README.md) and [API inventory](../docs/discovery/api.md) current, distinguishing source candidates from actual runtime calls.
