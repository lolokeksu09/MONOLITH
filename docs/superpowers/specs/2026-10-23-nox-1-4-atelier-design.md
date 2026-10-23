# NOX 1.4 — Atelier Premium UI
Date: 2026-10-23

## User observations and intent
Screenshots of NOX 1.3 (Home, Studio, Collection, Settings) show excellent real-time FLUX imagery but oversized blank Home space, repetitive generic Material 3 cards, weak preset-art prominence, a Studio save action buried below long scroll, and Settings disproportionately large for three controls. User requests a significant premium redesign, not a mockup.

## Distinctive direction
NOX as a dark digital light laboratory, not a dashboard. OLED #000000, functional cyan #30D9FF, restrained graphite #101923, less outlined chrome, expressive live artwork, editorial type hierarchy with no false metrics or meaningless numbered decoration. Touch targets >=44dp where possible. Native Compose only.

## Chosen approach
Redesign within :app (existing architecture), preserve OpenGL EGL pipeline, WallpaperService, model, history, Room schema, saved data, offline/no permissions. Tradeoffs: shader overhaul risks GPU performance; cosmetic color-only polish insufficient; curated Home + Gallery + workstation UX balances value and safety.

## Scope
- **Home Atelier:** flexibly-sized cinematic live scene fills available space; 3 tactile scene selectors with visual miniatures; one-tap deterministic Quick Remix using existing SceneLooks.shuffle, editable/saveable/exportable and applyable. Correct current-preview config must be used for apply and export. No auto-saving of unsaved remixes.
- **Studio:** retain pinned GL preview, preview size, Original compare and history actions. Add sticky bottom actions for Save / Export / Apply so user does not scroll all the way down for core tasks. Preserve precision sliders and all parameter edits.
- **Library:** two-column visual gallery using efficient Compose LazyVerticalGrid + existing lightweight canvas art miniatures; search by name and filter All/FLUX/HALO/NEBULA; open editor by tapping; contextual menu Use / Duplicate / Delete with confirmation; active badge. No extra GL instances.
- **Settings:** compact grouped FPS chips and gyro control, discreet privacy notes; remove giant generic-looking cards.

## Safety and upgrade
- New independent debug install `studio.nox.light.atelier`, versionCode=6, versionName=1.4.0. Keep stable debug versions and old private presets untouched; preset data does not automatically migrate.
- No Room migration, no new permissions, no extra GL views, no Internet, no dynamic download assets. Actions in Preview must be genuinely wired; no decorative buttons.

## Acceptance
Structural tests for Home Quick Remix wiring and correct apply/export, sticky Studio actions outside scroll, grid+filters+delete confirmation, settings handlers, existing pinned preview invariants. Kotlin domain tests continue. Android CI compiles and uploads `NOX-1.4-atelier-APK`; inspect APK zip and verify downloaded bytes. Runtime QA on Lenovo remains user's device test.
