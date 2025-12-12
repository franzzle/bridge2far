#!/bin/bash

# Get the user's home directory
HOME_DIR=$HOME

# Define platform-specific variables
JDK_FOLDER="jdk_mac_adoptjdk17"
JDK_ARCHIVE="OpenJDK17U-jdk_x64_mac_hotspot_17.0.9_9.tar.gz"
ASSETS_DIR="$PWD/lwjgl3/build/resources/main/."
ICON_FILE="icons/icon.icns"

# Run Gradle tasks
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/azul-17.0.6/Contents/Home"
export PATH=$JAVA_HOME/bin:$PATH
./gradlew clean lwjgl3:dist

# Set output directory
OUTPUT_NAME="$HOME_DIR/Development/GameDeployableDevelopment/Bridge2Far.app"

# Remove existing output directory
rm -rf "$OUTPUT_NAME"

# Pack and package the application
java -jar "$HOME_DIR/Development/Tooling/packr/packr-all-4.0.0.jar" \
     --platform mac \
     --jdk "$HOME_DIR/Development/Tooling/$JDK_FOLDER/$JDK_ARCHIVE"  \
     --executable pixelparody \
     --classpath "$PWD/lwjgl3/build/libs/bridge2far-1.0.0.jar" \
     --mainclass com.pimpedpixel.games.lwjgl3.Lwjgl3Launcher \
     --vmargs Xmx1G \
     --resources "$ASSETS_DIR" \
     --minimizejre soft \
     --icon "$ICON_FILE" \
     --output "$OUTPUT_NAME"

