# Changelog

## 0.6.0-alpha
- Apps can be hidden from the drawer via long-press; the Hidden Apps screen in Settings restores installed apps
- Hidden app IDs persist after restarts and round-trip through home backups (older backup formats still import)
- App icons are decoded off the main thread and cached in a size-limited in-memory LRU
- Package changes invalidate cached icons to avoid stale app art
- Transient package-catalog errors preserve the last successfully loaded app list and show a retry rather than blanking shortcuts
- New unit tests for visibility, catalog refresh recovery and backup compatibility
- Permanent release signing remains certificate-pinned to the 0.3.1–0.5 signer

## 0.5.0-alpha
- All Apps menu: A–Z, Z–A, most frequently launched, recently opened, newest and oldest installation order
- Local-only launch counting for the frequent/recent modes (no Android Usage Access permission)
- Independent app drawer density (3–6 columns), labels, pin markers, and 40/48/56dp icon sizing
- Sort preference and drawer settings persist through app updates and home backup export/import
- Backwards-compatible import of existing 0.4 home backups
- New sorting and backup regression tests
- Signed GitHub Releases use the same pinned signing certificate

## 0.4.0-alpha
- Added validated home backup export/import through Android's system file picker.
- Portable backups preserve pinned app order, folder contents and grid preferences.
- Invalid/oversized backups are rejected without replacing existing desktop.
- Restored swipe-up navigation on the clock area without blocking widget touches.
- Added more focused JVM tests for backup round-trips and invalid files.
- Pinned stable release-signing certificate fingerprint in GitHub Actions; accidental key rotation fails CI.
- Retained Android package ID, HOME role flow, folders, app widgets, and existing local layout storage.

## 0.3.1-alpha
- Enabled permanent APK release signing with owner-controlled GitHub Actions secrets.

## 0.3.0-alpha
- Persisted home folders, rearrangeable icons and Android AppWidgetHost integration.

## 0.2.0-alpha
- Improved home layout, app actions, app grid settings and app list refresh.

## 0.1.0-alpha
- First native Compose HOME launcher.
