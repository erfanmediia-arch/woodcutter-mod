#!/bin/bash
echo "========================================"
echo "  Woodcutter Bot Mod - Auto Builder"
echo "========================================"
echo

# Check Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java 17 not found!"
    echo "Install it from: https://adoptium.net/"
    exit 1
fi
echo "[OK] Java found: $(java -version 2>&1 | head -1)"

# Check MDK folder
if [ ! -d "forge-mdk" ]; then
    echo
    echo "[INFO] Forge MDK not found."
    echo "Please:"
    echo "  1. Go to https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html"
    echo "  2. Download the MDK version"
    echo "  3. Extract it into a folder named 'forge-mdk' next to this script"
    echo "  4. Run this script again"
    exit 1
fi

echo "[INFO] Copying mod files into Forge MDK..."
cp -r src forge-mdk/
cp build.gradle gradle.properties settings.gradle forge-mdk/

echo "[INFO] Building mod JAR..."
cd forge-mdk
chmod +x gradlew
./gradlew build

if [ $? -ne 0 ]; then
    echo
    echo "[ERROR] Build failed! Check the output above."
    exit 1
fi

echo
echo "========================================"
echo "[SUCCESS] Build complete!"
echo
echo "Your JAR file is at:"
echo "  forge-mdk/build/libs/woodcuttermod-1.0.0.jar"
echo
echo "Copy that .jar to your Minecraft mods folder."
echo "========================================"
