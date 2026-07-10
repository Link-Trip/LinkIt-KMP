plugins {
    id("kmp.core.convention")
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.linkit.company.core.navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.bundles.jetbrainsNavigation3)
            implementation(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
