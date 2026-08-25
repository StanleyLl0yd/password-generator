# Password Generator

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

**Version:** 1.4.4

**Min SDK:** 24 (Android 7.0)

**Target SDK:** 36

A modern, privacy-focused Android password generator with adaptive UI and clean architecture.

---

## ✨ Features

### 🔐 Password Generation
- **Flexible character sets**: lowercase, uppercase, digits, special characters
- **Customizable length**: 4-64 characters
- **Smart options**:
    - Exclude duplicate characters
    - Exclude similar characters (i I l 1 o O 0 B 8 G 6 S 5 Z 2) for better readability
- **Real-time strength indicator**: visual feedback with color-coded progress bar
- **Protected clipboard copy**: marked as sensitive and automatically cleared after 60 seconds

### 🎨 Modern UI/UX
- **Adaptive layout**: automatically switches between 1 or 2 columns based on screen height
- **Popup tooltips**: helpful hints that float above content without disrupting layout
- **Material 3 Design**: follows latest Google design guidelines
- **Smooth animations**: polished transitions and micro-interactions
- **Dark/Light theme**: automatic based on system settings

### 🌍 Localization
- **English** (default for all languages)
- **Русский** (Russian)
- Automatic language detection based on system settings

### 🔒 Privacy & Security
- **100% offline**: no network access required
- **No data collection**: zero analytics, tracking, or ads
- **Passwords stay in memory**: only generator settings are persisted; generated passwords are never saved
- **Upgrade cleanup**: legacy passwords stored by v1.4.1 and older are deleted automatically
- **Backup protection**: saved preferences explicitly excluded from Android auto-backup
- **Source available**: full code transparency under the PolyForm Noncommercial license

### 🏗️ Technical Excellence
- **Clean Architecture**: Domain/Data/UI separation
- **MVVM pattern**: StateFlow-based state management
- **Hilt DI**: dependency injection for testability
- **Jetpack Compose**: modern declarative UI
- **Component-based**: reusable UI components
- **Optimized performance**: minimal recomposition

---

## 📦 Installation

### From Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/StanleyLl0yd/password-generator.git
   cd password-generator
   ```

2. **Open in Android Studio**:
    - A current Android Studio version with Android Gradle Plugin 8.13 support
    - JDK 17 or newer

3. **Sync Gradle**:
    - Let Android Studio sync dependencies

4. **Build and Run**:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### APK Release

Download the latest APK from [Releases](https://github.com/StanleyLl0yd/password-generator/releases)

---

## 🛠️ Technology Stack

| Category | Technology |
|----------|------------|
| **Language** | Kotlin 2.2.0 |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt 2.57.2 |
| **Async** | Kotlin Coroutines + Flow |
| **Local Storage** | DataStore Preferences |
| **Build System** | Gradle 8.13 (Kotlin DSL) |

---

## 📁 Project Structure

```
app/src/main/java/com/sl/passwordgenerator/
├── data/
│   └── SettingsRepository.kt          # DataStore persistence
├── domain/
│   ├── model/
│   │   ├── GeneratorPreferences.kt    # User preferences model
│   │   ├── PasswordGenerationConfig.kt
│   │   ├── PasswordGenerationResult.kt
│   │   └── PasswordStrength.kt        # Strength enum with logic
│   ├── usecase/
│   │   └── PasswordGenerator.kt       # Core generation logic
│   └── PasswordConstants.kt           # Character sets & constants
├── ui/
│   ├── components/
│   │   ├── CheckboxRow.kt             # Reusable checkbox with tooltip
│   │   ├── LengthSlider.kt            # Password length slider
│   │   ├── PasswordField.kt           # Read-only result with visibility toggle
│   │   └── StrengthIndicator.kt       # Visual strength indicator
│   ├── theme/
│   │   ├── Color.kt                   # Material 3 color palette
│   │   ├── Theme.kt                   # Theme configuration
│   │   └── Type.kt                    # Typography system
│   ├── PasswordGeneratorScreen.kt     # Main composable screen
│   ├── PasswordGeneratorViewModel.kt  # State & business logic
│   └── PasswordGeneratorUiState.kt    # UI state data class
├── util/
│   ├── HapticFeedback.kt              # Vibration utility
│   └── SecureClipboard.kt             # Sensitive copy and timed cleanup
├── MainActivity.kt                     # Entry point
└── PasswordGeneratorApplication.kt    # Hilt application class
```

---

## 🎯 Key Features Explained

### Adaptive Layout

The app intelligently adapts to different screen sizes:

- **Small screens (< 700dp height)**: 2-column checkbox grid for space efficiency
- **Large screens (≥ 700dp height)**: 1-column layout for better readability

```kotlin
BoxWithConstraints {
    val useTwoColumns = maxHeight < 700.dp
    // Render the checkbox grid for the available content height.
}
```

### Password Strength Algorithm

The strength indicator uses a conservative entropy-based heuristic for generated output:

1. **Character space calculation**: aligned with the generator's actual 89-character pool
2. **Entropy formula**: `length × log₂(charSpace)`
3. **Repeated-block detection**: repeated content is scored using the effective unit length
4. **Normalization**: scaled so a 20-character full-pool password ≈ 100
5. **Penalties applied for**:
    - Short length (< 8 characters)
    - Digit-only short passwords
    - Sequential patterns (123456, abcdef)
    - Heavy repetition
    - Common patterns such as `password`, `qwerty`, and `admin`

**Result**: 0-100 score mapped to 5 levels (Very Weak → Very Strong)

### Popup Tooltips

Tooltips use Compose `Popup` for floating behavior:

```kotlin
Popup(
    alignment = Alignment.TopCenter,
    onDismissRequest = { showTooltip = false },
    properties = PopupProperties(focusable = true)
) {
    // Tooltip content
}
```

Benefits:
- ✅ Floats above content
- ✅ Doesn't shift layout
- ✅ Auto-dismisses on outside click
- ✅ Smooth fade animations

---

## 🔧 Configuration

### Gradle

Key dependencies in `app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.sl.passwordgenerator"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.4.4"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.material3:material3")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")
}
```

### App Configuration

Settings are stored in DataStore Preferences:

- Password length (4-64)
- Character set selections
- Exclude options

Generated passwords are never persisted. On upgrade, v1.4.4 also removes the legacy
`password` preference written by v1.4.1 and older.

---

## 🧪 Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

The suite covers generation invariants, filtered/unique pools, invalid configurations,
strength regressions, length clamping, and legacy password cleanup. GitHub Actions runs
the unit tests, Android Lint, and debug assembly for every push and pull request.

### UI Tests

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

The instrumented smoke test launches `MainActivity`, waits for initial password generation,
and checks that the primary screen actions remain available.

---

## 📊 Version History

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

**Latest:** v1.4.4
- Privacy migration deletes passwords persisted by v1.4.1 and older
- Clipboard content is sensitive and clears automatically after 60 seconds
- Strength scoring detects repeated blocks and common weak patterns
- Gradle Wrapper is complete again and domain regression tests replace placeholder tests

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable/function names
- Add comments for complex logic
- Keep functions small and focused

---

## 📄 License

This project is licensed under the **PolyForm Noncommercial License 1.0.0**.

**Copyright (c) 2025 Stanley Lloyd.**

**Noncommercial use, copying, modification, and distribution are permitted.** Commercial use requires a separate agreement; contact me for licensing.

See [LICENSE](LICENSE) file for full terms.

For commercial licensing inquiries, please contact Stanley Lloyd.

---

## 👨‍💻 Author

**Stanley Lloyd**

- GitHub: [@StanleyLl0yd](https://github.com/StanleyLl0yd)
- Repository: [password-generator](https://github.com/StanleyLl0yd/password-generator)

---

## 🙏 Acknowledgments

- Google Material Design team for Material 3 guidelines
- Jetpack Compose team for amazing declarative UI framework
- Android community for valuable feedback and contributions

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/StanleyLl0yd/password-generator/issues)
- **Discussions**: [GitHub Discussions](https://github.com/StanleyLl0yd/password-generator/discussions)

---

## 🔮 Roadmap

Future improvements planned:

- [ ] Password history (opt-in)
- [ ] Passphrase generator
- [ ] Quick profiles (PIN, Wi-Fi, 16/24/32 characters)
- [ ] Custom character sets
- [ ] Password templates
- [ ] Backup/restore settings
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] More languages (German, French, Spanish, Chinese)

---

<div align="center">
  <p>Made with ❤️ for security-conscious users</p>
  <p>⭐ Star this repo if you find it useful!</p>
</div>
