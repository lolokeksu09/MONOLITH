# MONOLITH roadmap

Updated 2026-11-05. Current stage: **0.13 beta**.

## Implemented

- Android HOME registration, app catalog, launches, sorting, hiding apps and launcher-only usage history.
- Signed beta releases with pinned certificate verification and no root/network requirement.
- Reference design: AMOLED minimal, animated orbital app cluster and editorial weekday screen.
- 0.12: removed Classic HOME, Dock, folders, widgets and old layout backup UI. The Reference home is now the only desktop.

## Delivered in 0.13 beta

- Rebuilt the reference-style editorial backdrop with an offline bundled rocky image and precise grayscale layout.
- Redesigned the orbit motion, screen transitions, media buttons, accessibility and app shortcuts.
- Added a local favorite selector and ordering for up to 15 apps.

## Next

1. Physical acceptance on the Lenovo Legion Y70 / Motorola Android 16 (gestures, navigation insets, HOME intents, rotation, keyboard, launch handling, media controls).
2. Improve Reference-mode icon arrangement for different screen sizes and font scales; no overlap or invisible hit targets.
3. Add a dedicated, opt-in favorites editor for the orbit without resurrecting the Classic Dock/grid system.
4. Extend focused Compose UI tests and on-device profiling for animation and cold start.

## Stable release gate

All CI checks plus physical-device behavior, update-signing continuity and a recovery path to the stock HOME must pass before 1.0 stable.
