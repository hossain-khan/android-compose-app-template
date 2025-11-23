# Project Overview

This is a template for an Android app using Jetpack Compose, designed to help you quickly set up a new project with best practices in mind. It includes features like dependency injection, Circuit UDF (Unidirectional Data Flow) architecture, and optional WorkManager integration.

The template is pre-configured with Circuit, a Compose-driven architecture for Kotlin and Android applications that provides a clean, unidirectional data flow pattern for building robust Android apps.

## Project Structure

```
android-compose-app-template/
├── app/
│   └── src/
│       └── main/java/app/example/
│           ├── CircuitApp.kt           # Main Application class
│           ├── MainActivity.kt         # Main Activity with Circuit
│           ├── circuit/                # Circuit screens and presenters
│           │   ├── ExampleInboxScreen.kt
│           │   ├── ExampleEmailDetailsScreen.kt
│           │   └── overlay/            # Circuit overlays
│           ├── data/                   # Repositories and data sources
│           ├── di/                     # Metro dependency injection
│           ├── work/                   # WorkManager workers
│           └── ui/theme/               # Compose theme configuration
└── gradle/
    └── libs.versions.toml              # Centralized dependency versions
```

## Architecture Patterns

### Circuit UDF (Unidirectional Data Flow)

1. **Use `@CircuitInject` annotation** for screens and presenters
2. **Screens** are composable functions that render UI
3. **Presenters** handle business logic and state management
4. **Events flow up, state flows down**

Example:
```kotlin
@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomePresenter(): HomeScreen.State {
    // Presenter logic - handle state and events
}

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomeContent(state: HomeScreen.State, modifier: Modifier = Modifier) {
    // UI composition - display state and emit events
}
```

### Metro Dependency Injection

1. **Use `@ContributesBinding`** for interface implementations
2. **Use `@Inject` constructor injection** for dependencies
3. **Scopes**: `@ApplicationContext`, `@ActivityKey`, `@WorkerKey`
4. **Multibindings**: Use for activity and worker factories

Example:
```kotlin
// Define interface
interface EmailRepository {
    fun getEmails(): List<Email>
}

// Implementation with Metro DI
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class EmailRepositoryImpl constructor() : EmailRepository {
    override fun getEmails() = listOf(/* emails */)
}
```

## Code Style

### Kotlin Guidelines

- **Follow [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)**
- **Formatting**: Enforced by Kotlinter plugin (ktlint)
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Functions/Properties: `camelCase`
  - Constants: `SCREAMING_SNAKE_CASE`
  - Composables: `PascalCase` (like classes)

### Material 3 / Material You Guidelines

**All UI components MUST be Material 3 compatible:**

1. **Use Material 3 Components**:
   - Use `androidx.compose.material3.*` (NOT `material` or `material2`)
   - Components: `Button`, `Card`, `TextField`, `TopAppBar`, `ListItem`, etc.

2. **Theme-Aware Colors**:
   - **NEVER use hardcoded colors** (e.g., `Color(0xFF4CAF50)`, `Color.Red`)
   - Always use `MaterialTheme.colorScheme.*`:
     - `primary`, `onPrimary` - Main brand colors
     - `primaryContainer`, `onPrimaryContainer` - Filled components
     - `secondary`, `tertiary` - Accent colors
     - `surface`, `onSurface` - Backgrounds
     - `error`, `onError` - Error states

3. **Typography**:
   - Use `MaterialTheme.typography.*` for all text
   - Available: `displayLarge`, `headlineMedium`, `titleLarge`, `bodyMedium`, `labelSmall`, etc.

**Example - Correct**:
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    Text(
        text = "Hello",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
```

**Example - Incorrect** ❌:
```kotlin
Card(colors = CardDefaults.cardColors(containerColor = Color.Blue)) {
    Text(text = "Hello", color = Color.White)
}
```

## Development Workflow

### Before Committing

**ALWAYS run these commands before committing:**

```bash
# 1. Format Kotlin code (auto-fixes style issues)
./gradlew formatKotlin

# 2. Build to ensure no compilation errors
./gradlew assembleDebug
```

### Common Gradle Tasks

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Check code formatting (doesn't modify files)
./gradlew lintKotlin

# Run specific module commands
./gradlew :app:formatKotlin
./gradlew :app:assembleDebug
```

## Testing Guidelines

- **Unit Tests**: Required for repositories and business logic
- **Test Coverage**: Aim for success cases, error cases, and edge cases
- **Coroutine Testing**: Use `kotlinx-coroutines-test` with `runTest`
- **Circuit Testing**: Use `circuit-test` library with `FakeNavigator`

## Dependencies Management

All dependency versions are centralized in `gradle/libs.versions.toml`:

**Major Dependencies**:
- Kotlin: 2.2.21
- Circuit: 0.31.0
- Metro: 0.7.7
- Compose BOM: 2025.11.01
- WorkManager: 2.11.0

## Common Patterns

### Adding a New Circuit Screen

1. Create a `Screen` data class that implements `Screen` interface
2. Create a `@CircuitInject` presenter function
3. Create a `@CircuitInject` composable UI function
4. Navigate using `Navigator.goTo(screen)`

### Adding a WorkManager Worker

1. Use `@AssistedInject` for constructor injection
2. Add `@WorkerKey` annotation
3. Implement `CoroutineWorker` or `Worker`
4. Schedule work using `WorkManager`

## Resources

- [Circuit Documentation](https://slackhq.github.io/circuit/)
- [Metro Documentation](https://zacsweers.github.io/metro/)
- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 Design System](https://m3.material.io/)
- [Material 3 Compose Components](https://developer.android.com/jetpack/compose/designsystems/material3)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

## Notes for AI Assistants

- Always suggest running `formatKotlin` before commits
- Follow Material 3 design system strictly
- Use theme colors, never hardcode colors
- Prefer constructor injection over field injection
- Follow existing code structure and patterns
- Keep code concise and readable
