package com.linkit.company.feature.explore

import com.linkit.company.core.common.architecture.contract.UiState
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo

enum class ExploreTab { COUNTRY, THEME }

enum class ExploreTheme(val label: String, val query: String?) {
    ALL("전체", null),
    FOOD("미식 여행", "맛집여행"),
    HEALING("힐링 여행", "힐링여행"),
    ACTIVITY("액티비티", "액티비티"),
}

data class ExploreUiState(
    val selectedTab: ExploreTab = ExploreTab.COUNTRY,
    val selectedCountry: String? = null,
    val selectedRegion: String? = null,
    val selectedTheme: ExploreTheme = ExploreTheme.ALL,
    val countries: List<DiscoverCountry> = emptyList(),
    val channels: List<DiscoverChannel> = emptyList(),
    val selectedChannelId: String? = null,
    val videos: List<DiscoverVideo> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isCatalogLoading: Boolean = true,
    val errorMessage: String? = null,
    val catalogErrorMessage: String? = null,
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
    val linkErrorMessage: String? = null,
) : UiState {
    val selectedChannel: DiscoverChannel?
        get() = channels.firstOrNull { it.channelId == selectedChannelId }
}
