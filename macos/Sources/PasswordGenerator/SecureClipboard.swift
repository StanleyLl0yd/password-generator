import AppKit

@MainActor
final class SecureClipboard {
    static let clearDelay: TimeInterval = 60

    private var workItem: DispatchWorkItem?
    private var changeCount: Int?

    func copy(_ password: String) {
        guard !password.isEmpty else { return }

        let pasteboard = NSPasteboard.general
        pasteboard.clearContents()
        pasteboard.setString(password, forType: .string)

        workItem?.cancel()
        changeCount = pasteboard.changeCount

        let item = DispatchWorkItem { [weak self] in
            MainActor.assumeIsolated {
                self?.clearIfStillCurrent()
            }
        }
        workItem = item
        DispatchQueue.main.asyncAfter(deadline: .now() + Self.clearDelay, execute: item)
    }

    func forget() {
        workItem?.cancel()
        workItem = nil
        changeCount = nil
    }

    private func clearIfStillCurrent() {
        defer { forget() }

        guard
            let changeCount,
            NSPasteboard.general.changeCount == changeCount
        else {
            return
        }

        NSPasteboard.general.clearContents()
    }
}
