package com.linkit.company.feature.map.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.map.main.MapScreen
import com.linkit.company.feature.map.mypage.MyPageScreen

fun EntryProviderScope<NavKey>.mapEntry(
    onOpenSchedule: () -> Unit,
    navigateToScheduleEdit: () -> Unit,
) {
    entry<LinkItNavKey.Map> {
        val navigator = LocalLinkItNavigator.current
        MapScreen(
            onOpenSchedule = onOpenSchedule,
            navigateToScheduleEdit = navigateToScheduleEdit,
            onOpenMyPage = { navigator.navigate(LinkItNavKey.MyPage) },
        )
    }

    entry<LinkItNavKey.MyPage> {
        val navigator = LocalLinkItNavigator.current
        MyPageScreen(onBack = navigator::navigateBack)
    }
}
