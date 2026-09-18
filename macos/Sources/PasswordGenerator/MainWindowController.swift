import AppKit

@MainActor
final class MainWindowController: NSWindowController {
    private let text = AppText.current
    private let generator = PasswordGenerator()
    private let settings = SettingsStore()
    private let clipboard = SecureClipboard()

    private var preferences = GeneratorPreferences()
    private var password = ""
    private var revealed = false

    private let passwordField = NSSecureTextField()
    private let revealedField = NSTextField()
    private let revealButton = NSButton()
    private let copyButton = NSButton()
    private let strengthBar = NSProgressIndicator()
    private let strengthLabel = NSTextField(labelWithString: "")
    private let lengthLabel = NSTextField(labelWithString: "")
    private let lengthSlider = NSSlider()
    private let lowerCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let upperCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let digitCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let symbolCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let similarCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let duplicateCheck = NSButton(checkboxWithTitle: "", target: nil, action: nil)
    private let statusLabel = NSTextField(wrappingLabelWithString: "")

    init() {
        let window = NSWindow(
            contentRect: NSRect(x: 0, y: 0, width: 520, height: 610),
            styleMask: [.titled, .closable, .miniaturizable],
            backing: .buffered,
            defer: false
        )
        window.title = text.title
        window.center()
        window.isReleasedWhenClosed = false

        super.init(window: window)
        buildUI()
        loadState()
        generatePassword(nil)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func windowDidLoad() {
        super.windowDidLoad()
        window?.makeFirstResponder(passwordField)
    }

    @objc func generatePassword(_ sender: Any?) {
        syncPreferencesFromControls()
        do {
            password = try generator.generate(preferences)
            setRevealed(false)
            passwordField.stringValue = password
            revealedField.stringValue = password
            updateStrength(generator.estimatePasswordScore(password))
            statusLabel.stringValue = ""
        } catch let error as GenerationError {
            switch error {
            case .invalidLength:
                statusLabel.stringValue = text.invalidLength
            case .noCharsets:
                statusLabel.stringValue = text.noCharsets
            case .notEnoughUniqueChars:
                statusLabel.stringValue = text.notEnoughUnique
            case .secureRandomFailed:
                statusLabel.stringValue = text.secureRandomFailed
            }
        } catch {
            statusLabel.stringValue = text.secureRandomFailed
        }
    }

    @objc func copyPassword(_ sender: Any?) {
        guard !password.isEmpty else { return }
        clipboard.copy(password)
        statusLabel.stringValue = text.copied
    }

    @objc func toggleReveal(_ sender: Any?) {
        setRevealed(!revealed)
    }

    @objc func hidePassword(_ sender: Any?) {
        if revealed { setRevealed(false) }
    }

    @objc func preferenceChanged(_ sender: Any?) {
        syncPreferencesFromControls()
        settings.save(preferences)
    }

    @objc func lengthChanged(_ sender: Any?) {
        preferences.length = Int(lengthSlider.doubleValue.rounded())
        lengthSlider.integerValue = preferences.length
        updateLengthLabel()
        settings.save(preferences)
    }

    @objc func decrementLength(_ sender: Any?) {
        setLength(preferences.length - 1)
    }

    @objc func incrementLength(_ sender: Any?) {
        setLength(preferences.length + 1)
    }

    @objc func presetLength(_ sender: NSButton) {
        setLength(sender.tag)
    }

    @objc func showAbout(_ sender: Any?) {
        let alert = NSAlert()
        alert.messageText = "Password Generator 1.6.0"
        alert.informativeText = Locale.preferredLanguages.first?.lowercased().hasPrefix("ru") == true
            ? "Приватный офлайн-генератор паролей. Пароли создаются только на устройстве и никогда не сохраняются.\n\nStanley Lloyd · PolyForm Noncommercial 1.0.0"
            : "A privacy-focused offline password generator. Passwords are generated locally and never stored.\n\nStanley Lloyd · PolyForm Noncommercial 1.0.0"
        alert.addButton(withTitle: "OK")

        let links = NSStackView()
        links.orientation = .vertical
        links.alignment = .leading
        links.spacing = 5

        for (title, url) in [
            (Locale.preferredLanguages.first?.lowercased().hasPrefix("ru") == true ? "Сайт приложения" : "App website",
             "https://stanleyll0yd.github.io/apps/password-generator/"),
            (Locale.preferredLanguages.first?.lowercased().hasPrefix("ru") == true ? "Политика конфиденциальности" : "Privacy Policy",
             "https://stanleyll0yd.github.io/apps/password-generator/privacy/"),
            (Locale.preferredLanguages.first?.lowercased().hasPrefix("ru") == true ? "Лицензия" : "License",
             "https://polyformproject.org/licenses/noncommercial/1.0.0")
        ] {
            let button = NSButton(title: title, target: self, action: #selector(openAboutLink(_:)))
            button.bezelStyle = .inline
            button.isBordered = false
            button.contentTintColor = .linkColor
            button.identifier = NSUserInterfaceItemIdentifier(url)
            links.addArrangedSubview(button)
        }

        alert.accessoryView = links
        alert.runModal()
    }

    @objc private func openAboutLink(_ sender: NSButton) {
        guard
            let value = sender.identifier?.rawValue,
            let url = URL(string: value)
        else { return }
        NSWorkspace.shared.open(url)
    }

    func handleCopyCommand() {
        copyPassword(nil)
    }

    private func buildUI() {
        guard let contentView = window?.contentView else { return }

        let root = NSStackView()
        root.orientation = .vertical
        root.alignment = .leading
        root.spacing = 12
        root.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(root)

        NSLayoutConstraint.activate([
            root.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 20),
            root.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -20),
            root.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 20),
            root.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor, constant: -20)
        ])

        root.addArrangedSubview(sectionLabel(text.password))

        passwordField.isEditable = false
        passwordField.isSelectable = true
        passwordField.font = .monospacedSystemFont(ofSize: 14, weight: .regular)
        revealedField.isEditable = false
        revealedField.isSelectable = true
        revealedField.font = passwordField.font
        revealedField.isHidden = true

        revealButton.title = text.show
        revealButton.target = self
        revealButton.action = #selector(toggleReveal(_:))

        copyButton.title = text.copy
        copyButton.target = self
        copyButton.action = #selector(copyPassword(_:))

        let passwordRow = NSStackView(views: [passwordField, revealedField, revealButton, copyButton])
        passwordRow.orientation = .horizontal
        passwordRow.spacing = 8
        passwordField.widthAnchor.constraint(greaterThanOrEqualToConstant: 250).isActive = true
        revealedField.widthAnchor.constraint(equalTo: passwordField.widthAnchor).isActive = true
        root.addArrangedSubview(passwordRow)

        let strengthHeader = NSStackView(views: [sectionLabel(text.strength), strengthLabel])
        strengthHeader.orientation = .horizontal
        strengthHeader.distribution = .fill
        root.addArrangedSubview(strengthHeader)

        strengthBar.minValue = 0
        strengthBar.maxValue = 100
        strengthBar.isIndeterminate = false
        strengthBar.controlSize = .small
        strengthBar.widthAnchor.constraint(equalTo: root.widthAnchor).isActive = true
        root.addArrangedSubview(strengthBar)

        lengthSlider.minValue = Double(PasswordConstants.minLength)
        lengthSlider.maxValue = Double(PasswordConstants.maxLength)
        lengthSlider.numberOfTickMarks = 0
        lengthSlider.allowsTickMarkValuesOnly = false
        lengthSlider.target = self
        lengthSlider.action = #selector(lengthChanged(_:))

        let minus = NSButton(title: "−", target: self, action: #selector(decrementLength(_:)))
        let plus = NSButton(title: "+", target: self, action: #selector(incrementLength(_:)))
        let lengthRow = NSStackView(views: [minus, lengthSlider, plus])
        lengthRow.orientation = .horizontal
        lengthRow.spacing = 8

        root.addArrangedSubview(lengthLabel)
        root.addArrangedSubview(lengthRow)

        let presets = [16, 24, 32].map { value -> NSButton in
            let button = NSButton(title: "\(value)", target: self, action: #selector(presetLength(_:)))
            button.tag = value
            return button
        }
        let presetRow = NSStackView(views: presets)
        presetRow.orientation = .horizontal
        presetRow.alignment = .centerY
        presetRow.spacing = 8
        root.addArrangedSubview(presetRow)

        root.addArrangedSubview(sectionLabel(text.charsets))

        lowerCheck.title = text.lowercase
        upperCheck.title = text.uppercase
        digitCheck.title = text.digits
        symbolCheck.title = text.symbols

        for control in [lowerCheck, upperCheck, digitCheck, symbolCheck, similarCheck, duplicateCheck] {
            control.target = self
            control.action = #selector(preferenceChanged(_:))
        }

        let groupGrid = NSGridView(views: [
            [lowerCheck, upperCheck],
            [digitCheck, symbolCheck]
        ])
        groupGrid.rowSpacing = 6
        groupGrid.columnSpacing = 16
        root.addArrangedSubview(groupGrid)

        root.addArrangedSubview(sectionLabel(text.advanced))
        similarCheck.title = text.excludeSimilar
        duplicateCheck.title = text.excludeDuplicates
        root.addArrangedSubview(similarCheck)
        root.addArrangedSubview(duplicateCheck)

        let generate = NSButton(title: text.generate, target: self, action: #selector(generatePassword(_:)))
        generate.keyEquivalent = "\r"
        generate.bezelStyle = .rounded
        generate.controlSize = .large

        let about = NSButton(title: text.about, target: self, action: #selector(showAbout(_:)))

        let actions = NSStackView(views: [generate, about])
        actions.orientation = .horizontal
        actions.spacing = 10
        root.addArrangedSubview(actions)

        statusLabel.textColor = .secondaryLabelColor
        statusLabel.maximumNumberOfLines = 2
        root.addArrangedSubview(statusLabel)
    }

    private func sectionLabel(_ value: String) -> NSTextField {
        let label = NSTextField(labelWithString: value)
        label.font = .systemFont(ofSize: NSFont.systemFontSize, weight: .semibold)
        return label
    }

    private func loadState() {
        preferences = settings.load()
        lengthSlider.integerValue = preferences.length
        lowerCheck.state = preferences.useLowercase ? .on : .off
        upperCheck.state = preferences.useUppercase ? .on : .off
        digitCheck.state = preferences.useDigits ? .on : .off
        symbolCheck.state = preferences.useSymbols ? .on : .off
        similarCheck.state = preferences.excludeSimilar ? .on : .off
        duplicateCheck.state = preferences.excludeDuplicates ? .on : .off
        updateLengthLabel()
    }

    private func syncPreferencesFromControls() {
        preferences.length = min(
            PasswordConstants.maxLength,
            max(PasswordConstants.minLength, lengthSlider.integerValue)
        )
        preferences.useLowercase = lowerCheck.state == .on
        preferences.useUppercase = upperCheck.state == .on
        preferences.useDigits = digitCheck.state == .on
        preferences.useSymbols = symbolCheck.state == .on
        preferences.excludeSimilar = similarCheck.state == .on
        preferences.excludeDuplicates = duplicateCheck.state == .on
    }

    private func setLength(_ value: Int) {
        preferences.length = min(PasswordConstants.maxLength, max(PasswordConstants.minLength, value))
        lengthSlider.integerValue = preferences.length
        updateLengthLabel()
        settings.save(preferences)
    }

    private func updateLengthLabel() {
        lengthLabel.stringValue = "\(text.length): \(preferences.length)"
        lengthLabel.font = .systemFont(ofSize: NSFont.systemFontSize, weight: .semibold)
    }

    private func setRevealed(_ value: Bool) {
        revealed = value
        passwordField.isHidden = value
        revealedField.isHidden = !value
        revealButton.title = value ? text.hide : text.show

        if #available(macOS 10.5, *) {
            window?.sharingType = value ? .none : .readOnly
        }
    }

    private func updateStrength(_ score: Int) {
        strengthBar.doubleValue = Double(score)
        let label =
            score < 20 ? text.veryWeak :
            score < 40 ? text.weak :
            score < 60 ? text.medium :
            score < 80 ? text.strong :
                         text.veryStrong
        strengthLabel.stringValue = "\(label) · \(score)/100"
        strengthLabel.textColor = .secondaryLabelColor
    }
}
