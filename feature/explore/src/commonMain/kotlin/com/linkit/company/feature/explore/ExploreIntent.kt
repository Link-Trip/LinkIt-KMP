package com.linkit.company.feature.explore

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ExploreIntent : Intent {
    data class SelectTab(val tab: ExploreTab) : ExploreIntent
    data class SelectCountry(val country: String?) : ExploreIntent
    data class SelectRegion(val region: String) : ExploreIntent
    data class SelectTheme(val theme: ExploreTheme) : ExploreIntent
    data class SelectChannel(val channelId: String) : ExploreIntent
    data object RetryVideos : ExploreIntent
    data object RetryCatalog : ExploreIntent
    data object LoadMore : ExploreIntent
    data object LinkOpenFailed : ExploreIntent
    data object DismissLinkError : ExploreIntent
}
