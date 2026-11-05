# MONOLITH 0.12: legacy feature removal

**Scope:** remove the old Classic HOME implementation, not the Android HOME role, app drawer, settings or security/update infrastructure.

## Removed from the application

- Classic HOME rendering, pinned tile grid and folder UI.
- Legacy Dock and add/move/remove shortcuts through the Dock.
- Android AppWidgetHost selection, binding, resizing, reordering and display UI.
- Old JSON home-layout export/import, including widget-related notices and options.
- Classic/Reference mode toggle and unused settings such as grid label/icon preferences.
- Dead persistence and tests specific to the removed Classic desktop.

## Preserved

- Reference HOME with two pages, animated orbital app cluster, dynamic weekday page and original artwork.
- Full app catalog, app launching, search, sort options, hide/unhide actions and successful-launch statistics.
- HOME role selection, handling repeated HOME intents, package-change refresh and safe fallback to the stock launcher.
- Existing application ID and stable release-signing certificate, so upgrades are possible without uninstalling.
- Existing on-device preference files from old Classic storage: **no code clears those values**. Removed functionality means old data is inaccessible, **not erased**.

## Upgrade consequences

The old widget host bindings and layout information are not presented in the 0.12 UI. If preservation matters, export the 0.11 home backup before installing 0.12. Existing exported old layout files are not importable into 0.12. Because 0.12 has a higher Android `versionCode`, simply installing 0.11 on top is not a supported rollback path.

No root, elevated permission, migration wiping data, signing key rotation or package name change was introduced.

## Quality gate

CI must pass for the exact revision being released. A successful build is not proof of on-device gesture safety; test the physical Motorola/Lenovo Android 16 handset with the stock launcher available as recovery.
