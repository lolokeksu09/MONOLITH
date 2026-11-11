# MONOLITH — product direction and quality criteria

**Status:** product direction and proposed next work, not an implementation commitment or a release announcement.  
**Updated:** 2026-11-11.  
**Current shipped baseline:** signed beta [v0.18.0-beta.400](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.18.0-beta.400), from `main` commit `cdbda9baa68946ffbba22e300e3997c8ca887866`. CI passed; physical Lenovo Legion Y70 / Motorola Android 16 acceptance remains unrecorded.

## Product thesis

MONOLITH is a native, offline-first, predominantly black Android HOME launcher **built around interaction with applications**, rather than a conventional always-visible grid. The goal is a recognisable, usable product: the supplied short video is a visual reference, not a specification to clone, and not proof of interaction quality.

Its distinguishing surfaces are:

- **Minimal HOME:** generous AMOLED-black negative space, vertical clock, unobtrusive media-key controls and an intentional central interaction point.
- **Orbit:** a compact, animated cluster of actual installed apps, chosen from saved favourites and launcher-local use; tap and long-press actions must be predictable.
- **Editorial:** a contrasting typography-led page with offline, original grayscale rock artwork and working quick-launch shortcuts.
- **All Apps:** dependable catalog, search, sorting and hidden-app management; it must remain easy to reach and recover from.

**Principle:** every visual flourish must either improve orientation, provide a reliable action, or justify its screen space. A distinctive look does not excuse extra taps, lag, inaccessible controls or a broken HOME role.

## Shipped / verified in code

The following is implemented in the signed 0.18 beta candidate (see [changelog](CHANGELOG.md)); **CI evidence is not physical acceptance**:

- One Reference HOME design with Minimal and Editorial scenes, Orbit expansion, app launch and All Apps navigation.
- Local app catalogue, favourite ordering, launcher-only usage statistics and persisted hide/sort/search settings.
- Original / Mono / INK app icon styles and 8 / 12 / 15 full Orbit densities.
- Centered partial Orbit arrangements for 1–15 visible apps while retaining the full-density layouts.
- Debounced nonblank drawer search, immediate clear, localized page-state accessibility semantics and Compose/JVM checks.
- Reduced decorative screen numbering, true-black launcher surfaces and adjusted Editorial/media visual hierarchy.
- Debug Preview uses `dev.monolith.launcher.preview`; **production** stays `dev.monolith.launcher` and uses a pinned certificate.

**Delivery record:** [PR #39](https://github.com/lolokeksu09/MONOLITH/pull/39) merged into `main`; [Android CI run #400](https://github.com/lolokeksu09/MONOLITH/actions/runs/43060986709) completed successfully and published `v0.18.0-beta.400`. The permanent-key certificate check passed. On-device install-over, real animations, gestures and launcher recovery still need evidence in [device acceptance](DEVICE_ACCEPTANCE.md).

## Proposed priorities (not shipped)

| Priority | Question / proposed work | Acceptance evidence | Decision state |
| --- | --- | --- | --- |
| **P0 — reliability** | Verify the exact signed 0.18 candidate as HOME on Lenovo Legion Y70 / Motorola Android 16; preserve a way back to the stock launcher. | Install over an earlier same-certificate MONOLITH without data loss; launch apps, reboot, recover stock HOME, test catalog failure and IME; record actual device/firmware/build. | **Pending device checks**, no pass claimed |
| **P0 — reference fidelity** | Compare Minimal, expanded/collapsing Orbit, Editorial and All Apps against the supplied video. Fix reproducible spacing, typography, clipping or transition mismatches, not subjective mockup differences. | Matched screenshots and short on-device recordings for both scenes, with locale/font/inset conditions recorded. | Proposed refinement |
| **P1 — one-handed usability** | Review drawer search position, Orbit trigger reach and common app-launch paths before changing gestures or controls. | On-device one-hand scenarios with IME visible/hidden and large fonts; document which layout is faster and more reliable. | Evaluation only; no position change approved |
| **P1 — motion and response** | Investigate cold-start icon readiness, interrupted expansion, accidental taps, scrolling and animation smoothness. Optimize measured bottlenecks without adding ornamental movement. | Reproducible device observations with actual conditions; confirm reduced-motion and TalkBack behavior. | Proposed; no frame-rate or latency claim |
| **P2 — Editorial utility** | Determine whether the second scene earns its place through real shortcuts and useful context, without fake track or status information. | Demonstrable launchable shortcuts and clear navigation; remove ornamental clutter if it impairs utility. | Product decision pending |
| **P2 — own identity** | Gradually evolve spacing, type rhythm and interaction choreography beyond the inspiration clip while retaining recognisable MONOLITH surfaces. | Side-by-side design review plus functional regression; avoid resembling a generic launcher template. | Direction, not a feature promise |

### Experience contracts

- **Orbit:** central trigger opens a coherent, symmetrical cluster; saved favourites, hidden-app exclusions and correct touch-target gating are preserved. Avoid adding mandatory extra taps for common actions without evidence.
- **HOME navigation:** swipe between Minimal and Editorial, swipe up to All Apps and Android Back remain predictable. The user can return to the system HOME using Android settings.
- **Typography and art:** prioritize legibility and purposeful negative space; the original offline artwork is an editorial accent, not a fake media/player view.
- **Accessibility:** Russian/English resources, TalkBack semantics, accessible targets and system reduced-motion preferences are non-negotiable.
- **Visual exclusions:** no neon gradients, indiscriminate glassmorphism, generic floating cards, dense filler labels, fabricated music metadata or unrequested continuous animations.

## Engineering and release boundaries

- Do **not** rotate the signing key, change production `applicationId`, remove historical preferences, or silently delete launcher data.
- Do **not** restore Classic HOME, Dock, widgets, folders or the legacy backup UI without a distinct approved scope.
- Do **not** introduce new permissions, tracking or network access merely to make the Editorial scene look occupied.
- For code-bearing UI changes, run unit tests, Android Lint, debug APK assembly and disposable-signature smoke test. Before release, verify main revision, pinned production certificate, version and published signed APK.
- Treat **implementation**, **CI pass**, **signed prerelease**, **installed-device QA** and **stable release** as separate states.
- Prioritize correct normal operation over visual fidelity if the two conflict; capture the conflict as a reviewable issue instead of disguising it.

## Definition of done for further visual work

1. State the observable difference from the supplied reference or a specific usability failure.
2. Change only the affected Compose/data paths, maintaining source architecture and preferences.
3. Add or update targeted regression checks where behavior is at risk.
4. Validate the exact changed revision in CI and on the intended Android device when physical claims are made.
5. Update the [roadmap](ROADMAP.md), [visual system](DESIGN.md), [changelog](CHANGELOG.md) and [device acceptance](DEVICE_ACCEPTANCE.md) as applicable; do not report unchecked items as complete.

This document is deliberately **a prioritised working direction**, not a new version number, approved redesign or assurance that proposed features exist.
