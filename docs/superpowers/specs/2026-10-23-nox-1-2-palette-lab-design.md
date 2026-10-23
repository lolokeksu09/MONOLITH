# NOX 1.2 — Palette Lab
Date: 2026-10-23

## Goal and intent
Advance the Android 16 native offline AMOLED live wallpaper editor with precise, creative color control, comparison and ergonomics. User confirmed NOX 1.1.1 works on hardware and wants more functions and a more premium UI. Preserve exactly the pinned preview interaction and 1.1.1 stability.

## Alternatives considered
1. New shader scenes and engine refactor: potentially visually dramatic, high risk to runtime and battery life.
2. Cloud palette gallery: introduces server, network and privacy issues, contradicts offline design.
3. Local Palette Lab with preview ergonomics (selected): clear user-facing benefit, no migrations or new permissions.

## Scope
1. **ColorHarmony** in core:model: analogous (25 degrees), complementary (180), triadic (120), computed from primary RGB using deterministic HSV conversion; secondary only changes. Neutral colors get enough saturation to make a visible harmony. Existing color swatches and manual HEX inputs remain.
2. **Pinned compare:** a switch in Studio temporarily displays scene's factory defaults in the existing GL view; restores current unsaved edits without mutating them. Distinguish "Заводская" and "Моя" modes in a single accessible button. No second GL surface.
3. **Preview height:** user-toggleable "Компактно/Крупно" preview, stays pinned, no scrolling of preview. Dynamic dp proportional to viewport, clamped, leaving controls accessible.
4. **UI quality:** refined cyan/graphite controls, labeled harmony swatches, crisp typographic hierarchy and explanatory labels; no gratuitous animations or overlays; 48dp minimum touch targets where practical.

## Constraints
- Repository lolokeksu09/NOX-LEGION, base feature/nox-1-1-studio; new feature/nox-1-2-palette-lab.
- No changes to Room schema, existing wallpaper service, shaders, APK release application ID studio.nox.light.
- Debug app ID studio.nox.light.previewlab; intentionally separate from prior preview (debug signature not guaranteed across CI runners). No migration of presets between independent preview apps.
- Kotlin 2.3.20, AGP 8.13.2, Gradle 8.13, compileSdk 36, Compose BOM 2025.10.01.
- Explicit test red-green: Kotlin model tests, Python UI structural checks; Android CI compile and artifact inspection before claiming build success.
- NOX 1.2.0-preview uses versionCode 4.
