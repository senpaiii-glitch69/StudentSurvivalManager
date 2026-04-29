#!/bin/bash

# Student Survival Management Deployment Script
# Creates native installers for macOS, Windows, and Linux

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="StudentSurvivalManager"
VERSION="2.0"
VENDOR="StudentSurvival"
MAIN_CLASS="com.student.StudentSurvivalManager"

echo "🚀 Building Student Survival Management v${VERSION}..."

# Clean and package
echo "📦 Building JAR..."
cd "$PROJECT_DIR"
mvn clean package -DskipTests

# Get the JAR file
JAR_FILE="$PROJECT_DIR/target/student-manager-1.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    exit 1
fi

# Find JavaFX path
JAVAFX_PATH=""
if [ -d "$HOME/javafx/javafx-sdk-17.0.19" ]; then
    JAVAFX_PATH="$HOME/javafx/javafx-sdk-17.0.19"
elif [ -d "/opt/javafx/javafx-sdk-17.0.19" ]; then
    JAVAFX_PATH="/opt/javafx/javafx-sdk-17.0.19"
else
    echo "⚠️  JavaFX not found in standard locations"
    echo "Please set JAVAFX_PATH environment variable"
    exit 1
fi

echo "📚 Using JavaFX from: $JAVAFX_PATH"

# Create output directory
OUTPUT_DIR="$PROJECT_DIR/dist"
mkdir -p "$OUTPUT_DIR"

# Detect OS
OS="$(uname -s)"
case "$OS" in
    Darwin*)
        PLATFORM="mac"
        ;;
    Linux*)
        PLATFORM="linux"
        ;;
    MINGW*|CYGWIN*|MSYS*)
        PLATFORM="windows"
        ;;
    *)
        echo "❌ Unsupported OS: $OS"
        exit 1
        ;;
esac

echo "🖥️  Detected platform: $PLATFORM"

# Build native package
echo "🔨 Creating native package..."

case "$PLATFORM" in
    mac)
        # macOS DMG
        jpackage \
            --name "$APP_NAME" \
            --type dmg \
            --app-version "$VERSION" \
            --vendor "$VENDOR" \
            --main-class "$MAIN_CLASS" \
            --main-jar "$JAR_FILE" \
            --module-path "$JAVAFX_PATH/lib" \
            --add-modules javafx.controls,javafx.fxml \
            --dest "$OUTPUT_DIR" \
            --icon "$PROJECT_DIR/src/main/resources/icon.icns" \
            --description "Your comprehensive student management application" \
            --copyright "Copyright 2024" \
            --mac-package-name "$APP_NAME" \
            --mac-package-identifier "com.student.studentsurvivalmanager"

        echo "✅ macOS DMG created: $OUTPUT_DIR/${APP_NAME}-${VERSION}.dmg"
        ;;

    linux)
        # Linux DEB and RPM
        jpackage \
            --name "$APP_NAME" \
            --type deb \
            --app-version "$VERSION" \
            --vendor "$VENDOR" \
            --main-class "$MAIN_CLASS" \
            --main-jar "$JAR_FILE" \
            --module-path "$JAVAFX_PATH/lib" \
            --add-modules javafx.controls,javafx.fxml \
            --dest "$OUTPUT_DIR" \
            --icon "$PROJECT_DIR/src/main/resources/icon.png" \
            --description "Your comprehensive student management application" \
            --copyright "Copyright 2024" \
            --linux-menu-group "Education" \
            --linux-shortcut

        echo "✅ Linux DEB created: $OUTPUT_DIR/${APP_NAME}_${VERSION}-1_amd64.deb"
        ;;

    windows)
        # Windows EXE
        jpackage \
            --name "$APP_NAME" \
            --type exe \
            --app-version "$VERSION" \
            --vendor "$VENDOR" \
            --main-class "$MAIN_CLASS" \
            --main-jar "$JAR_FILE" \
            --module-path "$JAVAFX_PATH/lib" \
            --add-modules javafx.controls,javafx.fxml \
            --dest "$OUTPUT_DIR" \
            --icon "$PROJECT_DIR/src/main/resources/icon.ico" \
            --description "Your comprehensive student management application" \
            --copyright "Copyright 2024" \
            --win-menu \
            --win-dir-chooser \
            --win-shortcut

        echo "✅ Windows EXE created: $OUTPUT_DIR/${APP_NAME}-${VERSION}.exe"
        ;;
esac

echo ""
echo "🎉 Build complete! Check the $OUTPUT_DIR directory for your installer."
