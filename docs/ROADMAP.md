# MONOLITH roadmap

Updated 2026-11-06. Current stage: **0.15.0-beta.380 released; physical acceptance open. 0.16 / INK documented, not implemented**.

## Implemented

- Android HOME registration, app catalog, launches, sorting, hiding apps and launcher-only usage history.
- Signed beta releases with pinned certificate verification and no root/network requirement.
- Reference design: AMOLED minimal, animated orbital app cluster and editorial weekday screen.
- 0.12: removed Classic HOME, Dock, folders, widgets and old layout backup UI. The Reference home is now the only desktop.

## Delivered in 0.13 beta

- Rebuilt the reference-style editorial backdrop with an offline bundled rocky image and precise grayscale layout.
- Redesigned the orbit motion, screen transitions, media buttons, accessibility and app shortcuts.
- Added a local favorite selector and ordering for up to 15 apps.

## Delivered in 0.14 beta

- Fixed clipped vertical rails and media text using safe measured layouts.
- Smoothed orbit and scene motion and reduced unnecessary visual weight around icons.

## Delivered in 0.15 beta

- Unified orbit opening/closing motion, synchronized supporting-element fades and softened scene transitions.
- Preloaded orbit icons and gated moving touch targets to avoid first-open pop-in and overlapping taps.
- Improved hint contrast, page-indicator motion, localized accessibility semantics and minute-aligned clock updates.
- Published `v0.15.0-beta.380` from main revision `5897868f`; CI and permanent signing succeeded. Physical-device acceptance remains open in [the 0.15 checklist](DEVICE_ACCEPTANCE.md).

## Planned for 0.16 / INK

The authoritative scope, REQ-ID registry, open decisions and evidence states live in [the 0.16 implementation plan](PLAN_0.16.md). The following is only a summary, not a claim that these features exist:

1. Add Original / Mono / INK application-icon appearances with safe fallbacks and consistent behavior across HOME, editorial shortcuts and the drawer.
2. Use one Material Symbols Rounded family for launcher controls and improve icon optical consistency and density-aware clarity.
3. Add persisted 8 / 12 / 15 orbit densities while preserving existing favorites, hidden apps and safe touch targets.
4. Simplify editorial-page copy and align All Apps typography, search and labels with the Reference visual system.
5. Make media and HOME-setup presentation contextual without fake metadata, new media permissions or loss of stock-HOME recovery.
6. Recheck accessibility, reduced motion, Russian/English layouts and real-device performance before calling the visual stage accepted.

## Next verification work

1. Complete physical acceptance on the Lenovo Legion Y70 / Motorola Android 16 for the released 0.15 APK: gestures, navigation insets, HOME intents, rotation, keyboard, launch handling, media controls and motion.
2. When 0.16 implementation starts, extend focused Compose/JVM tests and on-device profiling for every changed path; keep its requirement states in the single 0.16 registry rather than copying them here.

## Stable release gate

All CI checks plus physical-device behavior, update-signing continuity and a recovery path to the stock HOME must pass before 1.0 stable.
