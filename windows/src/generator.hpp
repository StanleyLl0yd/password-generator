#pragma once

#include <cstdint>
#include <string>
#include <variant>

namespace pg {

inline constexpr int kMinLength = 4;
inline constexpr int kMaxLength = 64;
inline constexpr int kDefaultLength = 16;
inline constexpr double kReferenceLengthForMaxScore = 20.0;

inline constexpr char kSimilarChars[] = "iIl1oO0B8G6S5Z2";
inline constexpr char kLowercaseChars[] = "abcdefghijklmnopqrstuvwxyz";
inline constexpr char kUppercaseChars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
inline constexpr char kDigitChars[] = "0123456789";
inline constexpr char kSymbolChars[] = "!@#$%^&*()-_=+[]{};:,.<>?/|";
inline constexpr int kFullCharSpace = 89;

struct Preferences {
    int length = kDefaultLength;
    bool useLowercase = true;
    bool useUppercase = true;
    bool useDigits = true;
    bool useSymbols = true;
    bool excludeDuplicates = true;
    bool excludeSimilar = true;
};

enum class GenerationError {
    InvalidLength,
    NoCharsets,
    NotEnoughUniqueChars
};

using GenerationResult = std::variant<std::string, GenerationError>;

class PasswordGenerator {
public:
    GenerationResult Generate(const Preferences& preferences) const;
    int EstimatePasswordScore(const std::string& password) const;

private:
    static std::uint32_t RandomUInt32();
    static std::size_t RandomIndex(std::size_t upperExclusive);
};

}  // namespace pg
