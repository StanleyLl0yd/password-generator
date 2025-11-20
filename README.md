Password Generator (Offline)

Android app for generating strong, unique passwords fully offline.

Overview

Password Generator is a minimal, privacy-friendly tool that helps you create strong passwords for any service.
All generation happens locally on your device: the app does not use the network, does not send data anywhere and does not show ads.

Features
	•	Offline password generation – no network access is required
	•	Flexible character sets:
	•	Lowercase letters (a–z)
	•	Uppercase letters (A–Z)
	•	Digits (0–9)
	•	Special characters (!@#…)
	•	Extra options:
	•	Exclude duplicate characters
	•	Exclude similar characters (i I 1 l o O 0) to make passwords easier to read and type
	•	Password length from 4 to 64 characters (default: 16)
	•	Visual password strength indicator:
	•	Score from 0 to 100
	•	Color gradient from red (weak) through yellow to green (strong)
	•	Text labels: Very weak / Weak / Medium / Strong / Very strong
	•	Single-screen UI in Material Design 3
	•	One-tap copy to clipboard

Password strength model

The app uses an entropy-based score, calibrated for real-world passwords:
	•	Estimates character space based on used sets (lowercase, uppercase, digits, symbols)
	•	Entropy ≈ length * log2(charSpace)
	•	Normalized so that a 20-character random password from the full set is close to 100
	•	Penalties are applied for:
	•	Short length (4–7 characters)
	•	Digits-only short passwords
	•	Strict ascending/descending sequences (e.g. 123456, abcdef)
	•	Heavy repetition and all-same characters

Resulting score is clamped to 0–100 and mapped to 5 levels:
Very weak, Weak, Medium, Strong, Very strong.

This score is only a heuristic and does not guarantee absolute security, but it clearly separates obviously weak passwords from strong, high-entropy ones.

Technology
	•	Language: Kotlin
	•	UI: Jetpack Compose
	•	Design: Material 3
	•	Min SDK: 24
	•	No external backend, everything is on-device
	•	Single MainActivity + composable UI

Project structure
	•	app/src/main/java/com/sl/passwordgenerator
	•	MainActivity.kt – entry point
	•	PasswordGeneratorScreen.kt – UI, generation logic, strength estimation
	•	app/src/main/java/com/sl/passwordgenerator/ui/theme
	•	Theme.kt, Color.kt, Type.kt – Material 3 theme
	•	app/src/main/res
	•	values/*.xml – strings, theme, colors
	•	mipmap-* / drawable – app icon

Getting started

Requirements
	•	Android Studio Hedgehog / Iguana or newer
	•	Android SDK 24+
	•	JDK 11

Build & run
	1.	Clone the repository
	•	git clone https://github.com/<your-username>/password-generator.git
	•	cd password-generator
	2.	Open the project in Android Studio
	3.	Let Gradle sync finish
	4.	Run on an emulator or a physical device (API 24+)

Privacy and data
	•	The app works 100% offline
	•	No network requests
	•	No analytics, no crash reporting, no ads
	•	Passwords are generated in memory and are not stored anywhere by the app
	•	The only operation with external state is copying to clipboard when you tap “Copy”

Roadmap / ideas

Planned and possible future improvements:
	•	History of recently generated passwords (optional, opt-in)
	•	Custom character sets
	•	Dark theme toggle (if you want to override system)
	•	Import/export of settings
	•	In-app help and security tips

⸻

Генератор паролей (офлайн)

Android-приложение для генерации надёжных паролей полностью офлайн.

Обзор

«Генератор паролей» — это минималистичное и приватное приложение для создания сильных и уникальных паролей для любых сервисов.
Все расчёты выполняются локально на устройстве: приложение не использует интернет, не отправляет данные и не показывает рекламу.

Возможности
	•	Генерация паролей без интернета
	•	Гибкий выбор наборов символов:
	•	строчные буквы (a–z)
	•	заглавные буквы (A–Z)
	•	цифры (0–9)
	•	спецсимволы (!@#…)
	•	Дополнительные опции:
	•	исключать повторяющиеся символы
	•	исключать похожие символы (i I 1 l o O 0), чтобы пароль было легче прочитать и ввести
	•	Длина пароля от 4 до 64 символов (по умолчанию – 16)
	•	Наглядная оценка надёжности:
	•	шкала от 0 до 100
	•	цветовой градиент от красного (слабый) через жёлтый к зелёному (сильный)
	•	текстовые уровни: Очень слабый / Слабый / Средний / Сильный / Очень сильный
	•	Весь интерфейс — на одном экране в стиле Material Design 3
	•	Быстрое копирование пароля в буфер обмена одной кнопкой

Модель оценки надёжности

Приложение использует оценку на основе энтропии, адаптированную под реальные пароли:
	•	Определяется «алфавит» по использованным наборам символов (строчные, заглавные, цифры, спецсимволы)
	•	Энтропия ≈ length * log2(charSpace)
	•	Энтропия нормируется так, чтобы случайный пароль длиной 20 символов из полного набора давал оценку около 100
	•	Дополнительно применяются штрафы за:
	•	короткую длину (4–7 символов)
	•	короткие пароли только из цифр
	•	простые последовательности (строго по возрастанию или убыванию, например 123456, abcdef)
	•	большое количество повторов и одинаковые символы

Результат ограничивается диапазоном 0–100 и переводится в 5 уровней:
Очень слабый, Слабый, Средний, Сильный, Очень сильный.

Это не «волшебная защита», а удобная оценка, которая чётко показывает, насколько пароль лучше или хуже очевидно слабых вариантов.

Технологии
	•	Язык: Kotlin
	•	UI: Jetpack Compose
	•	Дизайн: Material 3
	•	Min SDK: 24
	•	Без внешнего бэкенда, всё на устройстве
	•	Одна MainActivity + composable-экран

Структура проекта
	•	app/src/main/java/com/sl/passwordgenerator
	•	MainActivity.kt — входная точка приложения
	•	PasswordGeneratorScreen.kt — интерфейс, генерация пароля, оценка надёжности
	•	app/src/main/java/com/sl/passwordgenerator/ui/theme
	•	Theme.kt, Color.kt, Type.kt — тема Material 3
	•	app/src/main/res
	•	values/*.xml — строки, тема, цвета
	•	mipmap-* / drawable — иконка приложения

Как собрать и запустить

Требования
	•	Android Studio Hedgehog / Iguana или новее
	•	Android SDK 24+
	•	JDK 11

Шаги
	1.	Клонировать репозиторий
git clone https://github.com/<your-username>/password-generator.git
cd password-generator
	2.	Открыть проект в Android Studio
	3.	Дождаться окончания синхронизации Gradle
	4.	Запустить на эмуляторе или реальном устройстве (API 24+)

Конфиденциальность и данные
	•	Приложение работает полностью офлайн
	•	Нет сетевых запросов
	•	Нет аналитики, трекинга и рекламы
	•	Пароли генерируются в памяти и не сохраняются приложением
	•	Единственное действие «наружу» — копирование пароля в буфер обмена по нажатию кнопки

Дальнейшее развитие

Идеи для будущих версий:
	•	История недавно сгенерированных паролей (по желанию пользователя)
	•	Пользовательские наборы символов
	•	Переключатель темы (принудительно светлая/тёмная)
	•	Экспорт/импорт настроек
	•	Встроенные подсказки по безопасности паролей
