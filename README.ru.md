# Генератор Паролей

[![Android CI](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/StanleyLl0yd/password-generator/actions/workflows/android.yml)
[![Latest release](https://img.shields.io/github/v/release/StanleyLl0yd/password-generator)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/StanleyLl0yd/password-generator/total)](https://github.com/StanleyLl0yd/password-generator/releases)
[![Android 7+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)](https://github.com/StanleyLl0yd/password-generator/releases/latest)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-blue)](LICENSE)

[![en](https://img.shields.io/badge/lang-en-red.svg)](README.md)
[![ru](https://img.shields.io/badge/lang-ru-blue.svg)](README.ru.md)

Приватный офлайн-генератор паролей для Android на Kotlin, Jetpack Compose и Material 3.

[⬇️ Скачать последнюю версию APK](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Текущая версия: **1.5.0** · Min SDK: **24 (Android 7.0)** · Target SDK: **36**

## ✨ Возможности

- Строчные и заглавные буквы, цифры и специальные символы
- Длина пароля от 4 до 64 символов
- Исключение повторяющихся символов
- Исключение визуально похожих символов: `i I l 1 o O 0 B 8 G 6 S 5 Z 2`
- Индикатор надёжности пароля в реальном времени
- Безопасное копирование с автоматической очисткой буфера через 60 секунд
- Адаптивный интерфейс для разных размеров экранов
- Material 3 со светлой и тёмной темой по системным настройкам
- Русская и английская локализация

## 🔒 Приватность и безопасность

- **100% офлайн** — приложению не нужен доступ в интернет
- **Без аналитики, трекинга и рекламы**
- Сгенерированные пароли хранятся только в памяти и никогда не записываются на диск
- Сохраняются только настройки генератора
- Пароли, которые могли быть сохранены версиями 1.4.1 и старше, автоматически удаляются после обновления
- Настройки исключены из автоматического резервного копирования Android
- Содержимое буфера обмена помечается как конфиденциальное и очищается через 60 секунд, не затрагивая более новое содержимое

О проблемах безопасности сообщайте согласно [SECURITY.md](SECURITY.md).

## 📦 Установка

Рекомендуемый способ установки — скачать APK из последнего GitHub Release:

[Скачать последнюю версию](https://github.com/StanleyLl0yd/password-generator/releases/latest)

Для установки требуется Android 7.0 или новее.

### Сборка из исходного кода

```bash
git clone https://github.com/StanleyLl0yd/password-generator.git
cd password-generator
./gradlew assembleDebug
```

Для сборки нужен JDK 17 и актуальная версия Android Studio с поддержкой проекта.

## 🛠️ Технологии

| Категория | Технология |
|---|---|
| Язык | Kotlin 2.2.0 |
| UI | Jetpack Compose |
| Дизайн | Material 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt 2.57.2 |
| Асинхронность | Kotlin Coroutines + Flow |
| Хранилище настроек | DataStore Preferences |
| Сборка | Gradle 8.13, Kotlin DSL |

## 🧪 Проверка проекта

GitHub Actions запускает unit-тесты, Android Lint, debug-сборку, release APK и release AAB для каждого push в `main` и каждого Pull Request.

Локально основные проверки можно запустить так:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease
```

Инструментальные тесты требуют эмулятор или реальное устройство:

```bash
./gradlew connectedAndroidTest
```

## 📊 История версий

Подробная история изменений находится в [CHANGELOG.ru.md](CHANGELOG.ru.md).

Последний релиз: **v1.4.4**.

Основные изменения относительно v1.4.0:

- Пароли больше не сохраняются на устройстве
- Старые сохранённые пароли автоматически удаляются после обновления
- Буфер обмена защищён и очищается через 60 секунд
- Исправлена работа функции «Исключать повторы»
- Расширен список визуально похожих символов
- Существенно улучшена оценка надёжности пароля
- Улучшена стабильность интерфейса и сохранения настроек

## 🤝 Участие в разработке

Pull Request'ы приветствуются. Перед отправкой изменений убедитесь, что проект собирается и проверки проходят успешно.

Для сообщений об ошибках и предложений используйте [GitHub Issues](https://github.com/StanleyLl0yd/password-generator/issues).

## 📄 Лицензия

Проект распространяется по лицензии **PolyForm Noncommercial License 1.0.0**.

Некоммерческое использование, копирование, изменение и распространение разрешены. Для коммерческого использования требуется отдельное соглашение.

Полный текст лицензии: [LICENSE](LICENSE).

## 👨‍💻 Автор

**Stanley Lloyd** · [@StanleyLl0yd](https://github.com/StanleyLl0yd)

## 🔮 Дорожная карта

- [ ] Генератор парольных фраз
- [ ] Быстрые профили (PIN, Wi-Fi, 16/24/32 символа)
- [ ] Пользовательские наборы символов
- [ ] Шаблоны паролей
- [ ] История паролей с явным включением пользователем
- [ ] Резервное копирование и восстановление настроек
- [ ] Виджет
- [ ] Дополнительные языки

---

<div align="center">
  <p>Сделано с ❤️ для пользователей, заботящихся о приватности и безопасности.</p>
  <p>⭐ Поставьте звезду репозиторию, если приложение оказалось полезным.</p>
</div>
