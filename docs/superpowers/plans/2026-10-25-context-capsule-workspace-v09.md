# CONTEXT CAPSULE 0.9 Workspace Implementation Plan

> **For agentic workers:** Use the development-design-skills execution and verification checklists. Implement each task with test, implementation, verification and a scoped commit.

**Goal:** Deliver a usable workspace, separate task plan and evidence-preserving brief/full handoff without changing Room schema.  
**Architecture:** Compose presentation backed by existing StateFlow ViewModel and Room DAO. Pure domain calculations for task triage and handoff formatting; correlated SQL counts for dashboard metrics.  
**Tech Stack:** Kotlin, Jetpack Compose Material3, Room 2.8.5, JUnit, Android emulator API35, GitHub Actions.  
**Spec:** `docs/superpowers/specs/2026-10-25-context-capsule-workspace-v09.md`

## Global constraints

- No changes to `main`, PR stays draft until verification.
- No paid API or GitHub writes; preserve local projects and Room v2.
- Use explicit provenance and manual copy/share; never silently overwrite a backup.

## Tasks

- [x] Write failing JUnit contract for progress metrics, task scopes and compact handoff.
- [x] Implement deterministic `WorkspaceV9` domain functions and actual compact composer output.
- [x] Create a dedicated Compose task board and Android UI regression for filters and quick add.
- [x] Add Room project summary query and scope confirm/toggle operations by active project.
- [x] Split project presentation into Overview, Memory and Plan; add compact/full bottom selector.
- [x] Propagate handoff mode through clipboard, system share, preview and Markdown save.
- [x] Add local statistics to dashboard hero and project cards.
- [x] Add instrumented Room test for cross-project isolation.
- [ ] Confirm JUnit + APK compilation on exact release SHA.
- [ ] Confirm all Android UI and migration tests on exact release SHA.
- [ ] Inspect published artifact and record pass/fail evidence in PR #15.
- [ ] Run physical Android 16 visual/accessibility smoke; do not infer from emulator.
