plugins {
    id("kmp.library.convention")
    alias(libs.plugins.kotlinSerialization)
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


        androidMain.dependencies {
            implementation(libs.ktorClientOkhttp)
        }
    }
}
