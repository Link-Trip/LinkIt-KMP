package com.linkit.company.feature.explore

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ExploreIntent : Intent {
    data class SelectTab(val tab: ExploreTab) : ExploreIntent
    data class SelectCountry(val country: ExploreCountry) : ExploreIntent
}
