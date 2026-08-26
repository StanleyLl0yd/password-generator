# Password Generator

[![Android CI](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/password-generator)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/password-generator/total)](https://github.com/StanleyLl0yd/password-generator/releases)
[![Android 7+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

A privacy-focused offline password generator for Android, built with Kotlin, Jetpack Compose and Material 3.

[⬇️ Download the latest APK](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Current version: **1.5.0** · Min SDK: **24 (Android 7.0)** · Target SDK: **36**

## ✨ Features

- Lowercase, uppercase, digits and special characters
- Password length from 4 to 64 characters
- Optional exclusion of duplicate characters
- Optional exclusion of visually similar characters: `i I l 1 o O 0 B 8 G 6 S 5 Z 2`
- Real-time password strength indicator
- Sensitive clipboard copy with automatic cleanup after 60 seconds
- Adaptive layout for different screen sizes
- Material 3 interface with system light/dark theme
- English and Russian localization

## 🔒 Privacy & security

- **100% offline** — the app does not require network access
- **No analytics, tracking or ads**
- Generated passwords are kept only in memory and are never persisted
- Only generator preferences are saved
- Legacy passwords stored by v1.4.1 and older are removed automatically after upgrade
- Saved preferences are excluded from Android auto-backup
- Clipboard content is marked as sensitive and cleared after 60 seconds without removing newer clipboard content

Security issues should be reported according to [SECURITY.md](SECURITY.md).

## 📦 Installation

The recommended way to install the app is to download the APK from the latest GitHub Release:

[Download latest release](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Android 7.0 or newer is required.

## 🛠️ Build from source

Requirements:

- Current Android Studio with Android Gradle Plugin 8.13 support
- JDK 17 or newer
- Android SDK 36

```bash
git clone https://github.com/StanleyLl0yd/password-generator.git
cd password-generator
./gradlew assembleDebug
```

To run the project checks:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease
```

## 🧱 Technology

| Category | Technology |
| --- | --- |
| Language | Kotlin 2.2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency injection | Hilt 2.57.2 |
| Async | Kotlin Coroutines + Flow |
| Storage | DataStore Preferences |
| Build | Gradle 8.13, Kotlin DSL |

## ✅ Quality checks

GitHub Actions automatically checks pull requests and pushes to `main` with:

- unit tests
- Android Lint
- debug APK assembly
- release APK and AAB assembly with R8/resource shrinking

## 🌍 Languages

- English — default
- Русский

Translations follow the device language automatically.

## 📊 Changelog

- [English changelog](CHANGELOG.md)
- [Русский changelog](CHANGELOG.ru.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/password-generator/releases)

## 🤝 Contributing

Contributions and bug reports are welcome. Use the GitHub issue forms for bugs and feature requests, or open a pull request with a focused change.

Please keep changes small, follow Kotlin coding conventions, and include tests for behavior changes where practical.

## 🔮 Roadmap

- [ ] Password history (opt-in)
- [ ] Passphrase generator
- [ ] Quick profiles (PIN, Wi-Fi, 16/24/32 characters)
- [ ] Custom character sets
- [ ] Password templates
- [ ] Backup/restore settings
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] More languages

## 📄 License

Licensed under the **PolyForm Noncommercial License 1.0.0**.

Noncommercial use, copying, modification and distribution are permitted. Commercial use requires a separate agreement. See [LICENSE](LICENSE) for the full terms.

Copyright © 2025–2026 Stanley Lloyd.

## 👨‍💻 Author

Stanley Lloyd · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

Made with ❤️ for security-conscious users. If the project is useful to you, consider giving it a ⭐.
