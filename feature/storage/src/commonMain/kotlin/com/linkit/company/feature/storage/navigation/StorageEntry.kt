package com.linkit.company.feature.storage.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.feature.storage.StorageSearchScreen
import com.linkit.company.feature.storage.StorageSearchResultScreen
import com.linkit.company.feature.storage.StorageAddSavedScreen
import com.linkit.company.feature.storage.StorageDetailScreen
import com.linkit.company.feature.storage.StorageScreen

fun EntryProviderScope<NavKey>.storageEntry() {
    entry<LinkItNavKey.Storage> {
        val navigator = LocalLinkItNavigator.current
        StorageScreen(
            onSearch = { navigator.navigate(LinkItNavKey.StorageSearch) },
            onAddSavedItem = { navigator.navigate(LinkItNavKey.StorageAddSaved) },
            onOpenFolder = { navigator.navigate(LinkItNavKey.StorageDetail) },
        )
    }
    entry<LinkItNavKey.StorageSearch> {
        val navigator = LocalLinkItNavigator.current
        StorageSearchScreen(onSearch = { navigator.navigate(LinkItNavKey.StorageSearchResult) })
    }
    entry<LinkItNavKey.StorageSearchResult> {
        val navigator = LocalLinkItNavigator.current
        StorageSearchResultScreen(
            onBack = navigator::navigateBack,
            onOpenItem = { navigator.navigate(LinkItNavKey.StorageDetail) },
        )
    }
    entry<LinkItNavKey.StorageAddSaved> {
        val navigator = LocalLinkItNavigator.current
        StorageAddSavedScreen(onBack = navigator::navigateBack)
    }
    entry<LinkItNavKey.StorageDetail> {
        val navigator = LocalLinkItNavigator.current
        StorageDetailScreen(onBack = navigator::navigateBack)
    }
}
