#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
version = (ROOT / "VERSION").read_text(encoding="utf-8").strip()

if not re.fullmatch(r"\d+\.\d+\.\d+", version):
    raise SystemExit(f"VERSION is not semantic version: {version!r}")

android = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
windows_cmake = (ROOT / "windows/CMakeLists.txt").read_text(encoding="utf-8")
windows_rc = (ROOT / "windows/res/password-generator.rc.in").read_text(encoding="utf-8")
mac_plist = (ROOT / "macos/Resources/Info.plist.in").read_text(encoding="utf-8")
windows_manifest = (ROOT / "windows/res/password-generator.manifest").read_text(encoding="utf-8")
kotlin_constants = (ROOT / "app/src/main/java/com/sl/passwordgenerator/domain/PasswordConstants.kt").read_text(encoding="utf-8")
cpp_constants = (ROOT / "windows/src/generator.hpp").read_text(encoding="utf-8")
swift_constants = (ROOT / "macos/Sources/PasswordGenerator/Generator.swift").read_text(encoding="utf-8")

checks = [
    (f'versionName = "{version}"', android, "Android versionName"),
    (f"VERSION {version}", windows_cmake, "Windows CMake version"),
    (f'VALUE "FileVersion", "{version}\\0"', windows_rc, "Windows file version"),
    ("<string>@VERSION@</string>", mac_plist, "macOS version template"),
    (f'version="{version}.0"', windows_manifest, "Windows assembly manifest version"),
    ('name="Microsoft.Windows.Common-Controls"', windows_manifest, "Windows Common Controls dependency"),
    ('version="6.0.0.0"', windows_manifest, "Windows Common Controls v6 dependency"),
    ('publicKeyToken="6595b64144ccf1df"', windows_manifest, "Windows Common Controls public key"),
]

for needle, haystack, label in checks:
    if needle not in haystack:
        raise SystemExit(f"{label} is not synchronized with VERSION={version}")

constants = {
    "MIN_LENGTH": ("4", "kMinLength = 4", "minLength = 4"),
    "MAX_LENGTH": ("64", "kMaxLength = 64", "maxLength = 64"),
    "DEFAULT_LENGTH": ("16", "kDefaultLength = 16", "defaultLength = 16"),
    "SIMILAR_CHARS": ('"iIl1oO0B8G6S5Z2"', '"iIl1oO0B8G6S5Z2"', '"iIl1oO0B8G6S5Z2"'),
    "LOWERCASE_CHARS": ('"abcdefghijklmnopqrstuvwxyz"', '"abcdefghijklmnopqrstuvwxyz"', '"abcdefghijklmnopqrstuvwxyz"'),
    "UPPERCASE_CHARS": ('"ABCDEFGHIJKLMNOPQRSTUVWXYZ"', '"ABCDEFGHIJKLMNOPQRSTUVWXYZ"', '"ABCDEFGHIJKLMNOPQRSTUVWXYZ"'),
    "DIGIT_CHARS": ('"0123456789"', '"0123456789"', '"0123456789"'),
    "SYMBOL_CHARS": ('"!@#$%^&*()-_=+[]{};:,.<>?/|"', '"!@#$%^&*()-_=+[]{};:,.<>?/|"', '"!@#$%^&*()-_=+[]{};:,.<>?/|"'),
    "FULL_CHARSPACE": ("89", "kFullCharSpace = 89", "fullCharSpace = 89"),
}

for name, (kotlin, cpp, swift) in constants.items():
    if kotlin not in kotlin_constants:
        raise SystemExit(f"Android constant drift: {name}")
    if cpp not in cpp_constants:
        raise SystemExit(f"Windows constant drift: {name}")
    if swift not in swift_constants:
        raise SystemExit(f"macOS constant drift: {name}")

print(f"Platform contract synchronized for Password Generator {version}")
