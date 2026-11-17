#!/usr/bin/env bash
# Collect reproducible, non-destructive MONOLITH hardware evidence.
# Usage:
#   bash scripts/collect-device-acceptance.sh [preview|production] [report-file] [--launch]
# Optional environment: ADB_SERIAL=host:port (for a selected connected device).
# --launch opens the selected launcher ACTIVITY, but does not set default HOME,
# force-stop anything, wipe data, grant permissions, or install a package.
set -euo pipefail

mode="${1:-preview}"
case "$mode" in
  preview) package="dev.monolith.launcher.preview" ;;
  production) package="dev.monolith.launcher" ;;
  *) echo "Usage: $0 [preview|production] [report-file] [--launch]" >&2; exit 2 ;;
esac

output="${2:-monolith-device-acceptance.txt}"
launch="${3:-}"
if [[ -n "$launch" && "$launch" != "--launch" ]]; then
  echo "Third argument may only be --launch" >&2
  exit 2
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required on the machine connected to the test device" >&2
  exit 1
fi
adb_cmd=(adb)
if [[ -n "${ADB_SERIAL:-}" ]]; then adb_cmd+=(-s "$ADB_SERIAL"); fi
if [[ "$("${adb_cmd[@]}" get-state 2>/dev/null)" != "device" ]]; then
  echo "No authorized connected Android device" >&2
  exit 1
fi
if ! "${adb_cmd[@]}" shell pm path "$package" | grep -q '^package:'; then
  echo "Selected MONOLITH package is not installed: $package" >&2
  exit 1
fi

# The report captures system model/build and frame times, not app data or IDs.
# With --launch, the current foreground activity changes temporarily.
{
  printf 'MONOLITH P5 acceptance report\n'
  printf 'UTC timestamp: %s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  printf 'Package: %s\n' "$package"
  printf '\n=== DEVICE ===\n'
  for prop in ro.product.model ro.build.version.release ro.build.fingerprint; do
    printf '%s: ' "$prop"
    "${adb_cmd[@]}" shell getprop "$prop" | tr -d '\r'
  done
  printf '\n=== DISPLAY ===\n'
  "${adb_cmd[@]}" shell wm size
  "${adb_cmd[@]}" shell wm density
  printf 'font_scale: '
  "${adb_cmd[@]}" shell settings get system font_scale
  printf '\n=== HOME ROLE HOLDER (READ ONLY) ===\n'
  "${adb_cmd[@]}" shell cmd role holders android.app.role.HOME || true
  printf '\n=== INSTALLED APK ===\n'
  "${adb_cmd[@]}" shell pm path "$package"
  "${adb_cmd[@]}" shell dumpsys package "$package" | grep -m 3 -E 'versionCode=|versionName=|firstInstallTime=' || true
  if [[ "$launch" == "--launch" ]]; then
    printf '\n=== ACTIVITY START (WARM, NOT COLD) ===\n'
    "${adb_cmd[@]}" shell am start -W -n "$package/dev.monolith.launcher.MainActivity"
  fi
  printf '\n=== FRAMESTATS (IF PROCESS HAS RENDERED) ===\n'
  "${adb_cmd[@]}" shell dumpsys gfxinfo "$package" framestats || true
  printf '\n=== PENDING MANUAL CHECKS ===\n'
  printf 'Orbit gestures, launch/update persistence, settings, HOME recovery, TalkBack: NOT RUN by this script\n'
} > "$output"
printf 'Wrote %s\n' "$output"
