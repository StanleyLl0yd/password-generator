#!/usr/bin/env bash
set -euo pipefail
mapfile -t apps < <(find verification -name app-debug.apk)
mapfile -t tests < <(find verification -name app-debug-androidTest.apk)
test "${#apps[@]}" = 1
test "${#tests[@]}" = 1
adb install -r "${apps[0]}"
adb install -r "${tests[0]}"
mkdir -p build
adb shell am instrument -w -r \
  com.sl.passwordgenerator.test/androidx.test.runner.AndroidJUnitRunner | tee build/instrumentation.txt
grep -Eq '^OK \([1-9][0-9]* tests?\)' build/instrumentation.txt
if [[ "$(adb shell getprop ro.build.version.sdk | tr -d '\r')" = 36 ]]; then
  mkdir -p build/store-screenshots
  adb pull /sdcard/Android/data/com.sl.passwordgenerator/files/store-screenshots/. build/store-screenshots/
  test -s build/store-screenshots/01-generator.png
  test -s build/store-screenshots/02-about.png
  git rev-parse HEAD > build/store-screenshots/SOURCE_COMMIT
fi
