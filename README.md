# Android - Circuit App Template
An Android App template that is preconfigured with ⚡️ Circuit UDF architecture.

> [!TIP]
> Google also has an official architectural template available for starter apps.
> https://github.com/android/architecture-templates

## What do you get in this template? 📜
* ✔️ [Circuit](https://github.com/slackhq/circuit) library setup for the app
* ✔️ [Metro](https://zacsweers.github.io/metro/) Dependency Injection for all Circuit Screens & Presenter combo
* ✔️ GitHub Actions for CI and automated release builds
* ✔️ Automated APK/AAB builds with keystore signing (see [RELEASE.md](RELEASE.md))
* ✔️ [Google font](https://github.com/hossain-khan/android-compose-app-template/blob/main/app/src/main/java/app/example/ui/theme/Type.kt#L9-L14) for choosing different app font.
* ✔️ `BuildConfig` turned on with example of reading config from `local.properties` file.
* ✔️ [Kotlin formatter](https://github.com/jeremymailen/kotlinter-gradle) plugin for code formatting and linting
* ✔️ [Work Manager](https://developer.android.com/develop/background-work/background-tasks/persistent) for scheduling background tasks

> [!WARNING]
> _This template is only for Android app setup. If you are looking for a multi-platform supported template,_
> _look at the official [Circuit](https://github.com/slackhq/circuit) example apps included in the project repository._

### Post-process after cloning 🧑‍🏭

1. Checkout the cloned repo
2. Navigate to repo directory in your terminal

You have **two options** for customizing this template:

<details>
<summary>Option 1: Automated Customization (Recommended)</summary>

#### Option 1: Automated Customization (Recommended) 🤖
Run the setup script to automatically handle most of the configuration:

**Script Usage:**
```bash
./setup-project.sh <package-name> <AppName> [flags]
```

**Parameters:**
- `<package-name>` - Your app's package name in reverse domain notation (e.g., `com.mycompany.appname`)
- `<AppName>` - Your app's class name in **PascalCase** (e.g., `TodoApp`, `NewsApp`, `MyPhotos`)
  - Used to rename `CircuitApp` → `{AppName}App`
  - Becomes your main Application class name
  - Sets app display name in `strings.xml`
  - Used in git commit messages

**Examples:**
```bash
# Basic usage - keeps examples and WorkManager by default
./setup-project.sh com.mycompany.appname MyAppName

# Remove WorkManager if you don't need background tasks
./setup-project.sh com.mycompany.appname MyAppName --remove-workmanager

# Keep the script for debugging (useful during development)
./setup-project.sh com.mycompany.appname MyAppName --keep-script
```

**What the script does automatically:**
- Renames package from `app.example` to your preferred package name
- Preserves subdirectory structure (`ui/theme/`, `di/`, `circuit/`, `work/`, `data/`)
- Updates app name and package ID in XML and Gradle files
- Renames `CircuitApp` to `YourAppNameApp`
- Keeps WorkManager files by default (use `--remove-workmanager` to exclude)
- Creates a fresh git repository with descriptive initial commit
- Removes template-specific files

</details>

<details>
<summary>Option 2: Manual Customization 🔧</summary>

#### Option 2: Manual Customization 🔧
If you prefer manual control, complete these tasks:

* [ ] Rename the package from **`app.example`** to your preferred app package name.
* [ ] Update directory structure based on package name update
* [ ] Update app name and package id in XML and Gradle
* [ ] Rename `CircuitApp***` to preferred file names
* [ ] Remove `Example***` files that were added to showcase example usage of app and Circuit.
* [ ] Remove WorkManager and Worker example files if you are not using them.

</details>

<details>
<summary>Additional Manual Steps (Both Options) 📝</summary>

#### Additional Manual Steps (Both Options) 📝
These still need to be done manually after using the script:

* [ ] Update `.editorconfig` based on your project preference
* [ ] Update your app theme colors (_use [Theme Builder](https://material-foundation.github.io/material-theme-builder/)_)
* [ ] Generate your app icon (_use [Icon Kitchen](https://icon.kitchen/)_)
* [ ] Update/remove repository license
* [ ] Configure [renovate](https://github.com/apps/renovate) for dependency management or remove [`renovate.json`](https://github.com/hossain-khan/android-compose-app-template/blob/main/renovate.json) file
* [ ] Choose [Google font](https://github.com/hossain-khan/android-compose-app-template/blob/main/app/src/main/java/app/example/ui/theme/Type.kt#L16-L30) for your app, or remove it.
* [ ] Verify Android Gradle Plugin (AGP) version compatibility with your development environment in `gradle/libs.versions.toml`
* [ ] **(Optional)** Set up production keystore for release builds - see [RELEASE.md](RELEASE.md) for automated APK signing

</details>


## Demo 📹
Here is a demo of the template app containing screens shown in the 📖 [circuit tutorial](https://slackhq.github.io/circuit/tutorial/) documentation.

The demo showcases the basic Circuit architecture pattern with screen navigation and state management.

https://github.com/user-attachments/assets/56d6f28b-5b46-4aac-a30e-80116986589e


### Templated Apps
Here are some apps that has been created using the template.

| 📱 App | Repo URL | 
| ------ | ------- |
| <img alt="google-play" src="https://github.com/user-attachments/assets/18725aa7-ea0b-4d6d-962a-e0358703041c" height="14"> Weather Alert | https://github.com/hossain-khan/android-weather-alert |
| <img alt="google-play" src="https://github.com/user-attachments/assets/18725aa7-ea0b-4d6d-962a-e0358703041c" height="14"> Remote Notify | https://github.com/hossain-khan/android-remote-notify |
| <img alt="google-play" src="https://github.com/user-attachments/assets/18725aa7-ea0b-4d6d-962a-e0358703041c" height="14"> TRMNL Display | https://github.com/usetrmnl/trmnl-android |
| <img alt="google-play" src="https://github.com/user-attachments/assets/18725aa7-ea0b-4d6d-962a-e0358703041c" height="14"> TRMNL Buddy | https://github.com/hossain-khan/trmnl-android-buddy | 

## 📓 Additional References

<details>
    <summary>Metro Usage</summary>


## Metro Dependency Injection 🔧

This template uses [Metro](https://zacsweers.github.io/metro/latest/) (v0.7.2) - a modern, multiplatform Kotlin dependency injection framework. 

> **What is Dependency Injection?** DI is a design pattern that provides objects (dependencies) to a class rather than having the class create them itself. This makes code more testable, maintainable, and modular.

Metro combines the best features of:
- **Dagger**: Lean, efficient generated code with compile-time validation
- **kotlin-inject**: Simple, Kotlin-first API design
- **Anvil**: Powerful aggregation and contribution system

### Key Metro Features Used

The template demonstrates several Metro patterns:

- **[Dependency Graphs](https://zacsweers.github.io/metro/latest/dependency-graphs/)**: `AppGraph` is the root DI component scoped to the application lifecycle
- **[Constructor Injection](https://zacsweers.github.io/metro/latest/injection-types/#constructor-injection)**: Activities and other classes use `@Inject` for constructor-based DI
- **[Aggregation](https://zacsweers.github.io/metro/latest/aggregation/)**: `@ContributesTo` automatically contributes bindings to the graph without explicit wiring
- **[Multibindings](https://zacsweers.github.io/metro/latest/bindings/#multibindings)**: Activity and Worker factories use map multibindings for flexible injection
- **[Assisted Injection](https://zacsweers.github.io/metro/latest/injection-types/#assisted-injection)**: Workers mix runtime parameters with injected dependencies
- **[Scopes](https://zacsweers.github.io/metro/latest/scopes/)**: `@SingleIn(AppScope::class)` ensures singleton instances

### Metro Implementation Examples

Below are simplified examples from the template. See the actual implementation files for complete details.

```kotlin
// AppGraph - Root dependency graph
@DependencyGraph(scope = AppScope::class)
@SingleIn(AppScope::class)
interface AppGraph {
    val circuit: Circuit
    val workManager: WorkManager
    // ... other graph accessors
    
    @DependencyGraph.Factory
    interface Factory {
        fun create(@ApplicationContext @Provides context: Context): AppGraph
    }
}

// Activity with constructor injection
@ActivityKey(MainActivity::class)
@ContributesIntoMap(AppScope::class, binding = binding<Activity>())
@Inject
class MainActivity(
    private val circuit: Circuit,
) : ComponentActivity() {
    // ... activity implementation
}

// Worker with assisted injection (requires additional annotations for multibinding)
@AssistedInject
class SampleWorker(
    context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
    // ... worker implementation
    
    // Note: Requires @WorkerKey and @AssistedFactory annotations
    // See app/src/main/java/app/example/work/SampleWorker.kt for complete example
}
```

For complete Metro documentation and advanced features, see the [official Metro documentation](https://zacsweers.github.io/metro/latest/).

</details>
