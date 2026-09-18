# Password Generator Specification v1

This specification defines product behavior shared by Android, Windows, and macOS.

## Versioning

All supported platforms ship under one product version. The repository root `VERSION` file is the authoritative public version.

Platform build numbers may differ internally, but the user-visible version must equal `VERSION`.

## Character sets

- Minimum length: 4
- Maximum length: 64
- Default length: 16
- Lowercase: `abcdefghijklmnopqrstuvwxyz`
- Uppercase: `ABCDEFGHIJKLMNOPQRSTUVWXYZ`
- Digits: `0123456789`
- Symbols: `!@#$%^&*()-_=+[]{};:,.<>?/|`
- Similar characters: `iIl1oO0B8G6S5Z2`

Defaults:
- all four character groups enabled;
- exclude duplicates enabled;
- exclude similar characters enabled.

## Generation contract

Each platform implementation MUST:

1. use the operating system cryptographically secure random source;
2. reject lengths outside 4...64;
3. reject generation when every selected character group becomes empty;
4. include at least one character from every enabled non-empty group;
5. when duplicate exclusion is enabled, never emit the same character twice;
6. reject a no-duplicates request when the filtered pool is smaller than the requested length;
7. fill remaining positions from the full filtered pool;
8. apply a cryptographically random final Fisher-Yates-equivalent shuffle;
9. generate an initial password after loading preferences.

Platform RNGs:
- Android: `java.security.SecureRandom`;
- Windows: `BCryptGenRandom`;
- macOS: `SecRandomCopyBytes`.

Modulo-biased bounded random selection MUST NOT be introduced by the desktop implementations.

## Strength score

All platforms use the same 0...100 heuristic currently defined by the Android implementation.

Reference maximum entropy uses:
- reference length: 20;
- full character space: 89.

The score:
- detects exact repeated blocks and uses the shortest repeating unit as effective length;
- derives character-space size from character classes present;
- penalizes effective lengths below 8;
- penalizes short numeric-only passwords;
- penalizes ascending or descending sequences of at least four characters;
- penalizes excessive repetition;
- penalizes all-identical strings;
- penalizes common weak patterns: `password`, `qwerty`, `asdf`, `zxcv`, `letmein`, `admin`, `welcome`;
- is clamped to 0...100.

Strength labels:
- 0...19: very weak;
- 20...39: weak;
- 40...59: medium;
- 60...79: strong;
- 80...100: very strong.

## Secret handling

Generated passwords MUST:
- remain in volatile process memory only;
- never be written to preferences, registry, UserDefaults, files, logs, crash messages, analytics, or diagnostics;
- be redacted from diagnostic/string representations of password-bearing state where such representations exist;
- be hidden by default after every generation.

The application MUST remain fully offline and MUST NOT add analytics, advertising, tracking, accounts, telemetry, or cloud synchronization.

## Clipboard

Copying a password MUST use the native platform clipboard.

Where the platform permits it:
- mark the copied value as sensitive;
- schedule cleanup after 60 seconds;
- remove only the value originally copied by Password Generator;
- never erase clipboard contents that have subsequently been replaced by the user or another application.

## Screen capture

The normal masked interface may be captured.

While the password is explicitly revealed, each platform MUST enable the strongest reasonable native capture-exclusion mechanism available without adding a third-party dependency. This is best-effort platform protection, not a claim that physical or privileged capture is impossible.

## Preferences

Only generator preferences may persist:
- length;
- enabled character groups;
- exclude similar characters;
- exclude duplicates.

Passwords MUST NOT persist.

## Localization and UX

English and Russian are supported.

The platforms share product behavior, terminology, ordering, branding, privacy model, and versioning, but each implementation should use native controls and native interaction conventions instead of pixel-identical cross-platform UI.
