# Changelog

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
