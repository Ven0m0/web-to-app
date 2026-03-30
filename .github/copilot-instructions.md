# GitHub Copilot instructions for WebToApp

## Repo summary
- This repository is a security-sensitive Android app built mainly with **Kotlin + Jetpack Compose** in the `:app` module.
- It includes native code, APK build/export logic, WebView/GeckoView runtime logic, local HTTP serving, and encrypted/local credential handling.
- Main source root: `/home/runner/work/web-to-app/web-to-app/app/src/main`.

## How to work in this repo
- Make **small, localized changes** that preserve existing behavior.
- Use `rg` for file discovery and narrow searches before editing.
- Prefer existing patterns and utilities over introducing new abstractions.
- Do not reorganize packages or sample assets unless the task explicitly requires it.

## Be careful in these areas
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/cpp/**`
- `app/src/main/java/com/webtoapp/core/apkbuilder/**`
- `app/src/main/java/com/webtoapp/core/crypto/**`
- `app/src/main/java/com/webtoapp/core/hardening/**`
- `app/src/main/java/com/webtoapp/core/webview/**`
- `app/src/main/java/com/webtoapp/core/engine/**`

## Invariants to preserve
- ABI splits are intentionally disabled.
- Release/shipping behavior depends on stable class and packaging behavior; avoid renaming or obfuscation-related changes unless explicitly requested.
- `aaptOptions.ignoreAssetsPattern = ""` is intentional for packaged dotfiles.
- Keep signing configuration externalized in `local.properties`.
- Do not weaken localhost-only networking or path traversal protections.

## Security and content rules
- Never commit secrets, keystore values, tokens, or `local.properties`.
- Do not broaden cleartext traffic beyond localhost-style addresses without explicit approval.
- Treat `app/src/main/assets/sample_projects/**` and template assets as product content, not cleanup targets.
- Use string/resources patterns for user-facing text when appropriate.

## Validation
Use the smallest relevant existing commands:
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`

If Gradle fails before tasks start because of existing plugin/dependency resolution issues, report that as a baseline repository problem.

For fuller repository guidance, see `AGENTS.md`.
