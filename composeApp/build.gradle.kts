import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

// Основной модуль приложения (Android, Desktop, JS). Здесь собирается UI и подключаются фичи.

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    // iOS frameworks require Xcode toolchain (xcrun). To keep `./gradlew build` working on machines
    // without Xcode installed, iOS targets are opt-in via `-PenableIos=true`.
    val enableIos = (findProperty("enableIos") as String?)?.toBoolean() ?: false
    if (enableIos) {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        }
    } else {
        logger.warn("iOS targets are disabled for :composeApp. Enable with -PenableIos=true (requires Xcode command line tools).")
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    // Временное отключение wasmJs: SQLDelight пока нестабилен для wasm-драйверов
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.data)
            implementation(projects.network)
            implementation(projects.composeDS)
            implementation(projects.featureAuth)
            implementation(projects.featureProfile)
            implementation(projects.featureList)
            implementation(projects.featureDetail)
            implementation(projects.featureAdd)
            implementation(projects.featureSettings)
            implementation(projects.featureCamera)
            implementation(projects.core)
            implementation(libs.decompose.core)
            implementation(libs.decompose.extensions)
            implementation(libs.kotlinSerialization)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "ru.mikaeliv.beers"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.mikaeliv.beers"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "ru.mikaeliv.beers.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ru.mikaeliv.beers"
            packageVersion = "1.0.0"
        }
    }
}
