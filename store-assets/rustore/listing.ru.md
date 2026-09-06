# RuStore listing — Password Generator

Prepared for Password Generator 1.5.6 (`versionCode 17`, package `com.sl.passwordgenerator`).

## Application information

**Name**  
Генератор паролей

**Type**  
Application (not a game)

**Primary category**  
Полезные инструменты (`tools`)

**Additional category**  
None

**Age rating**  
0+

**Price**  
Free

**Ads**  
No

**Minimum Android version**  
Android 8.0 / API 26 (read automatically from the package)

## Short description

Офлайн-генератор надежных паролей без рекламы, аналитики и доступа в интернет.

Length: 78/80 characters.

## Full description

Генератор паролей — простой офлайн-генератор надежных паролей для Android.

Пароли создаются непосредственно на устройстве с использованием SecureRandom. Приложению не нужен доступ в интернет: оно не содержит рекламы, аналитики и трекеров и не передает пользовательские данные на серверы.

Возможности:
- длина пароля от 4 до 64 символов;
- строчные и заглавные буквы, цифры и специальные символы;
- обязательное включение хотя бы одного символа из каждого выбранного набора;
- исключение визуально похожих символов;
- генерация без повторяющихся символов;
- быстрые значения длины 16, 24 и 32 символа;
- оценка надежности пароля;
- скрытие и отображение сгенерированного пароля;
- безопасное копирование в системный буфер обмена;
- сохранение только настроек генератора;
- светлая и темная тема, Dynamic Color на Android 12 и новее;
- русский и английский интерфейс.

Конфиденциальность:
- сгенерированные пароли не сохраняются в постоянное хранилище;
- приложение не запрашивает разрешение INTERNET;
- резервное копирование данных приложения отключено;
- скопированные пароли помечаются как конфиденциальные;
- приложение пытается удалить собственное значение из буфера обмена через 60 секунд, если Android все еще разрешает доступ и значение не было заменено;
- когда пользователь явно раскрывает пароль, Android блокирует захват этого экрана и его попадание в миниатюру недавних приложений.

Приложение работает на Android 8.0 и новее.

## What's new

Версия 1.5.6:
- исправлено отображение иконки приложения на Android 8+ и некоторых сторонних лаунчерах;
- восстановлена фирменная иконка с замком для adaptive и обычных launcher-ресурсов.

## Moderator note

Офлайн-приложение: интернет не используется, данные не собираются и не передаются. Единственное разрешение — VIBRATE для тактильной обратной связи.

Length: 147/180 characters.

## Developer contact

Website:

https://stanleyll0yd.github.io/apps/password-generator/

## Privacy policy

https://stanleyll0yd.github.io/apps/password-generator/privacy/

## User data safety

**Data collected:** none.  
**Data shared with third parties:** none.  
**Analytics:** none.  
**Advertising:** none.  
**Tracking:** none.  
**Accounts/authentication:** none.  
**Network access:** none.

The application requests only `android.permission.VIBRATE` for haptic feedback.

## Package to upload

Upload the signed AAB from GitHub Release `v1.5.6`:

`password-generator-1.5.6.aab`

The release workflow signs the AAB with the permanent release key and verifies its certificate before publication. APK is retained only as an additional direct-install/GitHub artifact.

## Media

**Store icon:** `store-assets/app-icon-512.png`.

Existing RuStore screenshots may be retained for this update. Repository screenshot assets were captured from v1.5.3; do not re-upload `02-about.png` as a current screenshot without recapturing it from the current app, because the About links have changed since then.
