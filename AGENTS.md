# MONOLITH engineering rules

- Production identity is `dev.monolith.launcher`; maintain the existing signing certificate and GitHub Actions Secrets. Never commit keys or replace the keystore.
- 0.12+ has one desktop: `ReferenceHome`. Do not reintroduce Classic HOME, the legacy Dock or old widget/backup UI unless the user explicitly requests it.
- Keep compatibility with the existing drawer settings, hidden app IDs and launch statistics. Do not erase legacy SharedPreferences, or silently migrate/delete user data.
- Preserve safe HOME role selection and a route back to the stock Android launcher. Do not replace lock screen, recents or SystemUI.
- Version is configured through `monolithVersionName` in `gradle.properties`; CI uses it for release metadata.
- Run `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and verify disposable-key signing before merging UI changes. Main signed releases must match the pinned SHA-256 certificate.
- Provide Russian and English resources, accessible touch targets and grounded performance claims. Do not claim device validation based on CI alone.
- Focus new UI work on accurate, performant Reference-mode interactions and test them on Lenovo Legion Y70 / Motorola Android 16.
