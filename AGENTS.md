# MONOLITH repository working rules

- The production identity is `dev.monolith.launcher`. Keep it stable so signed APK updates work.
- The permanently signed APK depends on the existing private GitHub Actions Secrets and pinned SHA-256 certificate. Never commit keys, passwords or a replacement keystore.
- Keep existing on-device SharedPreferences keys and the portable home-backup format backward compatible; test migrations before altering them.
- Do not silently set the default HOME or replace/disable stock HOME, lock screen, SystemUI or recents.
- The public prerelease version is `monolithVersionName` in `gradle.properties`; release workflow titles, tags and APK asset names must agree with it.
- Run the CI verification tasks: `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and disposable-key signing validation.
- Preserve English/Russian resources and accessible touch targets for UI changes.
- Separate verified CI outcomes from results obtained on the actual Motorola/Lenovo device.
- Before building a theme engine, isolate large screen/activity components and protect the existing launcher behavior with tests.
