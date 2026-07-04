package com.linkit.company.feature.map.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.map.main.MapScreen

fun EntryProviderScope<NavKey>.mapEntry(
    navigateToScheduleEdit: () -> Unit,
) {
    entry<LinkItNavKey.Map> {
        MapScreen(
            navigateToScheduleEdit = navigateToScheduleEdit,
        )
    }
}
