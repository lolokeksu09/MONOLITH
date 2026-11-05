# MONOLITH 0.15 physical-device acceptance

Target: Lenovo Legion Y70 with Motorola Android 16 firmware. For each check, record date, firmware, tested signed APK tag and pass/fail evidence. CI success is not device success.

Release `v0.15.0-beta.380` is published, but this checklist remains open until real results are recorded. For a future 0.16 candidate, rerun every applicable item below and the additional per-requirement checks in [the 0.16 plan](PLAN_0.16.md). That plan is a test reference, not evidence that its unimplemented features work.

- [ ] Install the new APK over an existing permanently signed MONOLITH release; verify the same app ID, certificate, no uninstall and no crash.
- [ ] Select MONOLITH as HOME; repeatedly navigate home from apps and restore the stock launcher through Android settings.
- [ ] Reboot and confirm HOME behavior, drawer sorting, search and hidden-app preferences persist.
- [ ] Tap/hold the center of the AMOLED page: orbit opens and collapses without a visual snap, only visible installed apps are shown, icon taps launch reliably. Check the shared spring, synchronized trigger/close-label fades and 48dp hit targets under 1.3x font scaling. Interrupt opening with Back or HOME and verify reversal stays smooth; taps during overlapping movement must not launch an app.
- [ ] Verify the complete vertical time/date and hold hint remain visible inside the left safe area on both orbit states; confirm the full media hint wraps without clipping in Russian and English.
- [ ] In Settings choose/reorder 15 orbit favorites, relaunch/reboot, verify order and hidden-app exclusion. Confirm a 16th selection is blocked.
- [ ] Check reduced-motion system setting: app icons, page indicator and page transitions are not forced to animate. Confirm the indicator crossfades when motion is enabled.
- [ ] With TalkBack, verify the orbit announces expanded/collapsed state and both orbit and editorial icons expose Open application and Application actions in Russian and English. Confirm action hints remain readable on the physical AMOLED screen.
- [ ] Swipe left/right between minimal/editorial pages; the grayscale rock backdrop appears offline and quick icons launch apps; back and HOME behave correctly.
- [ ] Swipe up or tap All Apps; search with keyboard, clear/disable search, change sorting, hide/unhide an app.
- [ ] Verify correct layout on landscape/rotation where supported, system bars and gesture-navigation insets, and large system font sizes.
- [ ] Media play/pause and next track work with a compatible active media player; no crash when no player is active.
- [ ] With a simulated package catalog error, the previous app catalog remains visible and Retry works.
- [ ] Verify that old Classic UI and its settings are no longer reachable; old user data must not be automatically cleared.
- [ ] Measure cold start, scroll responsiveness and orbit animation smoothness on the phone. Compare the first orbit opening after process start with later openings and check for late icon pop-in or frame drops.

Never uninstall the stock HOME app; keep a working system fallback.
