# NOX 1.0 — Living Light Studio

Offline Android live wallpaper studio for Lenovo Legion Y70 2026 (Motorola firmware, Android 16), built without root. Three generative GPU scenes: FLUX, HALO and NEBULA.

## Tech stack
- Kotlin 2.3.20, Jetpack Compose (BOM 2026.09.00), Material 3
- Android SDK 36, minimum Android 8.0 (API 26), Gradle 8.13, AGP 8.13.2, JDK 17
- OpenGL ES 3 context with GLSL shaders, WallpaperService, Room and Preferences DataStore
- App ID: `studio.nox.light`.

## Building an APK
Prerequisites: JDK 17+, Android SDK with platforms;android-36 and build-tools;36.0.0, Gradle 8.13. From project root:

```bash
gradle :core:model:test :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. Android debug signing is handled by Android Gradle Plugin; **this is a debug APK, not a production release signed with the owner's key**.

### Build through GitHub Actions (easiest without PC)
1. Create a NEW repository for NOX. Do not upload NOX into any preexisting unrelated project.
2. Upload this entire source tree, including `.github/workflows/android.yml`, to the repository's `main` branch.
3. Open Actions > Build NOX APK > Run workflow; once green, download `NOX-1.0-debug-APK` workflow artifact. Extract the ZIP to get the APK.
4. Transfer the APK to your device's Download folder and open it to install; allow installation for the file manager only if prompted. Keep ownership of your signing keys private.

GitHub Actions runs `gradle :core:model:test :app:assembleDebug` with SDK 36. NOX has **not been verified on the target phone** in this environment; Android SDK and Gradle are not installed here and network downloads are unavailable. No prebuilt APK is included in the source archive.

## Features / expected behavior
- Home: live interactive scene preview and switch between all three scenes.
- Studio: edit color palette, speed, glow, density and parallax; save named presets.
- Library: locally stored presets; select, edit, duplicate or delete with confirmation.
- Settings: target 30/60/120 FPS (capped under thermal stress and battery saver); sensor toggle.
- Export: Android document picker writes a full-resolution PNG to selected URI.
- Apply: standard Android live wallpaper confirmation (`ACTION_CHANGE_LIVE_WALLPAPER`), never silent system changes.
- No network, contacts, microphone, photo permissions, background location, or private Android APIs.

## Important limits
- Live wallpaper can only be installed with Android's system confirmation. Lock-screen behavior and touch forwarding depend on Motorola launcher and OS.
- Sensor parallax derives from gravity/accelerometer, not gyroscope angular velocity; this yields stable device tilt for wallpaper effects.
- 120 FPS is a maximum requested cadence, not a guarantee. Battery saver reduces it to <=30 FPS, severe thermal load to <=15 FPS.
- Surface, EGL thread and gravity sensor are released/stopped when wallpaper is hidden or surface destroyed. Graphic actual FPS and long-run battery consumption need hardware QA.
- PRISM, video export, accounts, and cloud gallery intentionally excluded from v1.0.

## Design and plan
- `docs/superpowers/specs/2026-10-23-nox-1.0-design.md`
- `docs/superpowers/plans/2026-10-23-nox-1.0.md`
