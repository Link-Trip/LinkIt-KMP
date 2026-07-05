plugins {
    id("kmp.feature.convention")
}

android {
    namespace = "com.linkit.company.feature.map"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.google.maps.compose)
            implementation(libs.google.play.services.maps)
        }

        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.designsystem)
            implementation(projects.domain)
            implementation(projects.core.navigation)
            implementation(libs.bundles.jetbrainsNavigation3)
        }
    }
}
