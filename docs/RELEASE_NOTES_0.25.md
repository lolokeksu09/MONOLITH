# MONOLITH 0.25 beta — one-handed interface and reliability

**Release type:** production-certificate-signed Android **beta**. **Application ID:** `dev.monolith.launcher`. This is intended to install over the previous signed beta without uninstalling it, provided the pinned certificate remains unchanged.

## Added

- One-result search quick-launch: keyboard Search opens the unique, current, fully settled match, for both top and bottom drawer search.
- Optional Orbit press/drag/release selection: in expanded Orbit, hold the existing **Collapse orbit** control, drag to an actual app icon and release. Off-target releases/cancellation are inert, normal taps and long-press actions remain.
- Editorial now shows the actual apps most recently launched **through MONOLITH**, with a truthful EN/RU heading. On fresh installs or after clearing local history it keeps working quick-access shortcuts instead.

## Improved

- More compact All Apps grid labels in five/six-column layouts, clearer sorting selection, accessible full app names and radio-choice semantics.
- Responsive Minimal clock and Editorial composition under short effective displays and larger font scales, keeping Orbit and All Apps reachable.
- Clear first-load error recovery and a single retry; cached installed apps remain available after a refresh failure.
- Media and quick-access visual hierarchy without duplicate instructional labels.
- Catalog enumeration **and** launcher-local history reads moved off the HOME UI thread. Refreshes started before a new launch/history reset can no longer overwrite newer local statistics.

## Preserved

- Original native Android icon artwork, launcher app ID, permanently signed certificate, existing HOME-role behavior, hidden/favorite/sort/search settings and legacy SharedPreferences.
- Offline local-only statistics: no Usage Access, internet telemetry, new permissions or background app monitoring.
- Debug Preview remains a separate `dev.monolith.launcher.preview` application; it is **not** an update to production.

## Verification and limitations

- Every feature is covered by focused JVM/Robolectric tests; Android CI also checks Lint, debug APK, the native-icon guard, built APK package identities and a disposable-key signing smoke build.
- Publication **must** additionally verify the permanent signing certificate SHA-256 before uploading the actual signed APK.
- **Physical Lenovo Legion Y70 acceptance is NOT recorded** for these changes: real Orbit gesture arbitration, animation/jank, TalkBack, system font scaling, on-device update persistence and stock HOME recovery must still be checked.
- This is **not a stable 1.0 release**. Keep the stock Android launcher installed. Do not uninstall the existing MONOLITH before updating; uninstalling erases its stored data.

See `docs/PLAN_ONE_HANDED_2026-11-17.md` and `docs/DEVICE_ACCEPTANCE.md` for exact gates.
