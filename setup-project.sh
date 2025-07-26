#!/bin/bash
#
# Android Compose Circuit App Template Customizer
# Adapted for Circuit + Metro DI architecture
#
# Usage: bash setup-project.sh com.mycompany.appname AppName [--keep-examples] [--keep-workmanager]
#
# Licensed under the Apache License, Version 2.0 (the "License");
#

# Verify bash version. macOS comes with bash 3 preinstalled.
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]
then
  echo "You need at least bash 4 to run this script."
  exit 1
fi

# exit when any command fails
set -e

# Parse arguments
KEEP_EXAMPLES=false
KEEP_WORKMANAGER=false

# Check for flags
for arg in "$@"; do
  case $arg in
    --keep-examples)
      KEEP_EXAMPLES=true
      shift
      ;;
    --keep-workmanager)
      KEEP_WORKMANAGER=true
      shift
      ;;
  esac
done

if [[ $# -lt 2 ]]; then
   echo "Usage: bash setup-project.sh com.mycompany.appname AppName [--keep-examples] [--keep-workmanager]" >&2
   echo ""
   echo "Examples:"
   echo "  bash setup-project.sh com.mycompany.todoapp TodoApp"
   echo "  bash setup-project.sh com.mycompany.newsapp NewsApp --keep-examples"
   echo "  bash setup-project.sh com.mycompany.taskapp TaskApp --keep-workmanager"
   echo ""
   echo "Options:"
   echo "  --keep-examples     Keep Example* files for reference"
   echo "  --keep-workmanager  Keep WorkManager related files"
   exit 2
fi

PACKAGE=$1
APPNAME=$2
SUBDIR=${PACKAGE//.//} # Replaces . with /

echo "🚀 Starting Android Circuit App Template customization..."
echo "📦 New package: $PACKAGE"
echo "📱 App name: $APPNAME"
echo "🗂️  Keep examples: $KEEP_EXAMPLES"
echo "⚙️  Keep WorkManager: $KEEP_WORKMANAGER"
echo ""

# Step 1: Move package structure from app.example to new package
echo "📁 Step 1: Restructuring package directories..."
for n in $(find . -type d \( -path '*/src/androidTest' -or -path '*/src/main' -or -path '*/src/test' \) )
do
  if [ -d "$n/java/app/example" ]; then
    echo "Creating $n/java/$SUBDIR"
    mkdir -p $n/java/$SUBDIR
    echo "Moving files from $n/java/app/example/* to $n/java/$SUBDIR"
    mv $n/java/app/example/* $n/java/$SUBDIR/ 2>/dev/null || true
    echo "Removing old $n/java/app"
    rm -rf $n/java/app
  fi
done

# Step 2: Update package declarations and imports
echo "📝 Step 2: Updating package declarations and imports..."
find ./ -type f -name "*.kt" -exec sed -i.bak "s/package app\.example/package $PACKAGE/g" {} \;
find ./ -type f -name "*.kt" -exec sed -i.bak "s/import app\.example/import $PACKAGE/g" {} \;

# Step 3: Update Gradle files and XML
echo "🔧 Step 3: Updating Gradle and XML files..."
find ./ -type f -name "*.kts" -exec sed -i.bak "s/app\.example/$PACKAGE/g" {} \;
find ./ -type f -name "*.xml" -exec sed -i.bak "s/app\.example/$PACKAGE/g" {} \;

# Step 4: Rename CircuitApp to {AppName}App
echo "⚡ Step 4: Renaming CircuitApp to ${APPNAME}App..."
find ./ -type f -name "*.kt" -exec sed -i.bak "s/CircuitApp/${APPNAME}App/g" {} \;
find ./ -type f -name "*.xml" -exec sed -i.bak "s/CircuitApp/${APPNAME}App/g" {} \;
find ./ -type f -name "*.kts" -exec sed -i.bak "s/CircuitApp/${APPNAME}App/g" {} \;

# Rename CircuitApp.kt file
if [ -f "app/src/main/java/$SUBDIR/CircuitApp.kt" ]; then
    mv "app/src/main/java/$SUBDIR/CircuitApp.kt" "app/src/main/java/$SUBDIR/${APPNAME}App.kt"
    echo "Renamed CircuitApp.kt to ${APPNAME}App.kt"
fi

# Step 5: Update app name in strings.xml and other XML files
echo "🏷️  Step 5: Updating app name in XML files..."
find ./ -name "strings.xml" -exec sed -i.bak "s/<string name=\"app_name\">.*<\/string>/<string name=\"app_name\">$APPNAME<\/string>/g" {} \;

# Step 6: Handle Example files
if [ "$KEEP_EXAMPLES" = false ]; then
    echo "🗑️  Step 6: Removing Example* files..."
    find ./ -name "Example*.kt" -type f -delete
    find ./ -name "*Example*.kt" -type f -delete
    echo "Example files removed"
else
    echo "📚 Step 6: Keeping Example* files for reference..."
fi

# Step 7: Handle WorkManager files
if [ "$KEEP_WORKMANAGER" = false ]; then
    echo "🗑️  Step 7: Removing WorkManager files..."
    find ./ -path "*/work/*" -name "*.kt" -type f -delete
    find ./ -name "*Worker*.kt" -type f -delete
    find ./ -name "WorkerKey.kt" -type f -delete
    find ./ -name "AppWorkerFactory.kt" -type f -delete
    # Remove work directory if empty
    find ./ -name "work" -type d -empty -delete 2>/dev/null || true
    echo "WorkManager files removed"
else
    echo "⚙️  Step 7: Keeping WorkManager files..."
fi

# Step 8: Clean up backup files
echo "🧹 Step 8: Cleaning up backup files..."
find . -name "*.bak" -type f -delete

# Step 9: Update README and remove template-specific files
echo "📄 Step 9: Cleaning up template files..."
if [ -f "README.md" ]; then
    # Create a minimal README for the new project
    cat > README.md << EOF
# $APPNAME

An Android app built with:
- ⚡️ [Circuit](https://github.com/slackhq/circuit) for UI architecture
- 🏗️ [Metro](https://zacsweers.github.io/metro/) for Dependency Injection
- 🎨 [Jetpack Compose](https://developer.android.com/jetpack/compose) for UI
- 📱 Material Design 3

## Getting Started

1. Open the project in Android Studio
2. Update your app theme colors using [Theme Builder](https://material-foundation.github.io/material-theme-builder/)
3. Generate your app icon using [Icon Kitchen](https://icon.kitchen/)
4. Build and run!

## Architecture

This app follows the Circuit UDF (Unidirectional Data Flow) architecture with Metro for dependency injection.

EOF
fi

# Remove template-specific files
echo "🗑️  Removing template-specific files..."
rm -rf .google/ 2>/dev/null || true
rm -rf .github/ 2>/dev/null || true
rm -f CONTRIBUTING.md 2>/dev/null || true
rm -f LICENSE 2>/dev/null || true

# Remove renovate config (user can add back if needed)
if [ -f "renovate.json" ]; then
    echo "📋 Found renovate.json - you may want to review and configure this for your project"
fi

# Remove git history for fresh start
echo "🔄 Removing git history for fresh start..."
rm -rf .git/ 2>/dev/null || true

# Step 10: Create initial git repository
echo "📦 Step 10: Initializing new git repository..."
git init
git add .
git commit -m "Initial commit: $APPNAME

- Customized from android-compose-app-template
- Package: $PACKAGE  
- Circuit + Metro architecture
- Examples kept: $KEEP_EXAMPLES
- WorkManager kept: $KEEP_WORKMANAGER"

echo ""
echo "✅ Customization complete!"
echo ""
echo "🎉 Your $APPNAME is ready!"
echo "📦 Package: $PACKAGE"
echo "📁 Main class: ${APPNAME}App"
echo ""
echo "📋 Next steps:"
echo "   1. Open the project in Android Studio"
echo "   2. Update app theme colors"
echo "   3. Generate your app icon"
echo "   4. Review and update .editorconfig"
if [ "$KEEP_EXAMPLES" = true ]; then
    echo "   5. Remove Example* files when you no longer need them"
fi
if [ "$KEEP_WORKMANAGER" = false ]; then
    echo "   5. Add WorkManager back if you need background tasks"
fi
echo ""
echo "🚀 Happy coding!"

# Remove the setup script itself
rm -f setup-project.sh