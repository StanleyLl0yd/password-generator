# Password Generator

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

**Version:** 1.4.0  
**Min SDK:** 24 (Android 7.0)  
**Target SDK:** 36

A modern, privacy-focused Android password generator with adaptive UI and clean architecture.

---

## 📱 Screenshots

<div align="center">
  <img src="screenshots/main_en.png" width="250" alt="Main Screen (English)"/>
  <img src="screenshots/main_ru.png" width="250" alt="Main Screen (Russian)"/>
  <img src="screenshots/adaptive.png" width="250" alt="Adaptive Layout"/>
</div>

---

## ✨ Features

### 🔐 Password Generation
- **Flexible character sets**: lowercase, uppercase, digits, special characters
- **Customizable length**: 4-64 characters
- **Smart options**:
    - Exclude duplicate characters
    - Exclude similar characters (i I 1 l o O 0) for better readability
- **Real-time strength indicator**: visual feedback with color-coded progress bar
- **One-tap copy to clipboard** with haptic feedback

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
- **Local storage only**: passwords generated in memory, optionally saved locally
- **Open source**: full code transparency

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
    - Android Studio Hedgehog (2023.1.1) or newer
    - JDK 11 or higher

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
│   │   ├── PasswordField.kt           # Password input with visibility toggle
│   │   └── StrengthIndicator.kt       # Visual strength indicator
│   ├── theme/
│   │   ├── Color.kt                   # Material 3 color palette
│   │   ├── Theme.kt                   # Theme configuration
│   │   └── Type.kt                    # Typography system
│   ├── PasswordGeneratorScreen.kt     # Main composable screen
│   ├── PasswordGeneratorViewModel.kt  # State & business logic
│   └── PasswordGeneratorUiState.kt    # UI state data class
├── util/
│   └── HapticFeedback.kt              # Vibration utility
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
val useTwoColumns = configuration.screenHeightDp.dp < 700.dp
```

### Password Strength Algorithm

The strength indicator uses entropy-based scoring:

1. **Character space calculation**: based on selected character sets
2. **Entropy formula**: `length × log₂(charSpace)`
3. **Normalization**: scaled so 20-char full-charset password ≈ 100
4. **Penalties applied for**:
    - Short length (< 8 characters)
    - Digit-only short passwords
    - Sequential patterns (123456, abcdef)
    - Heavy repetition

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
        versionCode = 6
        versionName = "1.4.0"
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.material3:material3")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")
}
```

### App Configuration

Settings are stored in DataStore Preferences:

- Password length (4-64)
- Character set selections
- Exclude options
- Last generated password (optional)

---

## 🧪 Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

### UI Tests

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

---

## 📊 Version History

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

**Latest:** v1.4.0
- English and Russian localization
- Adaptive 1/2 column layout
- Popup tooltips
- Clean architecture refactor

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