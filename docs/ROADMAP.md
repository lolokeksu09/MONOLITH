# MONOLITH roadmap

Updated 2026-11-11. Current stage: **0.16 / INK merged to `main` via PR #35; exact-head PR CI passed; physical acceptance open**. `v0.15.0-beta.380` is the last release recorded in these documents; verify current signed assets via [GitHub Releases](https://github.com/lolokeksu09/MONOLITH/releases).

## Implemented

- Android HOME registration, app catalog, launches, sorting, hiding apps and launcher-only usage history.
- Signed beta releases with pinned certificate verification and no root/network requirement.
- Reference design: AMOLED minimal, animated orbital app cluster and editorial weekday screen.
- 0.12: removed Classic HOME, Dock, folders, widgets and old layout backup UI. Reference home is the only desktop.

## Delivered in 0.13 beta

- Rebuilt the reference-style editorial backdrop with an offline bundled rocky image and precise grayscale layout.
- Redesigned orbit motion, screen transitions, media buttons, accessibility and app shortcuts.
- Added a local favorite selector and ordering for up to 15 apps.

## Delivered in 0.14 beta

- Fixed clipped vertical rails and media text using safe measured layouts.
- Smoothed orbit and scene motion and reduced unnecessary visual weight around icons.

## Delivered in 0.15 beta

- Unified orbit opening/closing motion, synchronized supporting-element fades and softened scene transitions.
- Preloaded orbit icons and gated moving touch targets to avoid first-open pop-in and overlapping taps.
- Improved hint contrast, page-indicator motion, localized accessibility semantics and minute-aligned clock updates.
- Published `v0.15.0-beta.380` from main revision `5897868f`; CI and permanent signing succeeded. Physical-device acceptance remains open in [the checklist](DEVICE_ACCEPTANCE.md).

## Merged in 0.16 / INK (PR #35)

The authoritative scope, REQ-ID registry, decisions and evidence states live in [the 0.16 implementation plan](PLAN_0.16.md). The changes are in `main`; merge is not proof of a signed release or device acceptance:

1. Original / Mono / INK application-icon appearances with native Android 13+ monochrome layers and Original fallback.
2. Rounded launcher-control vectors and density-aware application icon decoding within a bounded cache.
3. Persisted 8 / 12 / 15 orbit densities while preserving favorites, hidden apps and safe touch targets.
4. Simplified editorial copy and a consistent All Apps header, labeled search and two-line names.
5. Honest media status without fake metadata or new permissions; existing HOME setup and stock recovery paths remain.
6. Russian/English resources and focused JVM tests, with physical accessibility and performance checks still open.

## Next verification work

1. PR #35 merged on 2026-11-07 after successful exact-head verification. Check signed release assets, version and certificate separately; do not infer availability from merge.
2. Test an exact, correctly signed 0.16 APK on Lenovo Legion Y70 / Motorola Android 16: all icon modes/fallbacks, all densities, persistence, first/interrupted opening, TalkBack, reduced motion, fonts, IME, media and HOME recovery.
3. Retain applicable open 0.15 physical checks and record evidence against the exact candidate rather than copying prior assumptions.
4. Review [draft PR #36](https://github.com/lolokeksu09/MONOLITH/pull/36) (0.17 search, accessibility and UI tests) separately; it is not merged.

## Stable release gate

All CI checks plus physical-device behavior, update-signing continuity and a recovery path to stock HOME must pass before 1.0 stable.
