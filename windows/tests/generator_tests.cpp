#include "generator.hpp"

#include <algorithm>
#include <cstdlib>
#include <iostream>
#include <string>
#include <string_view>
#include <unordered_set>

namespace {

void Require(bool condition, const char* message) {
    if (!condition) {
        std::cerr << "FAIL: " << message << '\n';
        std::exit(1);
    }
}

bool ContainsAny(const std::string& password, std::string_view chars) {
    return std::any_of(password.begin(), password.end(), [&](char ch) {
        return chars.find(ch) != std::string_view::npos;
    });
}

}  // namespace

int main() {
    pg::PasswordGenerator generator;

    pg::Preferences defaults;
    for (int i = 0; i < 256; ++i) {
        const auto result = generator.Generate(defaults);
        Require(std::holds_alternative<std::string>(result), "default generation succeeds");
        const auto& password = std::get<std::string>(result);
        Require(password.size() == 16, "default length");
        Require(ContainsAny(password, pg::kLowercaseChars), "lowercase represented");
        Require(ContainsAny(password, pg::kUppercaseChars), "uppercase represented");
        Require(ContainsAny(password, pg::kDigitChars), "digits represented");
        Require(ContainsAny(password, pg::kSymbolChars), "symbols represented");
        Require(std::unordered_set<char>(password.begin(), password.end()).size() == password.size(),
                "duplicates excluded");
        Require(!ContainsAny(password, pg::kSimilarChars), "similar chars excluded");
    }

    pg::Preferences minimum;
    minimum.length = 4;
    minimum.excludeDuplicates = false;
    minimum.excludeSimilar = false;
    const auto minimumResult = generator.Generate(minimum);
    Require(std::holds_alternative<std::string>(minimumResult), "minimum generation succeeds");
    const auto& minimumPassword = std::get<std::string>(minimumResult);
    Require(ContainsAny(minimumPassword, pg::kLowercaseChars), "minimum lowercase represented");
    Require(ContainsAny(minimumPassword, pg::kUppercaseChars), "minimum uppercase represented");
    Require(ContainsAny(minimumPassword, pg::kDigitChars), "minimum digit represented");
    Require(ContainsAny(minimumPassword, pg::kSymbolChars), "minimum symbol represented");

    pg::Preferences invalid;
    invalid.length = 3;
    Require(std::get<pg::GenerationError>(generator.Generate(invalid)) == pg::GenerationError::InvalidLength,
            "invalid length rejected");

    pg::Preferences empty;
    empty.useLowercase = false;
    empty.useUppercase = false;
    empty.useDigits = false;
    empty.useSymbols = false;
    Require(std::get<pg::GenerationError>(generator.Generate(empty)) == pg::GenerationError::NoCharsets,
            "empty charset rejected");

    pg::Preferences impossible;
    impossible.length = 64;
    impossible.useUppercase = false;
    impossible.useDigits = false;
    impossible.useSymbols = false;
    impossible.excludeDuplicates = true;
    Require(std::get<pg::GenerationError>(generator.Generate(impossible)) ==
                pg::GenerationError::NotEnoughUniqueChars,
            "impossible unique request rejected");

    Require(generator.EstimatePasswordScore("") == 0, "empty score");
    Require(generator.EstimatePasswordScore("aaaaaaaa") <= 5, "repeated password penalized");
    Require(generator.EstimatePasswordScore("1234") <= 5, "short sequence penalized");
    Require(generator.EstimatePasswordScore("password") <= 20, "common password penalized");

    std::cout << "Password Generator Windows contract tests: OK\n";
    return 0;
}
