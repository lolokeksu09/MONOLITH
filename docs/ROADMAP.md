# MONOLITH roadmap

Updated: 2026-11-05. The current repository baseline is **0.10.0-beta**; future entries are proposals, not implemented features.

## Delivered: 0.1–0.10 beta

- 0.1–0.3: HOME activity, app catalog, folders, pinning, persistent layout, widget hosting.
- 0.4: Portable, validated JSON home backup; quick drawer access; pinned release certificate.
- 0.5–0.6: Drawer sorting, display preferences, hidden apps, cached icons and resilient app loading.
- 0.7–0.8: Persistent Dock; system wallpaper; wallpaper-backed drawer, bottom search and gestures.
- 0.9–0.10: Persistent widget-height controls and order, safer removal, and reliable return to desktop.

See [Changelog](CHANGELOG.md) for details. Shipped features do **not** imply verified behavior on all OEM Android builds.

## Next: quality and architecture

1. Physical acceptance testing on the Lenovo Legion Y70 / Motorola Android 16 build. Record outcomes against [Device acceptance](DEVICE_ACCEPTANCE.md).
2. Split `MonolithScreen.kt` (1,331 lines) and `MainActivity.kt` (499 lines) into focused home, drawer, widget, settings and state components **without** changing existing storage formats or HOME behavior.
3. Add UI/instrumentation coverage for HOME intent, app launches, app drawer, widgets, restore and rotation.
4. Profile cold start, icon rendering, scrolling and transition jank on-device before performance claims.

## Proposed: themes

- A theme model separate from stored launcher layout, with safe defaults and explicit import/export.
- Built-in AMOLED minimalist and radial app-menu presets, inspired by customizable launchers rather than copied assets.
- Optional visual editing of clock, app-menu layout, icons and animations.
- Respect live wallpaper/gesture constraints, accessibility and user control. No root or privileged SystemUI dependency.

## Stable 1.0 acceptance gate

The launcher is not 1.0-ready until the existing CI remains green **and** the target Android device passes HOME role/restore, reboot, orientation, navigation gestures, keyboard/insets, app launching, widget providers, wallpapers, backup/import and upgrade compatibility. No claim of system lock-screen or recents replacement.
