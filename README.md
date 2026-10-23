# NOX 1.5 Portable Studio

Native offline AMOLED live-wallpaper studio for Android 16. Preserves NOX's GPU effects, pinned preview, editor/history, curated palettes, local Room presets and system wallpaper integration.

## What's new in 1.5

- **Portable library** in Settings: Export all presets to a user-chosen `.nox` file; import one from any previous NOX installation. Both use Android's system file chooser, no network or storage permissions. Imports **add** scenes; existing presets remain. Max 100 presets per file, 256k text-size bound; corrupted and incompatible formats are rejected.
- **Gallery sorting**: newest, by preset name or scene, in addition to existing filters and search.
- **Version header correctness**: derives from the Android BuildConfig, not a stale manually typed number.
- **NEBULA numerical stability**: star positions now use bounded trig-free hash. Rectangular artifacts visible in captured screenshots may also be caused by SurfaceView capture/composition or GPU driver differences; only actual device tests can confirm whether they disappear.

### Installing

`NOX 1.5 Portable` is a separate debug package `studio.nox.light.portable`. Previous NOX versions remain installed with their own data. To transfer scenes: open old NOX's Settings > Export if that version supports it, then import in 1.5. Existing versions without export must be upgraded with a **consistently signed build** to expose their data; sandboxed presets cannot be automatically read or copied by NOX 1.5. **Do not uninstall an old NOX to try 1.5, as that deletes its private presets.**

Build artifact: `NOX-1.5-portable-APK`. Requires on-device QA for Live Wallpapers, SurfaceView screenshot artifacts, scroll, file picker and performance. Debug APK is not an owner-signed stable release.

---

# NOX 1.4 — Atelier

A native, private, offline AMOLED live wallpaper studio for Android. Target tested phone family: Lenovo Legion Y70 2026 with Android 16 and seller-installed Motorola firmware.

## Atelier, release candidate

The four Android screens have a more cohesive, artwork-led layout:

- **Overview:** the animated scene fills available screen space, not a fixed-height card. Three lightweight interactive scene thumbnails switch FLUX / HALO / NEBULA. **New variant** runs the deterministic existing SceneLooks.shuffle on the actual preview, without silently overwriting the saved preset. Edit, PNG export and applying wallpaper use that displayed variant. Applying explicitly saves the chosen variant.
- **Studio:** the OpenGL preview stays pinned while controls scroll. The **Save / Export PNG / Apply wallpaper dock** is always visible at the bottom. Undo/redo, precise parameter steppers, color looks and harmonies remain functional.
- **Collection:** searchable two-column artwork gallery with FLUX/HALO/NEBULA filter, active-wallpaper mark, tap to edit and item menu for Edit / Use / Duplicate / Delete (delete requires confirmation). Thumbnails use simple Canvas graphics, avoiding extra GL contexts.
- **Settings:** compact, genuinely wired 30/60/120 FPS tiles and motion-sensor switch. Thermal/energy constraints remain enforced; no fake live telemetry.

All effects still use original GLSL renderer and WallpaperService. Existing Room schema, permissions, import/export behavior, and stored models are untouched.

### Safe installation

Debug APK label **NOX 1.4 Atelier**, separate Android package `studio.nox.light.atelier`. Because GitHub Actions produces a new debug signature, this preview installs **beside** previous NOX installations, never over them. Your presets in other versions are retained, but are **not migrated** between separate preview apps.

Do not uninstall NOX 1.3 until testing is complete.

### Build checks

CI checks Python structural contracts, Kotlin model unit tests and `:app:assembleDebug`; it uploads `NOX-1.4-atelier-APK`. Successful compilation is not proof of smooth operation or of battery performance on actual hardware; test these with the installed APK.

Built with Kotlin, Compose BOM 2025.10.01, OpenGL ES 3.0, AGP 8.13.2, Gradle 8.13, Android SDK 36. Offline and no root.

## Development history

Earlier releases and design plans remain in `docs/superpowers/`. Version 1.3 Signature includes bounded undo/redo and precision controls; 1.2 Palette Lab adds harmonic colors and pinned preview resizing; 1.1 added palette looks, search, full-screen preview and remix.

