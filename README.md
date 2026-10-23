# NOX — Living Light Studio

Offline, native Android AMOLED live wallpaper studio for Lenovo Legion Y70 2026 / Android 16 (no root). Three original OpenGL ES scenes: **FLUX**, **HALO**, **NEBULA**.

## NOX 1.1 Preview

- Immersive black-on-black main screen, redesigned navigation, refined preset library and settings.
- **Five curated looks:** Abyss, Ion Violet, Solar, Arctic and Chroma.
- **New variant:** deterministic remix of palette, speed and shader seed; original scene is retained.
- **Custom HEX color inputs** in addition to swatches.
- Full-screen interactive preview and reset-to-scene-defaults.
- Preset search by name or scene ID (case-insensitive); prior save, duplicate, edit, apply, delete remain available.
- PNG export, Room presets, WallpaperService and GPU rendering are preserved.

### Important: existing NOX 1.0 installation

GitHub Actions generates a fresh Android debug signing key unless the signer is persisted. That means a new Actions debug APK cannot be relied upon to update the old app in-place. **Never uninstall NOX 1.0 merely to try the preview; uninstalling deletes its private local presets.**

The NOX 1.1 debug build therefore uses a separate package **`studio.nox.light.preview`**, with launcher name **NOX 1.1 Preview**, installed beside existing NOX 1.0 (`studio.nox.light`). Its saved presets are separate; it does not migrate the original app's private data. When applying wallpaper, Android asks which live wallpaper to use.

### Build / tests

GitHub Actions `Build NOX APK` runs Python structural checks, JUnit model tests, and Android debug APK compilation (Gradle 8.13, AGP 8.13.2, API 36, Kotlin 2.3.20, Jetpack Compose BOM 2025.10.01, Java 17). A successful workflow uploads `NOX-1.1-preview-APK`. This is a **debug** APK, not a release build with a stable owner-controlled signing key.

```sh
python3 -m unittest discover -s tests -p 'verify_structure.py' -v
gradle :core:model:test :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

**Hardware validation still required:** preview/rendering, battery and thermal response, movement sensor, touch in launcher, system live-wallpaper selection and export on real Lenovo Legion Y70 2026.

## Core capabilities

- 3 realtime programmable scenes, touch and gravity-based parallax.
- 30, 60 or up-to-120 FPS request, thermally throttled and suspended when not visible.
- Offline storage via Room and DataStore; Android system confirmation for wallpaper installation.
- No internet, account, ads, analytics, location, media-library permission or root.

## Architecture

`:app` Compose app, `:core:model` value models/looks and frame budget, `:core:renderer` OpenGL renderer, `:core:data` persisted presets/settings, `:feature:wallpaper` Android WallpaperService.

Specifications and plans: `docs/superpowers/specs/` and `docs/superpowers/plans/`.
