#!/usr/bin/env python3
from pathlib import Path
import json
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

vectors = json.loads((ROOT / "shared/test-vectors/generator-contract.json").read_text())
constants = {
    "minLength": ("MIN_LENGTH", "kMinLength", "minLength"),
    "maxLength": ("MAX_LENGTH", "kMaxLength", "maxLength"),
    "defaultLength": ("DEFAULT_LENGTH", "kDefaultLength", "defaultLength"),
    "referenceLengthForMaxScore": ("REF_LENGTH_FOR_MAX_SCORE", "kReferenceLengthForMaxScore", "referenceLengthForMaxScore"),
    "similar": ("SIMILAR_CHARS", "kSimilarChars", "similar"),
    "lowercase": ("LOWERCASE_CHARS", "kLowercaseChars", "lowercase"),
    "uppercase": ("UPPERCASE_CHARS", "kUppercaseChars", "uppercase"),
    "digits": ("DIGIT_CHARS", "kDigitChars", "digits"),
    "symbols": ("SYMBOL_CHARS", "kSymbolChars", "symbols"),
    "fullCharacterSpace": ("FULL_CHARSPACE", "kFullCharSpace", "fullCharSpace"),
}

for key, names in constants.items():
    for language, name, source in zip(
        ("Android", "Windows", "macOS"), names,
        (kotlin_constants, cpp_constants, swift_constants)
    ):
        match = re.search(r"\b" + name + r'(?:\[\])?\s*=\s*(Array\("[^"\n]*"\)|"[^"\n]*"|[0-9.]+)(?:;|[ \t]*$)', source, re.MULTILINE)
        if not match:
            raise SystemExit(f"{language} constant missing: {name}")
        literal = match[1]
        if literal.startswith("Array(") and literal.endswith(")"):
            literal = literal[6:-1]
        if json.loads(literal) != vectors["constants"][key]:
            raise SystemExit(f"{language} constant drift: {name}")

print(f"Platform contract synchronized for Password Generator {version}")
