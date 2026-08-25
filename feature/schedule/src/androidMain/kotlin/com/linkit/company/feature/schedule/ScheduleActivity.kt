package com.linkit.company.feature.schedule

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModelProvider
import com.linkit.company.core.common.extension.enableEdgeToEdgeConfig
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.schedule.navigation.ScheduleNavDisplay
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey(ScheduleActivity::class)
@Inject
class ScheduleActivity(
    private val viewModelFactory: MetroViewModelFactory,
) : ComponentActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        val tripPlanId = intent.getStringExtra(ExtraTripPlanId)
        val startRoute: LinkItNavKey = if (tripPlanId == null) {
            LinkItNavKey.ScheduleEdit
        } else {
            LinkItNavKey.ScheduleTripDetail(
                tripPlanId = tripPlanId,
                title = intent.getStringExtra(ExtraTripPlanTitle),
                focusedPlaceId = intent.getStringExtra(ExtraFocusedPlaceId),
            )
        }

        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                LinkItTheme {
                    ScheduleNavDisplay(
                        onFinishActivity = this::finish,
                        startRoute = startRoute,
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
