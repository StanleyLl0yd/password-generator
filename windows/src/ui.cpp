#include "ui.hpp"

#include "settings.hpp"
#include "resource.h"

#include <commctrl.h>
#include <dwmapi.h>
#include <shellapi.h>
#include <uxtheme.h>

#include <algorithm>
#include <array>
#include <string>

namespace pg {
namespace {

constexpr wchar_t kWindowClass[] = L"PasswordGeneratorNativeWindow";
constexpr wchar_t kProductVersion[] = L"1.6.0";
constexpr int kWindowWidth = 540;
constexpr int kWindowHeight = 650;

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
        reinterpret_cast<HMENU>(static_cast<INT_PTR>(id)),
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
        .hbrBackground = reinterpret_cast<HBRUSH>(COLOR_WINDOW + 1),
        .lpszMenuName = nullptr,
        .lpszClassName = kWindowClass,
        .hIconSm = LoadIconW(instance_, MAKEINTRESOURCEW(IDI_APP_ICON))
    };

    if (RegisterClassExW(&windowClass) == 0 && GetLastError() != ERROR_CLASS_ALREADY_EXISTS) {
        return false;
    }

    RECT rect{0, 0, kWindowWidth, kWindowHeight};
    AdjustWindowRectEx(&rect, WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX, FALSE, 0);

    hwnd_ = CreateWindowExW(
        0,
        kWindowClass,
        T(russian_).title,
        WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX,
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
        CreateControls();
        LoadState();
        Generate();
        return 0;

    case WM_SIZE:
        LayoutControls(LOWORD(lParam), HIWORD(lParam));
        return 0;

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
        PostQuitMessage(0);
        return 0;

    default:
        break;
    }

    return DefWindowProcW(hwnd_, message, wParam, lParam);
}

void MainWindow::CreateControls() {
    const auto& text = T(russian_);
    const HFONT font = static_cast<HFONT>(GetStockObject(DEFAULT_GUI_FONT));

    auto addStatic = [&](const wchar_t* value) {
        HWND control = AddControl(hwnd_, 0, L"STATIC", value, SS_LEFT, 0);
        SendMessageW(control, WM_SETFONT, reinterpret_cast<WPARAM>(font), TRUE);
        return control;
    };

    addStatic(text.password);
    passwordEdit_ = AddControl(
        hwnd_,
        WS_EX_CLIENTEDGE,
        L"EDIT",
        L"",
        ES_AUTOHSCROLL | ES_READONLY | ES_PASSWORD,
        IdPassword
    );
    revealButton_ = AddControl(hwnd_, 0, L"BUTTON", text.show, BS_PUSHBUTTON, IdReveal);
    copyButton_ = AddControl(hwnd_, 0, L"BUTTON", text.copy, BS_PUSHBUTTON, IdCopy);

    addStatic(text.strength);
    strengthLabel_ = addStatic(text.veryWeak);
    strengthBar_ = AddControl(hwnd_, 0, PROGRESS_CLASSW, L"", PBS_SMOOTH, 0);
    SendMessageW(strengthBar_, PBM_SETRANGE32, 0, 100);

    lengthLabel_ = addStatic(L"");
    lengthSlider_ = AddControl(hwnd_, 0, TRACKBAR_CLASSW, L"", TBS_HORZ | TBS_AUTOTICKS, IdLengthSlider);
    SendMessageW(lengthSlider_, TBM_SETRANGE, TRUE, MAKELONG(kMinLength, kMaxLength));

    AddControl(hwnd_, 0, L"BUTTON", L"-", BS_PUSHBUTTON, IdLengthMinus);
    AddControl(hwnd_, 0, L"BUTTON", L"+", BS_PUSHBUTTON, IdLengthPlus);
    AddControl(hwnd_, 0, L"BUTTON", L"16", BS_PUSHBUTTON, IdPreset16);
    AddControl(hwnd_, 0, L"BUTTON", L"24", BS_PUSHBUTTON, IdPreset24);
    AddControl(hwnd_, 0, L"BUTTON", L"32", BS_PUSHBUTTON, IdPreset32);

    addStatic(text.charsets);
    lowerCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.lowercase, BS_AUTOCHECKBOX, IdLower);
    upperCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.uppercase, BS_AUTOCHECKBOX, IdUpper);
    digitCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.digits, BS_AUTOCHECKBOX, IdDigit);
    symbolCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.symbols, BS_AUTOCHECKBOX, IdSymbol);

    addStatic(text.advanced);
    similarCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.excludeSimilar, BS_AUTOCHECKBOX, IdSimilar);
    duplicateCheck_ = AddControl(hwnd_, 0, L"BUTTON", text.excludeDuplicates, BS_AUTOCHECKBOX, IdDuplicate);

    generateButton_ = AddControl(hwnd_, 0, L"BUTTON", text.generate, BS_DEFPUSHBUTTON, IdGenerate);
    AddControl(hwnd_, 0, L"BUTTON", text.about, BS_PUSHBUTTON, IdAbout);
    statusLabel_ = addStatic(L"");

    for (HWND child = GetWindow(hwnd_, GW_CHILD); child != nullptr; child = GetWindow(child, GW_HWNDNEXT)) {
        SendMessageW(child, WM_SETFONT, reinterpret_cast<WPARAM>(font), TRUE);
        SetWindowTheme(child, L"Explorer", nullptr);
    }

    LayoutControls(kWindowWidth, kWindowHeight);
}

void MainWindow::LayoutControls(int width, int) {
    const int margin = 18;
    const int content = width - margin * 2;
    const int button = 88;
    const int gap = 8;
    int y = 16;

    auto find = [&](int id) { return GetDlgItem(hwnd_, id); };
    auto placeLabelByText = [&](const wchar_t* labelText, int x, int yy, int w, int h) {
        for (HWND child = GetWindow(hwnd_, GW_CHILD); child != nullptr; child = GetWindow(child, GW_HWNDNEXT)) {
            wchar_t buffer[128]{};
            GetWindowTextW(child, buffer, 128);
            if (wcscmp(buffer, labelText) == 0) {
                MoveWindow(child, x, yy, w, h, TRUE);
                return;
            }
        }
    };

    const auto& text = T(russian_);
    placeLabelByText(text.password, margin, y, content, 20);
    y += 23;
    MoveWindow(passwordEdit_, margin, y, content - button * 2 - gap * 2, 30, TRUE);
    MoveWindow(revealButton_, width - margin - button * 2 - gap, y, button, 30, TRUE);
    MoveWindow(copyButton_, width - margin - button, y, button, 30, TRUE);
    y += 42;

    placeLabelByText(text.strength, margin, y, 180, 20);
    MoveWindow(strengthLabel_, width - margin - 160, y, 160, 20, TRUE);
    y += 22;
    MoveWindow(strengthBar_, margin, y, content, 14, TRUE);
    y += 28;

    MoveWindow(lengthLabel_, margin, y, 180, 20, TRUE);
    y += 22;
    MoveWindow(find(IdLengthMinus), margin, y, 38, 30, TRUE);
    MoveWindow(lengthSlider_, margin + 46, y, content - 92, 30, TRUE);
    MoveWindow(find(IdLengthPlus), width - margin - 38, y, 38, 30, TRUE);
    y += 38;

    const int presetWidth = 58;
    const int presetStart = margin + (content - (presetWidth * 3 + gap * 2)) / 2;
    MoveWindow(find(IdPreset16), presetStart, y, presetWidth, 28, TRUE);
    MoveWindow(find(IdPreset24), presetStart + presetWidth + gap, y, presetWidth, 28, TRUE);
    MoveWindow(find(IdPreset32), presetStart + (presetWidth + gap) * 2, y, presetWidth, 28, TRUE);
    y += 42;

    placeLabelByText(text.charsets, margin, y, content, 20);
    y += 22;
    const int half = (content - gap) / 2;
    MoveWindow(lowerCheck_, margin, y, half, 26, TRUE);
    MoveWindow(upperCheck_, margin + half + gap, y, half, 26, TRUE);
    y += 30;
    MoveWindow(digitCheck_, margin, y, half, 26, TRUE);
    MoveWindow(symbolCheck_, margin + half + gap, y, half, 26, TRUE);
    y += 38;

    placeLabelByText(text.advanced, margin, y, content, 20);
    y += 22;
    MoveWindow(similarCheck_, margin, y, content, 26, TRUE);
    y += 30;
    MoveWindow(duplicateCheck_, margin, y, content, 26, TRUE);
    y += 42;

    MoveWindow(generateButton_, margin, y, content - 108, 38, TRUE);
    MoveWindow(find(IdAbout), width - margin - 100, y, 100, 38, TRUE);
    y += 46;
    MoveWindow(statusLabel_, margin, y, content, 36, TRUE);
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
