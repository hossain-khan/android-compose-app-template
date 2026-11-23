# Project Overview

Android app template using Jetpack Compose, Circuit UDF (Unidirectional Data Flow), Metro DI, and optional WorkManager.

## Project Structure

```
android-compose-app-template/
├── app/src/main/java/dev/hossain/compose/example/
│   ├── CircuitApp.kt            # Application class (Metro graph + WorkManager config)
│   ├── MainActivity.kt          # Single-activity Circuit host
│   ├── circuit/                 # Circuit screens, presenters, and overlays
│   │   ├── ExampleComposeEmailScreen.kt
│   │   ├── ExampleEmailDetailsScreen.kt
│   │   ├── ExampleInboxScreen.kt
│   │   └── overlay/             # Circuit overlays (AppInfoOverlay.kt)
│   ├── data/                    # Models, network, preferences, repositories
│   ├── di/                      # Metro dependency graph and binding containers
│   ├── work/                    # WorkManager workers
│   └── ui/theme/                # Material 3 theme, colors, typography
├── gradle/libs.versions.toml    # Centralized dependency & plugin versions
└── setup-project.sh             # Template customization script
```

## Build & Toolchain

Dependency and Gradle versions live in `gradle/libs.versions.toml` and `gradle/wrapper/gradle-wrapper.properties`:
- **AGP 9.1+ built-in Kotlin**: Do NOT apply `kotlin-android` or `kotlin-kapt`. Requires Gradle `9.3.1+`. Use `kotlin.compilerOptions {}` (not `android.kotlinOptions {}`) and `android.sourceSets.kotlin {}` for custom source dirs.
- **Compiler strictness**: `allWarningsAsErrors.set(true)` is enabled in `app/build.gradle.kts` — any Kotlin warning fails the build.
- **Circuit + Metro codegen**: `enableCircuitCodegen.set(true)` is enabled in `app/build.gradle.kts` (uses Metro's compiler plugin; no KSP/KAPT needed).

## Architecture Patterns

### Circuit UDF
1. **Screens**: `@Serializable` + `@CircuitSerializable(AppScope::class)` types implementing `Screen` (with nested `@Stable`/`@Immutable` `State : CircuitUiState` and `Event : CircuitUiEvent`).
2. **Presenters**: `@CircuitInject(MyScreen::class, AppScope::class)` with `@Inject` (or `@AssistedInject` when injecting `Navigator` / `Screen`) on a `Presenter<State>` class (or `@Composable` presenter function). Events flow up, state flows down.
3. **UI**: `@CircuitInject(MyScreen::class, AppScope::class) @Composable fun MyContent(state: MyScreen.State, modifier: Modifier = Modifier)`.

```kotlin
@CircuitInject(HomeScreen::class, AppScope::class)
@Inject
class HomePresenter : Presenter<HomeScreen.State> {
    @Composable
    override fun present(): HomeScreen.State = HomeScreen.State
}

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomeContent(state: HomeScreen.State, modifier: Modifier = Modifier) {
    // Render Material 3 UI from state
}
```

### Metro Dependency Injection
- **Interface bindings**: Use `@ContributesBinding(AppScope::class)` (`@Inject` is implicit since Metro 0.10.0). Implementation classes can be `internal` because `generateContributionProviders.set(true)` is enabled.
- **Concrete classes**: Use `@Inject` constructor injection for non-contributing classes (prefer constructor injection over field injection).
- **Binding modules**: Use `@ContributesTo(AppScope::class)` + `@BindingContainer` (`object` for `@Provides`-only, or `interface` with `companion object` when combining `@Provides` and `@Multibinds`).
- **`metrox-android`**: `AppGraph` extends `MetroAppComponentProviders` and `CircuitApp` implements `MetroApplication` so `MetroAppComponentFactory` constructor-injects `MainActivity` via `@ActivityKey`.
- **Qualifiers & map keys**: `@ApplicationContext` (`dev.zacsweers.metro.Qualifier`), `@ActivityKey`, and `@WorkerKey`.

### WorkManager Workers
1. Annotate the `CoroutineWorker` class with `@AssistedInject` and inject `@ApplicationContext context: Context` and `@Assisted params: WorkerParameters`.
2. Add a nested `@WorkerKey(MyWorker::class)`, `@ContributesIntoMap(AppScope::class, binding = binding<WorkerInstanceFactory<*>>())`, `@AssistedFactory` `Factory : WorkerInstanceFactory<MyWorker>`.
3. Enqueue work via `WorkManager`.

## UI & Code Style

- **Formatting**: Follow official Kotlin conventions; enforced by Kotlinter (`ktlint`).
- **Material 3**: Always use `androidx.compose.material3.*` (never Material 2). Never hardcode colors (e.g., `Color(0xFF...)`, `Color.Red`); always use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*`.

## Commands & Testing

**Always run before committing:**
```bash
./gradlew formatKotlin
./gradlew assembleDebug
```

**Verification tasks:**
```bash
./gradlew lintKotlin           # Check formatting without modifying files
./gradlew testDebugUnitTest    # Run JVM unit tests (coroutines-test + circuit-test FakeNavigator)
./gradlew koverVerifyRelease   # Verify minimum 50% coverage on release variant
```

- Write unit tests for repositories and business logic covering success, error, and edge cases.
- Use **Google Truth** (`Truth.assertThat(...)`) for all test assertions (avoid `org.junit.Assert.*`).
- **Prefer Fakes over Mocks**: create lightweight fake implementations (e.g., `FakeEmailRepository`, `FakeNavigator`) instead of using mocking frameworks.

## GitHub CLI (`gh`) Sandbox Workaround

If `gh` fails in an agent sandbox due to remote URL parsing (especially with SSH remotes):
```bash
zsh -f -c 'gh pr create --repo hossain-khan/android-compose-app-template ...'
```
