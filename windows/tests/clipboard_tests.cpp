#include "clipboard.hpp"

#include <cstdlib>
#include <iostream>

namespace {
void Require(bool condition, const char* message) {
    if (!condition) {
        std::cerr << "FAIL: " << message << '\n';
        std::exit(1);
    }
}
}

int main() {
    HWND owner = CreateWindowExW(0, L"STATIC", L"Clipboard test", 0, 0, 0, 0, 0,
                                HWND_MESSAGE, nullptr, GetModuleHandleW(nullptr), nullptr);
    Require(owner != nullptr, "clipboard owner created");
    pg::SecureClipboard first;
    pg::SecureClipboard second;
    Require(first.Copy(owner, L"clipboard-test-fixture"), "initial copy");
    const DWORD initial = GetClipboardSequenceNumber();
    Require(second.Copy(owner, L"clipboard-test-fixture"), "replacement with same text");
    const DWORD replaced = GetClipboardSequenceNumber();
    Require(initial != replaced, "replacement changes clipboard sequence");
    first.ClearIfStillCurrent(owner);
    Require(GetClipboardSequenceNumber() == replaced, "old cleanup preserves newer identical text");
    second.ClearIfStillCurrent(owner);
    Require(OpenClipboard(owner) != FALSE, "open cleared clipboard");
    Require(GetClipboardData(CF_UNICODETEXT) == nullptr, "own still-current copy is cleared");
    CloseClipboard();
    Require(!first.Copy(owner, L""), "empty password ignored");
    DestroyWindow(owner);
    std::cout << "Windows clipboard ownership tests: OK\n";
    return 0;
}
