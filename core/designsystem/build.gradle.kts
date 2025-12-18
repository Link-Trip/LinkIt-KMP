plugins {
    id("kmp.core.convention")
    id("kmp.screenshot.test.convention")
}

android.namespace = "com.linkit.company.core.designsystem"

val composeExtension = extensions.getByType(org.jetbrains.compose.ComposeExtension::class.java)

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(composeExtension.dependencies.materialIconsExtended)
        }
    }
}

compose.resources {
    publicResClass = true
    // ref. https://www.jetbrains.com/help/kotlin-multiplatform-dev/whats-new-compose-180.html#option-to-change-the-generated-res-class-name
    nameOfResClass = "DesignRes"
}
