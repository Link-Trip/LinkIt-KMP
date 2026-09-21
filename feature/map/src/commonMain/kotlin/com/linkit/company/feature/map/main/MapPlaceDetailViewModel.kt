package com.linkit.company.feature.map.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** 장소 상세 지도가 앱 전역 지도 설정을 따르도록 저장소 값을 노출한다 (단순 조회라 Repository 직접 호출). */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MapPlaceDetailViewModel::class)
@Inject
class MapPlaceDetailViewModel(
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    val mapDisplayType: StateFlow<MapDisplayType> = appSettingsRepository
        .observeMapDisplayType()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapDisplayType.DEFAULT)
}
