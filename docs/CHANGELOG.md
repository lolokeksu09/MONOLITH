# CONTEXT CAPSULE — Changelog

## 2026-10-25 — 0.11.0-preview · Global Knowledge Search

- Cross-project local full-text substring search over notes and tasks, with Unicode case-insensitive Kotlin matching.
- New AMOLED search dialog with category chips and explicit opt-in for archived projects.
- Source project name, content, and trust/completion state on each result; result cap of 80 with newest-first ordering.
- Tap a result to navigate directly to its project and matching note or task via stable record ID. No long note text is inserted into a search field.
- Search runs asynchronously on Dispatchers.IO and cancels superseded requests; Compose 180 ms debounce.
- No Room migrations, new permissions, paid API, GitHub write, or remote indexing.
- Tests: `UnifiedSearchTest`, `GlobalSearchUiTest`; existing CI includes migration/project creation/navigation tests.
- versionCode 11, versionName 0.11.0-preview. Confirm exact HEAD Actions outcome and artifact before device install.

## 2026-10-25 — 0.10.0-preview · Project Control Center

- New project settings with title/public GitHub relink, backup export, archive/unarchive and explicitly confirmed removal.
- Archive/active filters on home, archive badges and source-backed project progress.
- Switching GitHub repository atomically invalidates the prior read-only snapshot; local notes/tasks remain untouched.
- Exact-title deletion + independent acknowledgement required in UI and exact title validated again in ViewModel.
- Room v3 migration ADD COLUMN projects.archived INTEGER NOT NULL DEFAULT 0; legacy projects stay active.
- JSON backup schema remains version 1 with optional project.archived, backwards-compatible with older backups.
- Added pure catalog tests, Android migration preservation test and UI tests for archive/relink/destruction.
- No extra Android permissions, root, paid API or Github write access.
- Exact HEAD CI and APK must be verified before recommending installation.

## 2026-10-25 — 0.9.0-preview · Workspace overhaul

- Distinct Overview, Memory, and Plan project sections; the existing Android share receiver, GitHub snapshots, note approval and editing flows remain wired.
- New premium task board with completion progress, filters, quick add, and edit/delete dialogs.
- Source-of-truth local metrics per project using correlated Room COUNT queries; compact dashboard with project progress and pending trust reviews.
- True compact handoff (not the prior full export with an extra line), explicitly retaining provenance, uncertainty, confirmed decisions and open tasks. The chosen mode is applied to copy, share, preview, and Markdown export.
- Strengthened DAO isolation for decision confirmation and task completion using both active project ID and record ID.
- No Room schema migration (v2), no paid AI API, no additional app permissions.
- Tests: `WorkspaceV9Test`, `TaskBoardTest`, `WorkspaceMetricsTest` plus existing Android creation / migration / dialog suites. Build and emulator must both pass on the exact commit before publishing an APK.

## 2026-10-24 — 0.8.0-preview · Memory management

- Editable decisions/notes and tasks, with explicit validated input controls.
- Pinned note ordering independent of trust/evidence labels; only user confirmation can mark approved.
- Explicit delete-confirmation dialog; no silent deletion. All data edits/deletes scoped to active project ID.
- Editing confirmed text sets note evidence to UNVERIFIED; notes with unchanged text keep their status.
- Room v2 migration uses ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0, preserving legacy data.
- Backup JSON remains schemaVersion 1 with optional pinned field; legacy backups remain importable and imported claims remain UNVERIFIED.
- New JVM trust-and-ordering tests, instrumented edit/delete dialog tests, migration preservation test; existing project/create/share/overview smoke retained.
- Version code 8, version name 0.8.0-preview; verify exact build CI and emulator before APK announcement.

## 2026-10-24 — 0.7.0-preview · GitHub Diff Inspector

- Compares previous and current GitHub HEAD using the read-only `compare/{base}...{head}` API endpoint, only for the same branch and well-formed SHA values.
- Captures API-reported ahead/behind/diverged/identical state, sampled changed paths and +/- line counts in the existing JSON snapshot (no Room migration).
- Adds a compact expandable changed-files panel and a direct GitHub comparison link to the Overview tab.
- The human-readable exported capsule now includes comparison evidence and a source URL.
- Handles rate limits, unreachable comparisons, diverged/rewritten history and truncated file lists with explicit warnings, never invented metrics.
- Version code 7, `0.7.0-preview`; includes JVM classification tests and emulator expansion test.

## 2026-10-24 — 0.6.0-preview · Focused navigation

- Split the long project page into Overview (GitHub/commits/activity) and Memory (handoff, notes, decisions, tasks, export) using two native Compose tabs.
- Move the real existing handoff copy action into a persistent bottom bar available in either tab, including long scroll sessions.
- Remove redundant handoff-copy button in the memory card.
- Add Android instrumentation regression for navigation between sections and permanent handoff availability.
- Preserve Room schema, GitHub sync, project creation, notes, tasks and no-paid-API behavior; versionCode 6.
- Verify exact build and emulator status before sharing the new APK.

## 2026-10-24 — 0.5.0-preview · Project Pulse

- Snapshot-to-snapshot comparison of default-branch HEAD, preserving prior SHA/branch/timestamp in existing `GithubSnapshot.summary`; no Room migrations.
- New compact activity banner distinguishes first sync, changed HEAD, unchanged HEAD and branch switches.
- Deterministic follow-up suggestions for failed/running CI, open PRs and Issues; user explicitly creates a local task, never writes to GitHub.
- Deduplicates suggested tasks against already saved local titles.
- New pure `ProjectPulseTest` and Android `RepositoryOverviewUiTest` for HEAD delta and action dispatch; prior create-project smoke retained.
- Version code 5, `0.5.0-preview`; Android build and emulator results tracked in GitHub Actions.

## 2026-10-24 — 0.4.0-preview · GitHub Project Intelligence

- Read-only evidence-based repository overview: description/README, commits, default-branch CI, open PR and Issues; explicit status for unavailable and truncated data.
- Dense AMOLED dashboard above the handoff capsule, direct links to verified GitHub elements, removal of duplicate legacy snapshot UI.
- Deterministic CI interpretation (no implication that the product is finished), and readable cited briefing inside exported Markdown capsules.
- Zero paid AI/API keys, no change to Room schema and private notes not sent to GitHub.
- Tests: `RepositoryAssessmentTest` for safe CI interpretation and `RepositoryOverviewUiTest` in Android emulator; existing project creation smoke remains.
- Android versionCode 4, versionName `0.4.0-preview`. Build and on-device QA require verified CI and actual device check.

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
