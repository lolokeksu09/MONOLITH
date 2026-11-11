# MONOLITH physical-device acceptance

Target: Lenovo Legion Y70 with Motorola Android 16 firmware. For each check, record date, firmware, tested signed APK tag and pass/fail evidence. CI success is not device success.

**Current signed candidate:** [`v0.18.0-beta.400`](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.18.0-beta.400), from main commit `cdbda9baa68946ffbba22e300e3997c8ca887866`. [Android CI #400](https://github.com/lolokeksu09/MONOLITH/actions/runs/43060986709) completed successfully with pinned production signing. **No physical-device acceptance evidence is recorded yet.** Run all applicable historical checks for this exact signed candidate; record the installed version, certificate continuity, firmware, date and observed result.

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
