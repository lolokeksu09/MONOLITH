# NOX 1.6 Curated — premium gallery and instant looks
Date: 2026-10-23
Status: implementation brief, extending hardware-tested NOX 1.5.

## User objective
Advance NOX's native AMOLED appearance while adding genuinely useful, offline features. Preserve prior versions and current scene pipeline; no root, networking or new permissions.

## Alternatives
- New GPU shader modes: more risk to performance and battery; not chosen.
- Online cloud gallery: contradicts offline-first and requires authentication; rejected.
- Selected: Favorite presets and a Quick Looks palette selector on Home; distinctive UI polish without renderer changes or database schema migration.

## Experience
1. Gallery: a star icon on each card toggles a favorite without opening editor. A "Избранное" filter works alongside scene-type filters, search and sorting. Favorite cards are visually identified but not gaudy. Deleting a preset cleans its favorite mark.
2. Home: a "Стили" button opens a native Material3 modal bottom sheet with five SceneLooks palettes, each shown as a two-color polished swatch. Tapping a look applies SceneLooks.apply() to the *current* displayed config as an unsaved draft (does not overwrite existing preset). Sheet dismisses after selection, fullscreen hero reacts immediately; save/edit/export/apply use the same draft. Existing Quick Remix remains.
3. Visual: preserve true #000000 background, electric cyan, brand header, pinned studio preview, 2-col art gallery; reduce chrome, emphasize artworks. Add refined card favorite affordance and concise labels, compact action row.
4. Data: favorites are a DataStore stringSetPreferencesKey of numeric preset IDs, filtered in application state. No Room migration. Prefer no new GL view.
5. Build: code9, 1.6.0, separate debug ID studio.nox.light.curated (old packages remain unchanged, no automatic migration). User-installed NOX1.5 preserved.

## Verification
Test-first structural checks for UI connections and DataStore presence; JUnit test for pure favorites filtering/ordering if moved to model (otherwise structural tests and CI), existing Kotlin model tests + Android assembleDebug. Download CI artifact and verify APK. On-phone behavior requires user QA.
