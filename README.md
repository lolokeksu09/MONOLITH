# MONOLITH

Native, local-first Android HOME launcher. Current candidate: **0.3.1-alpha**.

See [Stable signing setup](docs/SIGNING.md) for permanent release signing, update compatibility, and one-time Termux setup.

## Features

- AMOLED home, clock, launcher app drawer and search.
- Pinned applications, persistent folders, drag-to-reorder edit mode.
- Android AppWidgetHost picker/binding/configuration and widget removal.
- Local settings; optional system Home role selection.
- No account, advertising, root or Internet permission.

## Build

CI runs unit tests, Android lint, debug assembly, and a **release-signing smoke test with a disposable key**. Disposable test APKs are not intended for installation or distribution.

After the repository owner configures the two signing secrets, every successful `main` build signs the release APK with the same private key and publishes it to GitHub Releases as a prerelease.

## Installation safety

Keep the stock launcher installed. Select MONOLITH as the default Home app only after testing. You can revert in Android's **Default apps > Home app** settings.

**Important:** 0.1–0.3 debug-signed APKs may require a one-time uninstall before installing permanently signed 0.3.1 because their signatures differ, and uninstall clears private MONOLITH data. After migration, future stable-signed updates can retain data.
