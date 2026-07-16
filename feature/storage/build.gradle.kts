plugins {
    id("kmp.feature.convention")
    id("kmp.screenshot.test.convention")
}

android {
    namespace = "com.linkit.company.feature.storage"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.designsystem)
            implementation(projects.domain)
            implementation(libs.metrox.viewmodel)
            implementation(libs.metrox.viewmodel.compose)
            implementation(projects.core.navigation)
            implementation(libs.bundles.jetbrainsNavigation3)
        }
    }
}
