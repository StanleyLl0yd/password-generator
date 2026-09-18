#include "generator.hpp"

#include <algorithm>
#include <array>
#include <bcrypt.h>
#include <cmath>
#include <cctype>
#include <limits>
#include <stdexcept>
#include <string_view>
#include <unordered_set>
#include <vector>

namespace pg {
namespace {

struct CharPool {
    std::vector<std::string> groups;
    std::string allChars;
};

bool ContainsChar(std::string_view chars, char value) {
    return chars.find(value) != std::string_view::npos;
}

std::string FilterSimilar(std::string_view chars, bool excludeSimilar) {
    std::string result;
    result.reserve(chars.size());
    for (const char ch : chars) {
        if (!excludeSimilar || !ContainsChar(kSimilarChars, ch)) {
            result.push_back(ch);
        }
    }
    return result;
}

CharPool BuildCharPool(const Preferences& preferences) {
    CharPool pool;
    const auto addGroup = [&](std::string_view chars) {
        auto filtered = FilterSimilar(chars, preferences.excludeSimilar);
        if (!filtered.empty()) {
            pool.allChars += filtered;
            pool.groups.push_back(std::move(filtered));
        }
    };

    if (preferences.useLowercase) addGroup(kLowercaseChars);
    if (preferences.useUppercase) addGroup(kUppercaseChars);
    if (preferences.useDigits) addGroup(kDigitChars);
    if (preferences.useSymbols) addGroup(kSymbolChars);

    return pool;
}

int CalculateEffectiveLength(const std::string& password) {
    for (std::size_t unitLength = 1; unitLength <= password.size() / 2; ++unitLength) {
        if (password.size() % unitLength != 0) continue;
        bool repeats = true;
        for (std::size_t index = 0; index < password.size(); ++index) {
            if (password[index] != password[index % unitLength]) {
                repeats = false;
                break;
            }
        }
        if (repeats) return static_cast<int>(unitLength);
    }
    return static_cast<int>(password.size());
}

int CalculateCharSpace(const std::string& password) {
    int space = 0;
    if (std::any_of(password.begin(), password.end(), [](char ch) { return ContainsChar(kLowercaseChars, ch); })) {
        space += static_cast<int>(std::string_view(kLowercaseChars).size());
    }
    if (std::any_of(password.begin(), password.end(), [](char ch) { return ContainsChar(kUppercaseChars, ch); })) {
        space += static_cast<int>(std::string_view(kUppercaseChars).size());
    }
    if (std::any_of(password.begin(), password.end(), [](char ch) { return ContainsChar(kDigitChars, ch); })) {
        space += static_cast<int>(std::string_view(kDigitChars).size());
    }
    if (std::any_of(password.begin(), password.end(), [](char ch) { return !std::isalnum(static_cast<unsigned char>(ch)); })) {
        space += static_cast<int>(std::string_view(kSymbolChars).size());
    }
    return std::max(space, 1);
}

bool ContainsSequentialSubstring(const std::string& password, int minLength) {
    if (static_cast<int>(password.size()) < minLength) return false;
    int ascendingLength = 1;
    int descendingLength = 1;

    for (std::size_t index = 1; index < password.size(); ++index) {
        const int difference = static_cast<unsigned char>(password[index]) -
            static_cast<unsigned char>(password[index - 1]);

        if (difference == 1) {
            if (++ascendingLength >= minLength) return true;
        } else {
            ascendingLength = 1;
        }

        if (difference == -1) {
            if (++descendingLength >= minLength) return true;
        } else {
            descendingLength = 1;
        }
    }
    return false;
}

bool HasManyRepeats(const std::string& password) {
    if (password.size() < 4) return false;
    std::unordered_set<char> unique(password.begin(), password.end());
    return static_cast<double>(unique.size()) / static_cast<double>(password.size()) < 0.5;
}

bool ContainsCommonPattern(const std::string& password) {
    std::string normalized = password;
    std::transform(normalized.begin(), normalized.end(), normalized.begin(), [](unsigned char ch) {
        return static_cast<char>(std::tolower(ch));
    });

    constexpr std::array<std::string_view, 7> patterns{
        "password", "qwerty", "asdf", "zxcv", "letmein", "admin", "welcome"
    };

    return std::any_of(patterns.begin(), patterns.end(), [&](std::string_view pattern) {
        return normalized.find(pattern) != std::string::npos;
    });
}

int CalculatePenalty(const std::string& password, int effectiveLength) {
    int adjustment = 0;

    if (effectiveLength < 6) {
        adjustment -= 35;
    } else if (effectiveLength < 8) {
        adjustment -= 25;
    }

    const bool hasDigit = std::any_of(password.begin(), password.end(), [](char ch) {
        return ContainsChar(kDigitChars, ch);
    });
    const bool hasSymbol = std::any_of(password.begin(), password.end(), [](char ch) {
        return !std::isalnum(static_cast<unsigned char>(ch));
    });
    const bool hasLower = std::any_of(password.begin(), password.end(), [](char ch) {
        return ContainsChar(kLowercaseChars, ch);
    });
    const bool hasUpper = std::any_of(password.begin(), password.end(), [](char ch) {
        return ContainsChar(kUppercaseChars, ch);
    });

    if (effectiveLength < 10 && hasDigit && !hasLower && !hasUpper && !hasSymbol) adjustment -= 15;
    if (ContainsSequentialSubstring(password, 4)) adjustment -= 20;
    if (HasManyRepeats(password)) adjustment -= 10;

    std::unordered_set<char> unique(password.begin(), password.end());
    if (unique.size() == 1 && password.size() >= 3) adjustment -= 10;
    if (ContainsCommonPattern(password)) adjustment -= 30;

    return adjustment;
}

}  // namespace

std::uint32_t PasswordGenerator::RandomUInt32() {
    std::uint32_t value = 0;
    const NTSTATUS status = BCryptGenRandom(
        nullptr,
        reinterpret_cast<PUCHAR>(&value),
        static_cast<ULONG>(sizeof(value)),
        BCRYPT_USE_SYSTEM_PREFERRED_RNG
    );
    if (status < 0) {
        throw std::runtime_error("BCryptGenRandom failed");
    }
    return value;
}

std::size_t PasswordGenerator::RandomIndex(std::size_t upperExclusive) {
    if (upperExclusive == 0 || upperExclusive > std::numeric_limits<std::uint32_t>::max()) {
        throw std::invalid_argument("invalid random bound");
    }

    const auto bound = static_cast<std::uint32_t>(upperExclusive);
    const std::uint64_t range = static_cast<std::uint64_t>(std::numeric_limits<std::uint32_t>::max()) + 1ULL;
    const std::uint64_t limit = range - (range % bound);

    std::uint32_t value;
    do {
        value = RandomUInt32();
    } while (static_cast<std::uint64_t>(value) >= limit);

    return static_cast<std::size_t>(value % bound);
}

GenerationResult PasswordGenerator::Generate(const Preferences& preferences) const {
    if (preferences.length < kMinLength || preferences.length > kMaxLength) {
        return GenerationError::InvalidLength;
    }

    CharPool pool = BuildCharPool(preferences);
    if (pool.allChars.empty()) return GenerationError::NoCharsets;

    if (preferences.excludeDuplicates &&
        static_cast<std::size_t>(preferences.length) > pool.allChars.size()) {
        return GenerationError::NotEnoughUniqueChars;
    }

    std::string result;
    result.reserve(static_cast<std::size_t>(preferences.length));
    std::vector<char> available;
    if (preferences.excludeDuplicates) {
        available.assign(pool.allChars.begin(), pool.allChars.end());
    }

    for (const auto& group : pool.groups) {
        const char ch = group[RandomIndex(group.size())];
        result.push_back(ch);
        if (preferences.excludeDuplicates) {
            const auto it = std::find(available.begin(), available.end(), ch);
            if (it != available.end()) available.erase(it);
        }
    }

    while (static_cast<int>(result.size()) < preferences.length) {
        if (preferences.excludeDuplicates) {
            const auto index = RandomIndex(available.size());
            result.push_back(available[index]);
            available.erase(available.begin() + static_cast<std::ptrdiff_t>(index));
        } else {
            result.push_back(pool.allChars[RandomIndex(pool.allChars.size())]);
        }
    }

    for (std::size_t i = result.size(); i > 1; --i) {
        const auto j = RandomIndex(i);
        std::swap(result[i - 1], result[j]);
    }

    return result;
}

int PasswordGenerator::EstimatePasswordScore(const std::string& password) const {
    if (password.empty()) return 0;

    const int effectiveLength = CalculateEffectiveLength(password);
    const int charSpace = CalculateCharSpace(password);
    const double entropyBits = effectiveLength * (std::log(static_cast<double>(charSpace)) / std::log(2.0));
    const double maxEntropy = kReferenceLengthForMaxScore *
        (std::log(static_cast<double>(kFullCharSpace)) / std::log(2.0));
    const int entropyScore = static_cast<int>(entropyBits * 100.0 / maxEntropy);

    return std::clamp(entropyScore + CalculatePenalty(password, effectiveLength), 0, 100);
}

}  // namespace pg
