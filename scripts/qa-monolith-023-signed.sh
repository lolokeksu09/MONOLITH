#!/usr/bin/env bash
# Disposable Android 16 verification of exact permanently signed release.
set -euo pipefail
OUT="$PWD/qa-results-023"
mkdir -p "$OUT"
printf 'MONOLITH 0.23 permanently signed upgrade + Orbit quick-pin verification\n' > "$OUT/report.txt"

collect() {
  adb logcat -d -v threadtime > "$OUT/logcat.txt" 2>/dev/null || true
  grep -En -A 12 'FATAL EXCEPTION|ANR in dev\.monolith\.launcher|Process: dev\.monolith\.launcher' \
    "$OUT/logcat.txt" > "$OUT/crash-excerpts.txt" || true
}
trap collect EXIT

download() {
  local version="$1" hash="$2" apk="MONOLITH-$1-signed.apk"
  curl --fail --location --silent --show-error --retry 4 \
    "https://github.com/lolokeksu09/MONOLITH/releases/download/v$version/$apk" \
    --output "$RUNNER_TEMP/$apk"
  printf '%s  %s\n' "$hash" "$RUNNER_TEMP/$apk" | sha256sum -c -
  printf '%s verified\n' "$apk" >> "$OUT/report.txt"
}
download '0.22.0-beta.455' 'b0d53c526d3ff723b4e551aefc012787e1edf617fc64a4177f8e8fb3e43fd8da'
download '0.23.0-beta.460' 'c04dca00eed80487d29d2802f3e9bc98d8ae9565525b1b288466b3071bce2aa4'

echo "Android version $(adb shell getprop ro.build.version.release) SDK $(adb shell getprop ro.build.version.sdk)" >> "$OUT/report.txt"
adb shell wm size 1080x2400
adb shell wm density 420
adb install -r "$RUNNER_TEMP/MONOLITH-0.22.0-beta.455-signed.apk"
adb install -r "$RUNNER_TEMP/MONOLITH-0.23.0-beta.460-signed.apk"
adb shell dumpsys package dev.monolith.launcher | grep -E 'versionCode=|versionName=' | head -3 >> "$OUT/report.txt"
if ! adb shell dumpsys package dev.monolith.launcher | grep -Fq 'versionName=0.23.0-beta.460'; then
  echo 'FAIL: expected production version 0.23 not installed' >> "$OUT/report.txt"
  exit 1
fi
echo 'PASS: signed APK installed over signed 0.22, without uninstalling' >> "$OUT/report.txt"
adb shell cmd package set-home-activity dev.monolith.launcher/.MainActivity >> "$OUT/home-role.txt" 2>&1 || true
adb shell cmd role add-role-holder android.app.role.HOME dev.monolith.launcher >> "$OUT/home-role.txt" 2>&1 || true
adb shell am start -W -n dev.monolith.launcher/.MainActivity >> "$OUT/activity-start.txt"
sleep 3

dump() {
  adb shell uiautomator dump /sdcard/monolith023.xml >/dev/null 2>&1 || true
  adb pull /sdcard/monolith023.xml "$OUT/current.xml" >/dev/null 2>&1
}
capture() {
  local name="$1"
  adb exec-out screencap -p > "$OUT/$name.png"
  dump
  cp "$OUT/current.xml" "$OUT/$name.xml"
  echo "CAPTURE $name" >> "$OUT/report.txt"
}
find_label() {
  python3 - "$OUT/current.xml" "$1" <<'PY'
import sys,re,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
needle=sys.argv[2].casefold()
hits=[]
for n in root.iter('node'):
    a=n.attrib
    text=a.get('text','')
    desc=a.get('content-desc','')
    b=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',a.get('bounds',''))
    if not b: continue
    if needle not in text.casefold() and needle not in desc.casefold(): continue
    x1,y1,x2,y2=map(int,b.groups())
    rank=(0 if needle==desc.casefold() else 1 if needle==text.casefold() else 2)
    hits.append((rank,(x2-x1)*(y2-y1),(x1+x2)//2,(y1+y2)//2))
if hits:
    _,_,x,y=min(hits)
    print(x,y)
PY
}
tap() {
  local what="$1" xy
  dump
  xy="$(find_label "$what")"
  if [[ -z "$xy" ]]; then echo "FAIL: cannot find UI item $what" >> "$OUT/report.txt"; return 1; fi
  local x y
  read -r x y <<< "$xy"
  adb shell input tap "$x" "$y"
  echo "TAP $what at $x,$y" >> "$OUT/report.txt"
}
hold() {
  local what="$1" xy
  dump
  xy="$(find_label "$what")"
  if [[ -z "$xy" ]]; then echo "FAIL: cannot hold UI item $what" >> "$OUT/report.txt"; return 1; fi
  local x y
  read -r x y <<< "$xy"
  adb shell input swipe "$x" "$y" "$x" "$y" 850
  echo "LONG PRESS $what at $x,$y" >> "$OUT/report.txt"
}
assert_visible() {
  dump
  python3 - "$OUT/current.xml" "$1" <<'PY'
import sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
needle=sys.argv[2].casefold()
labels=[a.get('text','').casefold()+' '+a.get('content-desc','').casefold()
        for n in root.iter('node') for a in [n.attrib]]
if not any(needle in v for v in labels):
    raise SystemExit('FAIL: expected UI text not visible: '+sys.argv[2])
PY
  echo "PASS UI text: $1" >> "$OUT/report.txt"
}
assert_absent() {
  dump
  python3 - "$OUT/current.xml" "$1" <<'PY'
import sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
needle=sys.argv[2].casefold()
labels=[a.get('text','').casefold()+' '+a.get('content-desc','').casefold()
        for n in root.iter('node') for a in [n.attrib]]
if any(needle in v for v in labels):
    raise SystemExit('FAIL: unwanted UI text still visible: '+sys.argv[2])
PY
  echo "PASS UI hidden: $1" >> "$OUT/report.txt"
}

capture '01_home'
tap 'ALL APPS'
sleep 2
capture '02_drawer'
hold 'Calendar'
sleep 2
capture '03_context_sheet'
assert_visible 'Pin to Orbit'
tap 'Pin to Orbit'
sleep 1
capture '04_pinned'
assert_visible 'Remove from Orbit'
assert_visible 'Selected: 1 of 15'
tap 'Remove from Orbit'
sleep 1
capture '05_unpinned'
assert_visible 'Pin to Orbit'
assert_visible 'Selected: 0 of 15'

# Re-pin to verify the filtered customizer after dismissing the sheet.
tap 'Pin to Orbit'
sleep 1
adb shell input keyevent 4
sleep 1
tap 'Settings'
sleep 1
tap 'Customize orbit apps'
sleep 2
capture '06_orbit_customizer'
assert_visible 'Pinned only'
tap 'Pinned only'
sleep 1
capture '07_only_pinned'
assert_visible 'Calendar'
assert_absent 'Camera'
tap 'All applications'
sleep 1
assert_visible 'Camera'
echo 'PASS: long-press pin/unpin, selected count, and pinned-only filter on Android 16' >> "$OUT/report.txt"
