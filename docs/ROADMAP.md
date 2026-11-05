# MONOLITH roadmap

Updated 2026-11-06. Current stage: **0.15 beta in review**.

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

## In 0.15 review

- Unified orbit opening/closing motion, synchronized supporting-element fades and softened scene transitions.
- Preloaded orbit icons and gated moving touch targets to avoid first-open pop-in and overlapping taps.
- Improved hint contrast, page-indicator motion, localized accessibility semantics and minute-aligned clock updates.

## Next

1. Physical acceptance on the Lenovo Legion Y70 / Motorola Android 16 (gestures, navigation insets, HOME intents, rotation, keyboard, launch handling, media controls).
2. Improve Reference-mode icon arrangement for different screen sizes and font scales; no overlap or invisible hit targets.
3. Extend focused Compose UI tests and on-device profiling for animation and cold start.

## Stable release gate

All CI checks plus physical-device behavior, update-signing continuity and a recovery path to the stock HOME must pass before 1.0 stable.
