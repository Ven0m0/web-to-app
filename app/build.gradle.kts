import org.gradle.api.tasks.Exec
import java.net.URL
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Read signing config from local.properties (not committed to VCS)
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {

    signingConfigs {
        create("shiaho") {
            storeFile = file(localProperties.getProperty("signing.storeFile", ""))
            storePassword = localProperties.getProperty("signing.storePassword", "")
            keyAlias = localProperties.getProperty("signing.keyAlias", "")
            keyPassword = localProperties.getProperty("signing.keyPassword", "")
        }
    }
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 32
        versionName = "1.9.5"

        vectorDrawables {
            useSupportLibrary = true
        }
        
        // NDK 配置
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        
        // CMake 配置
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    // 外部 Native 构建配置
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            // 启用 R8 tree-shaking（移除未使用代码）+ 资源压缩
            // ProGuard 规则中已设置 -dontobfuscate，确保类名不被混淆
            // （WebToApp 使用自身 APK 作为模板，类名必须保持不变）
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("shiaho")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // 禁用 ABI splits，生成单一完整 APK
    // 这对于 WebToApp 是必要的，因为应用需要使用自身 APK 作为模板
    splits {
        abi {
            isEnable = false
        }
    }
    
    // 允许以 "." 开头的 assets 目录被打包（如 .pypackages）
    // 默认 aapt 会忽略 dot-prefixed 文件，但预打包的 Python 依赖需要它
    androidResources {
        ignoreAssetsPattern = ""
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // 确保 native 库不被压缩，否则安装会失败
        jniLibs {
            useLegacyPackaging = true
            // 排除 GeckoView 原生库 — 这些 .so 文件由用户按需下载，不内置到主 APK
            excludes += "**/libxul.so"
            excludes += "**/libmozglue.so"
            excludes += "**/libgeckoffi.so"
            excludes += "**/libmozavutil.so"
            excludes += "**/libmozavcodec.so"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    
    // Material Design 3
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("dev.chrisbanes.haze:haze:1.7.2")

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Room Database
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.13.2")

    // OkHttp for networking
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    
    // Koin for dependency injection
    implementation("io.insert-koin:koin-android:4.2.0")
    implementation("io.insert-koin:koin-androidx-compose:4.2.0")

    // WebKit for advanced WebView features
    implementation("androidx.webkit:webkit:1.15.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    
    // Security - EncryptedSharedPreferences for secure token storage
    implementation("androidx.security:security-crypto:1.1.0")
    
    // Apache Commons Compress for tar.gz/xz extraction (Linux environment)
    implementation("org.apache.commons:commons-compress:1.28.0")
    implementation("org.tukaani:xz:1.12")
    
    // APK 签名库（支持 v1/v2/v3 签名）
    implementation("com.android.tools.build:apksig:9.1.0")
    
    // GeckoView (Firefox 内核) — Java/Kotlin API 编译进 dex，原生 .so 排除（按需下载）
    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:149.0.20260318190823")
    
    // ZXing 二维码生成和扫描
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Vico 图表库（高级数据看板）
    implementation("com.patrykandpatrick.vico:compose-m3:3.1.0-alpha.3")
    
    // Google Play Billing (Pro/Ultra 订阅)
    implementation("com.android.billingclient:billing-ktx:8.3.0")
    
    // Google Sign-In (Credential Manager + Web OAuth fallback)
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    implementation("androidx.browser:browser:1.10.0") // Chrome Custom Tab for OAuth fallback
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.test:core:1.7.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

// ==================== PHP Binary Setup ====================
// Android 15+ enforces SELinux execute_no_trans denial for binaries in app data dir.
// Bundling PHP as a "native library" in jniLibs/ ensures it's extracted to nativeLibraryDir
// which has apk_data_file SELinux context (execute_no_trans allowed).
//
// Run: ./gradlew downloadPhpBinary
// This only needs to run once (the binary is gitignored).

val phpVersion = "8.4"
val phpArchiveUrl = "https://github.com/pmmp/PHP-Binaries/releases/download/pm5-php-${phpVersion}-latest/PHP-${phpVersion}-Android-arm64-PM5.tar.gz"
val phpTempDir = layout.buildDirectory.dir("tmp/php-download")
val phpArchiveFile = phpTempDir.map { it.file("php.tar.gz").asFile }
val phpOutputFile = layout.projectDirectory.file("src/main/jniLibs/arm64-v8a/libphp.so").asFile

val downloadPhpBinaryArchive = tasks.register("downloadPhpBinaryArchive") {
    description = "Downloads the PHP binary archive for Android arm64"
    group = "setup"

    outputs.file(phpArchiveFile)
    onlyIf { !phpOutputFile.exists() }

    doLast {
        val tempDir = phpTempDir.get().asFile
        val archiveFile = phpArchiveFile.get()

        tempDir.mkdirs()

        println("Downloading PHP $phpVersion for Android arm64...")
        URL(phpArchiveUrl).openStream().use { input ->
            archiveFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

val extractPhpBinaryArchive = tasks.register<Exec>("extractPhpBinaryArchive") {
    description = "Extracts the downloaded PHP binary archive"
    group = "setup"

    dependsOn(downloadPhpBinaryArchive)
    inputs.file(phpArchiveFile)
    outputs.dir(phpTempDir)
    onlyIf { !phpOutputFile.exists() }

    doFirst {
        phpTempDir.get().asFile.mkdirs()
    }

    commandLine(
        "tar",
        "-xzf",
        phpArchiveFile.get().absolutePath,
        "-C",
        phpTempDir.get().asFile.absolutePath
    )
}

tasks.register("downloadPhpBinary") {
    description = "Downloads pre-built PHP binary for Android arm64 and bundles it as native library"
    group = "setup"

    dependsOn(extractPhpBinaryArchive)
    outputs.file(phpOutputFile)
    onlyIf { !phpOutputFile.exists() }

    doLast {
        val tempDir = phpTempDir.get().asFile
        phpOutputFile.parentFile.mkdirs()

        println("Installing PHP binary...")

        // pmmp tar.gz structure: bin/php or just php
        val extracted = File(tempDir, "bin/php").takeIf { it.isFile }
            ?: tempDir.walkTopDown().firstOrNull { it.name == "php" && it.isFile }
            ?: throw GradleException("PHP binary not found in archive")

        extracted.copyTo(phpOutputFile, overwrite = true)
        phpOutputFile.setExecutable(true)
        tempDir.deleteRecursively()

        println("PHP binary installed: ${phpOutputFile.relativeTo(rootDir)}")
    }
}
