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
            implementation(libs.napier)
            // iOS 앱의 조립 지점. Android/common 모듈 의존성에는 UI를 추가하지 않는다.
            implementation(projects.core.navigation)
            implementation(projects.core.ui)
            implementation(projects.feature.intro)
            implementation(projects.feature.home)
            implementation(projects.feature.map)
            implementation(projects.feature.storage)
            implementation(projects.feature.explore)
            implementation(projects.feature.schedule)
        }

        val iosTest by creating {
            dependsOn(commonTest.get())
        }
        iosArm64Test.get().dependsOn(iosTest)
        iosSimulatorArm64Test.get().dependsOn(iosTest)
    }
}
