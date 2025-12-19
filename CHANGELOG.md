# Changelog

[![en](https://img.shields.io/badge/lang-en-red.svg)](CHANGELOG.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](CHANGELOG.ru.md)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.4.0] - 2025-12-19

### 🌍 Added - Localization
- **English localization** (default for all languages)
- **Russian localization** (for Russian system language)
- Automatic language detection based on system settings
- English used as fallback for all non-Russian languages

### 🎨 Added - UI/UX Improvements
- **Adaptive layout**: Automatically switches between 1-column (large screens ≥700dp) and 2-column (small screens <700dp) checkbox grid
- **Popup tooltips**: Helpful hints now float above content without disrupting layout
- **Compact design**: Optimized spacing and margins for better screen utilization
- **Renamed label**: "Исключать повторяющиеся" → "Исключать повторы" (Russian) / "Exclude duplicates" (English)

### 🏗️ Changed - Architecture Refactoring
- **Clean Architecture**: Complete restructure into Domain/Data/UI layers
- **Component-based UI**: Created reusable components:
    - `CheckboxRow.kt` - Unified checkbox with tooltip popup
    - `PasswordField.kt` - Password input with visibility toggle
    - `LengthSlider.kt` - Compact slider for password length
    - `StrengthIndicator.kt` - Visual strength indicator with animations
- **StateFlow migration**: Replaced LiveData with StateFlow for state management
- **ViewModel improvements**: Better separation of concerns with extension functions
- **Hilt DI integration**: Proper dependency injection throughout the app

### 🛠️ Changed - Technical Improvements
- **HapticFeedback utility**: Extracted haptic feedback logic into separate utility class
- **PasswordStrength enum**: Created enum with companion object for strength calculations
- **Extension functions**: Added mapping functions between layers for cleaner code
- **Minimized recomposition**: Optimized Compose performance
- **Updated dependencies**: Kotlin 2.2.0, AGP 8.13.2, Compose BOM 2025.12.01

### 🔧 Fixed
- **Clipboard API**: Updated to use standard Android ClipboardManager instead of deprecated Compose Clipboard
- **Text overflow**: Fixed checkbox labels cutting off text
- **Import cleanup**: Removed unused imports from all files
- **AndroidManifest warning**: Removed deprecated package attribute
- **Annotation warnings**: Fixed Hilt annotation targets for Kotlin 2.2.0 compatibility

### 📦 Technical Details
- Updated `versionCode` to 6
- Updated `versionName` to "1.4.0"
- Target SDK: 36
- Kotlin: 2.2.0
- AGP: 8.13.2
- Compose BOM: 2025.12.01
- Hilt: 2.57.2

---

## [1.2.0] - 2024-XX-XX

### Added
- Password strength indicator with color-coded visual feedback
- Real-time strength calculation based on entropy
- Exclude similar characters option (i I 1 l o O 0)
- Exclude duplicate characters option
- Haptic feedback on button press and copy
- Material 3 design implementation
- DataStore Preferences for settings persistence

### Changed
- Improved UI layout and spacing
- Enhanced password field with show/hide toggle
- Updated to Jetpack Compose
- Migrated to Material 3 components

### Technical
- Implemented MVVM architecture
- Added ViewModel with LiveData
- Integrated Hilt for dependency injection
- Added DataStore for local storage

---

## [1.1.0] - 2024-XX-XX

### Added
- Password length slider (4-64 characters)
- Special characters support
- One-tap copy to clipboard
- Toast notification on copy

### Changed
- Improved password generation algorithm
- Enhanced UI with better visual hierarchy

---

## [1.0.0] - 2024-XX-XX

### Added
- Initial release
- Basic password generation
- Character set selection (lowercase, uppercase, digits)
- Configurable password length
- Material Design UI
- Offline functionality
- No data collection or tracking

---

## Version Naming Convention

- **Major (X.0.0)**: Breaking changes, major architecture refactoring
- **Minor (1.X.0)**: New features, significant improvements
- **Patch (1.4.X)**: Bug fixes, minor improvements

---

## Links

- [Repository](https://github.com/StanleyLl0yd/password-generator)
- [Issues](https://github.com/StanleyLl0yd/password-generator/issues)
- [Releases](https://github.com/StanleyLl0yd/password-generator/releases)
- [Discussions](https://github.com/StanleyLl0yd/password-generator/discussions)

---

## License

Copyright (c) 2025 Stanley Lloyd.

Licensed under the PolyForm Noncommercial 1.0.0 license. Noncommercial use, copying, modification, and distribution are permitted. Commercial use requires a separate agreement; contact Stanley Lloyd for licensing.