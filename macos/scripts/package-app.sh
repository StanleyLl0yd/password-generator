#!/bin/bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MACOS_DIR="$ROOT/macos"
VERSION="$(tr -d '[:space:]' < "$ROOT/VERSION")"
OUT_DIR="${1:-$MACOS_DIR/dist}"
APP="$OUT_DIR/Password Generator.app"

rm -rf "$APP"
mkdir -p "$APP/Contents/MacOS" "$APP/Contents/Resources"

pushd "$MACOS_DIR" >/dev/null
swift build -c release --arch arm64 --arch x86_64
BIN_PATH="$(swift build -c release --arch arm64 --arch x86_64 --show-bin-path)"
popd >/dev/null

cp "$BIN_PATH/PasswordGenerator" "$APP/Contents/MacOS/PasswordGenerator"
sed "s/@VERSION@/$VERSION/g" "$MACOS_DIR/Resources/Info.plist.in" > "$APP/Contents/Info.plist"

chmod 755 "$APP/Contents/MacOS/PasswordGenerator"

echo "Created: $APP"
