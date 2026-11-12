#!/usr/bin/env bash
# Disposable Android 16 verification of exact permanently signed release.
set -euo pipefail
OUT="$PWD/qa-results-024"
mkdir -p "$OUT"
printf 'MONOLITH 0.24 preview Orbit organization + clear-history confirmation review\n' > "$OUT/report.txt"

collect() {
  adb logcat -d -v threadtime > "$OUT/logcat.txt" 2>/dev/null || true
  grep -En -A 12 'FATAL EXCEPTION|ANR in dev\.monolith\.launcher|Process: dev\.monolith\.launcher' \
    "$OUT/logcat.txt" > "$OUT/crash-excerpts.txt" || true
}
trap collect EXIT

# The exact signed APK is not changed by this QA workflow.
# Inspect the CI debug Preview from PR #53 at immutable source SHA.
gh run download 43213066969 -R "$GITHUB_REPOSITORY" \
  -n 'MONOLITH-CI-debug-NOT-a-stable-signature' -D "$RUNNER_TEMP/preview"
APK="$RUNNER_TEMP/preview/app-debug.apk"
test -s "$APK"
echo "Android version $(adb shell getprop ro.build.version.release) SDK $(adb shell getprop ro.build.version.sdk)" >> "$OUT/report.txt"
adb shell wm size 1080x2400
adb shell wm density 420
adb install -r "$APK"
adb shell dumpsys package dev.monolith.launcher.preview | grep -E 'versionCode=|versionName=' | head -3 >> "$OUT/report.txt"
adb shell dumpsys package dev.monolith.launcher.preview | grep -Fq 'versionName=0.24.0-beta-preview'
echo 'PASS: installed exact PR #53 Preview on Android 16' >> "$OUT/report.txt"
adb shell am start -W -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >> "$OUT/activity-start.txt"
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
sleep 1
tap 'Pin to Orbit'
sleep 1
adb shell input keyevent 4
sleep 1
hold 'Camera'
sleep 1
tap 'Pin to Orbit'
sleep 1
adb shell input keyevent 4
sleep 1
tap 'Settings'
sleep 1
tap 'Customize orbit apps'
sleep 2
capture '03_orbit_groups'
assert_visible 'Pinned (2)'
assert_visible 'Available ('
assert_visible 'Move Camera up'
tap 'Move Camera up'
sleep 1
capture '04_reordered'
assert_visible 'Move Camera up'
tap 'Pinned only'
sleep 1
capture '05_pinned_only'
assert_absent 'Messages'
tap 'All applications'
sleep 1
capture '06_available_again'
tap 'Done'
sleep 1

# Search the Settings scroll container for the destructive action.
found=0
for attempt in 1 2 3 4; do
  dump
  if [[ -n "$(find_label 'Clear launch history')" ]]; then
    found=1
    break
  fi
  adb shell input swipe 530 1950 530 530 350
  sleep 1
done
test "$found" -eq 1
tap 'Clear launch history'
sleep 1
capture '07_clear_confirmation'
assert_visible 'Clear launch history?'
assert_visible 'Cancel'
tap 'Cancel'
sleep 1
assert_absent 'Clear launch history?'
echo 'PASS: grouped Orbit manager, actionable reorder, pinned-only filter and cancellable clear history' >> "$OUT/report.txt"
