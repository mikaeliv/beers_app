import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
}

// Модуль сетевого слоя: Ktor клиент и API.

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.core)
            implementation(libs.ktor.contentNegotiation)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.logging)
            implementation(libs.kotlinSerialization)
        }
        androidMain.dependencies {
            implementation(libs.ktor.android)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.jvm)
        }
        iosMain.dependencies {
            implementation(libs.ktor.ios)
        }
        jsMain.dependencies {
            implementation(libs.ktor.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.clientMock)
        }
    }
}

android {
    namespace = "ru.mikaeliv.beers.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}
