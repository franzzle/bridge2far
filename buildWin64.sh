#!/bin/bash
convert_version_to_vXXX() {
    local version="$1"

    # Remove periods from the version string
    version=$(echo "$version" | tr -d '.')

    # Pad the version with leading zeros to make it three digits
    version=$(printf "%03d" "$version")

    # Add the 'v' prefix
    echo "v$version"
}
# Get the user's home directory
HOME_DIR=$HOME

# Define platform-specific variables
JDK_FOLDER="jdk_win_17"
JDK_ARCHIVE="OpenJDK17U-jdk_x64_windows_hotspot_17.0.17_10.zip"
ICON_FILE="icons/icon.ico"
ASSETS_DIR="$PWD/desktop/build/resources/main/."

# Run Gradle tasks
export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/azul-1.8.0_362/Contents/Home"
export PATH=$JAVA_HOME/bin:$PATH
./gradlew clean desktop:dist

# Read the version property from overview/artifacts.json
VERSION=$(cat overview/artifacts.json | jq -r '.version')
V_VERSION=$(convert_version_to_vXXX "$VERSION")

# Set output directory
OUTPUT_NAME="$HOME_DIR/Development/GameDeployableDevelopment/pixelparody_win64_$V_VERSION"

echo "Making the release with folder : $OUTPUT_NAME"
# Remove existing output directory
rm -rf "$OUTPUT_NAME"

# Pack and package the application
java -jar "$HOME_DIR/Development/Tooling/packr/packr-all-4.0.0.jar" \
     --platform windows64 \
     --jdk "$HOME_DIR/Development/Tooling/$JDK_FOLDER/$JDK_ARCHIVE"  \
     --executable pixelparody \
     --classpath "$PWD/desktop/build/libs/desktop-1.0.jar" \
     --removelibs "$PWD/desktop/build/libs/desktop-1.0.jar" \
     --mainclass com.franzzle.pixelparody.desktop.DesktopLauncher \
     --vmargs Xmx1G \
     --resources "$ASSETS_DIR" \
     --icon "$ICON_FILE" \
     --output "$OUTPUT_NAME"
