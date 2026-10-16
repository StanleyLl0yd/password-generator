#!/usr/bin/env python3
"""Check the compiled manifest and native libraries, not only Gradle source text."""
import argparse
from pathlib import Path
import re
import struct
from defusedxml import ElementTree as ET
import zipfile

ANDROID = '{http://schemas.android.com/apk/res/android}'
PACKAGE = 'com.sl.passwordgenerator'
ALLOWED_PERMISSIONS = {
    'android.permission.VIBRATE',
    f'{PACKAGE}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION',
}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def verify_manifest(text, version, version_code):
    root = ET.fromstring(text, forbid_dtd=True)
    require(root.get('package') == PACKAGE, 'Package identity changed')
    require(root.get(ANDROID + 'versionName') == version, 'Version name mismatch')
    require(root.get(ANDROID + 'versionCode') == str(version_code), 'Version code mismatch')
    sdk = root.find('uses-sdk')
    require(sdk is not None, 'Missing SDK declaration')
    require(sdk.get(ANDROID + 'minSdkVersion') == '26', 'Minimum SDK changed')
    require(int(sdk.get(ANDROID + 'targetSdkVersion', '0')) >= 36, 'Target SDK below 36')
    permissions = {node.get(ANDROID + 'name') for node in root if node.tag.startswith('uses-permission')}
    require(permissions <= ALLOWED_PERMISSIONS, f'Unexpected permissions: {permissions - ALLOWED_PERMISSIONS}')
    app = root.find('application')
    require(app is not None, 'Missing application')
    require(app.get(ANDROID + 'allowBackup') == 'false', 'Backup must be disabled')
    require(app.get(ANDROID + 'debuggable', 'false') == 'false', 'Release is debuggable')
    for tag in ('activity', 'activity-alias', 'service', 'receiver', 'provider'):
        for node in app.findall(tag):
            if node.get(ANDROID + 'exported') == 'true':
                require(tag == 'activity' and node.get(ANDROID + 'name') in {
                    '.MainActivity', PACKAGE + '.MainActivity'
                }, 'Unexpected exported component')


def verify_native_libraries(archive):
    native_count = 0
    with zipfile.ZipFile(archive) as bundle:
        for name in bundle.namelist():
            if not name.endswith('.so'):
                continue
            data = bundle.read(name)
            require(len(data) >= 16 and data[:4] == b'\x7fELF', f'{name}: invalid ELF')
            require(data[5] == 1, f'{name}: unsupported ELF endianness')
            require(len(data) >= (64 if data[4] == 2 else 52), f'{name}: truncated ELF header')
            if data[4] == 2:
                phoff = struct.unpack_from('<Q', data, 32)[0]
                size, count = struct.unpack_from('<HH', data, 54)
                expected_size = 56
                layout = '<IIQQQQQQ'
            else:
                require(data[4] == 1, f'{name}: invalid ELF class')
                phoff = struct.unpack_from('<I', data, 28)[0]
                size, count = struct.unpack_from('<HH', data, 42)
                expected_size = 32
                layout = '<IIIIIIII'
            require(size == expected_size and count > 0, f'{name}: invalid program headers')
            require(phoff + size * count <= len(data), f'{name}: truncated ELF')
            loads = 0
            for index in range(count):
                header = struct.unpack_from(layout, data, phoff + size * index)
                if header[0] != 1:
                    continue
                loads += 1
                if data[4] == 2:
                    offset, address, alignment = header[2], header[3], header[7]
                else:
                    offset, address, alignment = header[1], header[2], header[7]
                require(alignment > 0 and alignment & (alignment - 1) == 0, f'{name}: invalid alignment')
                if data[4] == 2:
                    require(alignment >= 16384, f'{name}: PT_LOAD below 16 KB')
                    require(offset % 16384 == address % 16384, f'{name}: PT_LOAD offset alignment')
            require(loads, f'{name}: no loadable segments')
            native_count += 1
    return native_count


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--manifest', type=Path, required=True)
    parser.add_argument('--archive', type=Path, required=True)
    args = parser.parse_args()
    version = Path('VERSION').read_text().strip()
    gradle = Path('app/build.gradle.kts').read_text()
    code = int(re.search(r'versionCode = (\d+)', gradle)[1])
    verify_manifest(args.manifest.read_text(), version, code)
    count = verify_native_libraries(args.archive)
    print(f'Android release contract: {version} ({code}), native libraries checked: {count}')


if __name__ == '__main__':
    main()
