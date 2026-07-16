package com.linkit.company.feature.explore.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItNavigator
import com.linkit.company.feature.explore.ExploreScreen
import com.linkit.company.feature.explore.RecommendedCreatorsScreen

fun EntryProviderScope<NavKey>.exploreEntry(navigator: LinkItNavigator) {
    entry<LinkItNavKey.Explore> {
        ExploreScreen(onOpenCreators = { navigator.navigate(LinkItNavKey.ExploreCreators) })
    }
    entry<LinkItNavKey.ExploreCreators> {
        RecommendedCreatorsScreen(onBack = navigator::navigateBack)
    }
}
