# NOX 1.3 Signature Studio Implementation Plan
> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement task-by-task. Steps use checkbox syntax.

**Goal:** Reliable history and precision editing with enhanced AMOLED presentation.
**Architecture:** Pure immutable model EditorHistory; Compose session state and slider gesture callback; no persistence migrations.
**Tech Stack:** Kotlin 2.3.20, Compose BOM 2025.10.01, Android SDK 36, OpenGL ES, JUnit, Python unittest, GitHub Actions.
**Spec:** docs/superpowers/specs/2026-10-23-nox-1-3-signature-design.md

## Global constraints
- No Room/GL/WallpaperService changes.
- Keep existing features and pinned preview intact.
- Separate preview ID studio.nox.light.signature; version 1.3.0 code 5.
- Test-first: watch failing JUnit model tests, then implement model. Watch failing UI structural check, then update UI.

## Task 1: History model
**Files:** core/model/src/test/kotlin/studio/nox/model/EditHistoryTest.kt, core/model/src/main/kotlin/studio/nox/model/EditHistory.kt
- [ ] Red tests for branch clearing, dedup, undo/redo, limit, sliders commit, null safety.
- [ ] Run failing JUnit CI.
- [ ] Implement immutable EditHistory with current, past, future, record, undo, redo and max depth.
- [ ] Green JUnit CI.

## Task 2: Studio editor and premium controls
**Files:** tests/verify_structure.py, app/src/main/java/studio/nox/app/NoxApp.kt
- [ ] Red UI regression tests for history, gesture commit, pinned one GL surface, precision steppers, visual hierarchy and preserved actions.
- [ ] Rewire editing through onEdit/onLiveEdit/onLiveCommit; reset when opening new preset.
- [ ] Add undo/redo buttons with disabled states in a compact toolbar; allow continuous preview on slider drag.
- [ ] Rewrite Parameter as precision control +/- 1%, clamping, separate slider onValueChangeFinished.
- [ ] UI polish: grouped editor sections, refined labels; no excessive effects.
- [ ] Green CI.

## Task 3: Packaging
**Files:** app/build.gradle.kts, app/src/debug/AndroidManifest.xml, .github/workflows/android.yml, README.md
- [ ] Set independent debug ID, version, preview name and artifact.
- [ ] Verify latest CI all green; download ZIP and inspect APK.
- [ ] Request review via GitHub PR; preserve main and 1.2 branches.
