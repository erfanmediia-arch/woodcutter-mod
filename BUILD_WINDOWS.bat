@echo off
echo ========================================
echo   Woodcutter Bot Mod - Auto Builder
echo ========================================
echo.

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 17 not found! Download from: https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java found.

:: Download Forge MDK if not present
if not exist "forge-mdk" (
    echo.
    echo [INFO] Downloading Forge MDK 1.20.1...
    echo Please download manually from:
    echo   https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
    echo.
    echo Download the "MDK" version, extract it into a folder named "forge-mdk"
    echo next to this script, then run this script again.
    pause
    exit /b 1
)

echo [INFO] Copying mod files into Forge MDK...

:: Copy source files
xcopy /E /I /Y "src" "forge-mdk\src" >nul
copy /Y "build.gradle" "forge-mdk\build.gradle" >nul
copy /Y "gradle.properties" "forge-mdk\gradle.properties" >nul
copy /Y "settings.gradle" "forge-mdk\settings.gradle" >nul

echo [INFO] Building mod JAR...
cd forge-mdk

call gradlew.bat build

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed! Check the output above.
    cd ..
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] Build complete!
echo.
echo Your JAR file is at:
echo   forge-mdk\build\libs\woodcuttermod-1.0.0.jar
echo.
echo Copy that .jar to your Minecraft mods folder.
echo ========================================

cd ..
pause
