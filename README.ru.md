# Генератор паролей

[![Android CI](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/password-generator)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/password-generator/total)](https://github.com/StanleyLl0yd/password-generator/releases)
[![Android 8+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

Приватный офлайн-генератор паролей для Android на Kotlin, Jetpack Compose и Material 3.

[⬇️ Скачать последнюю версию APK](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Текущая версия исходного кода: **1.5.1** (`versionCode 12`) · Min SDK: **26 (Android 8.0)** · Target SDK: **36**

## ✨ Возможности

- Длина пароля от **4 до 64** символов
- Строчные буквы `a-z`, заглавные буквы `A-Z`, цифры `0-9` и спецсимволы `!@#$%^&*()-_=+[]{};:,.<>?/|`
- В сгенерированный пароль входит как минимум один символ из каждого включённого набора
- Можно исключить визуально похожие символы: `i I l 1 o O 0 B 8 G 6 S 5 Z 2`
- Можно полностью запретить повторение символов; если выбранного набора недостаточно для заданной длины, приложение сообщает об ошибке
- Управление длиной: ползунок, кнопки `-` / `+` и быстрые значения **16**, **24** и **32** символа
- Сгенерированный пароль доступен только для чтения, по умолчанию скрыт; его можно показать или скопировать
- Индикатор надёжности учитывает длину, разнообразие символов, последовательности, повторения, повторяющиеся блоки и распространённые слабые шаблоны
- Настройки генератора сохраняются между запусками
- Интерфейс Material 3 следует системной светлой/тёмной теме и использует Dynamic Color на Android 12+
- Английская и русская локализация
- Раздел «О приложении» с описанием, установленной версией, автором, лицензией и ссылкой на репозиторий GitHub

Пароли создаются локально с использованием `SecureRandom`.

## 🔒 Приватность и безопасность

- **100% офлайн** — приложение не запрашивает Android-разрешение `INTERNET`
- **Без аналитики, трекинга и рекламы**
- Сгенерированные пароли находятся только в памяти и никогда не сохраняются в постоянное хранилище
- В DataStore сохраняются только настройки генератора
- Резервное копирование приложения средствами Android отключено; правила backup и переноса между устройствами дополнительно явно исключают настройки генератора
- Скопированные пароли помечаются как конфиденциальные
- Приложение планирует удаление собственного значения из буфера через **60 секунд** и удаляет его только если Android всё ещё разрешает доступ к буферу и это значение остаётся текущим; более новое содержимое не затрагивается
- После ухода приложения в фон Android может ограничить доступ к буферу; начиная с Android 13 система также использует собственную автоматическую очистку буфера
- Пароли, которые могли сохраняться версиями 1.4.1 и старше, автоматически удаляются при чтении или сохранении настроек

Ссылки на GitHub и лицензию из раздела «О приложении» открываются Android во внешнем приложении, например браузере.

О проблемах безопасности сообщайте согласно [SECURITY.md](SECURITY.md).

## 📦 Установка

Рекомендуемый способ установки — скачать подписанный APK из последнего GitHub Release:

[Скачать последнюю версию](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Для установки требуется Android 8.0 или новее.

## 🛠️ Сборка из исходного кода

Требования:

- JDK 17
- Android SDK 36
- Gradle 8.13 (включён через Gradle Wrapper)

```bash
git clone https://github.com/StanleyLl0yd/password-generator.git
cd password-generator
./gradlew assembleDebug
```

Для запуска проверок, которым не требуется release-подпись:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

CI дополнительно проверяет release APK с R8/shrink resources и release AAB, используя временный ключ только для CI. Официальный Android Release workflow восстанавливает release-keystore только из GitHub Actions Secrets, Gradle подписывает оба release-пакета, для APK и AAB проверяется ожидаемый release-сертификат, создаются SHA-256 checksums и artifact attestations, после чего подписанные APK, AAB и файлы checksums публикуются напрямую в GitHub Release Assets. Временные signing-файлы после выполнения workflow удаляются.

Для локальной release-сборки нужен локальный `key.properties`, указывающий на keystore. Официальный release-ключ и его учётные данные в репозитории не хранятся.

## 🧱 Технологии

| Категория | Технология |
| --- | --- |
| Язык | Kotlin 2.2.0 |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt 2.57.2 |
| Асинхронность / состояние | Kotlin Coroutines + Flow |
| Настройки | DataStore 1.2.0 |
| Сборка | Gradle 8.13, AGP 8.13.2, Kotlin DSL |

## ✅ Проверка проекта

GitHub Actions автоматически проверяет Pull Request'ы и push в `main`:

- unit-тесты
- Android Lint
- сборку debug APK
- компиляцию APK инструментальных тестов
- сборку release APK с R8 и shrink resources
- сборку release AAB

## 🌍 Языки

- English — по умолчанию
- Русский

Язык интерфейса автоматически следует языку устройства.

## 📊 История версий

- [English changelog](CHANGELOG.md)
- [Русский changelog](CHANGELOG.ru.md)
- [GitHub Releases](https://github.com/StanleyLl0yd/password-generator/releases)

## 🤝 Участие в разработке

Сообщения об ошибках, предложения и целевые Pull Request'ы приветствуются.

Изменения должны быть небольшими и целевыми, соответствовать Kotlin coding conventions, сохранять офлайн/privacy-first концепцию и по возможности сопровождаться тестами при изменении поведения.

## 📄 Лицензия

Проект распространяется по лицензии **PolyForm Noncommercial License 1.0.0**.

Некоммерческое использование, копирование, изменение и распространение разрешены в рамках условий лицензии. Для коммерческого использования требуется отдельное соглашение. Юридически значимый текст находится в [LICENSE](LICENSE).

Copyright © 2025–2026 Stanley Lloyd.

## 👨‍💻 Автор

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

---

<div align="center">
  <p>Сделано с ❤️ для пользователей, заботящихся о приватности.</p>
  <p>⭐ Поставьте звезду репозиторию, если приложение оказалось полезным.</p>
</div>
