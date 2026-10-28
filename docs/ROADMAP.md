# MONOLITH Roadmap

## 0.1: Bootstrap / HOME-capable alpha
- HOME intent registration and safe fallback to default home
- App catalog with launcher-visible activities only
- App launching and search
- Pinned shortcuts persisted privately
- Dark Compose layout with Russian/English resources
- Unit tests and CI debug APK artifact

## 0.2: Home usability
- Real grids, reorder, folders and drag-drop
- Broadcast-driven package refresh, cancellation-aware catalog loading
- Onboarding and choosing default home via RoleManager
- Accessibility (talkback labels, focus, target size)
- Screenshot tests on multiple devices

## 0.3: Widgets & spaces
- Android AppWidgetHost binding and picker
- Local spaces (work, gaming, daily)
- DataStore backed layout/preferences and migration tests

## 0.4: Polishing
- Real wallpaper integration
- Animation profiling and memory testing
- Backup/restore and safe schema migrations
- Android 16 Motorola physical-device validation

## Scope limits
A third-party HOME app cannot guarantee replacement of stock SystemUI, overview/recents integration, or OEM navigation animations without additional OS-level integration. No root is required for 0.1.

## Release gate
No production-ready claims until Gradle CI, emulator smoke tests, launcher role selection, reboot behavior and a physical Android 16 device pass.
