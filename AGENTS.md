# Repository Guidelines

## Project Structure & Modules
- Root Gradle: `build.gradle`, `settings.gradle`, `gradle.properties`.
- App module: `app/`
  - Code: `app/src/main/java/com/jwlryk/gkiosk/`
  - Resources: `app/src/main/res/`
  - Manifest: `app/src/main/AndroidManifest.xml`
  - Unit tests: `app/src/test/java/`
  - Instrumented tests: `app/src/androidTest/java/`
  - ProGuard: `app/proguard-rules.pro`

## Build, Test, Run
- Build debug APK: `./gradlew assembleDebug` — compiles and packages debug.
- Install on device: `./gradlew installDebug` — installs the debug APK.
- Unit tests: `./gradlew testDebugUnitTest` — runs JVM tests.
- Instrumented tests: `./gradlew connectedDebugAndroidTest` — runs on a connected device/emulator.
- Lint: `./gradlew lint` — Android lint checks, reports in `app/build/reports/`.
- Open in Android Studio to run/edit interactively.

## Coding Style & Naming
- Language: Java 8 (`sourceCompatibility 1.8`). Indent 4 spaces; K&R braces.
- Packages: `com.jwlryk.gkiosk`.
- Classes: UpperCamelCase (e.g., `KioskController`). Methods/fields: lowerCamelCase.
- Resources: snake_case.
  - Layouts: `activity_*.xml`, `fragment_*.xml` (e.g., `activity_main.xml`).
  - Drawables: descriptive (e.g., `ic_kiosk_logo`). IDs: `view_*` (e.g., `view_submit`).
- Keep activities/fragments lean; extract logic into plain classes.
- Use AndroidX APIs; avoid deprecated components.

## Testing Guidelines
- Frameworks: JUnit4 for unit tests; AndroidX Test + Espresso for instrumented.
- Place unit tests mirroring package structure under `app/src/test/java`.
- Name tests `*Test.java` and methods `should...` for behavior.
- Aim for coverage on presenters/view-model–like logic; use fakes over real Android types in unit tests.

## Commits & Pull Requests
- Commits: concise, imperative subject (e.g., "Add kiosk home screen"). Prefer Conventional Commits: `feat:`, `fix:`, `chore:`, `test:`, `docs:`.
- PRs: include summary, rationale, and screenshots/GIFs for UI.
  - Link issues (e.g., `Closes #123`).
  - Checklist: builds green, tests/lint pass, no debug logs, resources named per convention.

## Security & Configuration
- Do not commit secrets or keystores. Use Gradle properties/env vars.
- SDK/NDK paths live in `local.properties` (untracked). Min SDK 27, target/compile SDK 33.
