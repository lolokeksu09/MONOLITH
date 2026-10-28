# Changelog

## 0.10.0-beta
- Launching an app from the drawer, folder, home tile or Dock now returns MONOLITH to HOME, clearing stale drawer search and edit state
- Widget cards can be moved up or down; the ordered AppWidgetHost ID list is persisted without reallocating widgets
- Removing a widget now requires confirmation, preventing accidental loss of configured widgets
- Added regression tests for widget reordering, unchanged IDs, boundaries and persistent order
- Existing home layout, hidden apps, settings, backup format, per-widget height and permanent signing certificate stay unchanged
- Physical Android 16 HOME, gestures, widgets and OEM recovery remain a separate acceptance gate

## 0.9.0-beta
- Widget resizing controls: 128–512 dp heights in 64 dp increments, persisted per widget and restored after launcher restart
- Android AppWidgetHostView receives updated size options so responsive providers can render layouts appropriately
- Removed widgets also delete stored dimensions; widget IDs and sizes are never exported across installations
- Fixed invisible app filtering when the bottom search bar is disabled
- App drawer offers pull-down close gesture from its header, clear-search affordance and returns grid to the top after sorting/filtering
- Added focused JVM tests for resize bounds, height stepping and hidden search invariants
- Retained the existing Android HOME role, pinned apps, Dock, sorting, wallpaper and permanent signing certificate
- Still Beta until Lenovo Legion Y70 / Motorola Android 16 touch, wallpaper, IME and widget acceptance tests are performed

## 0.8.0-beta
- Redesigned All Apps as a full-height, wallpaper-backed drawer with a 4-column default and a compact header
- Search box moved to a bottom pill; Settings controls whether that search bar is displayed
- Added 25–100% drawer background opacity setting, separate from the HOME wallpaper preference
- Added an accessible swipe-up handle just above the Dock; the clock gesture and app button remain usable
- Android HOME intents reliably return to the desktop rather than leaving settings or drawer visible
- Saved drawer controls locally and in portable backups, with safe defaults for old backups
- Declared adjustResize to keep the bottom search usable with the keyboard
- Added backup and bounds regression tests; pinned signing certificate unchanged
- Beta pending physical device gestures, IME and wallpaper acceptance

## 0.7.0-beta
- Added a persistent 5-slot bottom Dock, independent from scrolling home content
- Long-press an application in All Apps and choose Add to Dock; long-press Dock icon to remove or move it
- Added opt-in Android system wallpaper rendering (static and live wallpaper), keeping AMOLED pure-black as the default
- Saved Dock order and wallpaper preference locally and in portable backups
- Existing backup versions 0.4–0.6 still import, with an empty Dock and wallpaper disabled by default
- Added dock capacity, order and backup rejection tests
- Existing signing certificate pinned to published 0.3.1–0.6 releases
- Beta release only; physical Lenovo/Motorola OEM acceptance remains mandatory before 1.0 stable

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
