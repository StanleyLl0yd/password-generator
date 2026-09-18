import AppKit

@main
@MainActor
final class AppDelegate: NSObject, NSApplicationDelegate {
    private var mainWindowController: MainWindowController?

    func applicationDidFinishLaunching(_ notification: Notification) {
        NSApp.setActivationPolicy(.regular)

        let controller = MainWindowController()
        mainWindowController = controller
        installMainMenu(controller: controller)

        controller.showWindow(nil)
        NSApp.activate(ignoringOtherApps: true)
    }

    func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool {
        true
    }

    private func installMainMenu(controller: MainWindowController) {
        let mainMenu = NSMenu()

        let appItem = NSMenuItem()
        mainMenu.addItem(appItem)
        let appMenu = NSMenu()
        appItem.submenu = appMenu

        appMenu.addItem(
            withTitle: AppText.current.about,
            action: #selector(MainWindowController.showAbout(_:)),
            keyEquivalent: ""
        ).target = controller
        appMenu.addItem(.separator())
        appMenu.addItem(
            withTitle: AppText.current.title == "Генератор паролей" ? "Завершить" : "Quit",
            action: #selector(NSApplication.terminate(_:)),
            keyEquivalent: "q"
        )

        let editItem = NSMenuItem()
        mainMenu.addItem(editItem)
        let editMenu = NSMenu(title: "Edit")
        editItem.submenu = editMenu

        let generateItem = NSMenuItem(
            title: AppText.current.generate,
            action: #selector(MainWindowController.generatePassword(_:)),
            keyEquivalent: "g"
        )
        generateItem.target = controller
        editMenu.addItem(generateItem)

        let copyItem = NSMenuItem(
            title: AppText.current.copy,
            action: #selector(copyGeneratedPassword(_:)),
            keyEquivalent: "c"
        )
        copyItem.target = self
        editMenu.addItem(copyItem)

        NSApp.mainMenu = mainMenu
    }

    @objc private func copyGeneratedPassword(_ sender: Any?) {
        mainWindowController?.handleCopyCommand()
    }
}
