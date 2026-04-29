@echo off
REM Student Survival Management Deployment Script for Windows

setlocal

set PROJECT_DIR=%~dp0
set APP_NAME=StudentSurvivalManager
set VERSION=2.0
set VENDOR=StudentSurvival
set MAIN_CLASS=com.student.StudentSurvivalManager

echo 🚀 Building Student Survival Management v%VERSION%...

REM Clean and package
echo 📦 Building JAR...
cd "%PROJECT_DIR%"
call mvn clean package -DskipTests

REM Get the JAR file
set JAR_FILE=%PROJECT_DIR%target\student-manager-1.0.jar

if not exist "%JAR_FILE%" (
    echo ❌ JAR file not found: %JAR_FILE%
    exit /b 1
)

REM Find JavaFX path
set JAVAFX_PATH=
if exist "%USERPROFILE%\javafx\javafx-sdk-17.0.19" (
    set JAVAFX_PATH=%USERPROFILE%\javafx\javafx-sdk-17.0.19
) else if exist "C:\javafx\javafx-sdk-17.0.19" (
    set JAVAFX_PATH=C:\javafx\javafx-sdk-17.0.19
) else (
    echo ⚠️  JavaFX not found in standard locations
    echo Please set JAVAFX_PATH environment variable
    exit /b 1
)

echo 📚 Using JavaFX from: %JAVAFX_PATH%

REM Create output directory
set OUTPUT_DIR=%PROJECT_DIR%dist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

echo 🖥️  Detected platform: Windows

REM Build native package
echo 🔨 Creating native package...

jpackage ^
    --name "%APP_NAME%" ^
    --type exe ^
    --app-version "%VERSION%" ^
    --vendor "%VENDOR%" ^
    --main-class "%MAIN_CLASS%" ^
    --main-jar "%JAR_FILE%" ^
    --module-path "%JAVAFX_PATH%\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    --dest "%OUTPUT_DIR%" ^
    --description "Your comprehensive student management application" ^
    --copyright "Copyright 2024" ^
    --win-menu ^
    --win-dir-chooser ^
    --win-shortcut

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 🎉 Build complete! Check the %OUTPUT_DIR% directory for your installer.
) else (
    echo ❌ Build failed
    exit /b 1
)

endlocal
