package com.linkit.company.feature.explore.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.explore.ExploreScreen

fun EntryProviderScope<NavKey>.exploreEntry() {
    entry<LinkItNavKey.Explore> {
        ExploreScreen()
    }
}
