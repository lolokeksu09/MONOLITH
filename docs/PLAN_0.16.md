# MONOLITH 0.16 / INK implementation plan

Status: **documented; runtime implementation not started**. This is the single scope and requirement registry for 0.16. A requirement marked partial here is not implemented, tested, merged or released merely because it appears in this document.

## Basis and boundaries

- Owner direction: document the visual-review recommendations as the next MONOLITH improvement stage so they are not lost.
- Base implementation: 0.15, main revision `5897868f0174e1fc1918d04de7ac68a41f832aa2`, signed prerelease `v0.15.0-beta.380`.
- Existing visual contract: [Reference visual system](DESIGN.md).
- Current physical regression checklist: [0.15 device acceptance](DEVICE_ACCEPTANCE.md); its unchecked items remain open.
- Engineering constraints: [root AGENTS.md](../AGENTS.md).
- The documentation branch and PR record delivery of this plan. They do not deliver the 0.16 runtime features.

### Included

1. Original / Mono / INK application-icon appearances across orbit, editorial shortcuts and the drawer.
2. One restrained Material Symbols Rounded family for launcher-owned controls, plus clearer density-aware application artwork.
3. Persisted 8 / 12 / 15 orbit-density choices without changing existing favorite order or hidden-app rules.
4. Editorial-page hierarchy cleanup and a drawer that belongs to the same visual system.
5. Contextual media and HOME-setup presentation with no fake playback metadata and no loss of stock-HOME recovery.
6. Accessibility, reduced motion, readable Russian/English resources and measured device verification for every changed UI path.

### Excluded unless the owner separately approves them

- Returning Classic HOME, the legacy Dock, folders, widgets or old backup UI.
- Replacing third-party brands with generic Material icons, forcing every icon into one mask, or silently recoloring user data/settings.
- Neon, gradients, glassmorphism, heavy cards, decorative continuous motion or dynamic Material You color as the default identity.
- Notification-listener access for track/artist metadata. Media metadata is not part of this plan.
- Erasing or silently migrating legacy SharedPreferences, changing the application ID, rotating the signing certificate or adding private keys.
- A version bump, APK release or merge authorization. Those require their own explicit delivery decision.

## Requirement registry

| ID | Requirement and source | Observable scenario and completion criterion | Verification | State | Evidence or remainder |
|---|---|---|---|---|---|
| REQ-016-001 | Owner visual review: provide Original, Mono and INK app-icon appearances. | Change the mode in Settings and see one selected treatment in orbit, editorial shortcuts and drawer after restart. Original preserves current art; Mono applies a tested grayscale treatment; INK uses the native monochrome adaptive-icon layer on Android 13+ when supplied and keeps original art as the visible fallback. Upgrades retain Original; no generic substitute brand icons or invisible glyphs. | Unit tests for mode/fallback/persistence; localized Settings checks; installed-candidate screenshots of all three modes with supported and unsupported adaptive icons; TalkBack labels unchanged. | Partial — scope documented | Implementation, automated evidence and device evidence pending. |
| REQ-016-002 | Owner visual review: unify launcher controls and improve application-art quality. | Search, settings, sorting, Back, close and media controls use one Material Symbols Rounded visual family with accessible names and 48dp targets. App icons have consistent optical bounds and crisp density-aware decoding without destructive cropping or unbounded cache growth. | L1/L2 affected UI checks; icon-bound and cache tests where meaningful; high-density phone screenshots; inspect press/focus/disabled states and large fonts. | Partial — scope documented | Asset selection, implementation and physical review pending. |
| REQ-016-003 | Owner visual review: allow 8, 12 or 15 orbit apps. | Select each density, restart and retain the choice. Existing 15 favorites, order and hidden-app exclusions remain intact; reducing density never deletes favorites. All visible targets remain separated, launch correctly and obey reduced motion. | Extend orbit geometry, capacity, ordering and hit-target tests for all densities; exercise first open, interrupted opening and accidental movement taps on the phone. | Partial — scope documented | Geometry/persistence design, implementation and device evidence pending. |
| REQ-016-004 | Owner visual review: strengthen the editorial page rather than redesign it. | Keep the offline rock and one oversized day abbreviation while removing redundant `THURSDAY` / `Today` repetition. Russian and English screens follow an intentional language hierarchy; date, time, greeting, quick access and page navigation remain readable at large fonts. The selected REQ-016-001 icon mode applies here too. | Russian/English screenshots of the installed candidate at default and 1.3x fonts; offline launch; quick-icon launch/actions; rotation and safe-inset checks. | Partial — scope documented | Final copy hierarchy, implementation and device evidence pending. |
| REQ-016-005 | Owner visual review: make All Apps visually consistent with Reference HOME. | Replace the heavy generic search/header treatment with the MONOLITH editorial rhythm, a slim labeled search field and coherent sorting iconography. App names may wrap to two lines instead of premature ellipsis. Search, clear/disable search, sorting, hide/unhide and keyboard behavior remain functional. | Affected Compose/UI tests; IME open/close and error/empty catalog checks; Russian/English screenshots; one-handed reach comparison before choosing top or bottom search placement. | Partial — scope documented | Search placement remains an open decision; implementation and device evidence pending. |
| REQ-016-006 | Owner visual review: make media and HOME setup contextual without weakening recovery. | No active-player wording is shown when playback is unavailable; the media area collapses or presents an honest inactive state without fabricated track data. Compatible play/pause/next still works. The HOME-setup action disappears when MONOLITH holds the HOME role, remains available when needed, and stock-HOME recovery stays reachable. No new media permission is introduced. | No-player and real-player tests on the installed candidate; HOME role grant/revoke, reboot and stock-launcher recovery; permission/manifest diff; Russian/English and reduced-motion checks. | Partial — scope documented | Feasibility of inactive-state detection, implementation and device evidence pending. |
| REQ-016-007 | Owner visual review and existing engineering rules: preserve restrained interaction and accessibility. | Press feedback does not shift layout; no perpetual decorative motion is added. Reduced motion, 48dp targets, sequential focus, localized actions, orbit state and meaningful icon labels survive every new mode and density. | Existing and new semantics tests where applicable; TalkBack in Russian/English; reduced-motion and largest-font device pass; touch-target inspection in light/dark AMOLED states that actually exist. | Partial — scope documented | Implementation-specific checks and physical accessibility evidence pending. |
| REQ-016-008 | Root AGENTS.md: preserve identity, data and truthful delivery states. | `dev.monolith.launcher`, pinned signing and existing drawer/hidden/statistics/favorites data remain compatible. Documentation distinguishes planned, implemented, CI-verified, device-accepted and released states. The signed update installs over 0.15 without uninstall. | Required CI command and disposable-key signing smoke test for code PRs; pinned-certificate main release check; install-over-0.15 and persistence test; exact-SHA diff and evidence review. | Partial — scope documented | This registry is the documentation evidence; all runtime and release evidence is pending. |

## Stages

| Stage | Change and dependencies | Completion criterion | State |
|---|---|---|---|
| 0. Documentation | Record 0.16 scope, requirements, open decisions and verification boundaries; do not change runtime code or app version. | This registry and links exist; documentation diff and applicable CI are reviewed. PR/open/merged status remains distinct from feature status. | Completed locally; Git delivery tracked by PR |
| 1. Icon foundation | REQ-016-001/002/003; persistence, fallback, density-aware rendering and geometry tests before broad screen restyling. | All icon modes and densities work in focused tests without changing existing user choices on upgrade. | Not started |
| 2. Reference surfaces | REQ-016-004/005, using Stage 1 icon behavior. | Editorial and drawer scenarios pass on the installed candidate in Russian and English. | Not started |
| 3. Contextual chrome and accessibility | REQ-016-006/007 plus any interaction gaps found in Stages 1–2. | Media/HOME role states and accessibility checks pass without new permissions or recovery regressions. | Not started |
| 4. Beta candidate verification | All runtime requirements; run the applicable 0.15 regression checklist and new requirement checks on one exact signed candidate. | Required PR CI is green and every physical result is recorded honestly. Beta may remain explicitly device-acceptance-pending; it must not be called visually accepted or stable while checks are open. | Not started |
| 5. Merge/release | Separate owner authorization for each merge and signed release. | Exact revision is merged by the approved path; main CI/signing succeeds; the published APK is checked over 0.15 and the result is recorded. | Not started |

## Verification and delivery policy

- **Current documentation PR:** applicable L1 only: inspect the exact diff, validate relative links and confirm no runtime/version/signing files changed. The repository PR `verify` job still runs as an existing gate. It is not UI evidence for unimplemented 0.16 features.
- **Future code PRs:** run `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and the existing disposable-key signing smoke test through [Android CI](../.github/workflows/android.yml). Add targeted L2/L3 checks for the changed persistence, geometry, UI and recovery paths.
- **Physical UI evidence:** test the actual changed APK on the Lenovo Legion Y70 / Motorola Android 16. Screenshots can support layout review but cannot prove motion smoothness, TalkBack behavior, gestures or frame timing.
- **Beta merge:** exact-head required checks must pass. Open physical checks remain visible and preserve beta status; they are not recorded as successful exceptions.
- **Stable 1.0:** the existing stable gate remains unchanged: all required CI and physical behavior, signing continuity and stock-HOME recovery must pass.
- **Signed delivery:** after separately authorized release, verify the published tag/APK, pinned certificate and install-over-0.15 behavior. Pipeline success alone is not L5 delivery evidence.

## Open decisions

1. **Clean-install default icon mode:** choose only after Original / Mono / INK device previews. Existing upgrades stay on Original regardless.
2. **Drawer search position:** top versus bottom is decided by one-handed reach, IME and large-font testing, not by aesthetic preference alone.
3. **Inactive media presentation:** collapse versus a compact honest inactive label depends on what can be detected reliably without notification-listener access.
4. **Haptic feedback:** optional only if a device check shows a clear benefit; it is not a visual-release gate and must not become excessive.

These decisions do not block icon fallback, persistence, geometry and documentation work. Once decided, update the corresponding requirement and record the source instead of silently rewriting history.

## Progress log

- 2026-11-06: 0.15 scope is delivered and published as `v0.15.0-beta.380`; its physical checklist remains open. The owner requested that the INK visual-review recommendations be preserved in the repository. This plan records that scope only; no 0.16 runtime behavior or APK exists yet.
