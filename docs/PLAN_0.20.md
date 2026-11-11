# MONOLITH 0.20 — Responsive catalog and Orbit interaction

**State:** implementation candidate in [PR #43](https://github.com/lolokeksu09/MONOLITH/pull/43). Neither a signed production release nor physical-device acceptance is established by this plan.  
**Base:** [v0.19.0-beta.414](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.19.0-beta.414), signed and published; on-device acceptance remains open.  
**Source of priorities:** [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md), P0 safe HOME/app access, P1 responsiveness and predictable interaction. 

## Focus and tradeoffs

0.19 made the Orbit trigger explicit and enabled opt-in bottom search. 0.20 follows up by moving the expensive drawer catalog operations off the UI thread and making results generation-aware. This is **not** evidence of a specific FPS improvement: measurements on the target Lenovo Legion Y70 are still required. Closed HOME remains free of invisible click regions. Tapping outside an **expanded** Orbit may dismiss it; controls and icons are layered above that background.

## Implementation registry

| ID | Requirement | Code | Verification | Device |
| --- | --- | --- | --- | --- |
| UX-020-01 | Filter and locale-aware sort installed apps on a cancellable background task, preserving the existing A–Z/frequent/install-date rules. | `data/DrawerProjection.kt`, `ui/MonolithScreen.kt` | JVM filter/sort parity test, exact-head Android CI and Compose drawer tests | Pending |
| UX-020-02 | Never show previously computed drawer results for a changed query/sort/hidden-app set. In particular, newly hidden applications must not briefly reappear while a new computation is pending. | `data/DrawerProjection.kt`, `ui/MonolithScreen.kt` | Request-identity stale-result tests, hidden-app and search UI regressions | Pending |
| UX-020-03 | Distinguish **processing** from **no results** without fabricating a zero count, and let users clear an empty search through a visible 48dp action as well as the field icon. | `ui/MonolithScreen.kt`, RU/EN strings | Compose search-clear test and localization review | Pending |
| UX-020-04 | A tap on free scene space closes the expanded Orbit, while shortcut icons, media transport actions and swipe gestures remain available; closed HOME only opens from the existing 48dp trigger. | `ui/ReferenceHome.kt`, RU/EN strings | Compose backdrop-dismiss test and physical tap/swipe scenarios | Pending |
| UX-020-05 | Maintain production application ID `dev.monolith.launcher`, original permanent certificate, settings file, hidden IDs, favourites, launch statistics, stock HOME fallback and offline behavior. | Existing architecture, no key rotation/migration | Exact-head checks and signed-release verification if authorized | Pending device |

### Regression / acceptance scenarios

- **Large catalog:** repeatedly type and clear search, switch six sort modes, hide/unhide an app, install/uninstall and refresh; verify no stale or hidden icon appears during a pending computation, no unexpected zero-results message and no crash.
- **Request cancellation:** new request arrives before an older calculation completes; rendered results must match only the *current request object*. A completed empty result is different from processing.
- **One-hand search:** test both Top and Bottom search settings with IME open and system gesture navigation; preserve the 0.19 choice after restart/reboot.
- **Orbit:** open/close rapidly, tap the background while expanded, tap and long-press a real icon (including the lone central icon), use media controls and change scene with a swipe. None of those operations should accidentally launch the wrong app.
- **Accessibility:** check TalkBack focus order for the backdrop-dismissing control and search-clear action, English/Russian text at large font, and reduced animation duration.
- **HOME safety:** upgrade a *permanently signed* APK over a previous same-certificate release without uninstalling; verify stock launcher selection/recovery and local preferences/data.

## Deliberately unchanged

- No Classic HOME, permanent app grid, widgets, Dock or backup UI.
- No new permission, network dependency, telemetry, fake media data or visual card system.
- No change to installed app identities, sorting/favourite logic, persisted icon modes or Orbit coordinates.
- No claim of smoothness, FPS, device QA or stable-release readiness from green CI alone.

## Evidence stages

1. New regression tests exist and are capable of failing before the corresponding implementation.
2. Confirm tests, Lint, debug APK assembly and disposable-signature check on the *exact* PR head in GitHub Actions.
3. Merge only an explicitly verified head into `main`; verify main CI separately.
4. If a production beta release is issued, use the original signing workflow and verify its pinned certificate and APK digest.
5. Complete [physical-device acceptance](DEVICE_ACCEPTANCE.md) separately; unchecked items remain unchecked.
