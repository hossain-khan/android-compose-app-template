# Project Overview

This is a template for an Android app using Jetpack Compose, designed to help you quickly set up a new project with best practices in mind. It includes features like dependency injection, circuit architecture, and optional WorkManager integration.

## Folder Structure

- `app/`: Contains the main application code.
- `build.gradle`: Contains the Gradle build scripts.
- `app/build.gradle`: Contains the app-specific Gradle build scripts.

## Libraries and Frameworks

- [Circuit](https://github.com/slackhq/circuit) for the app's UDF architecture.
- [Metro](https://github.com/ZacSweers/metro) for dependency injection

## Coding Standards

- Always use the `./gradlew formatKotlin` command to format your Kotlin code before committing.
- Run `./gradlew assembleDebug` to ensure your code compiles without errors.

## UI guidelines

- Use material 3 design components for compose
- Use material design best practices for UX and UI
