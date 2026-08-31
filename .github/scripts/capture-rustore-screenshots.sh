#!/usr/bin/env bash
set -euo pipefail

adb shell cmd uimode night no || true
./gradlew --dependency-verification=off assembleDebug assembleDebugAndroidTest --stacktrace

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

adb shell pm list instrumentation
RUNNER="com.sl.passwordgenerator.test/androidx.test.runner.AndroidJUnitRunner"
adb shell am instrument -w -r \
  -e class com.sl.passwordgenerator.RuStoreScreenshotTest \
  "$RUNNER" | tee /tmp/rustore-instrumentation.txt
grep -q '^OK (1 test)' /tmp/rustore-instrumentation.txt

rm -rf rustore-screenshots
mkdir -p rustore-screenshots
adb pull /sdcard/Android/data/com.sl.passwordgenerator/files/rustore/. rustore-screenshots/

mapfile -t screenshots < <(find rustore-screenshots -maxdepth 1 -type f -name '*.png' | sort)
test "${#screenshots[@]}" -eq 3
ls -lh "${screenshots[@]}"
file "${screenshots[@]}"
