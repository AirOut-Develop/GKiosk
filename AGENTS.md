# Repository Guidelines

## Project Structure & Module Organization
- Root Gradle: `build.gradle`, `settings.gradle`, `gradle.properties`.
- App module: `app/`
  - Code: `app/src/main/java/com/jwlryk/gkiosk/`
  - Resources: `app/src/main/res/`
  - Manifest: `app/src/main/AndroidManifest.xml`
  - Unit tests: `app/src/test/java/`
  - Instrumented tests: `app/src/androidTest/java/`
  - ProGuard: `app/proguard-rules.pro`

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` — compiles and packages debug.
- Install on device: `./gradlew installDebug` — installs the debug APK.
- Unit tests: `./gradlew testDebugUnitTest` — runs JVM tests.
- Instrumented tests: `./gradlew connectedDebugAndroidTest` — runs on a device/emulator.
- Lint: `./gradlew lint` — Android lint; reports in `app/build/reports/`.
- Open project in Android Studio for interactive run/debug.

## Coding Style & Naming Conventions
- Language: Java 8 (`sourceCompatibility 1.8`). Indent 4 spaces; K&R braces.
- Packages: `com.jwlryk.gkiosk`. Classes: UpperCamelCase. Methods/fields: lowerCamelCase.
- Resources: snake_case. Layouts `activity_*.xml` / `fragment_*.xml`; drawables `ic_*`; IDs `view_*`.
- Use AndroidX and Material; avoid deprecated APIs. Keep activities/fragments lean; extract logic.

## Testing Guidelines
- Frameworks: JUnit4 (unit), AndroidX Test + Espresso (instrumented).
- Place tests mirroring package structure under `app/src/test/java` and `app/src/androidTest/java`.
- Naming: `*Test.java`; methods `should...` describing behavior.
- Aim for coverage on presenters/view-model–like logic; prefer fakes over Android types.

## Commit & Pull Request Guidelines
- Commits: Conventional Commits (`feat:`, `fix:`, `chore:`, `test:`, `docs:`). Keep subjects concise and imperative.
- PRs: include summary, rationale, and screenshots/GIFs for UI. Link issues (e.g., `Closes #123`). Ensure build, tests, and lint pass; remove debug logs; follow resource naming.

## Branching, Versioning & Tags
- Branches: `DEVELOP/YYMMDD_NN_Title`, `RELEASE/YYMMDD_V0.01_Title`. Main is `main` (PR-only).
- Helper: `scripts/new-branch.sh dev|release <Title>`.
- Release tags match branch pattern, annotated, e.g.:
  - `git tag -a 'RELEASE/250903_V0.01_FirstDrop' -m 'Release 0.01 (2025-09-03): FirstDrop'`
  - `git push origin 'RELEASE/250903_V0.01_FirstDrop'`

## Security & Configuration
- Do not commit secrets/keystores. Use Gradle properties/env vars.
- SDK/NDK paths in untracked `local.properties`. Min SDK 27; compile/target SDK 33.
- Environment: JDK 17, Android Gradle Plugin 8.0.2.

## Agent-Specific Notes
- Colors: define in `values/colors.xml` with `primary_*`; override in `values-night/`.
- Theme: `Theme.MaterialComponents.DayNight.NoActionBar`; update via existing keys only.
- Navigation: launcher `Init_Splash` → delayed → `Init_Category`.
- Toggle theme: `Global.setThemeMode(...)` → `Global.applyNightMode()` → `activity.recreate()`.
