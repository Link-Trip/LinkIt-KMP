package com.linkit.company.feature.storage.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.feature.storage.StorageSearchScreen
import com.linkit.company.feature.storage.StorageScreen

fun EntryProviderScope<NavKey>.storageEntry() {
    entry<LinkItNavKey.Storage> {
        val navigator = LocalLinkItNavigator.current
        StorageScreen(onSearch = { navigator.navigate(LinkItNavKey.StorageSearch) })
    }
    entry<LinkItNavKey.StorageSearch> {
        StorageSearchScreen()
    }
}
