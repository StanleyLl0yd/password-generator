# RuStore listing — Password Generator

Prepared for Password Generator 1.5.3 (`versionCode 14`, package `com.sl.passwordgenerator`).

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

Версия 1.5.3:
- добавлена защита раскрытого пароля от скриншотов и миниатюры недавних приложений;
- строковые представления внутренних объектов теперь маскируют пароль, исключая случайную утечку через диагностический вывод;
- добавлены публичные Политики конфиденциальности и ссылка на них из раздела «О приложении»;
- русское название приложения локализовано как «Генератор паролей»;
- добавлены regression-тесты для новых защитных механизмов.

## Moderator note

Офлайн-приложение: интернет не используется, данные не собираются и не передаются. Единственное разрешение — VIBRATE для тактильной обратной связи.

Length: 147/180 characters.

## Developer contact

At least one contact method is required. Use the project website:

https://github.com/StanleyLl0yd/password-generator

## Privacy policy

Russian:

https://github.com/StanleyLl0yd/password-generator/blob/main/PRIVACY.ru.md

English:

https://github.com/StanleyLl0yd/password-generator/blob/main/PRIVACY.md

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

Preferred first-publication package:

`password-generator-1.5.3.apk`

Use the signed APK published in GitHub Release `v1.5.3`. The release workflow verifies its release certificate before publication.

The signed AAB `password-generator-1.5.3.aab` is also available if AAB publication is preferred.

## Media

**Store icon:** `store-assets/app-icon-512.png`.  
**Phone screenshots:**
- `store-assets/rustore/screenshots/01-generator.png`
- `store-assets/rustore/screenshots/02-about.png`
- `store-assets/rustore/screenshots/03-custom-password.png`

The screenshots are captured from the real signed `v1.5.3` APK running on an Android 36 emulator with the `ru-RU` system locale.