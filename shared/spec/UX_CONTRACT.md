# UX Contract

The three native applications expose the same product flow while following platform conventions.

Required primary controls:
1. generated password, read-only and hidden by default;
2. show/hide password;
3. copy password;
4. strength indicator and label;
5. length 4...64;
6. decrement/increment controls;
7. presets 16, 24, and 32;
8. lowercase, uppercase, digits, and symbols toggles;
9. exclude similar characters;
10. exclude duplicate characters;
11. Generate password primary action;
12. About entry showing version, author, license, privacy policy, and official website.

Required behavior:
- preferences load before initial generation;
- a new password is hidden even if the previous password was revealed;
- invalid combinations show a local error instead of silently changing requested rules;
- changing preferences does not automatically regenerate unless the user presses Generate;
- the primary Generate action remains easy to reach;
- platform accessibility APIs and native keyboard navigation are used.

Desktop shortcuts:
- Windows: Ctrl+G generates; Ctrl+C copies when the password field is focused or the main window handles the shortcut; Escape hides a revealed password.
- macOS: Command+G generates; Command+C copies when applicable; Escape hides a revealed password.
