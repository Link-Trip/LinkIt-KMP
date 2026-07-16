package com.linkit.company.feature.map.main

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface MapIntent : Intent {
    data object SelectSchedule : MapIntent
    data object SelectPlace : MapIntent
    data object ClearSelection : MapIntent
    data object ClosePlace : MapIntent
    data object ShowPreviousPlace : MapIntent
    data object ShowNextPlace : MapIntent
    data object ToggleCreateMenu : MapIntent
    data object ToggleMapType : MapIntent
}
