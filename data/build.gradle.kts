plugins {
    id("kmp.library.convention")
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

android {
    namespace = "com.linkit.company.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.domain)

            implementation(libs.kotlinxSerializationJson)
            implementation(libs.ktorfitLib)
            implementation(libs.ktorKotlinxSerializationJson)
            implementation(libs.ktorClientContentNegotiation)
            implementation(libs.androidxDataStorePreferencesCore)
        }

        commonTest.dependencies {
            implementation(libs.ktorClientMock)
        }

        androidMain.dependencies {
            implementation(libs.ktorClientOkhttp)
        }
    }
}
