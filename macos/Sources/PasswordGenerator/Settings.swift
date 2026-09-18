import Foundation

struct SettingsStore {
    private enum Key {
        static let length = "length"
        static let useLowercase = "use_lowercase"
        static let useUppercase = "use_uppercase"
        static let useDigits = "use_digits"
        static let useSymbols = "use_symbols"
        static let excludeDuplicates = "exclude_duplicates"
        static let excludeSimilar = "exclude_similar"
    }

    private let defaults = UserDefaults.standard

    func load() -> GeneratorPreferences {
        var preferences = GeneratorPreferences()

        if defaults.object(forKey: Key.length) != nil {
            preferences.length = min(
                PasswordConstants.maxLength,
                max(PasswordConstants.minLength, defaults.integer(forKey: Key.length))
            )
        }

        preferences.useLowercase = bool(forKey: Key.useLowercase, fallback: true)
        preferences.useUppercase = bool(forKey: Key.useUppercase, fallback: true)
        preferences.useDigits = bool(forKey: Key.useDigits, fallback: true)
        preferences.useSymbols = bool(forKey: Key.useSymbols, fallback: true)
        preferences.excludeDuplicates = bool(forKey: Key.excludeDuplicates, fallback: true)
        preferences.excludeSimilar = bool(forKey: Key.excludeSimilar, fallback: true)

        return preferences
    }

    func save(_ preferences: GeneratorPreferences) {
        defaults.set(preferences.length, forKey: Key.length)
        defaults.set(preferences.useLowercase, forKey: Key.useLowercase)
        defaults.set(preferences.useUppercase, forKey: Key.useUppercase)
        defaults.set(preferences.useDigits, forKey: Key.useDigits)
        defaults.set(preferences.useSymbols, forKey: Key.useSymbols)
        defaults.set(preferences.excludeDuplicates, forKey: Key.excludeDuplicates)
        defaults.set(preferences.excludeSimilar, forKey: Key.excludeSimilar)

        defaults.removeObject(forKey: "password")
    }

    private func bool(forKey key: String, fallback: Bool) -> Bool {
        guard defaults.object(forKey: key) != nil else { return fallback }
        return defaults.bool(forKey: key)
    }
}
