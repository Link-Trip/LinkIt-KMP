package com.linkit.company.feature.explore

import com.linkit.company.core.common.architecture.contract.UiState

enum class ExploreTab {
    COUNTRY,
    THEME,
}

enum class ExploreCountry(
    val label: String,
    val flag: String,
) {
    ALL("전체", "🌐"),
    JAPAN("일본", "🇯🇵"),
    CHINA("중국", "🇨🇳"),
    VIETNAM("베트남", "🇻🇳"),
    ASIA("아시아", "🌏"),
    EUROPE("유럽", "🌍"),
    NORTH_AMERICA("북미", "🇺🇸"),
}

data class ExploreUiState(
    val selectedTab: ExploreTab = ExploreTab.COUNTRY,
    val selectedCountry: ExploreCountry = ExploreCountry.ALL,
) : UiState
