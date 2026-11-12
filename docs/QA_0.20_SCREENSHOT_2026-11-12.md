# MONOLITH 0.20 — screenshot-only Lenovo Y70 checkpoint (2026-11-12)

**Evidence class:** user-supplied screenshots, visual inspection only. **Not a physical acceptance pass, automated device test or release validation.** This record does not prove which APK/version is installed or that MONOLITH is registered as the default HOME app.

**Current published baseline (repository evidence, NOT screenshot evidence):** signed [v0.20.0-beta.428](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.20.0-beta.428), source commit `4cd84776b511e7bdf7f3b0838ebb90e7553f128a`; production release workflow #428 passed. Screenshot captures do not display the installed version, package ID or signing certificate.

## Evidence inventory

Four PNG files were supplied by the user in the conversation on 2026-11-12, each showing 09:25 in the Android status bar. Filenames here refer to the conversation uploads, **not** repository assets. They are deliberately not committed, to avoid publishing the user's installed application inventory or device status details.

| Screenshot | SHA-256 of supplied file | What is visible |
| --- | --- | --- |
| `6264.png` | `8e3ce1ad89fcc482d145ebd84860b69725ed67a1171e6cc07e8551c7ce7c843e` | Minimal HOME with collapsed center trigger, vertical time/press hint, media-key controls, lower navigation hints and a HOME-role CTA. |
| `6265.png` | `1735d255abfb65cf316ad3f8d300f2e8e616494e6d46aba8fb45c9a9fc6e5192` | Minimal HOME with expanded Orbit showing a 15-icon arrangement and a visible close action. |
| `6266.png` | `1bec8292d3c6458f508e39e40a38f5c46f0d41fa5226a9e5c5011f7d637219fd` | All Apps catalogue with 73-app count, four-column grid, top search bar and a frequency-sort label. |
| `6268.png` | `f73272bc9bfbd29580925bd23945089f2a0c0137a03558e7affd2bef51895356` | Settings with INK selected, Orbit density 15, search shown at top, four columns selected, frequency-sort label and hidden-app count 0. |

The supplied images are 1440 px wide; the Settings image is taller than the others. No inference about installed version, motion, input handling, default HOME role, device firmware or continuous operation follows from pixel dimensions.

## Screenshot-visible observations

| Scope | Result | Evidence boundary |
| --- | --- | --- |
| Minimal scene | **VISIBLE**: predominantly black canvas, vertical time and secondary interaction labels, top media controls, settings affordance and central closed trigger. | Screenshot confirms static presentation only. |
| Expanded Orbit | **VISIBLE**: 15 icons arranged around the central interaction region, close action present. No obvious overlap or clipping in the captured frame. | A still frame cannot verify animation, icon hit targets, launch behavior or background-tap dismissal. |
| All Apps | **VISIBLE**: 73-app label, search input, sorting control and four-column icon grid. Visible names render with single/two-line labels. | Search response, sorting correctness, hide/unhide and scrolling cannot be checked. |
| Settings | **VISIBLE**: INK/15/Top/4-column selected states and relevant settings descriptions. | Save/restart persistence, IME behavior and version identity cannot be checked. |
| Catastrophic layout failure | **NOT OBSERVED in four frames**: no clear blocking overlay, clipped primary control or missing screen. | Cannot extrapolate to other states, font scales, orientations, or devices. |

### Non-blocking visual review candidates (not confirmed functional defects)

1. **P1 · contrast:** secondary rail/footer hints are very low contrast against the black canvas in the captured state; check outdoor legibility and TalkBack rather than immediately changing colors.
2. **P1 · Settings hierarchy:** long explanatory copy and repeated full-width outlined controls create substantial visual density. Evaluate grouping and progressive disclosure using the existing design system, without altering current preference keys.
3. **P2 · Orbit visual weight:** icons appear relatively small compared with the available black space; preserve 48dp hit-target invariants and the approved 8/12/15 geometry while comparing against the original reference video.
4. **Do not treat INK fallbacks as an automatic bug:** a mixture of system monochrome icons and original-color icons is documented behavior for apps without a suitable monochrome layer.

The screenshot-visible `Make default HOME` action **does not establish** whether Android's HOME role is already granted; verify via the system role dialog/settings.

## Remaining acceptance gates — ALL OPEN

- [ ] **Exact installed identity:** verify `dev.monolith.launcher`, the actual `versionCode`/release tag and certificate fingerprint on the Lenovo Legion Y70; avoid inferring `v0.20.0-beta.428` from visual similarity.
- [ ] **Update in place:** install the permanently signed APK over an earlier permanently signed release **without uninstalling or clearing data**, and verify favorites, hidden apps, sorting preferences and usage history remain.
- [ ] **HOME resilience:** choose MONOLITH as HOME, open apps, use HOME/Back, reboot, then verify a safe return to the stock launcher. Never uninstall the stock HOME.
- [ ] **Orbit interaction:** closed 48dp trigger must be the only opener; expanded empty-backdrop tap closes it; 15 icons and media controls remain usable; verify tap/hold, interrupted movement and partial 1/8/12-app layouts.
- [ ] **Catalogue reliability:** rapidly type/clear RU and EN queries; change sorts; hide/unhide an app during in-flight filtering; verify no stale result, false empty state or UI-thread freeze.
- [ ] **One-handed/IME:** exercise both Top and Bottom search, focus, soft keyboard, system insets, clearing and scrolling.
- [ ] **Navigation and persistence:** back-stack behavior from Settings and All Apps, reboot/restart persistence and actual selected settings.
- [ ] **Accessibility and performance:** TalkBack, large fonts, reduced motion, actual gesture performance and measured responsiveness on the physical target.
- [ ] **Reference fidelity:** matched screenshot/video comparison against the original supplied design reference; these four screens are not a controlled matched reference set.

## Decision

**Screenshot-only visual checkpoint: partial PASS for rendering the four shown states; full device acceptance remains OPEN.** No Kotlin/Gradle/manifest, signing, app identity or release workflow modifications are warranted solely by this evidence. A new signed APK/release is not required to record this checkpoint.
