package com.linkit.company.feature.map.main.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.map.main.MapPlaceDetailScreen
import com.linkit.company.feature.map.main.MapScreen
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.feature.map.mypage.MyPageScreen
import com.linkit.company.feature.map.mypage.terms.TermsDetailScreen
import com.linkit.company.feature.map.mypage.terms.TermsListScreen

fun EntryProviderScope<NavKey>.mapEntry(
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit,
    navigateToScheduleEdit: () -> Unit,
    onPlaceSelectionChanged: (Boolean) -> Unit = {},
    onAppReset: () -> Unit = {},
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
        MyPageScreen(
            onBack = navigator::navigateBack,
            onOpenTerms = { navigator.navigate(LinkItNavKey.Terms) },
            onAppReset = onAppReset,
        )
    }

    entry<LinkItNavKey.Terms> {
        val navigator = LocalLinkItNavigator.current
        TermsListScreen(
            onBack = navigator::navigateBack,
            onOpenDocument = { type -> navigator.navigate(LinkItNavKey.TermsDetail(type.name)) },
        )
    }

    entry<LinkItNavKey.TermsDetail> { route ->
        val navigator = LocalLinkItNavigator.current
        TermsDetailScreen(
            type = TermsDocumentType.valueOf(route.type),
            onBack = navigator::navigateBack,
        )
    }
}
