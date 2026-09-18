# Privacy Policy

Last updated: September 18, 2026

Password Generator is a privacy-focused offline password generator with native applications for Android, Windows, and macOS.

## Data collection

Password Generator does not collect, transmit, sell, share, or otherwise disclose personal data or usage data.

The application does not use analytics, advertising SDKs, tracking technologies, telemetry, crash-reporting services, cloud synchronization, remote accounts, or a backend service.

## Network access

Password Generator does not require network access for password generation or any other product feature.

The Android application does not request the `INTERNET` permission. The Windows and macOS applications do not contain product networking functionality.

Links in the About screen are opened by the operating system in an external application such as a web browser. Network activity caused by opening those external links belongs to the external application or website, not to Password Generator.

## Passwords

Passwords are generated locally using the operating system cryptographic random source:

- Android: `SecureRandom`;
- Windows: `BCryptGenRandom`;
- macOS: `SecRandomCopyBytes`.

Generated passwords are kept only in process memory and are not stored persistently by Password Generator.

When a password is explicitly revealed, the application enables the strongest practical native screen-capture exclusion available on that platform. This is a best-effort operating-system protection and does not claim to prevent physical photography or capture by privileged/compromised software.

## Clipboard

When the user copies a password, it is placed in the operating system clipboard.

Where the platform supports the relevant mechanism, Password Generator treats the value as sensitive and schedules cleanup after 60 seconds. Cleanup is conditional: the application clears only its own still-current copied value and does not erase clipboard content that has subsequently been replaced by the user or another application.

Clipboard behavior is also subject to operating-system policies and other software with clipboard access.

## Local preferences

Password Generator stores only non-secret generator preferences such as length, selected character groups, and generation options.

Platform storage:
- Android: DataStore;
- Windows: the current user's Registry hive;
- macOS: UserDefaults.

Generated passwords are not included in preferences.

On Android, application backup is disabled and backup/device-transfer rules explicitly exclude generator preferences.

## Platform permissions and capabilities

Android requests only `android.permission.VIBRATE` for haptic feedback and does not request the `INTERNET` permission.

The Windows application runs as the current user without elevation and uses native user-interface, cryptographic RNG, clipboard, and per-user Registry APIs.

The macOS application uses native AppKit/Foundation/Security APIs and is packaged with the App Sandbox enabled. It does not request a network entitlement for product functionality.

## Children

Password Generator does not knowingly collect data from anyone, including children, because it does not collect personal data or usage data at all.

## Third-party services

The application does not integrate third-party analytics, advertising, tracking, authentication, cloud, or telemetry services.

The About screen contains links to the official project website, software license, and this Privacy Policy. Those links are handled externally by the operating system. Any processing performed by the selected browser, external application, or website is governed by that service's own privacy terms.

## Changes to this policy

If Password Generator's data practices change, this Privacy Policy will be updated before or together with the corresponding application release.

## Contact

For privacy questions, bug reports, or security issues, use the public project repository:

https://github.com/StanleyLl0yd/password-generator

Security reports should follow the instructions in `SECURITY.md`.
