# Password Generator

[![Android CI](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml)
[![Android 7+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/StanleyLl0yd/password-generator/releases)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)

[English](README.md) · [Русский](README.ru.md)

A privacy-focused offline password generator for Android. Passwords are generated locally with `SecureRandom`; the app has no Internet permission and does not send generated data anywhere.

**Source version:** 1.5.0 (`versionCode 11`) · **Android:** 7.0+ (API 24) · **Target SDK:** 36

[GitHub Releases](https://github.com/StanleyLl0yd/password-generator/releases)

## Features

- Password length from **4 to 64** characters.
- Four independently selectable character groups:
  - lowercase letters `a-z`;
  - uppercase letters `A-Z`;
  - digits `0-9`;
  - symbols `!@#$%^&*()-_=+[]{};:,.<>?/|`.
- At least one character from every enabled group is included in a generated password.
- Optional exclusion of visually ambiguous characters: `i I l 1 o O 0 B 8 G 6 S 5 Z 2`.
- Optional exclusion of duplicate characters; if the selected pool is too small for the requested length, the app reports an error instead of weakening the rule.
- Length control with a slider, `-` / `+` buttons, and presets for **16**, **24**, and **32** characters.
- Generated password is read-only, hidden by default, and can be revealed or copied.
- Password-strength indicator based on the generated password; the score penalizes short passwords, sequences, heavy repetition, repeated blocks, and common weak patterns.
- Generator settings are saved between launches.
- Material 3 UI with system light/dark mode and Dynamic Color on Android 12+.
- English and Russian interface languages.
- **About** sheet with the installed version, description, author, license, and GitHub repository link.

## Privacy and clipboard

- The app has **no `INTERNET` permission**.
- No analytics, advertising, or tracking SDKs are included.
- Generated passwords are kept only in memory and are **not saved to persistent storage**.
- Only generator preferences are stored in DataStore.
- Generator preferences are excluded from Android cloud backup and device-to-device transfer.
- A copied password is marked as sensitive and scheduled for clipboard removal after **60 seconds**.
- Clipboard cleanup only removes the app's own still-current copied value; newer clipboard content is left untouched.

For security-related reports, see [SECURITY.md](SECURITY.md).

## Build from source

Requirements: JDK 17 and Android SDK 36.

```bash
git clone https://github.com/StanleyLl0yd/password-generator.git
cd password-generator
./gradlew assembleDebug
```

Project checks used by CI:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease
```

Main stack: Kotlin 2.2.0, Jetpack Compose, Material 3, Hilt, Coroutines/Flow, and DataStore Preferences.

## Changelog

[English](CHANGELOG.md) · [Русский](CHANGELOG.ru.md)

## License

Licensed under the **PolyForm Noncommercial License 1.0.0**. See [LICENSE](LICENSE) for the full terms.

Copyright © 2025–2026 Stanley Lloyd.

## Author

**Stanley Lloyd**
