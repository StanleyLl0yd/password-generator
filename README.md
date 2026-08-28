# Password Generator

[![Android CI](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/password-generator)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/password-generator/total)](https://github.com/StanleyLl0yd/password-generator/releases)
[![Android 8+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

A privacy-focused offline password generator for Android, built with Kotlin, Jetpack Compose and Material 3.

[⬇️ Download the latest APK](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Current source version: **1.5.0** (`versionCode 11`) · Min SDK: **26 (Android 8.0)** · Target SDK: **36**

## ✨ Features

- Password length from **4 to 64** characters
- Lowercase letters `a-z`, uppercase letters `A-Z`, digits `0-9` and special characters `!@#$%^&*()-_=+[]{};:,.<>?/|`
- At least one character from every enabled character group is included in the generated password
- Optional exclusion of visually ambiguous characters: `i I l 1 o O 0 B 8 G 6 S 5 Z 2`
- Optional exclusion of duplicate characters; the app reports an error if the selected pool is too small for the requested length
- Length control with a slider, `-` / `+` buttons and presets for **16**, **24** and **32** characters
- Generated password is read-only, hidden by default and can be revealed or copied
- Password-strength indicator that accounts for length, character variety, sequences, repetition, repeated blocks and common weak patterns
- Generator settings are saved between launches
- Material 3 interface with system light/dark theme and Dynamic Color on Android 12+
- English and Russian localization
- About section with description, installed version, author, license and GitHub repository link

Passwords are generated locally with `SecureRandom`.

## 🔒 Privacy & security

- **100% offline** — the app does not request the Android `INTERNET` permission
- **No analytics, tracking or ads**
- Generated passwords are kept only in memory and are never saved to persistent storage
- Only generator preferences are stored in DataStore
- Generator preferences are excluded from Android cloud backup and device-to-device transfer
- Copied passwords are marked as sensitive
- The app schedules its own copied value for removal after **60 seconds** and removes it only if Android still allows clipboard access and that value is still current; newer clipboard content is not touched
- Android may restrict clipboard access after the app leaves the foreground; Android 13+ also provides system clipboard auto-clear behavior
- Passwords that may have been stored by v1.4.1 and older are removed automatically when preferences are read or saved

The GitHub and license links in About are opened by Android in an external app such as a web browser.

Security issues should be reported according to [SECURITY.md](SECURITY.md).

## 📦 Installation

The recommended way to install the app is to download the signed APK from the latest GitHub Release:

[Download latest release](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Android 8.0 or newer is required.

## 🛠️ Build from source

Requirements:

- JDK 17
- Android SDK 36
- Gradle 8.13 (included through the Gradle Wrapper)

```bash
git clone https://github.com/StanleyLl0yd/password-generator.git
cd password-generator
./gradlew assembleDebug
```

To run the checks that do not require release signing:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

CI additionally verifies the R8/resource-shrunk release APK and AAB with a temporary CI-only signing key. The official Android Release workflow restores the release keystore only from GitHub Actions secrets, lets Gradle sign the release build, verifies the expected certificate with `apksigner`, creates a SHA-256 checksum and publishes the signed APK plus checksum directly to GitHub Release Assets. Temporary signing material is removed after the workflow finishes.

A local release build requires a local `key.properties` that points to a keystore. The official release key and its credentials are not stored in the repository.

## 🧱 Technology

| Category | Technology |
| --- | --- |
| Language | Kotlin 2.2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency injection | Hilt 2.57.2 |
| Async/state | Kotlin Coroutines + Flow |
| Preferences | DataStore 1.2.0 |
| Build | Gradle 8.13, AGP 8.13.2, Kotlin DSL |

## ✅ Quality checks

GitHub Actions automatically checks pull requests and pushes to `main` with:

- unit tests
- Android Lint
- debug APK assembly
- instrumentation-test APK compilation
- release APK assembly with R8/resource shrinking
- release AAB assembly

## 🌍 Languages

- English — default
- Русский

The interface follows the device language automatically.

## 📊 Changelog

- [English changelog](CHANGELOG.md)
- [Русский changelog](CHANGELOG.ru.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/password-generator/releases)

## 🤝 Contributing

Contributions, bug reports and focused pull requests are welcome.

Please keep changes small, follow Kotlin coding conventions, preserve the offline/privacy-first design, and include tests for behavior changes where practical.

## 📄 License

Licensed under the **PolyForm Noncommercial License 1.0.0**.

Noncommercial use, copying, modification and distribution are permitted under the license terms. Commercial use requires a separate agreement. See [LICENSE](LICENSE) for the authoritative text.

Copyright © 2025–2026 Stanley Lloyd.

## 👨‍💻 Author

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

Made with ❤️ for privacy-conscious users. If the project is useful to you, consider giving it a ⭐.
