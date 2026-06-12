package com.linkit.company.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.designsystem.legacy.showcase.DesignShowcaseScreen
import com.linkit.company.core.navigation.LinkItNavKey

fun EntryProviderScope<NavKey>.designShowcaseEntry(
    onBack: () -> Unit,
) {
    entry<LinkItNavKey.DesignShowcase> {
        DesignShowcaseScreen(
            onBack = onBack,
        )
    }
}
