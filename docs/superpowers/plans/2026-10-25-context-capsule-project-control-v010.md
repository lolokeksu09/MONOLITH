# CONTEXT CAPSULE 0.10 Project Control Center Implementation Plan

> **For agentic workers:** Follow development-design-skills executing-plans and test-driven-development. Each task needs a verified test and scoped commit.

**Goal:** Add safe editing, archive and deletion workflows to local project management.  
**Architecture:** pure `ProjectCatalog` rules, Room v3 migration and scoped DAO operations, ViewModel lifecycle functions, Compose management/dialogs and Home filters.  
**Tech Stack:** Kotlin, Compose Material3, Room, JUnit, Android emulator API 35, GitHub Actions.  
**Spec:** `docs/superpowers/specs/2026-10-25-context-capsule-project-control-v010.md`

## Tasks

- [x] Unit contracts for archived filtering, relink invalidation, exact-name deletion.
- [x] Domain filtering and validated lifecycle helpers.
- [x] Room v3 migration and query methods with archive flag/default.
- [x] Scoped ViewModel edit, archive/restore, delete and backward-compatible backup.
- [x] New dark management UI and exact-name deletion dialog.
- [x] Home active/archive filters and archived badges; project settings wiring.
- [x] Android instrumentation tests for dialogs and migration.
- [ ] Run JUnit, compile debug APK, and instrumented emulator tests against the exact final HEAD.
- [ ] Fix any actual CI errors; repeat only when needed.
- [ ] Verify APK artifact before claiming build success and report remaining device QA.
