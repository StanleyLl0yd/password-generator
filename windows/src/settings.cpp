#include "settings.hpp"

#include <windows.h>

namespace pg {
namespace {

constexpr wchar_t kRegistryPath[] = L"Software\\StanleyLloyd\\PasswordGenerator";

DWORD ReadDword(HKEY key, const wchar_t* name, DWORD fallback) {
    DWORD value = fallback;
    DWORD size = sizeof(value);
    if (RegGetValueW(key, nullptr, name, RRF_RT_REG_DWORD, nullptr, &value, &size) != ERROR_SUCCESS) {
        return fallback;
    }
    return value;
}

void WriteDword(HKEY key, const wchar_t* name, DWORD value) {
    RegSetValueExW(
        key,
        name,
        0,
        REG_DWORD,
        reinterpret_cast<const BYTE*>(&value),
        static_cast<DWORD>(sizeof(value))
    );
}

}  // namespace

Preferences LoadPreferences() {
    Preferences preferences;
    HKEY key = nullptr;
    if (RegOpenKeyExW(HKEY_CURRENT_USER, kRegistryPath, 0, KEY_QUERY_VALUE, &key) != ERROR_SUCCESS) {
        return preferences;
    }

    preferences.length = static_cast<int>(ReadDword(key, L"Length", kDefaultLength));
    if (preferences.length < kMinLength || preferences.length > kMaxLength) {
        preferences.length = kDefaultLength;
    }

    preferences.useLowercase = ReadDword(key, L"UseLowercase", 1) != 0;
    preferences.useUppercase = ReadDword(key, L"UseUppercase", 1) != 0;
    preferences.useDigits = ReadDword(key, L"UseDigits", 1) != 0;
    preferences.useSymbols = ReadDword(key, L"UseSymbols", 1) != 0;
    preferences.excludeDuplicates = ReadDword(key, L"ExcludeDuplicates", 1) != 0;
    preferences.excludeSimilar = ReadDword(key, L"ExcludeSimilar", 1) != 0;

    RegCloseKey(key);
    return preferences;
}

void SavePreferences(const Preferences& preferences) {
    HKEY key = nullptr;
    if (RegCreateKeyExW(
            HKEY_CURRENT_USER,
            kRegistryPath,
            0,
            nullptr,
            0,
            KEY_SET_VALUE,
            nullptr,
            &key,
            nullptr
        ) != ERROR_SUCCESS) {
        return;
    }

    WriteDword(key, L"Length", static_cast<DWORD>(preferences.length));
    WriteDword(key, L"UseLowercase", preferences.useLowercase ? 1U : 0U);
    WriteDword(key, L"UseUppercase", preferences.useUppercase ? 1U : 0U);
    WriteDword(key, L"UseDigits", preferences.useDigits ? 1U : 0U);
    WriteDword(key, L"UseSymbols", preferences.useSymbols ? 1U : 0U);
    WriteDword(key, L"ExcludeDuplicates", preferences.excludeDuplicates ? 1U : 0U);
    WriteDword(key, L"ExcludeSimilar", preferences.excludeSimilar ? 1U : 0U);

    RegCloseKey(key);
}

}  // namespace pg
