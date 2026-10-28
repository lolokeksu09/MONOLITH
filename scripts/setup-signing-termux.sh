#!/usr/bin/env bash
# Run once on your OWN trusted Termux device, not in public GitHub Actions.
# Generates a long-lived private key LOCALLY and stores only encrypted secrets on GitHub.
set -euo pipefail
umask 077

REPO="lolokeksu09/MONOLITH"
DIR="$HOME/.local/share/monolith-signing"
KEYSTORE="$DIR/monolith-release.p12"
PASSWORD_FILE="$DIR/keystore-password.txt"
ALIAS="monolith"

for command in gh keytool openssl base64 tr; do
  if ! command -v "$command" >/dev/null 2>&1; then
    printf 'Missing %s. In Termux: pkg install gh openjdk-17 openssl openssl-tool coreutils\n' "$command" >&2
    exit 1
  fi
done

if ! gh auth status -h github.com >/dev/null 2>&1; then
  echo "GitHub CLI is not authenticated. Run 'gh auth login' first." >&2
  exit 1
fi

existing="$(gh secret list -R "$REPO" --json name --jq '.[].name')"
has_key=false
has_password=false
if grep -qx 'MONOLITH_KEYSTORE_BASE64' <<< "$existing"; then has_key=true; fi
if grep -qx 'MONOLITH_KEY_PASSWORD' <<< "$existing"; then has_password=true; fi

if "$has_key" && "$has_password"; then
  echo "Both signing secrets already exist. Refusing to regenerate or overwrite the signing key."
  echo "Check GitHub Actions for a signed release. Do NOT rotate the key casually."
  exit 0
fi

if { "$has_key" || "$has_password"; } && { [[ ! -f "$KEYSTORE" ]] || [[ ! -f "$PASSWORD_FILE" ]]; }; then
  echo "Partial signing setup exists on GitHub but no matching local backup is present." >&2
  echo "Stopping: creating a new key could break updates. Recover the original private key." >&2
  exit 1
fi

if [[ -f "$KEYSTORE" && ! -f "$PASSWORD_FILE" ]] || [[ ! -f "$KEYSTORE" && -f "$PASSWORD_FILE" ]]; then
  echo "Found only one of the local signing files. Stopping to protect the signing key." >&2
  exit 1
fi

mkdir -p "$DIR"
chmod 700 "$DIR"

if [[ ! -f "$KEYSTORE" ]]; then
  export MONOLITH_KEY_PASSWORD="$(openssl rand -hex 32)"
  printf '%s' "$MONOLITH_KEY_PASSWORD" > "$PASSWORD_FILE"
  chmod 600 "$PASSWORD_FILE"
  keytool -genkeypair -noprompt -storetype PKCS12 \
    -keystore "$KEYSTORE" -alias "$ALIAS" \
    -keyalg RSA -keysize 4096 -validity 18250 \
    -dname "CN=MONOLITH, OU=Android, O=MONOLITH, C=US" \
    -storepass:env MONOLITH_KEY_PASSWORD -keypass:env MONOLITH_KEY_PASSWORD
  chmod 600 "$KEYSTORE"
else
  export MONOLITH_KEY_PASSWORD="$(cat "$PASSWORD_FILE")"
fi

keytool -list -keystore "$KEYSTORE" -alias "$ALIAS" \
  -storepass:env MONOLITH_KEY_PASSWORD >/dev/null

# gh encrypts both values locally before uploading; neither key nor password goes to git.
base64 "$KEYSTORE" | tr -d '\n' | gh secret set MONOLITH_KEYSTORE_BASE64 -R "$REPO"
printf '%s' "$MONOLITH_KEY_PASSWORD" | gh secret set MONOLITH_KEY_PASSWORD -R "$REPO"

unset MONOLITH_KEY_PASSWORD
echo "Permanent MONOLITH signing secrets configured."
echo "Private key backup: $KEYSTORE"
echo "Password backup: $PASSWORD_FILE"
echo "Store BOTH files in a second secure location, OFF the phone."
echo "Never upload them to a public repository, chat, or cloud folder without encryption."
echo "Requesting a new main-branch GitHub Actions build for the first signed APK..."
if ! gh workflow run android.yml -R "$REPO" --ref main; then
  echo "Could not dispatch workflow automatically. Open GitHub Actions > Android CI > Run workflow (main)." >&2
fi
