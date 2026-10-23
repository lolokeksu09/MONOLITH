# NOX 1.3 — Signature Studio design
Date: 2026-10-23

## Goal
Improve the premium offline AMOLED editing experience without changing the working EGL renderer, pinned preview, Room database, wallpaper service or JSON preset structure.

## Design approaches evaluated
- New GLSL effect suite: visually striking but risks device thermals and current renderer quality.
- Cloud assets/galleries: adds servers and permissions, contradicts private/offline-first NOX.
- **Signature Studio (selected)**: editorial UI polish and reliable creative editing (history + precision controls) within existing Compose flows, testable on CI.

## User experience
- **Undo / Redo:** clear, accessible actions near pinned live preview. One history entry for each discrete look, swatch, random variant, harmony, reset, or HEX change. Dragging a slider previews continuously, but commits one history entry when finger releases (not one per motion frame).
- **Reset** is undoable; opening a new scene/preset resets the history to that scene. New edits after Undo clear redo. Maximum history depth 40.
- **Precision tuning:** each motion parameter gets +/- buttons stepping 0.01 and a restrained progress value with the slider. Buttons clamp to 0-1, slider preview updates immediately. Fine adjustments push a single edit entry.
- **Visual identity:** jet-black AMOLED, cyan highlights, better grouped control hierarchy and an inset action toolbar with status; avoid heavy layered glass, default gradients on every card and excessive animation. Pinned GL area retains exactly one NoxSurfaceView and does not scroll.
- Existing ability to compare factory scene, compact preview, color harmonies, library filters, save/apply/export remains.
- No network, root, permissions, schema changes.
- New debug package studio.nox.light.signature (side-by-side with old debug builds due changing GitHub Actions signing certificate); NOX 1.3 Signature; versionCode 5.
- GitHub Actions: structural checks, JUnit model tests, assembleDebug and artifact NOX-1.3-signature-APK.
- Hardware QA remains necessary for actual responsiveness, battery, UI fit.

## Correctness criteria
- Undo/redo branches, max 40 entries, no-op deduplication, limit-safe precision math.
- Continuous live preview during slider gesture, single history step on release; undo returns to before drag.
- Pinned preview unchanged, zero extra GL contexts.
- 1.2 remains unmodified in its source branch; NOX 1.3 only in dedicated feature branch and PR.
