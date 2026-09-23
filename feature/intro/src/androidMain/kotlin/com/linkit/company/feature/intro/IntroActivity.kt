package com.linkit.company.feature.intro

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.linkit.company.core.common.extension.enableEdgeToEdgeConfig
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.navigator.feature.HomeNavigator
import com.linkit.company.feature.intro.navigation.IntroNavDisplay
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(IntroActivity::class)
@Inject
class IntroActivity(
    private val viewModelFactory: MetroViewModelFactory,
    private val homeNavigator: HomeNavigator,
) : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        val showResetCompletedToast = intent.getBooleanExtra(EXTRA_SHOW_RESET_TOAST, false)

        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                LinkItTheme {
                    IntroNavDisplay(
                        showResetCompletedToast = showResetCompletedToast,
                        onNavigateToHome = {
                            homeNavigator.navigate(this@IntroActivity)
                            finish()
                        },
                    )
                }
            }
        }
    }

    companion object {
        /** 앱 초기화 완료 후 진입 시 `true` 로 전달하면 온보딩 시작 화면에 완료 토스트를 표시한다. */
        const val EXTRA_SHOW_RESET_TOAST = "extra_show_reset_toast"
    }
}
