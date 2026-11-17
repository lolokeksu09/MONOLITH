# MONOLITH — All Apps screenshot follow-up

**Date:** 2026-11-17. **Scope:** All Apps panel only, after screenshots of v0.25.0 beta; no HOME, Settings or icon rework.

## Observed

At four columns / 94 installed apps, the screenshot showed a duplicated brand/title block, large vertical gaps above the grid and a prominent sort selector. There was no confirmed failure of app listing, search or gesture navigation.

## Patch contract

- Collapse the redundant **MONOLITH / Apps** stacked header to one 26sp title with existing Back and Settings actions. 48dp minimum targets stay intact.
- Trim whitespace above search, between search and count/sort, and between grid rows without changing icon artwork or tile layout.
- Keep the original divider, label hierarchy, black AMOLED surface, 56dp search minimum and up to 176dp sort selector. No new dependency, new setting or stored state mutation.
- Maintain Top/Bottom search, 180ms search settle behavior, unique-result keyboard Search launch and current sorting. The 3/4-column grid still displays two-line app labels; 5/6 columns retain one-line ellipsis.

## Verification

- [ ] Focused Compose test: title, search, sort change and Back navigation work after compacting header
- [ ] Full Android unit tests, lint, debug APK and native icon guard pass on exact branch commit
- [ ] Preview application ID and disposable signing smoke check pass
- [ ] Physical Lenovo: compare header and first 3 rows in four-column mode, both RU and EN; check 1.0 and enlarged font scale and gesture navigation
- [ ] Confirm 3, 5 and 6 column modes, Bottom search, IME and app long-press remain reachable

**Device acceptance and signed production release: NOT RUN by this change.** The signed v0.25.0-beta.487 remains the current main release; PR #55 independently handles opt-in INK bitmap fallback.
