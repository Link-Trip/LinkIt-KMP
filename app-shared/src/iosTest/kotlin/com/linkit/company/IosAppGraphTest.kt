package com.linkit.company

import androidx.lifecycle.ViewModel
import com.linkit.company.feature.explore.ExploreViewModel
import com.linkit.company.feature.map.main.MapPlaceDetailViewModel
import com.linkit.company.feature.map.main.MapViewModel
import com.linkit.company.feature.map.mypage.MyPageViewModel
import com.linkit.company.core.ui.terms.TermsViewModel
import com.linkit.company.feature.intro.IntroViewModel
import com.linkit.company.feature.schedule.NotificationPromptViewModel
import com.linkit.company.feature.schedule.ScheduleViewModel
import com.linkit.company.feature.schedule.TripDetailViewModel
import com.linkit.company.feature.storage.StorageViewModel
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class IosAppGraphTest {
    @Test
    fun registersAllScreenViewModelsWithoutInstantiatingThem() {
        val expectedViewModels: Set<KClass<out ViewModel>> = setOf(
            IntroViewModel::class,
            MapViewModel::class,
            MapPlaceDetailViewModel::class,
            MyPageViewModel::class,
            TermsViewModel::class,
            StorageViewModel::class,
            ExploreViewModel::class,
            ScheduleViewModel::class,
            TripDetailViewModel::class,
            NotificationPromptViewModel::class,
        )

        // Provider를 호출하지 않아 네트워크 요청과 실제 DataStore 접근을 피한다.
        assertEquals(expectedViewModels, createIosAppGraph().viewModelProviders.keys)
    }
}
