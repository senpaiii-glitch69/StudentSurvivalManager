#!/bin/bash

# Simple build script for Student Survival Management
# Creates a distributable JAR with all dependencies

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="StudentSurvivalManager"
VERSION="2.0"

echo "🚀 Building Student Survival Management v${VERSION}..."

cd "$PROJECT_DIR"

# Clean and package
echo "📦 Building JAR with dependencies..."
mvn clean package -DskipTests

# Check if JAR was created
JAR_FILE="$PROJECT_DIR/target/student-manager-1.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Build failed - JAR not found"
    exit 1
fi

# Create distribution directory
DIST_DIR="$PROJECT_DIR/dist"
mkdir -p "$DIST_DIR"

# Copy JAR to dist
cp "$JAR_FILE" "$DIST_DIR/${APP_NAME}-${VERSION}.jar"

# Create run script
cat > "$DIST_DIR/run.sh" << 'EOF'
#!/bin/bash
# Student Survival Management Launcher

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/StudentSurvivalManager-2.0.jar"

# Find JavaFX
JAVAFX_PATH=""
if [ -d "$HOME/javafx/javafx-sdk-17.0.19" ]; then
    JAVAFX_PATH="$HOME/javafx/javafx-sdk-17.0.19"
elif [ -d "/opt/javafx/javafx-sdk-17.0.19" ]; then
    JAVAFX_PATH="/opt/javafx/javafx-sdk-17.0.19"
else
    echo "⚠️  JavaFX not found. Please install JavaFX or set JAVAFX_PATH"
    exit 1
fi

# Find GSON
GSON_JAR=$(find ~/.m2/repository -name "gson-*.jar" 2>/dev/null | head -1)

# Run the application
java -Dprism.order=sw \
    --module-path "$JAVAFX_PATH/lib" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "$JAR_FILE:$GSON_JAR" \
    com.student.StudentSurvivalManager
EOF

chmod +x "$DIST_DIR/run.sh"

# Create Windows run script
cat > "$DIST_DIR/run.bat" << 'EOF'
@echo off
REM Student Survival Management Launcher for Windows

set SCRIPT_DIR=%~dp0
set JAR_FILE=%SCRIPT_DIR%StudentSurvivalManager-2.0.jar

REM Find JavaFX
set JAVAFX_PATH=
if exist "%USERPROFILE%\javafx\javafx-sdk-17.0.19" (
    set JAVAFX_PATH=%USERPROFILE%\javafx\javafx-sdk-17.0.19
) else if exist "C:\javafx\javafx-sdk-17.0.19" (
    set JAVAFX_PATH=C:\javafx\javafx-sdk-17.0.19
) else (
    echo JavaFX not found. Please install JavaFX.
    exit /b 1
)

REM Find GSON
for /f "delims=" %%i in ('dir /s /b "%USERPROFILE%\.m2\repository\com\google\code\gson\gson-*.jar" 2^>nul ^| findstr /i "gson-"') do (
    set GSON_JAR=%%i
    goto :found
)
:found

REM Run the application
java -Dprism.order=sw --module-path "%JAVAFX_PATH%\lib" --add-modules javafx.controls,javafx.fxml -cp "%JAR_FILE%;%GSON_JAR%" com.student.StudentSurvivalManager
EOF

# Create README for distribution
cat > "$DIST_DIR/README.txt" << 'EOF'
Student Survival Management v2.0
================================

Your comprehensive student management application.

REQUIREMENTS:
- Java 17 or higher
- JavaFX 21.0.5

RUNNING ON MAC/Linux:
1. Ensure JavaFX is installed at ~/javafx/javafx-sdk-17.0.19
2. Run: ./run.sh

RUNNING ON WINDOWS:
1. Ensure JavaFX is installed at C:\javafx\javafx-sdk-17.0.19
2. Run: run.bat

DEFAULT CREDENTIALS:
Username: admin
Password: 1234

DATA STORAGE:
All data is saved to ~/.studyhub/data.json

For more information, visit: https://github.com/yourusername/StudentSurvivalManager
EOF

echo ""
echo "✅ Distribution created in: $DIST_DIR"
echo ""
echo "Files created:"
echo "  - ${APP_NAME}-${VERSION}.jar"
echo "  - run.sh (macOS/Linux)"
echo "  - run.bat (Windows)"
echo "  - README.txt"
