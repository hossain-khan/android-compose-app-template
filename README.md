# Android - Circuit App Template
An Android App template that is preconfigured with ⚡️ Circuit UDF architecture.

> [!TIP]
> Google also has an official architectural template available for starter apps.
> https://github.com/android/architecture-templates

## What do you get in this template? 📜
* ✔️ [Circuit](https://github.com/slackhq/circuit) library setup for the app
* ✔️ [Metro](https://zacsweers.github.io/metro/) Dependency Injection for all Circuit Screens & Presenter combo
* ✔️ GitHub Actions for CI
* ✔️ [Google font](https://github.com/hossain-khan/android-compose-app-template/blob/main/app/src/main/java/app/example/ui/theme/Type.kt#L9-L14) for choosing different app font.
* ✔️ `BuildConfig` turned on with example of reading config from `local.properties` file.
* ✔️ [Kotlin formatter](https://github.com/jeremymailen/kotlinter-gradle) plugin for code formatting and linting
* ✔️ [Work Manager](https://developer.android.com/develop/background-work/background-tasks/persistent) for scheduling background tasks

> [!WARNING]  
> _This template is only for Android app setup. If you are looking for a multi-platform supported template,_
> _look at the official [Circuit](https://github.com/slackhq/circuit) example apps included in the project repository._

### Post-process after cloning 🧑‍🏭

You have **two options** for customizing this template:

<details>
<summary>Option 1: Automated Customization (Recommended)</summary>

#### Option 1: Automated Customization (Recommended) 🤖
Run the setup script to automatically handle most of the configuration:

```bash
# Basic usage - keeps examples and WorkManager by default
./setup-project.sh com.mycompany.appname MyAppName

# Remove examples if you don't need them
./setup-project.sh com.mycompany.appname MyAppName --remove-examples

# Remove WorkManager if you don't need background tasks
./setup-project.sh com.mycompany.appname MyAppName --remove-workmanager --remove-examples

# Keep the script for debugging (useful during development)
./setup-project.sh com.mycompany.appname MyAppName --keep-script

# Flags can be positioned flexibly - all of these work the same:
./setup-project.sh --remove-examples com.mycompany.appname MyAppName --remove-workmanager
./setup-project.sh com.mycompany.appname --remove-examples MyAppName --remove-workmanager
./setup-project.sh com.mycompany.appname MyAppName --remove-examples --remove-workmanager --keep-script
```

**What the script does automatically:**
- Renames package from `app.example` to your preferred package name
- Preserves subdirectory structure (`ui/theme/`, `di/`, `circuit/`, `work/`, `data/`)
- Updates app name and package ID in XML and Gradle files
- Renames `CircuitApp` to `YourAppNameApp`
- Keeps Example* files by default (use `--remove-examples` to exclude)
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

</details>


## Demo 📹
Here is a demo of the template app containing screens shown in the 📖 [circuit tutorial](https://slackhq.github.io/circuit/tutorial/) doc.

https://github.com/user-attachments/assets/56d6f28b-5b46-4aac-a30e-80116986589e
