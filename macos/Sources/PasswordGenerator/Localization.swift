import Foundation

struct AppText {
    let title: String
    let password: String
    let show: String
    let hide: String
    let copy: String
    let strength: String
    let length: String
    let charsets: String
    let lowercase: String
    let uppercase: String
    let digits: String
    let symbols: String
    let advanced: String
    let excludeSimilar: String
    let excludeDuplicates: String
    let generate: String
    let about: String
    let copied: String
    let invalidLength: String
    let noCharsets: String
    let notEnoughUnique: String
    let secureRandomFailed: String
    let veryWeak: String
    let weak: String
    let medium: String
    let strong: String
    let veryStrong: String

    static let english = AppText(
        title: "Password Generator",
        password: "Password",
        show: "Show",
        hide: "Hide",
        copy: "Copy",
        strength: "Password strength",
        length: "Length",
        charsets: "Character sets",
        lowercase: "a-z · Lowercase",
        uppercase: "A-Z · Uppercase",
        digits: "0-9 · Digits",
        symbols: "!@# · Symbols",
        advanced: "Advanced",
        excludeSimilar: "Exclude similar characters",
        excludeDuplicates: "Exclude duplicate characters",
        generate: "Generate password",
        about: "About Password Generator",
        copied: "Password copied. It will be cleared after 60 seconds if it is still current.",
        invalidLength: "Password length must be between 4 and 64 characters.",
        noCharsets: "Select at least one character set.",
        notEnoughUnique: "Not enough unique characters for this length without duplicates.",
        secureRandomFailed: "The macOS secure random source failed.",
        veryWeak: "Very weak",
        weak: "Weak",
        medium: "Medium",
        strong: "Strong",
        veryStrong: "Very strong"
    )

    static let russian = AppText(
        title: "Генератор паролей",
        password: "Пароль",
        show: "Показать",
        hide: "Скрыть",
        copy: "Копировать",
        strength: "Надёжность пароля",
        length: "Длина",
        charsets: "Наборы символов",
        lowercase: "a-z · строчные",
        uppercase: "A-Z · заглавные",
        digits: "0-9 · цифры",
        symbols: "!@# · спец.",
        advanced: "Дополнительно",
        excludeSimilar: "Исключать похожие символы",
        excludeDuplicates: "Исключать повторы",
        generate: "Сгенерировать пароль",
        about: "О приложении",
        copied: "Пароль скопирован. Через 60 секунд он будет удалён, если останется текущим.",
        invalidLength: "Длина пароля должна быть от 4 до 64 символов.",
        noCharsets: "Выберите хотя бы один набор символов.",
        notEnoughUnique: "Недостаточно уникальных символов для такой длины без повторов.",
        secureRandomFailed: "Не удалось получить случайные данные от macOS.",
        veryWeak: "Очень слабый",
        weak: "Слабый",
        medium: "Средний",
        strong: "Сильный",
        veryStrong: "Очень сильный"
    )

    static var current: AppText {
        let language = Locale.preferredLanguages.first?.lowercased() ?? "en"
        return language.hasPrefix("ru") ? .russian : .english
    }
}
