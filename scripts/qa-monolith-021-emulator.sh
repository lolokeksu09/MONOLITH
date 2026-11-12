#!/usr/bin/env bash
# Disposable Android 16 emulator inspection of permanently signed MONOLITH.
# No credentials, APK signing keys, private user data or production device.
set -euo pipefail
OUT="$PWD/qa-results"
mkdir -p "$OUT"
echo 'MONOLITH 0.21 production APK / Android 16 emulator QA' > "$OUT/report.txt"
echo "Run: $GITHUB_RUN_ID" >> "$OUT/report.txt"

collect_logs() {
  adb logcat -d -v threadtime > "$OUT/logcat.txt" 2>/dev/null || true
  grep -En -A 16 'FATAL EXCEPTION|ANR in dev\.monolith\.launcher|Process: dev\.monolith\.launcher|AndroidRuntime' "$OUT/logcat.txt" > "$OUT/crash-excerpts.txt" || true
  adb shell dumpsys activity activities > "$OUT/activities.txt" 2>/dev/null || true
  echo "Capture count: $(find "$OUT" -maxdepth 1 -name '*.png' | wc -l)" >> "$OUT/report.txt"
}
trap collect_logs EXIT

download_apk() {
  local version="$1" hash="$2"
  local filename="MONOLITH-$version-signed.apk"
  curl --fail --location --silent --show-error --retry 4 \
    "https://github.com/lolokeksu09/MONOLITH/releases/download/v$version/$filename" \
    --output "$RUNNER_TEMP/$filename"
  printf '%s  %s\n' "$hash" "$RUNNER_TEMP/$filename" | sha256sum --check
  echo "$filename SHA256 verified $hash" >> "$OUT/report.txt"
}
download_apk '0.20.0-beta.428' '49651d54ed9b7834b3a5b42300ffd6ebee7acf24d9751271aa87562e81186be6'
download_apk '0.21.0-beta.443' '65ca68a57b6f13b2899aa5e0f316e07e0a05d81c7a4fbe2d0317a0f6185e7fe5'

echo "Device: $(adb shell getprop ro.build.version.release) / SDK $(adb shell getprop ro.build.version.sdk)" >> "$OUT/report.txt"
adb shell wm size 1080x2400
adb shell wm density 420
adb shell settings put system accelerometer_rotation 0 || true

echo 'Installing previously signed 0.20...' >> "$OUT/report.txt"
adb install -r "$RUNNER_TEMP/MONOLITH-0.20.0-beta.428-signed.apk"
adb shell dumpsys package dev.monolith.launcher | grep -E 'versionCode=|versionName=' | head -3 >> "$OUT/report.txt"
echo 'Upgrading in place to permanently signed 0.21...' >> "$OUT/report.txt"
adb install -r "$RUNNER_TEMP/MONOLITH-0.21.0-beta.443-signed.apk"
adb shell dumpsys package dev.monolith.launcher | grep -E 'versionCode=|versionName=' | head -3 >> "$OUT/report.txt"
if ! adb shell dumpsys package dev.monolith.launcher | grep -Fq 'versionName=0.21.0-beta.443'; then
  echo 'FAIL: target versionName not installed' >> "$OUT/report.txt"
  exit 1
fi
echo 'PASS: 0.20 -> 0.21 install-over (no uninstall or clear-data)' >> "$OUT/report.txt"

# Start explicit activity and attempt HOME role selection.
adb shell cmd package set-home-activity dev.monolith.launcher/.MainActivity \
  >> "$OUT/home-role.txt" 2>&1 || true
adb shell cmd role add-role-holder android.app.role.HOME dev.monolith.launcher \
  >> "$OUT/home-role.txt" 2>&1 || true
adb shell am force-stop dev.monolith.launcher || true
adb shell am start -W -n dev.monolith.launcher/.MainActivity >> "$OUT/activity-start.txt" 2>&1
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

adb shell am start -n dev.monolith.launcher/.MainActivity >/dev/null
sleep 1
if ! tap_label 'EDITORIAL'; then
  adb shell input swipe 950 1200 150 1200 350
  echo 'Used horizontal-swipe fallback for Editorial' >> "$OUT/report.txt"
fi
sleep 2
capture '08_editorial'

echo 'DONE: inspect screenshots, XML, report and crash-excerpts before claiming UI success' >> "$OUT/report.txt"
