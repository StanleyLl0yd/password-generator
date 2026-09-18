import XCTest
@testable import PasswordGenerator

final class GeneratorTests: XCTestCase {
    func testDefaultGenerationContract() throws {
        let generator = PasswordGenerator()
        let preferences = GeneratorPreferences()

        for _ in 0..<256 {
            let password = try generator.generate(preferences)
            XCTAssertEqual(password.count, PasswordConstants.defaultLength)
            XCTAssertTrue(password.contains(where: { PasswordConstants.lowercase.contains($0) }))
            XCTAssertTrue(password.contains(where: { PasswordConstants.uppercase.contains($0) }))
            XCTAssertTrue(password.contains(where: { PasswordConstants.digits.contains($0) }))
            XCTAssertTrue(password.contains(where: { PasswordConstants.symbols.contains($0) }))
            XCTAssertEqual(Set(password).count, password.count)
            XCTAssertFalse(password.contains(where: { PasswordConstants.similar.contains($0) }))
        }
    }

    func testEveryEnabledGroupFitsAtMinimumLength() throws {
        let generator = PasswordGenerator()
        var preferences = GeneratorPreferences()
        preferences.length = 4
        preferences.excludeDuplicates = false
        preferences.excludeSimilar = false

        let password = try generator.generate(preferences)

        XCTAssertTrue(password.contains(where: { PasswordConstants.lowercase.contains($0) }))
        XCTAssertTrue(password.contains(where: { PasswordConstants.uppercase.contains($0) }))
        XCTAssertTrue(password.contains(where: { PasswordConstants.digits.contains($0) }))
        XCTAssertTrue(password.contains(where: { PasswordConstants.symbols.contains($0) }))
    }

    func testInvalidConfigurations() {
        let generator = PasswordGenerator()

        var invalid = GeneratorPreferences()
        invalid.length = 3
        XCTAssertThrowsError(try generator.generate(invalid)) {
            XCTAssertEqual($0 as? GenerationError, .invalidLength)
        }

        var empty = GeneratorPreferences()
        empty.useLowercase = false
        empty.useUppercase = false
        empty.useDigits = false
        empty.useSymbols = false
        XCTAssertThrowsError(try generator.generate(empty)) {
            XCTAssertEqual($0 as? GenerationError, .noCharsets)
        }

        var impossible = GeneratorPreferences()
        impossible.length = 64
        impossible.useUppercase = false
        impossible.useDigits = false
        impossible.useSymbols = false
        impossible.excludeDuplicates = true
        XCTAssertThrowsError(try generator.generate(impossible)) {
            XCTAssertEqual($0 as? GenerationError, .notEnoughUniqueChars)
        }
    }

    func testStrengthRegressionCases() {
        let generator = PasswordGenerator()

        XCTAssertEqual(generator.estimatePasswordScore(""), 0)
        XCTAssertLessThanOrEqual(generator.estimatePasswordScore("aaaaaaaa"), 5)
        XCTAssertLessThanOrEqual(generator.estimatePasswordScore("1234"), 5)
        XCTAssertLessThanOrEqual(generator.estimatePasswordScore("password"), 20)
    }
}
