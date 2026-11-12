# MONOLITH physical-device acceptance

Target: Lenovo Legion Y70 with Motorola Android 16 firmware. For each check, record date, firmware, tested signed APK tag and pass/fail evidence. CI success is not device success.

**Current signed candidate:** [v0.20.0-beta.428](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.20.0-beta.428), from source commit `4cd84776b511e7bdf7f3b0838ebb90e7553f128a`. [Signed-release CI #428](https://github.com/lolokeksu09/MONOLITH/actions/runs/43082198569) completed successfully with pinned production signing. **No physical-device acceptance evidence is recorded yet.** Run applicable historical checks for this exact signed APK; record the installed version, certificate continuity, firmware, date and observed result.

**2026-11-12 screenshot-only checkpoint:** Four user-provided images show collapsed/expanded Orbit, All Apps and Settings. See [the screenshot QA evidence record](QA_0.20_SCREENSHOT_2026-11-12.md). This is a **partial visual rendering observation, not a hardware QA pass**: installed release identity, gestures, install-over, HOME recovery, TalkBack and actual responsiveness remain unverified. **All device-test checkboxes below remain open.**

## 0.20 signed beta — catalog responsiveness and Orbit dismissal (not device-tested)

**Exact signed candidate:** [v0.20.0-beta.428](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.20.0-beta.428), built from merged [PR #43](https://github.com/lolokeksu09/MONOLITH/pull/43); requirements in [PLAN_0.20.md](PLAN_0.20.md). All hardware checks below remain open, regardless of CI success.

- [ ] Search a large installed-app list in both RU and EN with Top and Bottom search. Confirm keyboard input stays responsive and the filtered result matches the settled query. Record actual observed latency if available; do not infer FPS from architecture.
- [ ] Rapidly edit queries and switch A–Z / Z–A / frequent / recent / installation sorting; confirm no stale old results appear and no false `No applications found` state appears while still calculating.
- [ ] Hide an app from its action sheet and immediately inspect the drawer: that hidden ID must not reappear while a previous background result completes. Repeat after refresh/reboot; test unhide.
- [ ] Search for an impossible app, use the visible Clear search action, verify the full list returns and focus/IME behave in Top and Bottom layouts. Verify clear icon remains usable.
- [ ] Expand Orbit, tap an empty scene area to dismiss, and separately verify that icon taps, icon long-press, system media controls, swipe navigation and explicit Close work. A tap outside the **closed** 48dp center trigger must not open Orbit.
- [ ] Repeat expanded Orbit with a single centrally placed icon, then with 8 / 12 / 15 icons, to ensure the background action does not block shortcuts.
- [ ] Test TalkBack reading order for background dismiss and empty-search clear, enlarged fonts, reduced motion, safe drawing insets and IME visibility.
- [ ] Before marking installed-update acceptance, install a **permanently signed** candidate over the previously signed `dev.monolith.launcher` without uninstalling. Verify the pinned certificate, saved settings, favorites and stock-HOME recovery.

## 0.19 signed beta — not device-tested

Apply these checks to the exact signed [v0.19.0-beta.414](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.19.0-beta.414) build merged through [PR #41](https://github.com/lolokeksu09/MONOLITH/pull/41). Do not mark complete based on CI or successful signing alone.

- [ ] Tap the center of the closed Minimal screen; the 48dp trigger expands Orbit. Tap the surrounding former 304dp invisible region; it must **not** unexpectedly toggle Orbit.
- [ ] With exactly one available Orbit shortcut, expand and tap its central icon; verify the actual application opens rather than collapsing the Orbit.
- [ ] Long-press an Orbit icon, inspect the app actions sheet, press Back to dismiss it and verify navigation is stable. Check opening/closing interruptions, rapid taps, TalkBack and reduced motion.
- [ ] In Settings choose Top then Bottom search. Confirm Bottom is reachable with one hand while preserving focus/clear, search result filtering, sorting and hidden-app exclusion. Reboot and verify saved placement. Previous installations must remain Top without user action.
- [ ] Open Bottom search with the IME, type and clear, press the keyboard Search action, inspect system insets and keyboard overlap. Repeat in Russian and English at default and large font size. Test Top and search-disabled paths.
- [ ] Use Android Back through HOME → Settings → Orbit apps (or Hidden apps) → Settings → HOME; then All Apps → Settings → All Apps. Confirm no accidental exit or lost input.
- [ ] With no visible shortcuts on Editorial, use the explicit All Apps action and confirm the drawer opens. With shortcuts, test real launch and long-press.
- [ ] Check the All Apps icon tile with TalkBack and confirm the app name is not announced twice from the icon and text.
- [ ] Compare Minimal / Orbit / Editorial / All Apps screenshots and actual motion to the supplied video on Lenovo Legion Y70 / Motorola Android 16. Record real performance rather than assuming a speedup.
- [ ] Install the *permanently signed* `MONOLITH-0.19.0-beta.414-signed.apk` over an earlier same-certificate `dev.monolith.launcher` release (versionCode `10414`) without uninstalling. Confirm certificate continuity, HOME role recovery, sorting/search preferences, favorites, hidden apps and launch history. Debug Preview cannot update the production app.

## 0.18 signed release / optional debug Preview checks

- [ ] **Production update:** install the permanently signed `MONOLITH-0.18.0-beta.400-signed.apk` over an existing same-certificate MONOLITH installation (no uninstall); verify `dev.monolith.launcher`, versionCode `10400`, preserved settings, favorites, hidden-app list and launch history. Confirm HOME is still recoverable.
- [ ] **Optional debug Preview:** install `MONOLITH Preview` beside production with package `dev.monolith.launcher.preview`; verify separate app data and that the original launcher is intact. Preview is **not** the release APK.
- [ ] Check the Minimal screen against the provided video: quiet media controls upper left, vertical clock at left, uncluttered empty center, subtle Settings access.
- [ ] Expand Orbit with 1, 3, 5, 7, 9, 10, 11, 13, 14 and 15 installed apps. Check centering, symmetry, 48dp tap targets, no premature taps, and appearance modes.
- [ ] Check the Editorial screen against the video: one oversized localized weekday, right-side original rock art, compact quick apps and greeting. Test RU/EN and large fonts.
- [ ] Verify gesture transitions, reduction of motion, HOME role selection/recovery, app launches, local settings and device insets.
- [ ] Verify 0.17 drawer search, clear, sorting, screen-reader state and app-catalog error behavior.

## 0.16 / INK additions

- [ ] Select Original, Mono and INK in Settings; verify orbit, editorial quick access and All Apps use the same mode after process restart and reboot.
- [ ] On Android 13+, compare apps with and without native monochrome adaptive-icon layers. INK must show supplied monochrome artwork and visibly retain Original for unsupported apps; no blank icons or generic brand replacements.
- [ ] Confirm an upgrade with no new preference keys starts at Original / 15 and retains existing drawer, hidden-app, launch-statistics and favorite data.
- [ ] Select 8, 12 and 15 visible orbit apps. Restart and confirm the choice; switch 15 → 8 → 15 and verify all original favorites and their order return without deletion.
- [ ] At each density, verify icon spacing, launch/actions, cold first opening, interrupted/reversed motion and no accidental tap while targets overlap.
- [ ] Review editorial in Russian and English at default and 1.3x fonts: one oversized localized day abbreviation, no repeated `THURSDAY` / `Today`, readable date/time/greeting and offline rock.
- [ ] Review All Apps in Russian and English with IME open/closed, clear/disabled search, every sort, hide/unhide, empty/error catalog and large fonts. Compare provisional top search with one-handed reach before finalizing its position.
- [ ] Inspect Back, Settings, search, clear, sorting, close and media controls for consistent artwork, accessible names, press/focus/disabled states and 48dp targets.
- [ ] With no player and a compatible real player, verify honest `status unknown` copy and working play/pause/next dispatch. Confirm no track/artist fabrication and no new permission prompt.
- [ ] Repeat TalkBack and reduced-motion checks for every new mode/density and all added controls.

## 0.15 regression checklist

- [ ] Install the new APK over an existing permanently signed MONOLITH release; verify the same app ID, certificate, no uninstall and no crash.
- [ ] Select MONOLITH as HOME; repeatedly navigate home from apps and restore the stock launcher through Android settings.
- [ ] Reboot and confirm HOME behavior, drawer sorting, search and hidden-app preferences persist.
- [ ] Tap/hold the center of the AMOLED page: orbit opens and collapses without a visual snap, only visible installed apps are shown, icon taps launch reliably. Check shared spring, synchronized trigger/close-label fades and 48dp hit targets under 1.3x font scaling. Interrupt opening with Back or HOME; taps during overlapping movement must not launch an app.
- [ ] Verify complete vertical time/date and hold hint remain visible inside the left safe area on both orbit states; confirm media hint wraps without clipping in Russian and English.
- [ ] In Settings choose/reorder 15 orbit favorites, relaunch/reboot, verify order and hidden-app exclusion. Confirm a 16th selection is blocked.
- [ ] Check reduced-motion system setting: app icons, page indicator and page transitions are not forced to animate. Confirm indicator crossfades when motion is enabled.
- [ ] With TalkBack, verify orbit expanded/collapsed state and Open application / Application actions in Russian and English. Confirm action hints remain readable on the physical AMOLED screen.
- [ ] Swipe left/right between minimal/editorial pages; grayscale rock appears offline and quick icons launch apps; Back and HOME behave correctly.
- [ ] Swipe up or tap All Apps; search with keyboard, clear/disable search, change sorting, hide/unhide an app.
- [ ] Verify landscape/rotation where supported, system bars, gesture-navigation insets and large system font sizes.
- [ ] Media play/pause and next track work with a compatible media player; no crash when no player is active.
- [ ] With a simulated package catalog error, previous app catalog remains visible and Retry works.
- [ ] Verify old Classic UI and its settings are unreachable; old user data must not be automatically cleared.
- [ ] Measure cold start, scroll responsiveness and orbit animation smoothness. Compare first opening after process start with later openings and check late icon pop-in or frame drops.

Never uninstall the stock HOME app; keep a working system fallback.
