# NOX 1.0 — verification ledger, 2026-10-23

## Verified in this execution environment
- Kotlin 1.9 command-line compiler executed `tests/ModelContract.kt` against the pure model source: 12/12 assertions passed, including explicit RED (unresolved implementation) before GREEN.
- `python -m unittest discover -s tests -p verify_structure.py -v`: 6/6 structural assertions passed (XML well-formed, no requested permissions, modules declared, wallpaper declarations, export entrypoint, CI APK artifact path).
- Android source and Gradle module files written to disk and packaged separately.

## Blocked
- `:app:assembleDebug`, Android instrumentation, installation and GPU checks: Android SDK and Gradle are absent; DNS resolution for external SDK/Gradle endpoints fails in the container.
- Therefore NO APK binary is included or claimed to work. The configured GitHub Actions runner must execute the first actual Android build; failures must be fixed before distributing an APK.
- No validation has occurred on the Lenovo Legion Y70 2026 / Motorola Android 16 firmware.

## Device test checklist (not executed)
- Install debug APK; launch Home, Studio, Library, Settings.
- Verify three shader scenes visually and touch + gravity parallax.
- Save, reopen, duplicate, delete preset, then force-stop/relaunch.
- Export PNG to Downloads via picker and inspect pixels/orientation.
- Apply live wallpaper using OS confirmation; decline once, accept once.
- With wallpaper visible vs hidden, monitor GPU activity, FPS and sensors.
- Turn on battery saving and verify profile limitation.
- Screen lock/unlock, portrait/landscape, launcher and lockscreen behavior.
- 30-minute burn-in and thermal check.

## Threat / failure review
- No INTERNET or storage read permissions requested.
- User-controlled preset names trimmed/validated (1..48 chars).
- No automatic wallpaper changes; Android confirmation is required.
- Native GLES3 failure shows error in editor, but hardware compatibility still unverified.
- Debug signing is unsuitable for production distribution; release key must remain under owner control.
