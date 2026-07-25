package com.linkit.company.feature.home

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.linkit.company.core.common.extension.enableEdgeToEdgeConfig
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.navigator.feature.ScheduleNavigator
import com.linkit.company.feature.home.navigation.HomeNavDisplay
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(HomeActivity::class)
@Inject
class HomeActivity(
    private val viewModelFactory: MetroViewModelFactory,
    private val scheduleNavigator: ScheduleNavigator,
) : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                LinkItTheme {
                    HomeNavDisplay(
                        savedStateConfiguration = LinkItSavedStateConfiguration,
                        navigateToScheduleEdit = {
                            scheduleNavigator.navigate(this@HomeActivity)
                        },
                        navigateToSchedule = { tripPlanId, title, focusedPlaceId ->
                            scheduleNavigator.navigate(this@HomeActivity) {
                                putExtra(ExtraTripPlanId, tripPlanId)
                                putExtra(ExtraTripPlanTitle, title)
                                focusedPlaceId?.let { putExtra(ExtraFocusedPlaceId, it) }
                                this
                            }
                        },
                    )
                }
            }
        }
    }

    private companion object {
        const val ExtraTripPlanId = "trip_plan_id"
        const val ExtraTripPlanTitle = "trip_plan_title"
        const val ExtraFocusedPlaceId = "focused_place_id"
    }
}
