# CONTEXT CAPSULE — Changelog

## 2026-10-24 — 0.3.0-preview · Search & Share Inbox

- Native text sharing receiver (`ACTION_SEND` + `text/plain`), user reviews received text and selects a destination project before saving.
- Every external share is marked `UNVERIFIED` and can be confirmed later; 20,000-character bound avoids silently truncated project memory.
- Search across local projects by title/repository, with compact returning-user dashboard.
- Search decisions within the active project, and filter those that need confirmation.
- Added `WorkspaceToolsTest` covering case-insensitive search, evidence filtering, incoming text validation.
- Version bump to `0.3.0-preview` (code 3), Room schema unchanged. CI and device verification are separate.
- Added custom vector adaptive launcher icon matching the capsule visual identity.

## 2026-10-24 — 0.2.0-preview · AMOLED UX redesign

- Fixed unreadable black title on near-black background by using explicit high-contrast UI colors, supported by palette contrast tests.
- Replaced plain home with custom native capsule illustration, stronger typography, Russian copy, first-project onboarding and substantive project cards.
- Replaced old project detail UI with coherent premium workspace: confirmed decisions, task status, GitHub snapshot, capsule preview and export.
- Kept Room persistence, read-only GitHub sync, JSON import/export and manual ChatGPT handoff wired to existing implementation.
- Incremented Android versionCode to 2 and versionName to `0.2.0-preview`.
- No new app permissions, telemetry or paid AI API.
- Validation: APK only considered ready after successful Android CI for exact release candidate SHA. Device UX remains to be checked on Lenovo Legion Y70 2026.

## 2026-10-24 — Android 0.1.0 implementation branch (unverified build)

- Privacy refinement: JSON backup now requires explicit plaintext warning/confirmation; document import reads are bounded to 1 MB.

- Started native Kotlin/Jetpack Compose application in `feature/context-capsule-v1` with new Android package `dev.lolokeksu.contextcapsule`.
- Added Room project/decision/task/snapshot entities; all imported notes start unverified; explicit owner confirmation is required.
- Implemented public GitHub read-only synchronization with HEAD SHA, commits, PR and Actions summary (partial responses marked).
- Added deterministic Markdown capsule formatting, JUnit contract tests, a premium AMOLED projects UI, manual copy/share, document export and versioned JSON backup import.
- Added Android GitHub Actions workflow for unit tests and debug APK. CI, actual device installation, instrumentation tests and real APK publication are **not yet verified**.
- No paid AI API, root, backend or access to ChatGPT private history. No modifications to `main`.

## 2026-10-24 — repository documentation alignment

- Rechecked GitHub after legacy cleanup: default branch `main` contains only five documentation/configuration files, and no Android source or CI workflow.
- Verified that branch cleanup left only `main` and there are zero open pull requests at the time of inspection.
- Updated `README.md` and `AGENTS.md` to reflect real current scope, limitations, project identity, and product development rules.
- Added the phased Android v1 implementation plan with precise test targets and explicit review/approval gate.
- Still **no runnable Android application, test execution, GitHub Actions APK, release, or renamed repository**.

## 2026-10-24 — repository cleanup

- Removed the previous Android application's source, Gradle modules, tests, assets and workflow from the current branch tree.
- Retained the CONTEXT CAPSULE specification and basic contributor guidance.
- Git commit history has not been rewritten; older source may be reachable through historical commits.
- No code compilation or APK was claimed.
