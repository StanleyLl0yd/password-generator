#pragma once

#include "clipboard.hpp"
#include "generator.hpp"

#include <windows.h>

#include <string>

namespace pg {

class MainWindow {
public:
    bool Create(HINSTANCE instance, int showCommand);
    int Run();

private:
    static LRESULT CALLBACK WindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam);
    static HRESULT CALLBACK AboutCallback(
        HWND hwnd,
        UINT notification,
        WPARAM wParam,
        LPARAM lParam,
        LONG_PTR referenceData
    );

    LRESULT HandleMessage(UINT message, WPARAM wParam, LPARAM lParam);
    void CreateControls();
    void LayoutControls(int width, int height);
    void CreateFonts();
    void DestroyFonts();
    void ApplyFonts();
    void LoadState();
    void SaveState();
    void Generate();
    void CopyPassword();
    void SetRevealed(bool revealed);
    void UpdateLength(int length);
    void UpdatePreferencesFromControls();
    void UpdateStrength(int score);
    void ShowAbout();
    void ShowStatus(const std::wstring& text);
    void ClearPassword();

    int Scale(int value) const;
    bool IsCheckbox(HWND control) const;

    static bool IsRussian();
    static std::wstring Widen(const std::string& value);

    HINSTANCE instance_ = nullptr;
    HWND hwnd_ = nullptr;
    HACCEL accelerators_ = nullptr;
    UINT dpi_ = 96;

    HFONT uiFont_ = nullptr;
    HFONT sectionFont_ = nullptr;

    HWND passwordLabel_ = nullptr;
    HWND passwordEdit_ = nullptr;
    HWND revealButton_ = nullptr;
    HWND copyButton_ = nullptr;

    HWND strengthTitleLabel_ = nullptr;
    HWND strengthBar_ = nullptr;
    HWND strengthLabel_ = nullptr;
    HWND firstSeparator_ = nullptr;

    HWND lengthLabel_ = nullptr;
    HWND lengthSlider_ = nullptr;
    HWND lengthMinusButton_ = nullptr;
    HWND lengthPlusButton_ = nullptr;
    HWND preset16Button_ = nullptr;
    HWND preset24Button_ = nullptr;
    HWND preset32Button_ = nullptr;
    HWND secondSeparator_ = nullptr;

    HWND charsetsTitleLabel_ = nullptr;
    HWND lowerCheck_ = nullptr;
    HWND upperCheck_ = nullptr;
    HWND digitCheck_ = nullptr;
    HWND symbolCheck_ = nullptr;
    HWND thirdSeparator_ = nullptr;

    HWND advancedTitleLabel_ = nullptr;
    HWND similarCheck_ = nullptr;
    HWND duplicateCheck_ = nullptr;
    HWND fourthSeparator_ = nullptr;

    HWND generateButton_ = nullptr;
    HWND aboutButton_ = nullptr;
    HWND statusLabel_ = nullptr;

    Preferences preferences_;
    PasswordGenerator generator_;
    SecureClipboard clipboard_;
    std::string password_;
    bool revealed_ = false;
    bool russian_ = false;
};

}  // namespace pg
