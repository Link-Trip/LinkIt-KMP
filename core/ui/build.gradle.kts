plugins {
    id("kmp.core.convention")
    id("kmp.screenshot.test.convention")
}

android {
    namespace = "com.linkit.company.core.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.domain)
            implementation(libs.metrox.viewmodel)
            implementation(libs.metrox.viewmodel.compose)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
