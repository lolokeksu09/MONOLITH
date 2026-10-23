# NOX 1.2 Palette Lab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add deterministic color harmony, before/after comparison, and adjustable pinned preview while retaining offline GL wallpaper/editor.

**Architecture:** ColorHarmony is a pure core:model component; Compose StudioScreen uses its API and local state for compare/size; preserve NoxApp screen API and Room schema.

**Tech Stack:** Kotlin 2.3.20, Jetpack Compose BOM 2025.10.01, JUnit4, Python unittest, Gradle 8.13, Android API 36.

**Spec:** docs/superpowers/specs/2026-10-23-nox-1-2-palette-lab-design.md

## Constraints
- No new Android permissions, schema changes or renderer changes.
- Original preview 1.1.1 and stable 1.0 remain installable side-by-side.
- Publish new debug package studio.nox.light.previewlab / NOX 1.2 Palette Lab, versionCode 4.

## Task 1 — Color harmony domain
**Files:** core/model/src/test/kotlin/studio/nox/model/ColorHarmonyTest.kt; core/model/src/main/kotlin/studio/nox/model/ColorHarmony.kt.
- [ ] Write red tests for mode determinism, 24-bit RGB, neutral colors and preservation of non-color scene parameters.
- [ ] Observe fail in CI :core:model:test.
- [ ] Implement pure Hue/Saturation/Value rotation; no Android graphics dependencies.
- [ ] Verify green JUnit run.

## Task 2 — Pinned preview usability
**Files:** tests/verify_structure.py, app/src/main/java/studio/nox/app/NoxApp.kt.
- [ ] Add structural test for independent scroll, preview toggle, compare restoring config, harmony UI.
- [ ] Observe test failure.
- [ ] Implement two-state preview size and factory-original compare in StudioScreen, keeping one GL view.
- [ ] Add three harmonic swatches and explanatory copy; no other screen regression.
- [ ] Verify green tests and compile.

## Task 3 — preview build and handoff
**Files:** app/build.gradle.kts, app/src/debug/AndroidManifest.xml, .github/workflows/android.yml, README.md.
- [ ] Update preview versionName/code and applicationIdSuffix, label, artifact name.
- [ ] Build via GitHub Actions, review logs and make targeted fixes, verify APK artifact exists.
- [ ] Leave main intact and deliver verified side-by-side preview.
