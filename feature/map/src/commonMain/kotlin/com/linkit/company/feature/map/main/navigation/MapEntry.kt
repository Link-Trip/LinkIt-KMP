package com.linkit.company.feature.map.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.map.main.MapPlaceDetailScreen
import com.linkit.company.feature.map.main.MapScreen
import com.linkit.company.feature.map.mypage.MyPageScreen

fun EntryProviderScope<NavKey>.mapEntry(
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit,
    navigateToScheduleEdit: () -> Unit,
    onPlaceSelectionChanged: (Boolean) -> Unit = {},
) {
    entry<LinkItNavKey.Map> {
        val navigator = LocalLinkItNavigator.current
        MapScreen(
            onOpenSchedule = onOpenSchedule,
            navigateToScheduleEdit = navigateToScheduleEdit,
            onOpenPlaceDetail = { place ->
                navigator.navigate(
                    LinkItNavKey.PlaceDetail(
                        placeId = place.placeId,
                        name = place.name,
                        categoryLabel = place.categoryLabel,
                        description = place.description,
                        tips = place.tips,
                        address = place.address,
                        latitude = place.latitude,
                        longitude = place.longitude,
                    ),
                )
            },
            onOpenStorage = { navigator.navigate(LinkItNavKey.Storage) },
            onOpenMyPage = { navigator.navigate(LinkItNavKey.MyPage) },
            onPlaceSelectionChanged = onPlaceSelectionChanged,
        )
    }

    entry<LinkItNavKey.PlaceDetail> { route ->
        val navigator = LocalLinkItNavigator.current
        MapPlaceDetailScreen(
            route = route,
            onBack = navigator::navigateBack,
        )
    }

    entry<LinkItNavKey.MyPage> {
        val navigator = LocalLinkItNavigator.current
        MyPageScreen(onBack = navigator::navigateBack)
    }
}
