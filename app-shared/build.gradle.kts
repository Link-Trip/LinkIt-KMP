plugins {
    id("kmp.shared.convention")
}

android {
    namespace = "com.linkit.company"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core modules
            implementation(projects.core.common)
            implementation(libs.metrox.viewmodel)

            // Domain & Data
            implementation(projects.domain)
            implementation(projects.data)
        }

        iosMain.dependencies {
            // IosAppGraph에서 DataStore<Preferences>를 직접 provide하기 위해 필요
            implementation(libs.androidxDataStorePreferencesCore)
        }
    }
}

