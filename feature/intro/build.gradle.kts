plugins {
    id("kmp.feature.convention")
    id("kmp.screenshot.test.convention")
}

android {
    namespace = "com.linkit.company.feature.intro"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.core.designsystem)
            implementation(projects.domain)
            implementation(libs.metrox.viewmodel)
            implementation(libs.metrox.viewmodel.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            compileOnly(libs.metrox.android)
        }
    }
}
