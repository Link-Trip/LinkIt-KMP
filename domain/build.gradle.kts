plugins {
    id("kmp.library.convention")
}

android {
    namespace = "com.linkit.company.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.kotlinxCoroutinesCore)
        }
    }
}
