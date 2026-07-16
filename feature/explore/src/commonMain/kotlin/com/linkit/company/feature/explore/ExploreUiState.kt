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

enum class ExploreTheme(val label: String) {
    ALL("전체"),
    FOOD("미식 여행"),
    HEALING("힐링 여행"),
    CITY("도심지 여행"),
    NATURE("자연속 여행"),
}

data class ExploreUiState(
    val selectedTab: ExploreTab = ExploreTab.COUNTRY,
    val selectedCountry: ExploreCountry = ExploreCountry.ALL,
    val selectedTheme: ExploreTheme = ExploreTheme.ALL,
) : UiState
