#!/usr/bin/env bash
# Disposable Android 16 emulator inspection of permanently signed MONOLITH.
# No credentials, APK signing keys, private user data or production device.
set -euo pipefail
OUT="$PWD/qa-results"
mkdir -p "$OUT"
echo 'MONOLITH 0.22 Preview / Android 16 emulator QA' > "$OUT/report.txt"
echo "Run: $GITHUB_RUN_ID" >> "$OUT/report.txt"

collect_logs() {
  adb logcat -d -v threadtime > "$OUT/logcat.txt" 2>/dev/null || true
  grep -En -A 16 'FATAL EXCEPTION|ANR in dev\.monolith\.launcher|Process: dev\.monolith\.launcher|AndroidRuntime' "$OUT/logcat.txt" > "$OUT/crash-excerpts.txt" || true
  adb shell dumpsys activity activities > "$OUT/activities.txt" 2>/dev/null || true
  echo "Capture count: $(find "$OUT" -maxdepth 1 -name '*.png' | wc -l)" >> "$OUT/report.txt"
}
trap collect_logs EXIT

echo 'Downloading the exact-head CI debug Preview artifact (not a production update)' >> "$OUT/report.txt"
gh run download 43195012753 -R "$GITHUB_REPOSITORY" \
  -n 'MONOLITH-CI-debug-NOT-a-stable-signature' -D "$RUNNER_TEMP/preview"
APK="$RUNNER_TEMP/preview/app-debug.apk"
test -s "$APK"
echo "Device: $(adb shell getprop ro.build.version.release) / SDK $(adb shell getprop ro.build.version.sdk)" >> "$OUT/report.txt"
adb shell wm size 1080x2400
adb shell wm density 420
adb shell settings put system accelerometer_rotation 0 || true
adb install -r "$APK"
adb shell dumpsys package dev.monolith.launcher.preview.preview | grep -E 'versionCode=|versionName=' | head -3 >> "$OUT/report.txt"
if ! adb shell dumpsys package dev.monolith.launcher.preview.preview | grep -Fq 'versionName=0.22.0-beta-preview'; then
  echo 'FAIL: 0.22 Preview not installed' >> "$OUT/report.txt"
  exit 1
fi
echo 'PASS: exact-head 0.22 Preview installed on Android 16 emulator' >> "$OUT/report.txt"

# Start explicit activity and attempt HOME role selection.
adb shell cmd package set-home-activity dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity \
  >> "$OUT/home-role.txt" 2>&1 || true
adb shell cmd role add-role-holder android.app.role.HOME dev.monolith.launcher.preview \
  >> "$OUT/home-role.txt" 2>&1 || true
adb shell am force-stop dev.monolith.launcher.preview || true
adb shell am start -W -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >> "$OUT/activity-start.txt" 2>&1
sleep 4

capture() {
  local label="$1"
  adb exec-out screencap -p > "$OUT/$label.png"
  adb shell uiautomator dump /sdcard/monolith_qa.xml >/dev/null 2>&1 || true
  adb pull /sdcard/monolith_qa.xml "$OUT/$label.xml" >/dev/null 2>&1 || true
  echo "CAPTURE: $label" >> "$OUT/report.txt"
}
tap_label() {
  local label="$1" coords
  adb shell uiautomator dump /sdcard/monolith_qa.xml >/dev/null 2>&1 || true
  adb pull /sdcard/monolith_qa.xml "$OUT/current.xml" >/dev/null 2>&1 || true
  coords="$(python3 - "$OUT/current.xml" "$label" <<'PY'
import re,sys,xml.etree.ElementTree as ET
try:
  root=ET.parse(sys.argv[1]).getroot()
  needle=sys.argv[2].casefold()
  hits=[]
  for n in root.iter('node'):
    a=n.attrib
    label=(a.get('text','')+' '+a.get('content-desc','')).casefold()
    match=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',a.get('bounds',''))
    if needle in label and match:
      x1,y1,x2,y2=map(int,match.groups())
      hits.append(((x2-x1)*(y2-y1), (x1+x2)//2, (y1+y2)//2))
  if hits:
    _,x,y=min(hits,key=lambda a:a[0])
    print(x,y)
except Exception:
  pass
PY
)"
  if [[ -z "$coords" ]]; then
    echo "MISSING UI target: $label" >> "$OUT/report.txt"
    return 1
  fi
  read -r x y <<< "$coords"
  echo "TAP $label: $x,$y" >> "$OUT/report.txt"
  adb shell input tap "$x" "$y"
}

capture '01_home_collapsed'
adb shell input tap 540 1200
sleep 2
capture '02_orbit_expanded'
adb shell input tap 880 1700
sleep 2
capture '03_orbit_dismissed'

if ! tap_label 'ALL APPS'; then
  adb shell input swipe 540 2020 540 700 400
  echo 'Used swipe-up fallback for drawer' >> "$OUT/report.txt"
fi
sleep 2
capture '04_all_apps'

if tap_label 'Search applications'; then
  adb shell input text 'Settings'
  sleep 2
  capture '05_drawer_search'
  adb shell input keyevent 4 || true
  adb shell input keyevent 4 || true
  sleep 1
fi
if ! tap_label 'Settings'; then
  echo 'Settings button not found in current hierarchy' >> "$OUT/report.txt"
fi
sleep 2
capture '06_settings'

if tap_label 'Back'; then
  sleep 1
  capture '07_back_from_settings'
fi

adb shell am start -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >/dev/null
sleep 1
if ! tap_label 'EDITORIAL'; then
  adb shell input swipe 950 1200 150 1200 350
  echo 'Used horizontal-swipe fallback for Editorial' >> "$OUT/report.txt"
fi
sleep 2
capture '08_editorial'


# Second acceptance pass: verify actual All Apps -> Settings -> Back navigation.
adb shell am force-stop dev.monolith.launcher.preview || true
adb shell am start -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >/dev/null
sleep 2
tap_label 'ALL APPS'
sleep 2
capture '09_drawer_before_settings'
tap_label 'Settings'
sleep 2
capture '10_settings_from_drawer'
tap_label 'INK'
sleep 1
capture '11_settings_ink_selected'
tap_label 'Back'
sleep 2
capture '12_drawer_after_settings'
python3 - "$OUT/12_drawer_after_settings.xml" <<'PY'
import sys,xml.etree.ElementTree as ET
root=ET.parse(sys.argv[1]).getroot()
labels=[' '.join([n.get('text',''),n.get('content-desc','')]) for n in root.iter('node')]
if not any('Search applications' in label for label in labels):
  raise SystemExit('FAIL: Back from Settings did not restore the All Apps search field')
print('PASS: Back from Settings returned to All Apps')
PY
echo 'PASS: Settings Back from All Apps returned to the drawer' >> "$OUT/report.txt"

# Check that selected monochrome INK renders on HOME and in the drawer.
adb shell am force-stop dev.monolith.launcher.preview || true
adb shell am start -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >/dev/null
sleep 2
adb shell input tap 540 1200
sleep 1
capture '13_home_orbit_ink'
tap_label 'ALL APPS'
sleep 2
capture '14_drawer_ink'

# Larger Android font: observe instead of claiming layout acceptance.
adb shell settings put system font_scale 1.3
adb shell am force-stop dev.monolith.launcher.preview || true
adb shell am start -n dev.monolith.launcher.preview/dev.monolith.launcher.MainActivity >/dev/null
sleep 2
tap_label 'Settings'
sleep 2
capture '15_settings_large_font'
adb shell settings put system font_scale 1.0
echo 'DONE: screenshots, labels and logs captured; manually assess scaling and quality' >> "$OUT/report.txt"
