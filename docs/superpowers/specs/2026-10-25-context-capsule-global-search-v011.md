# CONTEXT CAPSULE 0.11 — Global Knowledge Search

**Date:** 2026-10-25  
**Target:** feature/context-capsule-v1; Room v3; native Kotlin/Compose.

## User experience

An app-wide search panel is reachable from the home screen without opening a particular project. Typing a phrase searches note bodies and task titles in all active projects. Three chips filter All / Notes / Tasks. An opt-in control includes archived projects. Each result displays source project, excerpt and existing trust/task status; opening it navigates to the exact note in Memory or task in Plan, with a clear 'show all' exit. Empty queries and no-result states are explicit; results max 80 by newest timestamp.

## Data boundary

No internet access or AI model is used. Room DAO reads local notes and tasks only on demand. Search matches are determined by Kotlin `contains(ignoreCase=true)` for Cyrillic case insensitivity; this is simple substring search, not stemming/fuzzy ranking. Only existing project IDs are eligible. Archived project content is excluded unless the user opts in. The selected record is linked by stable ID; no stale textual query or note body is used as a synthetic primary key.

## Integration

A pure `UnifiedSearch` domain object is tested in JUnit. The ViewModel cancels previous searches and runs reads off the main thread. Compose Dialog presents filters and a LazyColumn, opened from Home and closed on selection or dismissal. The selected hit opens the project with an initial section and a single-record context banner; all original editing and local-scoped operations remain.

## Acceptance

- Cyrillic uppercase/lowercase search works without confusing Cyrillic АПК with Latin APK.
- Archived notes never appear unless opted in.
- Category filters and 80-result limit apply consistently.
- One tap opens the matching record, not another record of identical text.
- No Room schema migration and no private network uploads.
- JUnit, Android UI tests and debug APK build pass for the exact feature SHA; physical Android 16 QA remains separate.
