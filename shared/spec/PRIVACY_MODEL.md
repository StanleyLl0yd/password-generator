# Privacy Model

Password Generator is intentionally a small offline utility.

## Invariants

The application:
- does not require or request network access for product functionality;
- has no backend and no user accounts;
- contains no advertising, analytics, tracking, telemetry, crash-upload SDK, or cloud synchronization;
- generates passwords locally with the platform cryptographic RNG;
- never persists a generated password;
- stores only non-secret generator preferences;
- does not copy secrets into logs or diagnostics;
- hides generated passwords by default;
- protects explicitly revealed passwords from screen capture where the operating system provides a native mechanism;
- treats clipboard copies as ephemeral and clears only its own still-current value where the operating system permits reliable cleanup.

Any platform-specific change that weakens one of these invariants requires explicit project-owner approval.
