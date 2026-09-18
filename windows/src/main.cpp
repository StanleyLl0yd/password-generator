#include "ui.hpp"

#include <commctrl.h>
#include <windows.h>

int WINAPI wWinMain(HINSTANCE instance, HINSTANCE, PWSTR, int showCommand) {
    SetProcessDpiAwarenessContext(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2);

    INITCOMMONCONTROLSEX controls{
        .dwSize = sizeof(INITCOMMONCONTROLSEX),
        .dwICC = ICC_STANDARD_CLASSES | ICC_BAR_CLASSES | ICC_PROGRESS_CLASS | ICC_LINK_CLASS
    };
    InitCommonControlsEx(&controls);

    pg::MainWindow window;
    if (!window.Create(instance, showCommand)) return 1;
    return window.Run();
}
