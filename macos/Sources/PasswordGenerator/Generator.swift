import Foundation
import Security

struct GeneratorPreferences: Equatable {
    var length = PasswordConstants.defaultLength
    var useLowercase = true
    var useUppercase = true
    var useDigits = true
    var useSymbols = true
    var excludeDuplicates = true
    var excludeSimilar = true
}

enum PasswordConstants {
    static let minLength = 4
    static let maxLength = 64
    static let defaultLength = 16
    static let referenceLengthForMaxScore = 20.0

    static let similar = Array("iIl1oO0B8G6S5Z2")
    static let lowercase = Array("abcdefghijklmnopqrstuvwxyz")
    static let uppercase = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    static let digits = Array("0123456789")
    static let symbols = Array("!@#$%^&*()-_=+[]{};:,.<>?/|")
    static let fullCharSpace = 89
}

enum GenerationError: Error, Equatable {
    case invalidLength
    case noCharsets
    case notEnoughUniqueChars
    case secureRandomFailed
}

struct PasswordGenerator {
    func generate(_ preferences: GeneratorPreferences) throws -> String {
        guard (PasswordConstants.minLength...PasswordConstants.maxLength).contains(preferences.length) else {
            throw GenerationError.invalidLength
        }

        let groups = buildGroups(preferences)
        let pool = groups.flatMap { $0 }

        guard !pool.isEmpty else {
            throw GenerationError.noCharsets
        }

        if preferences.excludeDuplicates && preferences.length > pool.count {
            throw GenerationError.notEnoughUniqueChars
        }

        var result: [Character] = []
        result.reserveCapacity(preferences.length)
        var available = preferences.excludeDuplicates ? pool : []

        for group in groups {
            let character = group[try secureRandomIndex(upperBound: group.count)]
            result.append(character)
            if preferences.excludeDuplicates, let index = available.firstIndex(of: character) {
                available.remove(at: index)
            }
        }

        while result.count < preferences.length {
            if preferences.excludeDuplicates {
                let index = try secureRandomIndex(upperBound: available.count)
                result.append(available.remove(at: index))
            } else {
                result.append(pool[try secureRandomIndex(upperBound: pool.count)])
            }
        }

        if result.count > 1 {
            for index in stride(from: result.count - 1, through: 1, by: -1) {
                let other = try secureRandomIndex(upperBound: index + 1)
                if index != other {
                    result.swapAt(index, other)
                }
            }
        }

        return String(result)
    }

    func estimatePasswordScore(_ password: String) -> Int {
        guard !password.isEmpty else { return 0 }

        let characters = Array(password)
        let effectiveLength = calculateEffectiveLength(characters)
        let charSpace = calculateCharSpace(characters)
        let entropyBits = Double(effectiveLength) * log2(Double(charSpace))
        let maxEntropy = PasswordConstants.referenceLengthForMaxScore *
            log2(Double(PasswordConstants.fullCharSpace))
        let entropyScore = Int(entropyBits * 100.0 / maxEntropy)

        return min(100, max(0, entropyScore + calculatePenalty(characters, effectiveLength: effectiveLength)))
    }

    private func buildGroups(_ preferences: GeneratorPreferences) -> [[Character]] {
        func filtered(_ characters: [Character]) -> [Character] {
            guard preferences.excludeSimilar else { return characters }
            return characters.filter { !PasswordConstants.similar.contains($0) }
        }

        var groups: [[Character]] = []
        if preferences.useLowercase { groups.append(filtered(PasswordConstants.lowercase)) }
        if preferences.useUppercase { groups.append(filtered(PasswordConstants.uppercase)) }
        if preferences.useDigits { groups.append(filtered(PasswordConstants.digits)) }
        if preferences.useSymbols { groups.append(filtered(PasswordConstants.symbols)) }
        return groups.filter { !$0.isEmpty }
    }

    private func secureRandomIndex(upperBound: Int) throws -> Int {
        guard upperBound > 0, upperBound <= Int(UInt32.max) else {
            throw GenerationError.secureRandomFailed
        }

        let bound = UInt64(upperBound)
        let range = UInt64(UInt32.max) + 1
        let limit = range - (range % bound)

        while true {
            var value: UInt32 = 0
            let status = withUnsafeMutableBytes(of: &value) { buffer in
                SecRandomCopyBytes(kSecRandomDefault, buffer.count, buffer.baseAddress!)
            }
            guard status == errSecSuccess else {
                throw GenerationError.secureRandomFailed
            }
            if UInt64(value) < limit {
                return Int(UInt64(value) % bound)
            }
        }
    }

    private func calculateEffectiveLength(_ password: [Character]) -> Int {
        guard password.count >= 2 else { return password.count }

        for unitLength in 1...(password.count / 2) where password.count.isMultiple(of: unitLength) {
            var repeats = true
            for index in password.indices where password[index] != password[index % unitLength] {
                repeats = false
                break
            }
            if repeats { return unitLength }
        }
        return password.count
    }

    private func calculateCharSpace(_ password: [Character]) -> Int {
        var space = 0
        if password.contains(where: { PasswordConstants.lowercase.contains($0) }) {
            space += PasswordConstants.lowercase.count
        }
        if password.contains(where: { PasswordConstants.uppercase.contains($0) }) {
            space += PasswordConstants.uppercase.count
        }
        if password.contains(where: { PasswordConstants.digits.contains($0) }) {
            space += PasswordConstants.digits.count
        }
        if password.contains(where: {
            !PasswordConstants.lowercase.contains($0) &&
            !PasswordConstants.uppercase.contains($0) &&
            !PasswordConstants.digits.contains($0)
        }) {
            space += PasswordConstants.symbols.count
        }
        return max(space, 1)
    }

    private func calculatePenalty(_ password: [Character], effectiveLength: Int) -> Int {
        var adjustment = 0

        if effectiveLength < 6 {
            adjustment -= 35
        } else if effectiveLength < 8 {
            adjustment -= 25
        }

        let hasDigit = password.contains(where: { PasswordConstants.digits.contains($0) })
        let hasLower = password.contains(where: { PasswordConstants.lowercase.contains($0) })
        let hasUpper = password.contains(where: { PasswordConstants.uppercase.contains($0) })
        let hasSymbol = password.contains(where: {
            !PasswordConstants.lowercase.contains($0) &&
            !PasswordConstants.uppercase.contains($0) &&
            !PasswordConstants.digits.contains($0)
        })

        if effectiveLength < 10 && hasDigit && !hasLower && !hasUpper && !hasSymbol {
            adjustment -= 15
        }

        if containsSequentialSubstring(password, minimumLength: 4) { adjustment -= 20 }
        if password.count >= 4 && Double(Set(password).count) / Double(password.count) < 0.5 {
            adjustment -= 10
        }
        if Set(password).count == 1 && password.count >= 3 { adjustment -= 10 }

        let normalized = String(password).lowercased()
        let commonPatterns = ["password", "qwerty", "asdf", "zxcv", "letmein", "admin", "welcome"]
        if commonPatterns.contains(where: { normalized.contains($0) }) { adjustment -= 30 }

        return adjustment
    }

    private func containsSequentialSubstring(_ password: [Character], minimumLength: Int) -> Bool {
        guard password.count >= minimumLength else { return false }

        let bytes = password.map { Array(String($0).utf8).first ?? 0 }
        var ascending = 1
        var descending = 1

        for index in 1..<bytes.count {
            let difference = Int(bytes[index]) - Int(bytes[index - 1])
            if difference == 1 {
                ascending += 1
                if ascending >= minimumLength { return true }
            } else {
                ascending = 1
            }

            if difference == -1 {
                descending += 1
                if descending >= minimumLength { return true }
            } else {
                descending = 1
            }
        }

        return false
    }
}
