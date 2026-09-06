# RuStore submission checklist

This checklist is for updating the existing public RuStore listing to Password Generator 1.5.6.

## 1. Application

- Name: `Генератор паролей`
- Package: `com.sl.passwordgenerator`
- Version: `1.5.6`
- Version code: `17`
- Type: application
- Primary category: `Полезные инструменты`
- Additional category: none
- Age rating: `0+`
- Price: free
- Advertising: no

## 2. Build

Upload the signed AAB from GitHub Release `v1.5.6`:

- `password-generator-1.5.6.aab`

Do not use a CI/debug artifact. The GitHub Release package is signed with the permanent release key and verified by the release workflow.

## 3. Description

Use the current short description and full description from `listing.ru.md`.

For the update notes use the `What's new` section for version 1.5.6.

## 4. Privacy and data safety

Privacy policy URL:

`https://stanleyll0yd.github.io/apps/password-generator/privacy/`

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

Store icon:

- `store-assets/app-icon-512.png`

The store icon is synchronized with the current application icon.

For an ordinary version update, the existing RuStore screenshots can be retained. The repository screenshot set was captured from v1.5.3. If screenshots are refreshed, recapture them from the current signed v1.5.6 build; in particular, do not reuse `02-about.png` as a current screenshot because the About links have changed.

## 6. Contacts

Website/project page:

`https://stanleyll0yd.github.io/apps/password-generator/`

Use the developer account's valid support email/phone where RuStore Console requires account-level contact information.

## 7. Final verification before moderation

- confirm package/version are `com.sl.passwordgenerator` / `1.5.6 (17)`
- confirm the uploaded file is `password-generator-1.5.6.aab` from GitHub Release `v1.5.6`
- confirm the privacy policy opens without authentication
- confirm the application is marked as free and without advertising
- confirm data collection and data sharing are both declared as absent
- confirm the moderation note states that the app is offline and only requests VIBRATE
- if the store icon is replaced, confirm it is the current lock icon
- submit the update for moderation
