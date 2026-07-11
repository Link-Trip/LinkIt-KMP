plugins {
    id("kmp.feature.convention")
    id("kmp.screenshot.test.convention")
}

android {
    namespace = "com.linkit.company.feature.map"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.google.maps.compose)
        }

        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.designsystem)
            implementation(projects.domain)
            implementation(projects.core.navigation)
            implementation(libs.metrox.viewmodel)
            implementation(libs.metrox.viewmodel.compose)
            implementation(libs.bundles.jetbrainsNavigation3)
        }
    }
}
