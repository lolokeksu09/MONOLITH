# MONOLITH physical-device acceptance checklist

Target: Lenovo Legion Y70 with Motorola Android 16 firmware. Record phone build, tested APK release tag, date and pass/fail evidence for each check. Do not mark these as passed from CI alone.

- [ ] Install an APK from a permanently signed GitHub prerelease without removing the stock launcher.
- [ ] Select MONOLITH as default HOME, open/close applications, return HOME repeatedly; restore the stock HOME.
- [ ] Restart the phone and verify preferred HOME, home layout, Dock, app visibility and sorting persist.
- [ ] Swipe-up and edge-back system gestures work as expected; verify navigation bar/gesture insets and no conflicts.
- [ ] Open drawer and bottom search; show/hide search, filter, clear text, change sort, dismiss keyboard and close drawer.
- [ ] Add, reorder and remove pinned apps and folders; restart and verify state.
- [ ] Add Android widgets from multiple providers, finish widget configuration, resize/reorder, cancel removal, confirm removal.
- [ ] Apply black background, static Android wallpaper and live wallpaper; check scrolling and battery behavior where applicable.
- [ ] Rotate and use split-screen where supported; verify widget layout, density, touch targets and keyboard visibility.
- [ ] Export JSON, deliberately alter layout, then import and verify restore. Verify corrupt/oversized import is rejected.
- [ ] Install a newer permanently signed prerelease **over** the existing one; confirm package ID, signature continuity and data retention.
- [ ] Measure cold startup, app-grid scrolling and memory use on the target device; record rather than infer performance.

Safety: do not uninstall the stock launcher; keep a working HOME fallback and an exported backup before reinstall/upgrade experiments.
