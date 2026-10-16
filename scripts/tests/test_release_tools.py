import base64
import importlib.util
import os
from pathlib import Path
import shutil
import struct
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import zipfile
from defusedxml.common import DTDForbidden

ROOT = Path(__file__).resolve().parents[2]


def load(name):
    spec = importlib.util.spec_from_file_location(name, ROOT / 'scripts' / f'{name}.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


package = load('verify_android_package')
signing = load('configure_android_signing')
MANIFEST = '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
package="com.sl.passwordgenerator" android:versionName="1.6.2" android:versionCode="20">
<uses-sdk android:minSdkVersion="26" android:targetSdkVersion="36"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<application android:allowBackup="false"><activity android:name=".MainActivity" android:exported="true"/></application>
</manifest>'''


class PackageVerificationTests(unittest.TestCase):
    def test_accepts_offline_release_manifest(self):
        package.verify_manifest(MANIFEST, '1.6.2', 20)

    def test_rejects_manifest_regressions(self):
        for original, changed in [
            ('android.permission.VIBRATE', 'android.permission.INTERNET'),
            ('allowBackup="false"', 'allowBackup="true"'),
            ('<application ', '<application android:debuggable="true" '),
            ('targetSdkVersion="36"', 'targetSdkVersion="35"'),
            ('versionCode="20"', 'versionCode="19"'),
            ('name=".MainActivity"', 'name=".UnexpectedActivity"'),
            ('package="com.sl.passwordgenerator"', 'package="com.sl.other"'),
        ]:
            with self.subTest(changed=changed), self.assertRaises(ValueError):
                package.verify_manifest(MANIFEST.replace(original, changed), '1.6.2', 20)

    def test_rejects_xml_doctype_and_entity_declarations(self):
        document = '<!DOCTYPE manifest [<!ENTITY secret SYSTEM "file:///etc/passwd">]>' + MANIFEST
        with self.assertRaises(DTDForbidden):
            package.verify_manifest(document, '1.6.2', 20)

    def test_profile_installer_receiver_requires_signature_permission(self):
        receiver = '<receiver android:name="androidx.profileinstaller.ProfileInstallReceiver" android:exported="true" android:permission="android.permission.DUMP"/>'
        manifest = MANIFEST.replace('</application>', receiver + '</application>')
        package.verify_manifest(manifest, '1.6.2', 20)
        with self.assertRaises(ValueError):
            package.verify_manifest(manifest.replace(' android:permission="android.permission.DUMP"', ''), '1.6.2', 20)

    def check_elf(self, data):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'fixture.aab'
            with zipfile.ZipFile(path, 'w') as archive:
                archive.writestr('base/lib/arm64-v8a/libfixture.so', data)
            return package.verify_native_libraries(path)

    def elf(self, alignment=16384, offset=0):
        data = bytearray(120)
        data[:6] = b'\x7fELF\x02\x01'
        struct.pack_into('<Q', data, 32, 64)
        struct.pack_into('<HH', data, 54, 56, 1)
        struct.pack_into('<IIQQQQQQ', data, 64, 1, 5, offset, 0, 0, 120, 120, alignment)
        return data

    def test_accepts_16kb_native_library(self):
        self.assertEqual(1, self.check_elf(self.elf()))

    def test_rejects_4kb_and_misaligned_elf(self):
        for data in [self.elf(4096), self.elf(16384, 1), self.elf(24576), self.elf()[:100]]:
            with self.subTest(length=len(data)), self.assertRaises(ValueError):
                self.check_elf(data)


class SigningTests(unittest.TestCase):
    @unittest.skipUnless(shutil.which('java'), 'JDK is unavailable')
    def test_special_password_round_trip_through_java_properties(self):
        fixture = '  fixture\\value:=#!\t\nпароль🔐 '
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory)
            (path / 'key.properties').write_text('key=' + signing.properties_value(fixture) + '\n')
            (path / 'ReadProperties.java').write_text('''
import java.util.*; import java.io.*; import java.nio.charset.StandardCharsets;
class ReadProperties { public static void main(String[] args) throws Exception {
    Properties p = new Properties();
    try (InputStream in = new FileInputStream(args[0])) { p.load(in); }
    System.out.print(Base64.getEncoder().encodeToString(p.getProperty("key").getBytes(StandardCharsets.UTF_8)));
}}''')
            result = subprocess.check_output(['java', str(path / 'ReadProperties.java'), str(path / 'key.properties')])
            self.assertEqual(fixture, base64.b64decode(result).decode())

    def test_signing_files_are_private_and_content_is_preserved(self):
        with tempfile.TemporaryDirectory() as directory, patch.dict(os.environ, {
            'RUNNER_TEMP': directory, 'KEYSTORE_BASE64': base64.b64encode(b'fixture').decode(),
            'KEYSTORE_PASSWORD': 'fixture', 'KEY_ALIAS': 'fixture', 'KEY_PASSWORD': 'fixture',
        }):
            previous = Path.cwd()
            try:
                os.chdir(directory)
                signing.configure()
                self.assertEqual(b'fixture', Path('password-generator-release.jks').read_bytes())
                if os.name != 'nt':
                    self.assertEqual(0o600, Path('key.properties').stat().st_mode & 0o777)
                    self.assertEqual(0o600, Path('password-generator-release.jks').stat().st_mode & 0o777)
            finally:
                os.chdir(previous)

class MetadataTests(unittest.TestCase):
    def test_platform_contract_rejects_named_constant_drift(self):
        paths = [
            'scripts/verify_platform_contract.py', 'VERSION', 'app/build.gradle.kts',
            'windows/CMakeLists.txt', 'windows/res/password-generator.rc.in',
            'macos/Resources/Info.plist.in', 'windows/res/password-generator.manifest',
            'app/src/main/java/com/sl/passwordgenerator/domain/PasswordConstants.kt',
            'windows/src/generator.hpp', 'macos/Sources/PasswordGenerator/Generator.swift',
            'shared/test-vectors/generator-contract.json',
        ]
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in paths:
                destination = root / name
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(ROOT / name, destination)
            constants = root / paths[7]
            constants.write_text(constants.read_text().replace('MIN_LENGTH = 4', 'MIN_LENGTH = 5'))
            result = subprocess.run(['python3', str(root / paths[0])], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertIn('Android constant drift: MIN_LENGTH', result.stderr)


if __name__ == '__main__':
    unittest.main()
