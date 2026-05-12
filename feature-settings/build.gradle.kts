plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val generatedAppVersionDir = layout.buildDirectory.dir("generated/source/appVersion/commonMain")
val generateAppVersionSource by tasks.registering {
    inputs.property("appVersion", providers.gradleProperty("app.version"))
    outputs.dir(generatedAppVersionDir)

    doLast {
        val escapedVersion = inputs.properties["appVersion"].toString()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val packageDir = outputs.files.singleFile
            .resolve("ru/mikaeliv/beers/feature/settings")
        packageDir.mkdirs()
        packageDir.resolve("AppVersion.kt").writeText(
            """
            package ru.mikaeliv.beers.feature.settings

            internal const val APP_VERSION = "$escapedVersion"
            """.trimIndent()
        )
    }
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    js { browser() }
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedAppVersionDir)
        }
        commonMain.dependencies {
            implementation(projects.composeDS)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.decompose.core)
            implementation(libs.decompose.extensions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn(generateAppVersionSource)
}
