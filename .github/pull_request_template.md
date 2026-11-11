# MONOLITH pull request

## Scope

- Summary of change:
- Affected HOME/drawer/settings path (if applicable):
- Compatibility or migration impact:

## Verification

- [ ] `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` passes, or failures are documented.
- [ ] Disposable-key release signing smoke test passes (CI).
- [ ] Android API 26–36 compatibility and RU/EN accessibility considered.
- [ ] Existing user preferences, favorites, hidden apps, and launch statistics are preserved, or an explicit migration is explained.
- [ ] HOME role and stock-launcher recovery stay functional (if affected).
- [ ] Physical Lenovo Legion Y70 / Motorola Android 16 acceptance is recorded in `docs/DEVICE_ACCEPTANCE.md`, **or remains explicitly pending**.

## Delivery

- [ ] No claim that CI verifies device animation, gestures, TalkBack or insets.
- [ ] No private signing key or password included.
- [ ] Signed GitHub Release requested separately from merging this PR.
