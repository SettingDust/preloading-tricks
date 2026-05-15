# AGENTS.md

AI agent instructions for this repository.

## Scope

These rules apply to the whole repository.

## Project Facts

| Area | Value |
| --- | --- |
| Project | Preloading Tricks |
| Type | Minecraft mod/library for early loader hooks, instrumentation class transforms, and SPI callbacks |
| Build system | Gradle Kotlin DSL |
| Main build files | `settings.gradle.kts`, `build.gradle.kts`, `buildSrc/build.gradle.kts` |
| Root project name | `PreloadingTricks` |
| Java toolchain | 25 in CI and `buildSrc` |
| Main plugin model | `cloche` multi-loader targets through custom `buildSrc` convention plugins |
| Primary language | Java sources with Kotlin Gradle/buildSrc logic |

## Commands

| Task | Command |
| --- | --- |
| Full local build on Windows | `.\gradlew.bat build` |
| CI build command | `gradle build` |

Prefer focused Gradle tasks when they directly exercise the edited area. Respect `GRADLE_USER_HOME` when dependency/cache behavior matters.

## Source Map

| Path | Purpose |
| --- | --- |
| `src/api/` | Public API shared by consumers |
| `src/core/` | Common runtime implementation and shared resources |
| `src/shared/forgelike/` | Shared Forge-like code/resources |
| `src/shared/modlauncher/` | Shared ModLauncher integration |
| `src/shared/neoforge/` | Shared NeoForge integration |
| `src/platform/fabric/` | Fabric platform implementation/resources |
| `src/platform/forge/modlauncher/` | Forge ModLauncher platform implementation/resources |
| `src/platform/neoforge/modlauncher/` | NeoForge ModLauncher platform implementation/resources |
| `src/platform/neoforge/fancy-mod-loader/` | NeoForge FancyModLoader platform implementation/resources |
| `buildSrc/src/main/kotlin/` | Repository Gradle convention logic and dependency/version helpers |
| `.github/workflows/` | CI and publishing workflows |
| `build/`, `bin/`, `run/` | Generated/local outputs; do not treat as source of truth |

## Working Rules

- Read the existing source and build patterns before editing.
- Keep changes small and targeted; do not refactor unrelated code.
- Do not add documentation files unless explicitly requested by the user.
- Do not rely on generated output under `build/`, `bin/`, or `run/`.
- Do not discard or overwrite user changes.
- Do not run destructive git operations without explicit permission.
- Use Conventional Commits when asked to prepare a commit message.

## API and Runtime Rules

- Treat `src/api/` as public API. Check downstream usage in `src/core/`, `src/shared/`, and `src/platform/` before changing signatures.
- Keep SPI service files in `META-INF/services/` aligned with implementation package/class names.
- Keep mixin configs, class transform configs, and manifest metadata aligned with source packages.
- Loader-family shared code should stay loader-family neutral unless the change explicitly targets one platform.

## Platform Rules

| Platform area | Notes |
| --- | --- |
| Fabric | Keep fabric metadata, mixin configs, and class transform config resources aligned. |
| Forge ModLauncher | Use `settingdust.preloading_tricks.forge.modlauncher` naming for Forge ModLauncher resources/classes. |
| NeoForge ModLauncher | Keep separate from NeoForge FancyModLoader wiring. |
| NeoForge FancyModLoader | Keep separate from ModLauncher wiring. |
| Forge-like shared code | Avoid platform-specific assumptions unless required by the task. |

## Build Logic Rules

- Preserve existing cloche target names and source-set structure unless the task explicitly changes them.
- Dependency and packaging changes can affect embedded runtime jars; avoid changing scopes unless the packaging impact is understood.
- `buildSrc/src/main/kotlin/FinalJarDsl.kt` configures final ShadowJar outputs and publication variants.
- Keep resource transformers intact for merged jars.
- Class duplicate filtering is class-specific: `filesMatching("**/*.class") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }`.

## Verification

- Before claiming code/build changes are complete, run the smallest relevant Gradle verification.
- For build-script or packaging changes, prefer a Gradle task over syntax-only inspection.
- For AGENTS.md-only edits, verify by reading the file and checking referenced paths/commands exist.
