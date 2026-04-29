plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js { browser() }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.data)
            implementation(projects.composeDS)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.decompose.core)
            implementation(libs.decompose.extensions)
        }
    }
}

android {
    namespace = "ru.mikaeliv.beers.feature.list"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}
