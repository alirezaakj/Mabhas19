# Mechanical checklist workspace

The Android `feature:mechanical` module implements the mechanical domain view for F15 in edition `1399-v4`. It reproduces the page's six topic groups, source clause identifiers and wording, technical fields, and evidence/status controls. The public mechanical page supplied 24 static clause cards; `mep-checklist65.js` injects a separate evaporative-cooler control into this view, so the Android catalogue contains 25 cards and 59 atomic prompts.

Answers default to unknown. The editor stores project-bound statuses (`unknown`, `compliant`, `noncompliant`, `not_applicable`), drawing/document references, notes and technical values locally. They are a draft for review, not a certification or regulatory approval. The app does not call A15; that endpoint evaluates the shared chapter engine and its server payload is not the Android draft schema.

The projection is stored under `arch_chapter45` in an explicitly versioned native envelope (`nativeWorkspaceSchema: 1`, `edition: 1399-v4`, `projections.mechanical`). Other projections and unknown envelope properties are preserved on save. A draft with an unrecognized format or a stale local revision stays read-only, avoiding a destructive overwrite. Context values are shown from the selected project when present; missing context keeps applicability unknown.

The source catalogue is derived from a public guest response and the served JavaScript as text; the scripts were statically inspected and not executed in the research environment. No member project, invitation, server draft or live calculation was changed. Raw page and script captures remain in ignored `research/local/mechanical/` and are not part of the published evidence.

This native screen has not been visually compared with the live Chrome tab. Only the mechanical domain is implemented here; electrical and renewables remain separate F15 work, and the broader F16 chapter evaluator remains unimplemented.
