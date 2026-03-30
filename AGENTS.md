# AGENTS.md

## Project overview
- **WebToApp** is an Android app that turns websites, media projects, frontend bundles, and some server-side projects into Android apps.
- Primary stack: **Kotlin**, **Jetpack Compose**, **Room**, **DataStore**, **Koin**, **OkHttp**, **WebView**, **GeckoView**, plus native code under `app/src/main/cpp`.
- Main module: `:app`.
- Main source root: `/home/runner/work/web-to-app/web-to-app/app/src/main`.

## Repository map
- `app/src/main/java/com/webtoapp/core`: build/export, runtime, networking, crypto, engine, and platform integration logic.
- `app/src/main/java/com/webtoapp/ui`: Compose UI, screens, components, theme, and navigation.
- `app/src/main/java/com/webtoapp/di`: dependency wiring.
- `app/src/main/java/com/webtoapp/data` and `app/src/main/java/com/webtoapp/ui/data`: persistence and model-related code. Follow existing package declarations instead of assuming directories should be reorganized.
- `app/src/main/assets`: templates, embedded resources, and sample projects. Treat these as product assets, not cleanup targets.
- `app/src/main/cpp`: native C/C++ code built via CMake.

## Working rules
- Make the **smallest practical change** that fully solves the task.
- Preserve existing architecture and package boundaries unless the task explicitly requires restructuring.
- Match existing Kotlin and Compose patterns instead of introducing new abstractions.
- Use `rg` for file discovery and targeted search before editing.
- Prefer focused edits over broad search-and-replace, especially under `assets/`, template files, and sample projects.

## High-risk areas
Be conservative when changing any of the following:
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/cpp/**`
- `app/src/main/java/com/webtoapp/core/apkbuilder/**`
- `app/src/main/java/com/webtoapp/core/crypto/**`
- `app/src/main/java/com/webtoapp/core/hardening/**`
- `app/src/main/java/com/webtoapp/core/webview/**`
- `app/src/main/java/com/webtoapp/core/engine/**`
- language/runtime support code such as `core/linux`, `core/nodejs`, `core/php`, `core/python`, `core/golang`, and `core/wordpress`

## Build and runtime invariants
- `app/build.gradle.kts` intentionally keeps **ABI splits disabled** because the app uses its own APK as a template when exporting apps.
- Release builds intentionally preserve required class names; do not casually change obfuscation, package naming, or shell/runtime class identity.
- `aaptOptions.ignoreAssetsPattern = ""` is intentional so dot-prefixed assets can be packaged.
- Native library packaging is intentional, including the GeckoView exclusions and `jniLibs.useLegacyPackaging = true`.
- Signing values come from `local.properties`. Never hardcode keystore paths, passwords, aliases, tokens, or other secrets in tracked files.

## Security rules
- Never commit secrets, signing material, API keys, tokens, or `local.properties`.
- Do not relax `network_security_config.xml` cleartext rules without an explicit requirement.
- Preserve loopback-only behavior and canonical-path checks in `core/webview/LocalHttpServer.kt`.
- Do not weaken crypto, integrity, anti-tamper, or sandbox/isolation logic when touching related files.
- Treat local HTTP serving, APK building/signing, and exported app templating as security-sensitive features.

## UI and content guidance
- Avoid hardcoding new user-visible strings in Kotlin when existing resources should be used.
- Respect the existing multilingual resource structure when editing text.
- Keep comments minimal and only add them when they explain non-obvious constraints.

## Validation
Before and after changes, prefer the smallest relevant existing validation commands:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`

If Gradle fails before task execution because of repository configuration or dependency/plugin resolution, report that separately as a pre-existing issue instead of masking it.

## Agent-specific notes
- When asked to update repository guidance, keep `AGENTS.md` as the canonical source of truth.
- Keep `CLAUDE.md` as a symlink to `AGENTS.md` so agent instructions do not drift.
- Keep `.github/copilot-instructions.md` shorter than this file and optimized for high-signal coding guidance.
