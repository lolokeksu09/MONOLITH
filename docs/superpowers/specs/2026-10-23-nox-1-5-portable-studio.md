# NOX 1.5 Portable Studio — reviewed scope
Date: 2026-10-23

## Intent
Improve native AMOLED NOX based on four Lenovo Legion Y70 2026 screenshots and the exact current Kotlin / Compose / OpenGL repo code. Keep the existing visual identity and all functioning NOX 1.4.1 behavior including pinned Studio preview, existing wallpaper service, scene catalog, local Room schema and no network/root permissions.

## Evidence
- Version badge in NoxApp.kt is hardcoded as "1.4" even on 1.4.1 builds; use generated BuildConfig.VERSION_NAME.
- Different prior preview packages have separate Android private data and cannot exchange saved presets. Build offline, versioned export/import to a local file.
- NEBULA screenshot shows hard rectangular regions; fragment shader used fract(sin(largeValue)*43758.5453) for star position hashing. Replace with bounded trig-free hash to avoid one plausible source of GPU precision artifacts. **It remains unverified whether the screenshot artifacts originate in the shader, SurfaceView screenshot compositing, or device GPU driver.** Do not claim the visual artifact fixed without hardware comparison.
- Collection gallery lacks useful sorting. Add newest/name/scene selection while keeping type filters, search, and existing per-preset actions.

## Technical decision
- New :core:model PresetTransfer data format: header NOX-PRESETS/1, each line UTF-8 base64url name + validated fields. Strictly bound 100 presets and 256k chars, reject unknown scene IDs, invalid colors, out-of-range or non-finite floats and corrupted names. Do not run arbitrary scripts or network requests.
- Android Storage Access Framework via CreateDocument/OpenDocument; import appends records to Room, never clears existing presets or alters active wallpaper.
- Preview package studio.nox.light.portable, version 1.5.0/code 8; separate from previously-installed debug versions so their private data stays untouched. Settings exposes export/import and caveat. Import validates entire file before saving.
- Preserve existing GLSL modes and draw loop. Only hash function changes. Follow-up device QA must compare normal UI, PNG and system screenshots for NEBULA and measure thermals.
- GitHub PR based on feature/nox-1-4-1-collection-settings, never directly alter stable main until code review.

## QA
TDD JUnit roundtrip and hostile input cases, Python structural contracts for version/hash/gallery/settings, CI Kotlin tests and :app:assembleDebug; verify exact artifact ID and APK zip structure. Runtime on Lenovo must be separately tested.
