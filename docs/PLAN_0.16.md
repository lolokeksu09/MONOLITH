# MONOLITH 0.16 / INK implementation plan

Status: **runtime implementation present on `feature/016-ink`; CI and physical acceptance pending; not merged or released**. This is the single scope and requirement registry for 0.16. A requirement is not CI-verified, device-accepted or released merely because implementation code exists.

## Basis and boundaries

- Owner direction: implement the visual-review recommendations as the next MONOLITH improvement stage.
- Base implementation: 0.15, main revision `5897868f0174e1fc1918d04de7ac68a41f832aa2`, signed prerelease `v0.15.0-beta.380`.
- Documentation base: main revision `196cb68a6827db72657346b680f75f39d2642128`.
- Existing visual contract: [Reference visual system](DESIGN.md).
- Physical regression checklist: [device acceptance](DEVICE_ACCEPTANCE.md); its unchecked items remain open.
- Engineering constraints: [root AGENTS.md](../AGENTS.md).
- The feature branch and PR can deliver candidate code only. Merge, signed release and device acceptance remain separate states and decisions.

### Included

1. Original / Mono / INK application-icon appearances across orbit, editorial shortcuts and the drawer.
2. One restrained Material Symbols Rounded family for launcher-owned controls, plus clearer density-aware application artwork.
3. Persisted 8 / 12 / 15 orbit-density choices without changing existing favorite order or hidden-app rules.
4. Editorial-page hierarchy cleanup and a drawer that belongs to the same visual system.
5. Honest media and HOME-setup presentation with no fake playback metadata and no loss of stock-HOME recovery.
6. Accessibility, reduced motion, readable Russian/English resources and measured device verification for every changed UI path.

### Excluded unless the owner separately approves them

- Returning Classic HOME, the legacy Dock, folders, widgets or old backup UI.
- Replacing third-party brands with generic Material icons, forcing every icon into one mask, or silently recoloring user data/settings.
- Neon, glassmorphism, heavy cards, decorative continuous motion or dynamic Material You color as the default identity.
- Notification-listener access for track/artist metadata. Media metadata is not part of this plan.
- Erasing or silently migrating legacy SharedPreferences, changing the application ID, rotating the signing certificate or adding private keys.
- Merge or release authorization. Those require their own explicit delivery decision.

## Requirement registry

| ID | Requirement and source | Observable scenario and completion criterion | Verification | State | Evidence or remainder |
|---|---|---|---|---|---|
| REQ-016-001 | Original, Mono and INK app-icon appearances. | Change the mode in Settings and see one selected treatment in orbit, editorial shortcuts and drawer after restart. Original preserves current art; Mono applies grayscale; INK uses native monochrome adaptive-icon art on Android 13+ when supplied and original art otherwise. Upgrades retain Original. | JVM preference tests; CI build/lint; installed-candidate screenshots of all modes and fallback; TalkBack labels. | Implemented in branch; verification pending | `LauncherSettingsStore`, `AppIconCache`, HOME/drawer call paths implemented. Native-layer rendering and upgrade persistence still require CI/device evidence. |
| REQ-016-002 | Unify launcher controls and improve application-art quality. | Search, settings, sorting, Back, close and media controls use one rounded vector family with accessible names and 48dp targets. App icons use consistent optical bounds and density-aware bitmap sizes with an 8 MiB LRU. | Affected UI checks; high-density screenshots; press/focus/disabled states and large fonts. | Implemented in branch; verification pending | Reviewed local vector drawables and cache keys by app/style/size added. Physical optical review and accessibility remain open. |
| REQ-016-003 | Allow 8, 12 or 15 orbit apps. | Select each density, restart and retain it. Existing favorites, order and hidden exclusions remain intact; reducing density never deletes favorites. Visible targets remain separated. | Extend geometry, capacity, ordering and hit-target tests; exercise first/interrupted opening on the phone. | Implemented with JVM tests; CI/device pending | `OrbitLayout`, density preferences and non-mutating limit tests added; persistence and motion still need installed-candidate checks. |
| REQ-016-004 | Strengthen the editorial page rather than redesign it. | Keep the rock and one oversized localized day abbreviation while removing redundant `THURSDAY` / `Today` repetition. Date, time, greeting, quick access and navigation remain readable. Selected icon mode applies. | RU/EN screenshots at default and 1.3x fonts; offline launch; quick-icon actions; rotation and insets. | Implemented in branch; device pending | Duplicate copy removed; locale-aware day/date and shared icon mode implemented. Layout acceptance is not claimed. |
| REQ-016-005 | Make All Apps visually consistent with Reference HOME. | Editorial header rhythm, slim labeled search, coherent sorting icon and two-line app names. Search, clear/disable, sorting, hide/unhide and IME behavior remain functional. | Affected UI checks; IME open/close and error/empty catalog; RU/EN screenshots and reach review. | Implemented in branch; device pending | Top search remains provisional pending the documented reach/IME/font decision. |
| REQ-016-006 | Honest media and HOME setup without weakening recovery. | Media controls identify system media keys and unknown status rather than claiming an active player. No track data is fabricated. HOME setup hides while the role is held and remains available otherwise. No new media permission. | No-player/real-player tests; HOME grant/revoke, reboot and stock recovery; manifest diff; RU/EN checks. | Implemented in branch; device pending | Neutral status copy selected without detection permission. Existing role and recovery flow retained; actual playback/HOME behavior still needs device evidence. |
| REQ-016-007 | Preserve restrained interaction and accessibility. | Press feedback does not shift layout; no perpetual decorative motion. Reduced motion, 48dp targets, localized actions, orbit state and meaningful labels survive every mode and density. | Semantics checks where applicable; TalkBack RU/EN; reduced-motion and largest-font device pass. | Partially implemented; verification pending | Existing spring/semantics retained and new controls labeled. Sequential focus and all new states still require physical review. |
| REQ-016-008 | Preserve identity, data and truthful delivery states. | `dev.monolith.launcher`, pinned signing and existing drawer/hidden/statistics/favorites data remain compatible. Signed update installs over 0.15. Delivery states remain distinct. | Required CI and disposable signing; pinned main release check after authorization; install-over-0.15 and persistence; exact-SHA diff. | Implementation complete; CI/release/device pending | Existing preference file/keys and app ID retained; version is 0.16.0-beta in this branch. No main release or certificate result is claimed. |

## Stages

| Stage | Change and dependencies | Completion criterion | State |
|---|---|---|---|
| 0. Documentation | Record scope, requirements, decisions and verification boundaries. | Registry and links exist; applicable CI reviewed. | Completed in main |
| 1. Icon foundation | REQ-016-001/002/003; persistence, fallback, density-aware rendering and geometry tests. | Implementation and focused tests exist; required CI and device checks follow. | Code/tests implemented; verification pending |
| 2. Reference surfaces | REQ-016-004/005, using Stage 1 icon behavior. | Editorial and drawer scenarios pass on installed candidate in RU/EN. | Code implemented; device acceptance pending |
| 3. Contextual chrome and accessibility | REQ-016-006/007 plus interaction gaps. | Media/HOME role states and accessibility checks pass without new permissions or recovery regressions. | Code implemented; device acceptance pending |
| 4. Beta candidate verification | All runtime requirements and applicable 0.15 regressions on one exact signed candidate. | Required PR CI green; physical results recorded honestly. | Not completed |
| 5. Merge/release | Separate owner authorization for each merge and signed release. | Exact revision merged; main CI/signing succeeds; published APK checked over 0.15. | Not authorized or started |

## Verification and delivery policy

- **Code PR:** run `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and the existing disposable-key signing smoke test through [Android CI](../.github/workflows/android.yml). Targeted JVM tests cover preference parsing, non-mutating density limits and geometry.
- **Physical UI evidence:** test the exact changed APK on Lenovo Legion Y70 / Motorola Android 16. Screenshots can support layout review but cannot prove smoothness, TalkBack, gestures or frame timing.
- **Beta merge:** exact-head required checks must pass. Open physical checks remain visible and preserve beta status.
- **Stable 1.0:** all required CI and physical behavior, signing continuity and stock-HOME recovery must pass.
- **Signed delivery:** only after separately authorized release, verify tag/APK, pinned certificate and install-over-0.15 behavior.

## Open decisions

1. **Clean-install default icon mode:** remains Original for this candidate; change only after Original / Mono / INK device previews. Existing upgrades also stay Original.
2. **Drawer search position:** the implementation retains top search provisionally. Bottom placement still requires one-handed reach, IME and large-font comparison.
3. **Inactive media presentation:** implemented as a compact honest `status unknown` label because no notification-listener or fabricated metadata is allowed. Confirm usefulness with no player and a real player.
4. **Haptic feedback:** not added. Consider only if a device check shows clear benefit.

## Progress log

- 2026-11-06: 0.15 scope is delivered and published as `v0.15.0-beta.380`; its physical checklist remains open. The owner requested that the INK recommendations be preserved in the repository.
- 2026-11-06: the owner authorized implementation. `feature/016-ink` adds icon appearances, density preferences, density-aware caching, launcher vector controls, editorial/drawer cleanup, neutral media copy, RU/EN resources and focused JVM tests. This records branch implementation only; CI, device acceptance, merge and release remain pending.
