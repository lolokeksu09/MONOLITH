# MONOLITH — combined visual-QA Preview (17 November 2026)

**Source base:** production-signed `v0.25.0-beta.487` merge commit `2ce466624a1c17a8d27834f75dd0f4495038ed9b`.

This branch combines the **source and tests** from [PR #55](https://github.com/lolokeksu09/MONOLITH/pull/55) (INK), [PR #56](https://github.com/lolokeksu09/MONOLITH/pull/56) (drawer), [PR #58](https://github.com/lolokeksu09/MONOLITH/pull/58) (Settings) and [PR #59](https://github.com/lolokeksu09/MONOLITH/pull/59) (Orbit).

**Excluded:** PR #57 is a competing All Apps layout alternative, not another additive patch. Combining #56 and #57 would override the same drawer design. Keep #57 separate.

## Intent and boundaries

- **Only** the separate `dev.monolith.launcher.preview` Debug Preview is built for visual comparison. This build uses a **test certificate**, not the permanent production certificate. It does **not** upgrade the installed `dev.monolith.launcher` or authorize a signed release.
- No original app-icon artwork, HOME-role logic, existing preferences, app identifiers or release signing configuration changes.
- Automated Android unit/Compose tests, icon-fidelity guard, Android Lint and APK identity/signing smoke checks must all pass against the **integration commit**. Success on the individual PRs does not establish combined correctness.
- **Physical Lenovo checks remain NOT RUN** unless the user provides observations/screenshots. A green CI is not a screenshot or measured touch/gesture result.

## Lenovo acceptance — one installed Preview

- [ ] Verify the installed Preview package is `dev.monolith.launcher.preview` and the signed production `dev.monolith.launcher` remains installed and can be restored as HOME.
- [ ] **INK / Original:** compare `Диск` and another previously faint icon. Broken/transparent native monochrome icon should use the unchanged original fallback. Original mode must look exactly as before. Capture screenshots in both modes.
- [ ] **All Apps:** single-line title; Back/Settings controls; search Top, Bottom and disabled; long labels in 4/6 columns, search IME launches only a single exact candidate.
- [ ] **Settings:** tapping the **label** of Show Search toggles the setting; Search Position is hidden when off and returns when on, with prior option intact; return HOME action works. Test TalkBack Switch announcement.
- [ ] **Orbit:** compare visible collapsed ring and label with v0.25.0; tap center and collapse; hold and drag to icon; cancel outside icons; swipe up to All Apps. Check TalkBack Button role, large font, reduced motion.
- [ ] **HOME role:** on production package, `adb shell cmd role holders android.app.role.HOME` is a **read-only** diagnostic. If it is not `dev.monolith.launcher`, the Set Default prompt is expected. Do not grant or modify role automatically.
- [ ] Report all clipped text, unexpected overlays, missing hit targets, jank and accidental launches. Record exact firmware, version, density/font scale and screenshots.

The next signed production beta may be considered **only after** the combined CI passes and the critical Lenovo visual/touch comparisons above are reviewed. Merging a Preview PR does not itself publish a signed release.
