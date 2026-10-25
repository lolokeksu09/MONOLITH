# CONTEXT CAPSULE 0.11 Implementation Plan

**Goal:** local-first unified search and record-targeted navigation.  
**Spec:** `docs/superpowers/specs/2026-10-25-context-capsule-global-search-v011.md`.  
**Stack:** Room v3, Kotlin, Compose Material3, JUnit, Actions emulator API35.

- [x] Add unit contracts for Unicode, archived visibility, category filtering and empty/orphaned records.
- [x] Implement pure UnifiedSearch and SearchHit data with stable record and project IDs.
- [x] Add two Room read-only cross-project queries; ViewModel coroutine cancellation and search result flow.
- [x] Add native UI search dialog with category chips, local-search label and capped results.
- [x] Wire home entry point, modal search and project focus to Memory and Plan.
- [x] Focus notes and tasks by ID; restore complete record list on explicit action.
- [x] Add Android Compose UI regression for hit selection and no-results-without-query state.
- [ ] Verify JVM suite and assembleDebug for current final HEAD.
- [ ] Verify Android emulator UI suite and all migration regressions for current final HEAD.
- [ ] Record CI and artifact verification, physical device test remains pending.

**Constraints:** no Room migrations, no remote indexing, no new permission, no paid AI API, no merge into main until review.
