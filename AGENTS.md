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

## Branching & Versioning
- Development branches: `DEVELOP/YYMMDD_NN_Title` (e.g., `DEVELOP/250903_00_UIBaseLayout`).
- Release branches: `RELEASE/YYMMDD_V0.01_Title` (e.g., `RELEASE/250903_V0.01_FirstDrop`).
- Main branch: `main` (protected); merge via PR only.
- Tip: Use the helper `scripts/new-branch.sh dev|release <Title>` to scaffold names.

## Tagging
- Release tags: `RELEASE/YYMMDD_V0.01_Title` (same pattern as release branches).
  - Example: `RELEASE/250903_V0.01_FirstDrop`
- Create annotated tag:
  - `git tag -a 'RELEASE/250903_V0.01_FirstDrop' -m 'Release 0.01 (2025-09-03): FirstDrop'`
  - Push: `git push origin 'RELEASE/250903_V0.01_FirstDrop'`
- Notes:
  - Use annotated tags (-a) with a clear message (scope, changes, SHA).
  - Pre-push hook validates RELEASE/* tag names; other tags are allowed.

## Security & Configuration
- Do not commit secrets or keystores. Use Gradle properties/env vars.
- SDK/NDK paths live in `local.properties` (untracked). Min SDK 27, target/compile SDK 33.

## Agent-Specific Instructions
- Environment: Use JDK 17 and Android Gradle Plugin 8.0.2 with `compileSdk 33`.
- File edits: Prefer `apply_patch`-style minimal diffs; avoid unrelated changes.
- Colors: Define palettes in `values/colors.xml`; expose theme-aware aliases via `values-night/` overrides. Use `primary_*` for brand.
- Themes: Base on `Theme.MaterialComponents.DayNight.NoActionBar`; no title/action bar. Update `themes.xml` only via existing keys.
- Activities: Place under `com.jwlryk.gkiosk`; register in `AndroidManifest.xml`. Launcher is `Init_Splash` → delayed → `Init_Category`.
- Dependencies: Keep SDK 33–compatible libs (`appcompat 1.6.1`, `material 1.9.0`); align Kotlin via BOM if needed.
- Git: Commit focused changes with Conventional Commits; keep `.gitattributes` line endings intact.

## Using This Guide
- Ask changes clearly, e.g.:
  - "Add activity Foo and navigate from Bar after 2s."
  - "Add color palette X and wire it to primary_* keys."
  - "Update theme to use new colors and remove old references."
- Verify locally:
  - Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`).
  - Install: `./gradlew installDebug`.
  - Tests/Lint: `./gradlew testDebugUnitTest lint`.
- Toggle theme at runtime: set via `Global.setThemeMode(...)` → `Global.applyNightMode()` → `activity.recreate()`.
