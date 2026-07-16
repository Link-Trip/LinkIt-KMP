package com.linkit.company.feature.intro

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.linkit.company.core.common.extension.enableEdgeToEdgeConfig
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.navigator.feature.HomeNavigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(IntroActivity::class)
@Inject
class IntroActivity(
    private val homeNavigator: HomeNavigator,
) : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            LinkItTheme {
                IntroScreen(
                    onNavigateToHome = {
                        homeNavigator.navigate(this@IntroActivity)
                        finish()
                    },
                )
            }
        }
    }
}
