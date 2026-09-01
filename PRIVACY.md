# Privacy Policy

Last updated: September 1, 2026

Password Generator is a privacy-focused offline Android application.

## Data collection

Password Generator does not collect, transmit, sell, share, or otherwise disclose personal data or usage data.

The application does not use analytics, advertising SDKs, tracking technologies, telemetry, crash-reporting services, cloud synchronization, or remote accounts.

## Network access

Password Generator does not request the Android `INTERNET` permission and does not communicate with remote servers.

## Passwords

Passwords are generated locally on the device using Android secure random number generation. Generated passwords are kept only in memory and are not stored persistently by the application.

When a password is copied, it is placed in the Android system clipboard. The application marks copied passwords as sensitive where supported and attempts to clear its own copied value after 60 seconds if Android still permits clipboard access and the clipboard still contains that same value. Clipboard handling is also subject to the behavior and privacy controls of the Android operating system and any keyboard or system software installed on the device.

## Local preferences

The application stores only generator preferences, such as selected character groups and generation options, locally on the device using Android DataStore. These preferences do not contain generated passwords.

Android application backup is disabled. Backup and device-transfer rules also explicitly exclude generator preferences.

## Permissions

Password Generator requests only the Android vibration permission, used for optional haptic feedback. It does not request contacts, location, camera, microphone, storage, phone, SMS, advertising, or network permissions.

## Children

Password Generator does not knowingly collect data from anyone, including children, because it does not collect personal data or usage data at all.

## Third-party services

The application does not integrate third-party analytics, advertising, tracking, authentication, or cloud services.

The About screen contains links to the project's GitHub repository, software license, and this Privacy Policy. These links are opened externally by Android, for example in a web browser. Any data processing performed by the external application or website is governed by that service's own privacy policy.

## Changes to this policy

If the application's data practices change, this Privacy Policy will be updated before or together with the corresponding application release.

## Contact

For privacy questions, bug reports, or security issues, use the public project repository:

https://github.com/StanleyLl0yd/password-generator

Security reports should follow the instructions in `SECURITY.md`.
