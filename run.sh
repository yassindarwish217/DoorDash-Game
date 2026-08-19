#!/usr/bin/env bash
set -euo pipefail

# DoorDasH Game - Compile & Run Script
#
# Requirements:
# - JDK 17 or newer
# - JavaFX SDK / OpenJFX
#
# Set JAVAFX_HOME to your JavaFX SDK folder before running this script.
# Example on macOS with Homebrew:
#   export JAVAFX_HOME="$(brew --prefix openjfx)/libexec"

PROJ="$(cd "$(dirname "$0")" && pwd)"
SRC="$PROJ/src"
OUT="$PROJ/bin"

if ! command -v javac >/dev/null 2>&1; then
    echo "javac was not found. Install JDK 17 or newer first."
    exit 1
fi

if [ -z "${JAVAFX_HOME:-}" ]; then
    echo "JAVAFX_HOME is not set."
    echo "Install JavaFX/OpenJFX, then run:"
    echo '  export JAVAFX_HOME="/path/to/javafx-sdk"'
    exit 1
fi

if [ ! -d "$JAVAFX_HOME/lib" ]; then
    echo "Could not find JavaFX libraries at: $JAVAFX_HOME/lib"
    exit 1
fi

mkdir -p "$OUT"
sources_file="$OUT/sources.txt"
find "$SRC" -path "*/tests/*" -prune -o -name "*.java" -print > "$sources_file"

echo "Compiling source files..."
javac \
    --module-path "$JAVAFX_HOME/lib" \
    --add-modules javafx.controls,javafx.graphics,javafx.base \
    -d "$OUT" \
    -sourcepath "$SRC" \
    @"$sources_file"

echo "Launching DoorDasH..."
cd "$PROJ"
java \
    --module-path "$JAVAFX_HOME/lib" \
    --add-modules javafx.controls,javafx.graphics,javafx.base \
    -cp "$OUT" \
    game.gui.Main
