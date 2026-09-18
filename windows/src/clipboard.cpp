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

    CloseClipboard();

    Forget();
    copied_ = password;
    sequence_ = GetClipboardSequenceNumber();
    SetTimer(owner, kTimerId, kClearDelayMs, nullptr);
    return true;
}

void SecureClipboard::ClearIfStillCurrent(HWND owner) {
    KillTimer(owner, kTimerId);

    if (copied_.empty() || sequence_ == 0 || GetClipboardSequenceNumber() != sequence_) {
        Forget();
        return;
    }

    if (OpenClipboard(owner)) {
        HANDLE data = GetClipboardData(CF_UNICODETEXT);
        if (data != nullptr) {
            const auto* text = static_cast<const wchar_t*>(GlobalLock(data));
            const bool matches = text != nullptr && copied_ == text;
            if (text != nullptr) GlobalUnlock(data);
            if (matches) EmptyClipboard();
        }
        CloseClipboard();
    }

    Forget();
}

void SecureClipboard::Forget() {
    if (!copied_.empty()) {
        SecureZeroMemory(copied_.data(), copied_.size() * sizeof(wchar_t));
        copied_.clear();
    }
    sequence_ = 0;
}

}  // namespace pg
