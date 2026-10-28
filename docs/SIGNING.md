# MONOLITH: stable APK signing

## Problem

Previously released MONOLITH APKs were signed by the temporary debug keystore on each GitHub Actions runner. Debug keys are ephemeral and must not be used to promise future in-place updates.

## Solution

From version 0.3.1-alpha forward, distributable APKs are built from the `release` build type and signed with one developer-controlled PKCS#12 key, alias `monolith`. The Android application ID remains `dev.monolith.launcher`.

The signing key is never committed to Git. Only encrypted GitHub Actions Secrets hold a CI copy:
- `MONOLITH_KEYSTORE_BASE64` — base64 of the PKCS#12 binary keystore
- `MONOLITH_KEY_PASSWORD` — keystore/key password

The first key is generated locally on the owner's trusted device with `scripts/setup-signing-termux.sh`, and the script stores a local backup at `~/.local/share/monolith-signing/`. Make a second, secure, offline backup of BOTH files. **If the signing key is lost, self-distributed APK updates cannot continue with a different key.**

## One-time setup (Termux)

1. `pkg install gh openjdk-17 openssl coreutils -y`
2. `gh auth login` and complete GitHub authentication in the browser.
3. Get the current MONOLITH repository and run `bash scripts/setup-signing-termux.sh` locally.
4. The script refuses to rotate an already configured key.
5. The setup script starts a new `main` workflow dispatch automatically. The signed APK appears as a prerelease on GitHub Releases after test/lint/compilation and Android signature verification succeed.

GitHub CLI encrypts secret values locally. Do not paste any signing password or keystore into public issues, commits, terminals with shared logging or chat.

## CI

PR validation uses a throwaway test-only signing key to verify that the `release` build can be signed, but never distributes that APK. Main builds publish a permanent-key signed release only after both secrets exist. Without secrets, CI still tests the code but **will not publish a pretend stable-signature APK**.

`MONOLITH_VERSION_CODE` is derived from the GitHub Actions run number (10000 + run number) for strictly increasing codes on successive release builds. `MONOLITH_VERSION_NAME` includes the run number as well.

## Update compatibility

Android requires the same application ID and compatible signing certificate (plus a nondecreasing versionCode). Earlier debug-signed MONOLITH 0.1–0.3 APKs are **not compatible** with the first permanently signed APK, unless the certificate happens to be the same (it will not be with this new key). A one-time uninstall/reinstall is normally required and removes all app-private MONOLITH data, including pinned apps, folders and widgets.

Keep the stock Android launcher installed until MONOLITH is verified on device. Later updates from one permanently signed release to another should install in place without clearing data.

## Verification

After a signed release build, verify its signing certificate through `apksigner verify --print-certs`. The signing certificate SHA-256 must match on every stable-signed release. Never share the private signing keystore or password.
