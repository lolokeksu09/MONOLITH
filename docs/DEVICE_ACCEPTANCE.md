# MONOLITH 0.14 physical-device acceptance

Target: Lenovo Legion Y70 with Motorola Android 16 firmware. For each check, record date, firmware, tested signed APK tag and pass/fail evidence. CI success is not device success.

- [ ] Install the new APK over an existing permanently signed MONOLITH release; verify the same app ID, certificate, no uninstall and no crash.
- [ ] Select MONOLITH as HOME; repeatedly navigate home from apps and restore the stock launcher through Android settings.
- [ ] Reboot and confirm HOME behavior, drawer sorting, search and hidden-app preferences persist.
- [ ] Tap/hold the center of the AMOLED page: orbit opens and collapses without a visual snap, only visible installed apps are shown, icon taps launch reliably. Check spring motion, stagger and 48dp hit targets under 1.3x font scaling.
- [ ] Verify the complete vertical time/date and hold hint remain visible inside the left safe area on both orbit states; confirm the full media hint wraps without clipping in Russian and English.
- [ ] In Settings choose/reorder 15 orbit favorites, relaunch/reboot, verify order and hidden-app exclusion. Confirm a 16th selection is blocked.
- [ ] Check reduced-motion system setting: app icons and page transitions are not forced to animate.
- [ ] Swipe left/right between minimal/editorial pages; the grayscale rock backdrop appears offline and quick icons launch apps; back and HOME behave correctly.
- [ ] Swipe up or tap All Apps; search with keyboard, clear/disable search, change sorting, hide/unhide an app.
- [ ] Verify correct layout on landscape/rotation where supported, system bars and gesture-navigation insets, and large system font sizes.
- [ ] Media play/pause and next track work with a compatible active media player; no crash when no player is active.
- [ ] With a simulated package catalog error, the previous app catalog remains visible and Retry works.
- [ ] Verify that old Classic UI and its settings are no longer reachable; old user data must not be automatically cleared.
- [ ] Measure cold start, scroll responsiveness and orbit animation smoothness on the phone.

Never uninstall the stock HOME app; keep a working system fallback.
