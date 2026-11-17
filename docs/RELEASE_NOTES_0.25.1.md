# MONOLITH 0.25.1 beta — INK, App Drawer, Settings & Orbit

**Beta update** to `v0.25.0-beta.487`, signed with MONOLITH's existing permanent production certificate. Android application ID: `dev.monolith.launcher`.

## Fixed
- **INK icon rendering:** nearly transparent native monochrome icons fall back to their untouched original activity icon; visible monochrome alpha masks are tinted consistently.
- **Settings search toggle:** tapping the entire settings row, including its label, toggles app-drawer search. Search-position options stay available when search is enabled.

## Improved
- **All Apps:** compact one-line title, less duplicated header decoration, and a search field with one accessible label. Search placement (Top or Bottom), sorting and existing keyboard Search behavior are retained.
- **Minimal HOME / Orbit:** higher-contrast central control (approximately 34dp visible ring inside the unchanged 48dp touch target), more legible caption and clearer TalkBack Button semantics for opening and collapsing Orbit.
- **Settings:** fewer redundant helper paragraphs and clearer Russian "На главный экран" navigation label.

## Preserved
- Application-owned icons remain unchanged in **Original** mode and for valid artwork. MONO and opt-in INK modes remain selectable, with saved choices intact.
- Existing favorites, hidden apps, search/sort preferences, gestures, app IDs, local data and Android HOME-role selection are unchanged.
- The Preview build remains `dev.monolith.launcher.preview` and **is not** a production update. The published release APK is the actual signed `dev.monolith.launcher` build.

## Verification and outstanding limitations
- The combined PR #60 source passed Android CI [#44055318432](https://github.com/lolokeksu09/MONOLITH/actions/runs/44055318432): unit/Compose tests, Android Lint, icon-fidelity guard, Preview identity, Debug APK and disposable-key smoke signing.
- Production release workflow **must verify the pinned permanent certificate SHA-256** before a release can be published. A passing disposable-key check alone is insufficient.
- **No physical Lenovo validation has been completed** for the combined build. INK visual fidelity for "Диск", interaction with TalkBack, Orbit drag/swipe arbitration, typography, animation smoothness and retained preferences after install-over remain to be checked.
- This is **not a stable release**. Keep the stock launcher installed. **Install over** the currently signed MONOLITH to retain its app data; do not uninstall the old version first.

See [combined Lenovo QA checklist](QA_COMBINED_PREVIEW_2026-11-17.md) and [device acceptance](DEVICE_ACCEPTANCE.md).
