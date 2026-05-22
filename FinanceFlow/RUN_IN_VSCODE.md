# Run FinanceFlow In VS Code

This project is a native Android app built with Kotlin, Jetpack Compose, Gradle, Hilt, and Firebase.

## 1. Open The Correct Folder

Open this folder in VS Code:

```text
personal-finance-manager-app/FinanceFlow
```

Do not open only the `app` folder, because Gradle files live at the `FinanceFlow` root.

## 2. Install Recommended Extensions

When VS Code opens the project, it should suggest the extensions from `.vscode/extensions.json`.

Install these:

- Extension Pack for Java
- Kotlin
- Gradle for Java
- XML
- Android iOS Emulator

## 3. Android SDK Requirement

VS Code can edit the app, but Android builds still require Android SDK tools.

Smallest setup without full Android Studio:

- Android command line tools
- `platform-tools`
- `platforms;android-35`
- `build-tools;35.0.0`

After the SDK exists, create `local.properties` in this folder:

```properties
sdk.dir=C\:\\Users\\Sadumina Rathnayaka\\AppData\\Local\\Android\\Sdk
```

You can copy `local.properties.example` and adjust the path if needed.

## 4. Build From VS Code

Use `Terminal > Run Task` and choose:

```text
Android: Build debug APK
```

Or run:

```powershell
.\gradlew.bat assembleDebug
```

The APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 5. Install On Phone Or Emulator

Connect a phone with USB debugging enabled, then run:

```powershell
.\gradlew.bat installDebug
```

In VS Code you can also use:

```text
Terminal > Run Task > Android: Install debug APK
```

## 6. If Gradle Fails

This project currently uses Android Gradle Plugin `8.7.0`. If Gradle reports a compatibility error, change `gradle/wrapper/gradle-wrapper.properties` to use a Gradle 8.x version supported by AGP 8.7, then rebuild.
