#!/usr/bin/env bash
set -euo pipefail
mapfile -t apps < <(find verification -name app-debug.apk)
mapfile -t tests < <(find verification -name app-debug-androidTest.apk)
test "${#apps[@]}" = 1
test "${#tests[@]}" = 1
adb install -r "${apps[0]}"
adb install -r "${tests[0]}"
mkdir -p build
api="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
screenshots_class=com.sl.passwordgenerator.StoreScreenshotsTest
test_filter=()
if [[ "$api" = 36 ]]; then
  adb shell am instrument -w -r -e class "$screenshots_class" \
    com.sl.passwordgenerator.test/androidx.test.runner.AndroidJUnitRunner | tee build/store-screenshots-test.txt
  grep -Eq '^OK \([1-9][0-9]* tests?\)' build/store-screenshots-test.txt
  test_filter=(-e notClass "$screenshots_class")
fi
adb shell am instrument -w -r \
  "${test_filter[@]}" \
  com.sl.passwordgenerator.test/androidx.test.runner.AndroidJUnitRunner | tee build/instrumentation.txt
grep -Eq '^OK \([1-9][0-9]* tests?\)' build/instrumentation.txt
if [[ "$api" = 36 ]]; then
  mkdir -p build/store-screenshots
  adb pull /sdcard/Android/data/com.sl.passwordgenerator/files/store-screenshots/. build/store-screenshots/
  python3 - <<'CHECK_SCREENSHOTS'
from pathlib import Path
import struct
for name in ('01-generator.png', '02-about.png'):
    with (Path('build/store-screenshots') / name).open('rb') as image:
        header = image.read(33)
    if len(header) != 33 or header[:8] != b'\x89PNG\r\n\x1a\n' or header[8:16] != b'\x00\x00\x00\rIHDR':
        raise SystemExit(f'{name}: invalid PNG header')
    width, height, depth, color, _, _, _ = struct.unpack('>IIBBBBB', header[16:29])
    if depth != 8 or color != 2:
        raise SystemExit(f'{name}: Google Play requires 24-bit RGB PNG without alpha')
    if not (320 <= min(width, height) <= max(width, height) <= 3840) or max(width, height) > 2 * min(width, height):
        raise SystemExit(f'{name}: unsupported screenshot dimensions {width}x{height}')
    print(f'{name}: {width}x{height}, 24-bit RGB PNG: OK')
CHECK_SCREENSHOTS
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
