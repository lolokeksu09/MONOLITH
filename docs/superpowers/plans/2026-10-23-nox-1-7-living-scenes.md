# NOX 1.7 Living Scenes — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Status: PLANNED, awaiting design/plan review.** This file is an implementation plan, **not** evidence of implemented or built scenes.

**Goal:** Ship native, offline NOX 1.7 with three new premium procedural live wallpapers (AURORA, PULSE, COMET), category navigation and nine ready-made looks, preserving all NOX 1.6 behaviors and data.

**Architecture:** Extend the existing SceneConfig/SceneCatalog domain API, add lightweight catalog metadata and named preset variants, dispatch three more modes through the existing single-GLSL renderer. Compose uses the same EGL-backed LivePreview and Canvas thumbnails; no new Room tables, services or permissions.

**Tech Stack:** Kotlin 2.3.20; Jetpack Compose BOM 2025.10.01; Android SDK 36; Gradle 8.13 / AGP 8.13.2; GLES3 context with GLES2-compatible fragment shader; JUnit4 + Python unittest; GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-10-23-nox-1-7-living-scenes-design.md`.

## Global Constraints

- Base: `feature/nox-1-6-curated`; implementation branch: `feature/nox-1-7-living-scenes`; do not merge automatically into `main`.
- Existing three scenes remain byte-for-byte equivalent in shader mathematics where possible; NEBULA's `uMode=2` must not fall through to new modes.
- `SceneConfig` persisted fields are unchanged: `sceneId`, `primary`, `secondary`, `speed`, `glow`, `density`, `motion`, `seed`.
- No new root, Internet or other permissions. No new Room schema, additional concurrently active EGL previews, network content or tracking.
- NOX's studio preview remains pinned above independently scrollable editor tools. Bottom Save/PNG/Apply dock remains fixed.
- Runtime `FrameBudget.allowed` and WallpaperService visibility/sensor lifecycle remain unchanged.
- New debug package `studio.nox.light.livingscenes`, planned `versionName=1.7.0`, `versionCode=10`, artifact name `NOX-1.7-living-scenes-APK`; older packages remain installed.
- After **every patch**, update `docs/CHANGELOG.md` and relevant spec/README/tests, commit to GitHub, verify SHA. See `AGENTS.md` and `docs/DEVELOPMENT_WORKFLOW.md`.

## Review Focus

1. **Unknown scene** in `SceneConfig.normalized()` must not silently change a known new 1.7 scene to FLUX; invalid IDs retain the established fallback. Add a catalog test in Task 1.
2. **Legacy NEBULA** must render with mode 2, and AURORA/PULSE/COMET with distinct modes 3/4/5. Add structural test in Task 2.
3. **A freshly chosen style** must only change the unsaved draft; applying/exporting/editing must use that *same* draft and must not overwrite the active preset beforehand. Test in Task 4.
4. **Importing a .nox file** with 1.7 scene IDs must round-trip without missing parameters; malformed IDs must fail validation. Test in Task 1 / Task 3.
5. **Frame pacing/thermal protection** must still work, wallpaper must stop rendering when hidden; structural and on-device tests in Task 5.

---

## Task 1 — Scene catalog and compatibility (Patch 1)

**Files**
- Modify: `core/model/src/main/kotlin/studio/nox/model/SceneConfig.kt`.
- Create: `core/model/src/main/kotlin/studio/nox/model/SceneRegistry.kt`.
- Create: `core/model/src/test/kotlin/studio/nox/model/SceneRegistryTest.kt`.
- Modify: `docs/CHANGELOG.md`, spec if implementation choices change.

**Interfaces**
- `enum class SceneCategory { SIGNATURE, AMBIENT, TECH, COSMIC }`.
- `data class SceneDefinition(val id:String, val category:SceneCategory, val isNew:Boolean)`.
- `object SceneRegistry { val entries:List<SceneDefinition>; fun get(id:String):SceneDefinition? }` with definitions for 6 IDs.
- `SceneCatalog.ids` returns the ordered six IDs, preserving the original order `FLUX,HALO,NEBULA`, appending `AURORA,PULSE,COMET`.
- `SceneCatalog.default(id:String)` returns scene-specific parameter/color defaults for AURORA, PULSE, COMET without changing existing defaults.

- [ ] Add tests asserting exact six IDs, unique IDs, categories, `isNew` only for three 1.7 IDs, normalization of each new ID, and legacy defaults unchanged.
- [ ] Run `gradle :core:model:test --stacktrace`, observe red tests.
- [ ] Implement registry and defaults.
- [ ] Re-run model tests to green. Run `python3 -m unittest discover -s tests -p 'verify_structure.py' -v`.
- [ ] Update changelog with **catalog added**, tests actually run, compatibility note. Commit `feat(catalog): register AURORA PULSE COMET and scene categories`. Verify SHA.

## Task 2 — GPU modes and renderer safety (Patch 2)

**Files**
- Modify: `core/renderer/src/main/java/studio/nox/renderer/GlBackend.kt`.
- Modify: `tests/verify_structure.py`.
- Update: `docs/CHANGELOG.md`, shader design section in spec if performance-driven adjustments occur.

**Interfaces**
- Kotlin mode dispatch: `FLUX=0f`, `HALO=1f`, `NEBULA=2f`, `AURORA=3f`, `PULSE=4f`, `COMET=5f`.
- Existing GLSL uniforms retain their names; `FRAGMENT` keeps dedicated explicit branches for each mode.
- All modes are procedural, deterministic from `uSeed`, time continuous and use bounded iteration counts.

- [ ] Add red structural tests for all six `uMode` cases, separate NEBULA branch, unchanged `GL_TRIANGLE_STRIP` and GLSL uniforms.
- [ ] Run Python structural tests and confirm red.
- [ ] Implement AURORA smooth ribbons, PULSE non-strobing radial rings and COMET moving heads/trails in the shared shader. Maintain aspect ratio and `uTouch/uMotion`.
- [ ] Re-run structural tests; run CI `gradle :app:assembleDebug --stacktrace` to catch GLSL integration/code compile issues. **A passing Kotlin build does not prove GLSL compiles on the device**; verify shader logs / visual on-phone later.
- [ ] Update changelog with GPU-mode changes and known unverified device risks. Commit `feat(renderer): add three procedural living scene shaders`. Verify SHA.

## Task 3 — Nine launch looks and portable scenes (Patch 3)

**Files**
- Create: `core/model/src/main/kotlin/studio/nox/model/ScenePresets.kt`.
- Create: `core/model/src/test/kotlin/studio/nox/model/ScenePresetsTest.kt`.
- Extend: `core/model/src/test/kotlin/studio/nox/model/PresetTransferTest.kt` (already exists).
- Update: `docs/CHANGELOG.md`, user-facing preset instructions in `README.md`.

**Interfaces**
- `data class NamedScenePreset(val name:String,val config:SceneConfig)`.
- `object ScenePresets { fun forScene(sceneId:String):List<NamedScenePreset> }`.
- For AURORA: Polar, Emerald, Violet Mist; PULSE: Cyan Beat, Crimson Core, Neon Echo; COMET: Deep Space, White Tail, Blue Drift.
- Preset values use `SceneConfig` fields and colors from spec; no Room insert until user chooses to save.

- [ ] Add tests for exactly three unique names per new scene, all nine scenes' config IDs/colors/value ranges, deterministic seeds, and no unexpected edits to existing presets.
- [ ] Add `.nox` export/import round-trip tests for all three new IDs; older-version rejection of unknown IDs is an acknowledged compatibility limitation, not an implicit conversion.
- [ ] Run red tests; implement `ScenePresets`; run green tests and CI.
- [ ] Update changelog and README with compatibility/export behavior. Commit `feat(presets): add nine scene-native appearance variants`. Verify SHA.

## Task 4 — Premium navigation, previews and editor integration (Patch 4)

**Files**
- Modify: `app/src/main/java/studio/nox/app/NoxApp.kt` (`HomeScreen`, `SceneMiniature`, `Miniature`, gallery filters, preset look selector).
- Modify: `tests/verify_structure.py`.
- Update: `docs/CHANGELOG.md`, relevant spec UI section.

**Interfaces and behaviors**
- Horizontal scrollable catalog of six scene cards; do not fit six thumbnails by shrinking them inside one fixed row.
- Distinct native Canvas artwork for new scenes; never create six NoxSurfaceViews in the selector/gallery.
- Categories `Все/Signature/Ambient/Tech/Cosmic` based on registry metadata. `NEW` is true only for AURORA/PULSE/COMET.
- User can apply a named variant to `homeDraft`, immediately preview it, edit it, export PNG or explicitly save/apply it. No implicit overwrite; preserve current Quick Looks/Remix.
- Existing scene filters, search, favorites, two-column gallery, sorting, pinned Studio and fixed action dock keep working.

- [ ] Add red UI structural tests for six-scene selector scrolling, three new Canvas miniatures, category mapping, draft not auto-saved, pinned preview and action dock.
- [ ] Run red structural tests; implement UI changes in small private helpers without inventing additional activities.
- [ ] Run structural tests and Android build. Visually inspect on a real phone before treating UX/performance as verified.
- [ ] Update changelog and UI notes. Commit `feat(ui): explore and edit six scenes with premium catalog`. Verify SHA.

## Task 5 — Release qualification and APK (Patch 5)

**Files**
- Modify: `app/build.gradle.kts` (versionCode=10, versionName=1.7.0, debug applicationIdSuffix=`.livingscenes`; release application ID unchanged).
- Modify: `app/src/debug/AndroidManifest.xml` (debug label NOX 1.7 Living Scenes).
- Modify: `.github/workflows/android.yml` (artifact name).
- Update: `README.md`, `docs/CHANGELOG.md`, relevant spec/plan statuses, optionally `docs/QA_STATUS.md`.

- [ ] Review lossless legacy FLUX/HALO/NEBULA playback; confirm no Room migration and no additional Android permissions.
- [ ] Run full Python structure tests: `python3 -m unittest discover -s tests -p 'verify_structure.py' -v`.
- [ ] Run `gradle :core:model:test --stacktrace` and `gradle :app:assembleDebug --stacktrace` in GitHub Actions. Require all success and an uploaded, non-expired APK artifact.
- [ ] Verify artifact originates from the intended commit SHA; extract ZIP, inspect APK ZIP integrity, `AndroidManifest.xml`, DEX and Android app ID/version if tooling available. Never deliver a renamed or unverified path.
- [ ] Update CHANGELOG with evidence and hardware QA limitations. Commit `build(release): package NOX 1.7 living scenes preview`. Verify SHA, open/mark ready PR after review.
- [ ] Only after an on-device test, add runtime QA results (installation, 6 scenes, wallpaper/PNG, 10–15 min per new scene, screen lock/unlock and thermals) in a **separate follow-up QA commit**. Until then clearly state *not tested on device*.

## Patch completion output contract

After **each** task, send a concise report with:
`Patch` — `What changed` — `Branch` — `Commit SHA/link` — `Docs updated` — `Tests/CI evidence` — `Device limitations`.

**Never** mark a task complete from documentation or source alone. Do not merge into `main` without explicit approval.
