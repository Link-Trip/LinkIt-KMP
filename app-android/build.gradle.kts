plugins {
    id("android.application.convention")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
}

android {
    namespace = "com.linkit.company.android"

    defaultConfig {
        applicationId = "com.linkit.company"
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)

        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.android)
    implementation(project(":app-shared"))
    implementation(project(":feature:intro"))
    implementation(project(":feature:home"))
    implementation(project(":feature:explore"))
    implementation(project(":feature:map"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:storage"))
    implementation(libs.androidx.activity.compose)
}
