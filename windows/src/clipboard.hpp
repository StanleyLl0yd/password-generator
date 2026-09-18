#pragma once

#include <windows.h>

#include <string>

namespace pg {

class SecureClipboard {
public:
    static constexpr UINT_PTR kTimerId = 0x5047;
    static constexpr UINT kClearDelayMs = 60'000;

    bool Copy(HWND owner, const std::wstring& password);
    void ClearIfStillCurrent(HWND owner);
    void Forget();

private:
    DWORD sequence_ = 0;
    std::wstring copied_;
};

}  // namespace pg
