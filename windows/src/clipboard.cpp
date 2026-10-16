#include "clipboard.hpp"

namespace pg {

bool SecureClipboard::Copy(HWND owner, const std::wstring& password) {
    if (password.empty() || !OpenClipboard(owner)) return false;

    const SIZE_T bytes = (password.size() + 1) * sizeof(wchar_t);
    HGLOBAL memory = GlobalAlloc(GMEM_MOVEABLE, bytes);
    if (memory == nullptr) {
        CloseClipboard();
        return false;
    }

    void* destination = GlobalLock(memory);
    if (destination == nullptr) {
        GlobalFree(memory);
        CloseClipboard();
        return false;
    }

    CopyMemory(destination, password.c_str(), bytes);
    GlobalUnlock(memory);

    EmptyClipboard();
    if (SetClipboardData(CF_UNICODETEXT, memory) == nullptr) {
        GlobalFree(memory);
        CloseClipboard();
        return false;
    }

    const UINT excluded = RegisterClipboardFormatW(L"ExcludeClipboardContentFromMonitorProcessing");
    HGLOBAL marker = GlobalAlloc(GMEM_MOVEABLE | GMEM_ZEROINIT, sizeof(DWORD));
    if (marker != nullptr && (excluded == 0 || SetClipboardData(excluded, marker) == nullptr)) {
        GlobalFree(marker);
    }

    Forget();
    CloseClipboard();
    sequence_ = GetClipboardSequenceNumber();
    SetTimer(owner, kTimerId, kClearDelayMs, nullptr);
    return true;
}

void SecureClipboard::ClearIfStillCurrent(HWND owner) {
    KillTimer(owner, kTimerId);

    if (sequence_ == 0) {
        Forget();
        return;
    }

    if (OpenClipboard(owner)) {
        if (GetClipboardOwner() == owner && GetClipboardSequenceNumber() == sequence_) EmptyClipboard();
        CloseClipboard();
    }

    Forget();
}

void SecureClipboard::Forget() {
    sequence_ = 0;
}

}  // namespace pg
