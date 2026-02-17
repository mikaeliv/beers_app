plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

// Этот файл определяет плагины верхнего уровня и делает их доступными для сабмодулей.
// Флаг `apply false` означает, что плагины только объявлены здесь, а фактически
// применяются в конкретных модулях (composeApp, data, feature-*), где они нужны.