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

mapfile -t releases < <(find verification -name app-release.apk)
test "${#releases[@]}" = 1
adb uninstall com.sl.passwordgenerator.test
adb uninstall com.sl.passwordgenerator
adb install -r "${releases[0]}"
adb shell am start -W -n com.sl.passwordgenerator/.MainActivity | tee build/release-launch.txt
grep -q 'Status: ok' build/release-launch.txt
adb shell uiautomator dump /sdcard/password-generator-window.xml
adb pull /sdcard/password-generator-window.xml build/release-window.xml
python3 - <<'CHECK_UI'
from defusedxml import ElementTree
root = ElementTree.parse('build/release-window.xml', forbid_dtd=True).getroot()
if not any(node.get('content-desc') in {'Copy', 'Копировать'} and node.get('enabled') == 'true'
           for node in root.iter('node')):
    raise SystemExit('R8 release did not reach the initialized generator screen')
print('R8 release launch and initial generation: OK')
CHECK_UI
