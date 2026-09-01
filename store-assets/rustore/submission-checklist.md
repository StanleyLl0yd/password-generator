# RuStore submission checklist

This checklist is for the first public RuStore submission of Password Generator 1.5.2.

## 1. Application

- Name: `Password Generator`
- Package: `com.sl.passwordgenerator`
- Version: `1.5.2`
- Version code: `13`
- Type: application
- Primary category: `Полезные инструменты`
- Additional category: none
- Age rating: `0+`
- Price: free
- Advertising: no

## 2. Build

Upload the signed release package from GitHub Release `v1.5.2`:

- preferred: `password-generator-1.5.2.apk`
- alternative: `password-generator-1.5.2.aab`

Do not use a CI/debug artifact. The GitHub Release package is signed with the permanent release key and verified by the release workflow.

## 3. Description

Copy the short description, full description, What's New text, and moderator note from `listing.ru.md`.

## 4. Privacy and data safety

Privacy policy URL:

`https://github.com/StanleyLl0yd/password-generator/blob/main/PRIVACY.ru.md`

English version:

`https://github.com/StanleyLl0yd/password-generator/blob/main/PRIVACY.md`

Declare:

- personal data collection: no
- user/usage data collection: no
- data transfer to third parties: no
- analytics: no
- advertising: no
- tracking: no
- accounts/authentication: no
- internet/network access: no

Only `android.permission.VIBRATE` is requested, for haptic feedback.

## 5. Media

Upload:

- icon: `store-assets/app-icon-512.png`
- phone screenshots:
  - `store-assets/rustore/screenshots/01-generator.png`
  - `store-assets/rustore/screenshots/02-options.png`
  - `store-assets/rustore/screenshots/03-about.png`

Screenshots are captured from a real Android emulator running the application, not mocked or reconstructed images.

## 6. Contacts

Website/project page:

`https://github.com/StanleyLl0yd/password-generator`

Use the developer account's valid support email/phone where RuStore Console requires account-level contact information.

## 7. Final verification before moderation

- confirm all three screenshots are uploaded and displayed in portrait orientation
- confirm the icon is the current application icon
- confirm package/version are `com.sl.passwordgenerator` / `1.5.2 (13)`
- confirm the privacy policy opens without authentication
- confirm the application is marked as free and without advertising
- confirm data collection and data sharing are both declared as absent
- confirm the moderation note states that the app is offline and only requests VIBRATE
- submit for moderation
