package com.linkit.company.feature.map.main

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface MapIntent : Intent {
    data object RetryLoad : MapIntent
    data class SelectSchedule(val scheduleId: String) : MapIntent
    data class SelectPlace(
        val scheduleId: String,
        val markerId: String,
    ) : MapIntent
    data object ClearSelection : MapIntent
    data object ClosePlace : MapIntent
    data object ShowPreviousPlace : MapIntent
    data object ShowNextPlace : MapIntent
    data object ToggleCreateMenu : MapIntent
    data object ShowComingSoonDialog : MapIntent
    data object DismissComingSoonDialog : MapIntent
    data object ToggleMapType : MapIntent
    data class ToggleFilter(val filter: MapFilterType) : MapIntent
    data class SelectRegion(val region: String?) : MapIntent
    data class SelectStyle(val style: MapTravelStyleFilter?) : MapIntent
    data class SelectDuration(val duration: MapDurationFilter) : MapIntent
    data object RequestCurrentLocation : MapIntent
    data class CurrentLocationResolved(
        val latitude: Double,
        val longitude: Double,
    ) : MapIntent
    data class CurrentLocationUnavailable(val message: String) : MapIntent
    data class CameraChanged(
        val latitude: Double,
        val longitude: Double,
        val zoom: Float,
    ) : MapIntent
}
