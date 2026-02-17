import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.androidLibrary)
}

// Модуль данных: SQLDelight схема, драйверы и репозиторий.

kotlin {
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
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies { implementation(libs.sqldelight.android) }
        jvmMain.dependencies { implementation(libs.sqldelight.jvm) }
        iosMain.dependencies { implementation(libs.sqldelight.ios) }
        // JS: временно без драйвера БД. Реализация DatabaseDriverFactory на JS бросает исключение.
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}

android {
    namespace = "ru.mikaeliv.beers.data"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}

sqldelight {
    databases {
        create("BeersDatabase") {
            packageName.set("ru.mikaeliv.beers.db")
            srcDirs.setFrom("src/commonMain/sqldelight")
            // SQLDelight сгенерирует типобезопасные API по *.sq файлам из каталога выше
        }
    }
}

