# Changelog

[![en](https://img.shields.io/badge/lang-en-red.svg)](CHANGELOG.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](CHANGELOG.ru.md)

All notable changes to this project are documented here.

---

## [1.5.2] - 2026-09-01

### Privacy & reliability
- Disabled Android application backup as defense in depth while keeping explicit backup and device-transfer exclusions for generator preferences.
- Added regression coverage that verifies every enabled character group is represented even at the minimum password length.

### Maintenance
- Simplified the Compose hierarchy and source imports, removed an unused Compose preview dependency, and cleaned haptic, debounce and repeat-detection code without changing application behavior.
- Made Qodana run a strict full-project scan on pull requests with `qodana.recommended` and `failThreshold: 0`, using only narrowly scoped exclusions for confirmed false positives.
- Updated CodeQL Action to 4.37.9 and `actions/download-artifact` to 8.0.1 while keeping immutable commit SHA pins.

### Technical
- Updated `versionCode` to 13 and `versionName` to `1.5.2`.
- User-facing functionality and UI/UX remain unchanged.

---

## [1.5.1] - 2026-09-01

### Changed
- Replaced the application launcher icon with the finalized Password Generator artwork.
- Added a canonical icon source so all future releases keep the same visual identity.

### Technical
- Updated launcher resources for all supported Android densities and the adaptive launcher icon.
- Added a 512×512 store asset derived from the same artwork without redesigning it.
- Updated `versionCode` to 12 and `versionName` to `1.5.1`.

---

## [1.5.0] - 2026-08-26

### Added
- Added an About sheet with the installed version, author **Stanley Lloyd**, PolyForm Noncommercial 1.0.0 license and GitHub repository link.
- Added `-` / `+` length controls and quick presets for 16, 24 and 32 characters.

### Changed
- Redesigned the main screen into a more compact Material 3 layout optimized for phones.
- Moved the password and strength indicator into one result card and kept generated passwords read-only and hidden by default.
- Replaced the long character-set list with a compact 2×2 selection grid.
- Simplified advanced options and made each option row fully tappable.
- Kept the Generate action fixed at the bottom for easier access.
- Removed the redundant offline banner from the working screen.
- Updated English and Russian documentation to match the current application behavior.

### Privacy
- Password generation remains local and uses `SecureRandom`.
- Generated passwords are not persisted; only generator preferences are stored.
- Copied passwords are marked as sensitive. The app schedules its own copied value for removal after 60 seconds when Android still allows clipboard access and does not overwrite newer clipboard content.

### Technical
- Updated `versionCode` to 11 and `versionName` to `1.5.0`.

---

## [1.4.4] - 2026-08-25

### Security & privacy
- Removed the legacy persisted-password key used by v1.4.1 and older before preferences are exposed and on later settings writes.
- Added safe DataStore recovery for corrupted files and recoverable I/O failures.
- Marked copied passwords as sensitive and added guarded clipboard cleanup that never replaces newer clipboard content.
- Excluded generator preferences from Android cloud backup and device-to-device transfer.

### Fixed
- Repeated blocks and common weak patterns no longer receive unrealistically high strength scores.
- Strength normalization now uses the generator's actual 89-character pool.
- Generated output is always read-only and every newly generated password starts hidden.
- Invalid direct generation lengths now return `INVALID_LENGTH`.
- Improved one-shot event delivery, preference persistence and lifecycle-aware UI state collection.

### Tests & build
- Added regression tests for generation invariants, exclusions, error cases, strength scoring, length clamping and legacy-key deletion.
- Added an instrumented launch/generation smoke test.
- Added GitHub Actions checks for unit tests, Android Lint and build artifacts.
- Restored the official Gradle Wrapper JAR and removed stale generated release metadata.
- Updated `versionCode` to 10 and `versionName` to `1.4.4`.

---

## [1.4.3] - 2026-03-01

### Changed
- Removed unused generation and UI state code.
- Moved password generation off the main thread.
- Debounced preference writes while the length slider is moving.
- Moved length clamping into the domain layer.
- Enabled release minification and resource shrinking.

### Fixed
- Completed removal of the persisted password field from preferences.
- Updated the visually similar character set to `i I l 1 o O 0 B 8 G 6 S 5 Z 2`.
- Updated `versionCode` to 9 and `versionName` to `1.4.3`.

---

## [1.4.2] - 2026-03-01

### Fixed
- Corrected duplicate-exclusion behavior so the generator never silently falls back to repeated characters.
- Moved generation progress into persistent UI state and prevented double generation.
- Stopped persisting generated passwords to DataStore.
- Improved sequential-pattern detection and explicit `SecureRandom` integration.
- Updated `versionCode` to 8 and `versionName` to `1.4.2`.

---

## [1.4.1] - 2026-02-25

### Fixed
- Cleaned compiler and Hilt annotation warnings for Kotlin 2.2.0+.
- Migrated to `hilt-lifecycle-viewmodel-compose`.
- Moved password visibility labels into localized resources.
- Added backup rules for generator preferences.
- Updated `versionCode` to 7 and `versionName` to `1.4.1`.

---

## [1.4.0] - 2025-12-19

### Added
- Added English as the default localization and Russian localization for Russian system language.
- Added Material 3 UI components, password-strength feedback and haptic feedback.

### Changed
- Restructured the application into Domain/Data/UI layers with StateFlow and Hilt dependency injection.
- Migrated the UI to reusable Compose components.
- Updated the Android toolchain and dependencies used by the project.
- Updated `versionCode` to 6 and `versionName` to `1.4.0`.

---

## Historical versions

Exact release dates for the early versions below are not recorded in this changelog.

### 1.2.0
- Added password-strength feedback, similar-character exclusion, duplicate exclusion, haptic feedback and DataStore-backed settings.
- Migrated the interface to Jetpack Compose and Material 3.

### 1.1.0
- Added the 4–64 character length control, special characters and one-tap clipboard copy.

### 1.0.0
- Initial Android password generator with lowercase, uppercase and digit selection.

---

## Links

- [Repository](https://github.com/StanleyLl0yd/password-generator)
- [Issues](https://github.com/StanleyLl0yd/password-generator/issues)
- [Releases](https://github.com/StanleyLl0yd/password-generator/releases)

## License

Copyright © 2025–2026 Stanley Lloyd.

Licensed under the PolyForm Noncommercial License 1.0.0. See [LICENSE](LICENSE) for the authoritative terms.
