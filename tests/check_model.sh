#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
out="$(mktemp --suffix=.jar)"
trap 'rm -f "$out"' EXIT
kotlinc core/model/src/main/kotlin/studio/nox/model/SceneConfig.kt tests/ModelContract.kt -include-runtime -d "$out"
java -jar "$out"
