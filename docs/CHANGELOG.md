# Changelog

## 0.16.0-beta
- Adds Original, Mono and INK application-icon appearances across orbit, editorial shortcuts and All Apps; INK uses native Android 13+ monochrome artwork when available and original artwork otherwise.
- Adds persisted 8 / 12 / 15 orbit densities without truncating saved favorites, changing their order or bypassing hidden-app exclusions.
- Decodes density-aware icons off the main thread and separates cache entries by app, appearance and size within the existing 8 MiB bound.
- Uses a consistent rounded vector family for launcher-owned Back, Settings, search, clear, sorting, close and media controls while retaining accessible labels and 48dp targets.
- Simplifies editorial copy to one localized oversized day abbreviation and aligns All Apps with a compact editorial header, labeled search and two-line application names.
- Replaces active-player wording with honest system-media-key status; adds no notification or media permission and preserves the existing HOME role flow.
- Adds JVM coverage for preference defaults, density normalization, favorite preservation and all three orbit geometries.
- Implementation is on a feature branch pending CI and physical-device acceptance; merge, signed release and install-over-0.15 verification are not claimed.

## 0.15.0-beta
- Unified orbit expansion and collapse under one no-bounce spring, removing the index stagger and the abrupt animation-spec switch.
- Synchronized the center trigger with orbit progress and added soft close-label fades; the closed trigger is slightly easier to see.
- Reduced scene travel and matched incoming/outgoing fades for calmer page changes.
- Preloads the selected orbit icons off the main thread before the first expansion to avoid first-open placeholder pop-in.
- Keeps app touch targets disabled until expansion separates them, with boundary regression tests and unchanged 48dp targets.
- Crossfades the page indicator and raises action-hint contrast to 4.63:1 on black without brightening decorative rails.
- Adds localized orbit expanded/collapsed state and explicit open/action labels for TalkBack in Russian and English.
- Aligns clock refreshes to minute boundaries instead of recomposing HOME every 15 seconds; remembers orbit geometry by app count.
- Preserves application ID, signing configuration, HOME behavior, local settings, favorites, hidden apps and launch statistics. Physical-device motion acceptance remains required.

## 0.14.0-beta
- Fixed clipped vertical time/date and hold-hint rails with measured rotated bounds that stay inside the safe area.
- Removed the fixed-width media label and allowed the player hint to wrap instead of cutting off text.
- Reworked orbit expansion with a no-bounce spring, softer stagger and scale, a shorter controlled collapse, and symmetrical scene fades.
- Reduced visual weight around orbit icons, editorial shortcuts and navigation labels while preserving 48dp hit targets.
- Slightly opened the editorial gradients so the bundled rock texture remains readable.
- Preserved application ID, permanent signing configuration, HOME role, local settings, favorites and hidden-app data; physical-device visual and gesture acceptance remains required.

## 0.13.0-beta
- Replaced the 0.12 placeholder editorial mountain with a bundled, original grayscale cliff texture (offline, no borrowed TikTok artwork or wallpaper).
- Redesigned the Reference UI around the clip: true AMOLED black, refined vertical time/date typography, clean media transport icons, monochrome editorial quick launches and a more deliberate 15-app honeycomb orbit.
- Added a spatial expansion/collapse motion with stagger, page transitions and system disabled-animation support.
- Added a persistent orbit favorites picker (max 15, explicit order), hidden-app protection, and regression tests for spacing, ordering, bounds and collapsed hit targets.
- Moved app-drawer search to the top and refined the drawer grid and type sizing. Existing app ID, signing, Android HOME role and previous user preferences are preserved.
- Automated tests and signing are CI-gated; physical Lenovo Legion Y70 / Motorola Android 16 acceptance remains required.

## 0.12.0-beta
- Reference HOME is the only desktop; removed Classic HOME, Dock, pinned/folder UI, AppWidgetHost integration and obsolete backup/import controls.
- Removed obsolete home/widget persistence implementations and obsolete tests; old local data is not cleared during an upgrade.
- Retained Android HOME selection, app catalog, drawer, search, sorting, hidden applications, launch stats and media controls.
- Reference shortcuts now favor successful launches from MONOLITH, with alphabetical fallback.
- Simplified drawer settings and refreshed Russian/English resources and Android 16 device acceptance criteria.
- Kept application ID and permanent signing certificate unchanged; still beta pending device QA.

## 0.11.0-beta
- Added native Reference HOME mode inspired by the supplied TikTok clip: AMOLED minimal landing, touch/hold orbital app cluster, swipe to editorial weekday/artwork page and functional app shortcuts.
- Preserved full All Apps, settings, HOME role, widgets and classic layout through an explicit Reference HOME switch.
- Added active media transport controls (play/pause, next) without requiring notification access; player metadata is not accessible without an opt-in notification listener.
- Deterministic orbit ordering: Dock, pinned apps, then visible installed apps; hidden unpinned apps stay hidden.
- Added durable settings/JSON backup compatibility and JVM regression tests, preserving existing backup format version.
- Still beta pending Motorola Android 16 on-device review for gestures, playback dispatch and touch interactions.

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
