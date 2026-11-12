# MONOLITH engineering rules

- Production identity is `dev.monolith.launcher`; maintain the existing signing certificate and GitHub Actions Secrets. Never commit keys or replace the keystore.
- 0.12+ has one desktop: `ReferenceHome`. Do not reintroduce Classic HOME, the legacy Dock or old widget/backup UI unless the user explicitly requests it.
- Keep compatibility with the existing drawer settings, hidden app IDs and launch statistics. Do not erase legacy SharedPreferences, or silently migrate/delete user data.
- Preserve safe HOME role selection and a route back to the stock Android launcher. Do not replace lock screen, recents or SystemUI.
- Version is configured through `monolithVersionName` in `gradle.properties`; CI uses it for release metadata.
- Run `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` and verify disposable-key signing before merging UI changes. Main signed releases must match the pinned SHA-256 certificate.
- Provide Russian and English resources, accessible touch targets and grounded performance claims. Do not claim device validation based on CI alone.
- Focus new UI work on accurate, performant Reference-mode interactions and test them on Lenovo Legion Y70 / Motorola Android 16.

## Locked native application icons — user decision 2026-11-12

**Do not redesign, reinterpret or decorate app-owned icons in future MONOLITH releases.**
Their appearance comes from the installed app's Android activity icon, not from MONOLITH.

- `AppIconStyle.ORIGINAL` is the default and canonical experience. Render the original application's artwork with original shapes, colours, alpha and proportions. Do not substitute generated icon packs, Material icons, replacements, custom masks, saturation filters, blur, tints, strokes, shadows, badges or new rounded-square/circle backplates.
- **Approved presentation baseline (0.21):** `DrawerAppTile` has a transparent 51dp host with 44dp source artwork; `OrbitCluster` has 38dp source artwork and independent 48dp hit targets. Do not change these visuals as part of a generic premium redesign. Existing composable motion may move/fade a shortcut, not edit its bitmap.
- Existing `Original / Mono / INK` settings are an explicit user-choice feature. They must never be silently selected, reset or forced by a redesign or upgrade. Only `Original` promises fully original colours; `Mono` and `INK` are intentional opt-in exceptions, not design defaults. Preserve saved preference keys.
- Do not add icon containers or borders in All Apps or Orbit. Avoid extending the legacy Editorial shortcut frame. Layout and typography around icons may evolve; application artwork may not.
- Any future icon presentation change requires an **explicit, separate user request**, its own scoped PR, visual review and corresponding update of this rule. No implicit exception for a "premium" redesign.
- Keep the corresponding CI contract check `python3 scripts/check_native_icon_fidelity.py` passing. Do not weaken/delete it simply to satisfy an unrelated design change.
