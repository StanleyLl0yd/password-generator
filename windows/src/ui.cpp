#include "ui.hpp"

#include "settings.hpp"
#include "resource.h"
#include "version.hpp"

#include <commctrl.h>
#include <shellapi.h>

#include <algorithm>
#include <array>
#include <string>

namespace pg {
namespace {

constexpr wchar_t kWindowClass[] = L"PasswordGeneratorNativeWindow";

constexpr int kInitialClientWidth = 620;
constexpr int kInitialClientHeight = 570;
constexpr int kMinimumClientWidth = 520;
constexpr int kMinimumClientHeight = 540;

constexpr DWORD kWindowStyle = WS_OVERLAPPEDWINDOW;

constexpr int IdPassword = 100;
constexpr int IdReveal = 101;
constexpr int IdCopy = 102;
constexpr int IdLengthSlider = 103;
constexpr int IdLengthMinus = 104;
constexpr int IdLengthPlus = 105;
constexpr int IdPreset16 = 106;
constexpr int IdPreset24 = 107;
constexpr int IdPreset32 = 108;
constexpr int IdLower = 109;
constexpr int IdUpper = 110;
constexpr int IdDigit = 111;
constexpr int IdSymbol = 112;
constexpr int IdSimilar = 113;
constexpr int IdDuplicate = 114;
constexpr int IdGenerate = 115;
constexpr int IdAbout = 116;

constexpr int CmdGenerate = 40001;
constexpr int CmdCopy = 40002;
constexpr int CmdHide = 40003;

#ifndef TBS_TRANSPARENTBKGND
#define TBS_TRANSPARENTBKGND 0x1000
#endif

struct Texts {
    const wchar_t* title;
    const wchar_t* password;
    const wchar_t* show;
    const wchar_t* hide;
    const wchar_t* copy;
    const wchar_t* strength;
    const wchar_t* length;
    const wchar_t* charsets;
    const wchar_t* lowercase;
    const wchar_t* uppercase;
    const wchar_t* digits;
    const wchar_t* symbols;
    const wchar_t* advanced;
    const wchar_t* excludeSimilar;
    const wchar_t* excludeDuplicates;
    const wchar_t* generate;
    const wchar_t* about;
    const wchar_t* copied;
    const wchar_t* invalidLength;
    const wchar_t* noCharsets;
    const wchar_t* notEnoughUnique;
    const wchar_t* veryWeak;
    const wchar_t* weak;
    const wchar_t* medium;
    const wchar_t* strong;
    const wchar_t* veryStrong;
};

const Texts kEnglish{
    L"Password Generator",
    L"Password",
    L"Show",
    L"Hide",
    L"Copy",
    L"Password strength",
    L"Length",
    L"Character sets",
    L"a-z  Lowercase",
    L"A-Z  Uppercase",
    L"0-9  Digits",
    L"!@#  Symbols",
    L"Advanced",
    L"Exclude similar characters",
    L"Exclude duplicate characters",
    L"Generate password",
    L"About",
    L"Password copied. It will be cleared after 60 seconds if it is still current.",
    L"Password length must be between 4 and 64 characters.",
    L"Select at least one character set.",
    L"Not enough unique characters for this length without duplicates.",
    L"Very weak",
    L"Weak",
    L"Medium",
    L"Strong",
    L"Very strong"
};

const Texts kRussian{
    L"Генератор паролей",
    L"Пароль",
    L"Показать",
    L"Скрыть",
    L"Копировать",
    L"Надёжность пароля",
    L"Длина",
    L"Наборы символов",
    L"a-z  строчные",
    L"A-Z  заглавные",
    L"0-9  цифры",
    L"!@#  спецсимволы",
    L"Дополнительно",
    L"Исключать похожие символы",
    L"Исключать повторы",
    L"Сгенерировать пароль",
    L"О приложении",
    L"Пароль скопирован. Через 60 секунд он будет удалён, если останется текущим.",
    L"Длина пароля должна быть от 4 до 64 символов.",
    L"Выберите хотя бы один набор символов.",
    L"Недостаточно уникальных символов для такой длины без повторов.",
    L"Очень слабый",
    L"Слабый",
    L"Средний",
    L"Сильный",
    L"Очень сильный"
};

const Texts& T(bool russian) {
    return russian ? kRussian : kEnglish;
}

HWND AddControl(
    HWND parent,
    DWORD exStyle,
    const wchar_t* className,
    const wchar_t* text,
    DWORD style,
    int id
) {
    return CreateWindowExW(
        exStyle,
        className,
        text,
        WS_CHILD | WS_VISIBLE | style,
        0,
        0,
        10,
        10,
        parent,
        id == 0 ? nullptr : reinterpret_cast<HMENU>(static_cast<INT_PTR>(id)),
        GetModuleHandleW(nullptr),
        nullptr
    );
}

void SetChecked(HWND control, bool checked) {
    SendMessageW(control, BM_SETCHECK, checked ? BST_CHECKED : BST_UNCHECKED, 0);
}

bool IsChecked(HWND control) {
    return SendMessageW(control, BM_GETCHECK, 0, 0) == BST_CHECKED;
}

}  // namespace

bool MainWindow::IsRussian() {
    return PRIMARYLANGID(GetUserDefaultUILanguage()) == LANG_RUSSIAN;
}

std::wstring MainWindow::Widen(const std::string& value) {
    return std::wstring(value.begin(), value.end());
}

int MainWindow::Scale(int value) const {
    return MulDiv(value, static_cast<int>(dpi_), 96);
}

bool MainWindow::IsCheckbox(HWND control) const {
    return control == lowerCheck_ ||
        control == upperCheck_ ||
        control == digitCheck_ ||
        control == symbolCheck_ ||
        control == similarCheck_ ||
        control == duplicateCheck_;
}

bool MainWindow::Create(HINSTANCE instance, int showCommand) {
    instance_ = instance;
    russian_ = IsRussian();

    WNDCLASSEXW windowClass{
        .cbSize = sizeof(WNDCLASSEXW),
        .style = CS_HREDRAW | CS_VREDRAW,
        .lpfnWndProc = WindowProc,
        .cbClsExtra = 0,
        .cbWndExtra = 0,
        .hInstance = instance_,
        .hIcon = LoadIconW(instance_, MAKEINTRESOURCEW(IDI_APP_ICON)),
        .hCursor = LoadCursorW(nullptr, IDC_ARROW),
        .hbrBackground = GetSysColorBrush(COLOR_WINDOW),
        .lpszMenuName = nullptr,
        .lpszClassName = kWindowClass,
        .hIconSm = LoadIconW(instance_, MAKEINTRESOURCEW(IDI_APP_ICON))
    };

    if (RegisterClassExW(&windowClass) == 0 && GetLastError() != ERROR_CLASS_ALREADY_EXISTS) {
        return false;
    }

    const UINT initialDpi = GetDpiForSystem();
    RECT rect{
        0,
        0,
        MulDiv(kInitialClientWidth, static_cast<int>(initialDpi), 96),
        MulDiv(kInitialClientHeight, static_cast<int>(initialDpi), 96)
    };
    AdjustWindowRectExForDpi(&rect, kWindowStyle, FALSE, 0, initialDpi);

    hwnd_ = CreateWindowExW(
        0,
        kWindowClass,
        T(russian_).title,
        kWindowStyle,
        CW_USEDEFAULT,
        CW_USEDEFAULT,
        rect.right - rect.left,
        rect.bottom - rect.top,
        nullptr,
        nullptr,
        instance_,
        this
    );
    if (hwnd_ == nullptr) return false;

    const std::array<ACCEL, 3> accelerators{{
        {FVIRTKEY | FCONTROL, 'G', CmdGenerate},
        {FVIRTKEY | FCONTROL, 'C', CmdCopy},
        {FVIRTKEY, VK_ESCAPE, CmdHide}
    }};
    accelerators_ = CreateAcceleratorTableW(
        const_cast<LPACCEL>(accelerators.data()),
        static_cast<int>(accelerators.size())
    );

    ShowWindow(hwnd_, showCommand);
    UpdateWindow(hwnd_);
    return true;
}

int MainWindow::Run() {
    MSG message{};
    while (GetMessageW(&message, nullptr, 0, 0) > 0) {
        if (accelerators_ != nullptr && TranslateAcceleratorW(hwnd_, accelerators_, &message) != 0) {
            continue;
        }
        TranslateMessage(&message);
        DispatchMessageW(&message);
    }

    if (accelerators_ != nullptr) DestroyAcceleratorTable(accelerators_);
    return static_cast<int>(message.wParam);
}

LRESULT CALLBACK MainWindow::WindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
    MainWindow* self = nullptr;

    if (message == WM_NCCREATE) {
        const auto* create = reinterpret_cast<CREATESTRUCTW*>(lParam);
        self = static_cast<MainWindow*>(create->lpCreateParams);
        self->hwnd_ = hwnd;
        SetWindowLongPtrW(hwnd, GWLP_USERDATA, reinterpret_cast<LONG_PTR>(self));
    } else {
        self = reinterpret_cast<MainWindow*>(GetWindowLongPtrW(hwnd, GWLP_USERDATA));
    }

    return self != nullptr
        ? self->HandleMessage(message, wParam, lParam)
        : DefWindowProcW(hwnd, message, wParam, lParam);
}

LRESULT MainWindow::HandleMessage(UINT message, WPARAM wParam, LPARAM lParam) {
    switch (message) {
    case WM_CREATE:
        dpi_ = GetDpiForWindow(hwnd_);
        CreateFonts();
        CreateControls();
        LoadState();
        Generate();
        return 0;

    case WM_SIZE:
        LayoutControls(LOWORD(lParam), HIWORD(lParam));
        return 0;

    case WM_GETMINMAXINFO: {
        auto* info = reinterpret_cast<MINMAXINFO*>(lParam);
        RECT minimum{
            0,
            0,
            Scale(kMinimumClientWidth),
            Scale(kMinimumClientHeight)
        };
        AdjustWindowRectExForDpi(&minimum, kWindowStyle, FALSE, 0, dpi_);
        info->ptMinTrackSize.x = minimum.right - minimum.left;
        info->ptMinTrackSize.y = minimum.bottom - minimum.top;
        return 0;
    }

    case WM_DPICHANGED: {
        dpi_ = HIWORD(wParam);
        const auto* suggested = reinterpret_cast<RECT*>(lParam);
        SetWindowPos(
            hwnd_,
            nullptr,
            suggested->left,
            suggested->top,
            suggested->right - suggested->left,
            suggested->bottom - suggested->top,
            SWP_NOZORDER | SWP_NOACTIVATE
        );
        CreateFonts();
        ApplyFonts();

        RECT client{};
        GetClientRect(hwnd_, &client);
        LayoutControls(client.right - client.left, client.bottom - client.top);
        return 0;
    }

    case WM_SETTINGCHANGE:
        CreateFonts();
        ApplyFonts();
        InvalidateRect(hwnd_, nullptr, TRUE);
        return 0;

    case WM_SYSCOLORCHANGE:
        InvalidateRect(hwnd_, nullptr, TRUE);
        return 0;

    case WM_CTLCOLORSTATIC: {
        HDC dc = reinterpret_cast<HDC>(wParam);
        HWND control = reinterpret_cast<HWND>(lParam);
        SetBkMode(dc, TRANSPARENT);
        SetTextColor(dc, GetSysColor(control == statusLabel_ ? COLOR_GRAYTEXT : COLOR_WINDOWTEXT));
        return reinterpret_cast<LRESULT>(GetSysColorBrush(COLOR_WINDOW));
    }

    case WM_CTLCOLORBTN: {
        HWND control = reinterpret_cast<HWND>(lParam);
        if (IsCheckbox(control)) {
            HDC dc = reinterpret_cast<HDC>(wParam);
            SetBkMode(dc, TRANSPARENT);
            SetTextColor(dc, GetSysColor(COLOR_WINDOWTEXT));
            return reinterpret_cast<LRESULT>(GetSysColorBrush(COLOR_WINDOW));
        }
        break;
    }

    case WM_HSCROLL:
        if (reinterpret_cast<HWND>(lParam) == lengthSlider_) {
            UpdateLength(static_cast<int>(SendMessageW(lengthSlider_, TBM_GETPOS, 0, 0)));
            SaveState();
        }
        return 0;

    case WM_TIMER:
        if (wParam == SecureClipboard::kTimerId) clipboard_.ClearIfStillCurrent(hwnd_);
        return 0;

    case WM_COMMAND: {
        const int id = LOWORD(wParam);
        switch (id) {
        case IdReveal:
            SetRevealed(!revealed_);
            return 0;
        case IdCopy:
        case CmdCopy:
            CopyPassword();
            return 0;
        case IdGenerate:
        case CmdGenerate:
            Generate();
            return 0;
        case CmdHide:
            if (revealed_) SetRevealed(false);
            return 0;
        case IdLengthMinus:
            UpdateLength(preferences_.length - 1);
            SaveState();
            return 0;
        case IdLengthPlus:
            UpdateLength(preferences_.length + 1);
            SaveState();
            return 0;
        case IdPreset16:
            UpdateLength(16);
            SaveState();
            return 0;
        case IdPreset24:
            UpdateLength(24);
            SaveState();
            return 0;
        case IdPreset32:
            UpdateLength(32);
            SaveState();
            return 0;
        case IdLower:
        case IdUpper:
        case IdDigit:
        case IdSymbol:
        case IdSimilar:
        case IdDuplicate:
            UpdatePreferencesFromControls();
            SaveState();
            return 0;
        case IdAbout:
            ShowAbout();
            return 0;
        default:
            break;
        }
        break;
    }

    case WM_DESTROY:
        SaveState();
        clipboard_.Forget();
        ClearPassword();
        DestroyFonts();
        PostQuitMessage(0);
        return 0;

    default:
        break;
    }

    return DefWindowProcW(hwnd_, message, wParam, lParam);
}

void MainWindow::CreateFonts() {
    DestroyFonts();

    NONCLIENTMETRICSW metrics{};
    metrics.cbSize = sizeof(metrics);

    if (!SystemParametersInfoForDpi(
            SPI_GETNONCLIENTMETRICS,
            sizeof(metrics),
            &metrics,
            0,
            dpi_
        )) {
        metrics.cbSize = sizeof(metrics);
        if (!SystemParametersInfoW(
                SPI_GETNONCLIENTMETRICS,
                sizeof(metrics),
                &metrics,
                0
            )) {
            return;
        }
    }

    uiFont_ = CreateFontIndirectW(&metrics.lfMessageFont);

    LOGFONTW section = metrics.lfMessageFont;
    section.lfWeight = FW_SEMIBOLD;
    sectionFont_ = CreateFontIndirectW(&section);
}

void MainWindow::DestroyFonts() {
    if (uiFont_ != nullptr) {
        DeleteObject(uiFont_);
        uiFont_ = nullptr;
    }
    if (sectionFont_ != nullptr) {
        DeleteObject(sectionFont_);
        sectionFont_ = nullptr;
    }
}

void MainWindow::ApplyFonts() {
    const HFONT ui = uiFont_ != nullptr
        ? uiFont_
        : static_cast<HFONT>(GetStockObject(DEFAULT_GUI_FONT));
    const HFONT section = sectionFont_ != nullptr ? sectionFont_ : ui;

    for (HWND child = GetWindow(hwnd_, GW_CHILD); child != nullptr; child = GetWindow(child, GW_HWNDNEXT)) {
        SendMessageW(child, WM_SETFONT, reinterpret_cast<WPARAM>(ui), TRUE);
    }

    for (HWND label : {
            passwordLabel_,
            strengthTitleLabel_,
            lengthLabel_,
            charsetsTitleLabel_,
            advancedTitleLabel_
        }) {
        if (label != nullptr) {
            SendMessageW(label, WM_SETFONT, reinterpret_cast<WPARAM>(section), TRUE);
        }
    }
}

void MainWindow::CreateControls() {
    const auto& text = T(russian_);

    auto addStatic = [&](const wchar_t* value, DWORD style = SS_LEFT | SS_NOPREFIX) {
        return AddControl(hwnd_, 0, L"STATIC", value, style, 0);
    };

    auto addSeparator = [&]() {
        return AddControl(hwnd_, 0, L"STATIC", L"", SS_ETCHEDHORZ, 0);
    };

    passwordLabel_ = addStatic(text.password);
    passwordEdit_ = AddControl(
        hwnd_,
        WS_EX_CLIENTEDGE,
        L"EDIT",
        L"",
        ES_AUTOHSCROLL | ES_READONLY | ES_PASSWORD | WS_TABSTOP,
        IdPassword
    );
    revealButton_ = AddControl(hwnd_, 0, L"BUTTON", text.show, BS_PUSHBUTTON | WS_TABSTOP, IdReveal);
    copyButton_ = AddControl(hwnd_, 0, L"BUTTON", text.copy, BS_PUSHBUTTON | WS_TABSTOP, IdCopy);

    strengthTitleLabel_ = addStatic(text.strength);
    strengthLabel_ = addStatic(text.veryWeak, SS_RIGHT | SS_NOPREFIX);
    strengthBar_ = AddControl(hwnd_, 0, PROGRESS_CLASSW, L"", PBS_SMOOTH, 0);
    SendMessageW(strengthBar_, PBM_SETRANGE32, 0, 100);
    firstSeparator_ = addSeparator();

    lengthLabel_ = addStatic(L"");
    lengthSlider_ = AddControl(
        hwnd_,
        0,
        TRACKBAR_CLASSW,
        L"",
        TBS_HORZ | TBS_NOTICKS | TBS_TRANSPARENTBKGND | WS_TABSTOP,
        IdLengthSlider
    );
    SendMessageW(lengthSlider_, TBM_SETRANGE, TRUE, MAKELONG(kMinLength, kMaxLength));
    SendMessageW(lengthSlider_, TBM_SETPAGESIZE, 0, 4);

    lengthMinusButton_ = AddControl(
        hwnd_, 0, L"BUTTON", L"−", BS_PUSHBUTTON | WS_TABSTOP, IdLengthMinus
    );
    lengthPlusButton_ = AddControl(
        hwnd_, 0, L"BUTTON", L"+", BS_PUSHBUTTON | WS_TABSTOP, IdLengthPlus
    );
    preset16Button_ = AddControl(
        hwnd_, 0, L"BUTTON", L"16", BS_PUSHBUTTON | WS_TABSTOP, IdPreset16
    );
    preset24Button_ = AddControl(
        hwnd_, 0, L"BUTTON", L"24", BS_PUSHBUTTON | WS_TABSTOP, IdPreset24
    );
    preset32Button_ = AddControl(
        hwnd_, 0, L"BUTTON", L"32", BS_PUSHBUTTON | WS_TABSTOP, IdPreset32
    );
    secondSeparator_ = addSeparator();

    charsetsTitleLabel_ = addStatic(text.charsets);
    lowerCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.lowercase, BS_AUTOCHECKBOX | WS_TABSTOP, IdLower
    );
    upperCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.uppercase, BS_AUTOCHECKBOX | WS_TABSTOP, IdUpper
    );
    digitCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.digits, BS_AUTOCHECKBOX | WS_TABSTOP, IdDigit
    );
    symbolCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.symbols, BS_AUTOCHECKBOX | WS_TABSTOP, IdSymbol
    );
    thirdSeparator_ = addSeparator();

    advancedTitleLabel_ = addStatic(text.advanced);
    similarCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.excludeSimilar, BS_AUTOCHECKBOX | WS_TABSTOP, IdSimilar
    );
    duplicateCheck_ = AddControl(
        hwnd_, 0, L"BUTTON", text.excludeDuplicates, BS_AUTOCHECKBOX | WS_TABSTOP, IdDuplicate
    );
    fourthSeparator_ = addSeparator();

    generateButton_ = AddControl(
        hwnd_, 0, L"BUTTON", text.generate, BS_DEFPUSHBUTTON | WS_TABSTOP, IdGenerate
    );
    aboutButton_ = AddControl(
        hwnd_, 0, L"BUTTON", text.about, BS_PUSHBUTTON | WS_TABSTOP, IdAbout
    );
    statusLabel_ = addStatic(L"", SS_LEFT | SS_NOPREFIX);

    ApplyFonts();

    SendMessageW(
        passwordEdit_,
        EM_SETMARGINS,
        EC_LEFTMARGIN | EC_RIGHTMARGIN,
        MAKELPARAM(Scale(8), Scale(8))
    );

    for (HWND control : {
            passwordEdit_,
            revealButton_,
            copyButton_,
            strengthBar_,
            lengthSlider_,
            lengthMinusButton_,
            lengthPlusButton_,
            preset16Button_,
            preset24Button_,
            preset32Button_,
            lowerCheck_,
            upperCheck_,
            digitCheck_,
            symbolCheck_,
            similarCheck_,
            duplicateCheck_,
            generateButton_,
            aboutButton_
        }) {
        SetWindowTheme(control, L"Explorer", nullptr);
    }

    RECT client{};
    GetClientRect(hwnd_, &client);
    LayoutControls(client.right - client.left, client.bottom - client.top);
}

void MainWindow::LayoutControls(int width, int height) {
    if (width <= 0 || height <= 0) return;

    const int margin = Scale(20);
    const int gap = Scale(8);
    const int sectionGap = Scale(16);
    const int labelHeight = Scale(20);
    const int fieldHeight = Scale(32);
    const int compactButtonWidth = Scale(90);
    const int copyButtonWidth = Scale(98);
    const int content = std::max(Scale(1), width - margin * 2);

    int y = Scale(18);

    auto move = [](HWND control, int x, int yy, int w, int h) {
        if (control != nullptr) {
            MoveWindow(control, x, yy, std::max(1, w), std::max(1, h), TRUE);
        }
    };

    move(passwordLabel_, margin, y, content, labelHeight);
    y += labelHeight + Scale(6);

    const int passwordWidth = std::max(
        Scale(150),
        content - compactButtonWidth - copyButtonWidth - gap * 2
    );
    move(passwordEdit_, margin, y, passwordWidth, fieldHeight);
    move(
        revealButton_,
        margin + passwordWidth + gap,
        y,
        compactButtonWidth,
        fieldHeight
    );
    move(
        copyButton_,
        margin + passwordWidth + gap + compactButtonWidth + gap,
        y,
        copyButtonWidth,
        fieldHeight
    );
    y += fieldHeight + sectionGap;

    move(strengthTitleLabel_, margin, y, content / 2, labelHeight);
    move(strengthLabel_, margin + content / 2, y, content / 2, labelHeight);
    y += labelHeight + Scale(6);
    move(strengthBar_, margin, y, content, Scale(10));
    y += Scale(10) + sectionGap;
    move(firstSeparator_, margin, y, content, Scale(2));
    y += Scale(2) + sectionGap;

    move(lengthLabel_, margin, y, content, labelHeight);
    y += labelHeight + Scale(6);

    const int stepButton = Scale(36);
    move(lengthMinusButton_, margin, y, stepButton, fieldHeight);
    move(
        lengthSlider_,
        margin + stepButton + gap,
        y,
        content - stepButton * 2 - gap * 2,
        fieldHeight
    );
    move(lengthPlusButton_, width - margin - stepButton, y, stepButton, fieldHeight);
    y += fieldHeight + Scale(8);

    const int presetWidth = Scale(64);
    const int presetGroupWidth = presetWidth * 3 + gap * 2;
    const int presetStart = margin + std::max(0, (content - presetGroupWidth) / 2);
    move(preset16Button_, presetStart, y, presetWidth, Scale(30));
    move(preset24Button_, presetStart + presetWidth + gap, y, presetWidth, Scale(30));
    move(preset32Button_, presetStart + (presetWidth + gap) * 2, y, presetWidth, Scale(30));
    y += Scale(30) + sectionGap;
    move(secondSeparator_, margin, y, content, Scale(2));
    y += Scale(2) + sectionGap;

    move(charsetsTitleLabel_, margin, y, content, labelHeight);
    y += labelHeight + Scale(6);

    const int half = (content - gap) / 2;
    const int checkHeight = Scale(26);
    move(lowerCheck_, margin, y, half, checkHeight);
    move(upperCheck_, margin + half + gap, y, half, checkHeight);
    y += checkHeight + Scale(4);
    move(digitCheck_, margin, y, half, checkHeight);
    move(symbolCheck_, margin + half + gap, y, half, checkHeight);
    y += checkHeight + sectionGap;
    move(thirdSeparator_, margin, y, content, Scale(2));
    y += Scale(2) + sectionGap;

    move(advancedTitleLabel_, margin, y, content, labelHeight);
    y += labelHeight + Scale(6);
    move(similarCheck_, margin, y, content, checkHeight);
    y += checkHeight + Scale(4);
    move(duplicateCheck_, margin, y, content, checkHeight);
    y += checkHeight + sectionGap;
    move(fourthSeparator_, margin, y, content, Scale(2));
    y += Scale(2) + sectionGap;

    const int aboutWidth = Scale(112);
    move(generateButton_, margin, y, content - aboutWidth - gap, Scale(38));
    move(aboutButton_, width - margin - aboutWidth, y, aboutWidth, Scale(38));
    y += Scale(38) + Scale(10);

    const int statusHeight = Scale(34);
    const int statusY = std::max(y, height - margin - statusHeight);
    move(statusLabel_, margin, statusY, content, statusHeight);
}

void MainWindow::LoadState() {
    preferences_ = LoadPreferences();
    UpdateLength(preferences_.length);
    SetChecked(lowerCheck_, preferences_.useLowercase);
    SetChecked(upperCheck_, preferences_.useUppercase);
    SetChecked(digitCheck_, preferences_.useDigits);
    SetChecked(symbolCheck_, preferences_.useSymbols);
    SetChecked(similarCheck_, preferences_.excludeSimilar);
    SetChecked(duplicateCheck_, preferences_.excludeDuplicates);
}

void MainWindow::SaveState() {
    UpdatePreferencesFromControls();
    SavePreferences(preferences_);
}

void MainWindow::UpdatePreferencesFromControls() {
    preferences_.length = static_cast<int>(SendMessageW(lengthSlider_, TBM_GETPOS, 0, 0));
    preferences_.useLowercase = IsChecked(lowerCheck_);
    preferences_.useUppercase = IsChecked(upperCheck_);
    preferences_.useDigits = IsChecked(digitCheck_);
    preferences_.useSymbols = IsChecked(symbolCheck_);
    preferences_.excludeSimilar = IsChecked(similarCheck_);
    preferences_.excludeDuplicates = IsChecked(duplicateCheck_);
}

void MainWindow::UpdateLength(int length) {
    preferences_.length = std::clamp(length, kMinLength, kMaxLength);
    SendMessageW(lengthSlider_, TBM_SETPOS, TRUE, preferences_.length);

    std::wstring label = std::wstring(T(russian_).length) + L": " + std::to_wstring(preferences_.length);
    SetWindowTextW(lengthLabel_, label.c_str());
}

void MainWindow::Generate() {
    UpdatePreferencesFromControls();

    GenerationResult result;
    try {
        result = generator_.Generate(preferences_);
    } catch (...) {
        ShowStatus(russian_ ? L"Не удалось получить случайные данные от Windows." : L"Windows secure random source failed.");
        return;
    }

    if (std::holds_alternative<GenerationError>(result)) {
        const auto error = std::get<GenerationError>(result);
        const auto& text = T(russian_);
        switch (error) {
        case GenerationError::InvalidLength:
            ShowStatus(text.invalidLength);
            break;
        case GenerationError::NoCharsets:
            ShowStatus(text.noCharsets);
            break;
        case GenerationError::NotEnoughUniqueChars:
            ShowStatus(text.notEnoughUnique);
            break;
        }
        return;
    }

    ClearPassword();
    password_ = std::get<std::string>(std::move(result));
    SetRevealed(false);
    SetWindowTextW(passwordEdit_, Widen(password_).c_str());
    UpdateStrength(generator_.EstimatePasswordScore(password_));
    ShowStatus(L"");
}

void MainWindow::ClearPassword() {
    if (!password_.empty()) {
        SecureZeroMemory(password_.data(), password_.size());
        password_.clear();
    }
    SetWindowTextW(passwordEdit_, L"");
}

void MainWindow::CopyPassword() {
    if (password_.empty()) return;
    if (clipboard_.Copy(hwnd_, Widen(password_))) {
        ShowStatus(T(russian_).copied);
    }
}

void MainWindow::SetRevealed(bool revealed) {
    revealed_ = revealed;
    SendMessageW(passwordEdit_, EM_SETPASSWORDCHAR, revealed ? 0 : 0x25CF, 0);
    InvalidateRect(passwordEdit_, nullptr, TRUE);
    SetWindowTextW(revealButton_, revealed ? T(russian_).hide : T(russian_).show);

#ifndef WDA_EXCLUDEFROMCAPTURE
#define WDA_EXCLUDEFROMCAPTURE 0x00000011
#endif
    const DWORD affinity = revealed ? WDA_EXCLUDEFROMCAPTURE : WDA_NONE;
    if (!SetWindowDisplayAffinity(hwnd_, affinity) && revealed) {
        SetWindowDisplayAffinity(hwnd_, WDA_MONITOR);
    }
}

void MainWindow::UpdateStrength(int score) {
    SendMessageW(strengthBar_, PBM_SETPOS, score, 0);

    const auto& text = T(russian_);
    const wchar_t* label =
        score < 20 ? text.veryWeak :
        score < 40 ? text.weak :
        score < 60 ? text.medium :
        score < 80 ? text.strong :
                     text.veryStrong;

    const WPARAM progressState =
        score < 40 ? PBST_ERROR :
        score < 60 ? PBST_PAUSED :
                     PBST_NORMAL;
    SendMessageW(strengthBar_, PBM_SETSTATE, progressState, 0);

    std::wstring value = std::wstring(label) + L" · " + std::to_wstring(score) + L"/100";
    SetWindowTextW(strengthLabel_, value.c_str());
}

void MainWindow::ShowStatus(const std::wstring& text) {
    SetWindowTextW(statusLabel_, text.c_str());
}

HRESULT CALLBACK MainWindow::AboutCallback(
    HWND,
    UINT notification,
    WPARAM,
    LPARAM lParam,
    LONG_PTR
) {
    if (notification == TDN_HYPERLINK_CLICKED) {
        ShellExecuteW(nullptr, L"open", reinterpret_cast<const wchar_t*>(lParam), nullptr, nullptr, SW_SHOWNORMAL);
    }
    return S_OK;
}

void MainWindow::ShowAbout() {
    const wchar_t* title = russian_ ? L"О приложении" : L"About";
    const wchar_t* content = russian_
        ? L"Приватный офлайн-генератор паролей. Пароли создаются только на устройстве и никогда не сохраняются.\n\n"
          L"<a href=\"https://stanleyll0yd.github.io/apps/password-generator/\">Сайт приложения</a>\n"
          L"<a href=\"https://stanleyll0yd.github.io/apps/password-generator/privacy/\">Политика конфиденциальности</a>\n"
          L"<a href=\"https://polyformproject.org/licenses/noncommercial/1.0.0\">Лицензия</a>"
        : L"A privacy-focused offline password generator. Passwords are generated locally and never stored.\n\n"
          L"<a href=\"https://stanleyll0yd.github.io/apps/password-generator/\">App website</a>\n"
          L"<a href=\"https://stanleyll0yd.github.io/apps/password-generator/privacy/\">Privacy Policy</a>\n"
          L"<a href=\"https://polyformproject.org/licenses/noncommercial/1.0.0\">License</a>";

    std::wstring mainInstruction = std::wstring(T(russian_).title) + L" " + kProductVersion;

    TASKDIALOGCONFIG config{
        .cbSize = sizeof(TASKDIALOGCONFIG),
        .hwndParent = hwnd_,
        .hInstance = instance_,
        .dwFlags = TDF_ENABLE_HYPERLINKS | TDF_ALLOW_DIALOG_CANCELLATION,
        .dwCommonButtons = TDCBF_OK_BUTTON,
        .pszWindowTitle = title,
        .pszMainIcon = TD_INFORMATION_ICON,
        .pszMainInstruction = mainInstruction.c_str(),
        .pszContent = content,
        .pfCallback = AboutCallback,
        .lpCallbackData = reinterpret_cast<LONG_PTR>(this)
    };

    TaskDialogIndirect(&config, nullptr, nullptr, nullptr);
}

}  // namespace pg
