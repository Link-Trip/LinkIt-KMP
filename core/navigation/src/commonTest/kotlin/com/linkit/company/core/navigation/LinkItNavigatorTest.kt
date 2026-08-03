package com.linkit.company.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkItNavigatorTest {
    @Test
    fun duplicateDestinationIsKeptOnce() {
        val state = navigationState()
        val navigator = LinkItNavigator(state)

        navigator.navigate(LinkItNavKey.MyPage)
        navigator.navigate(LinkItNavKey.MyPage)

        assertEquals(listOf<NavKey>(LinkItNavKey.Map, LinkItNavKey.MyPage), state.currentTopLevelBackStack)
    }

    @Test
    fun switchingTabsPreservesEachBackStack() {
        val state = navigationState()
        val navigator = LinkItNavigator(state)

        navigator.navigate(LinkItNavKey.Explore)
        navigator.navigate(LinkItNavKey.ExploreCreators)
        navigator.navigate(LinkItNavKey.Map)
        navigator.navigate(LinkItNavKey.Explore)

        assertEquals(LinkItNavKey.ExploreCreators, state.currentRoute)
    }

    @Test
    fun backPopsDetailThenReturnsToPreviousTab() {
        val state = navigationState()
        val navigator = LinkItNavigator(state)

        navigator.navigate(LinkItNavKey.Explore)
        navigator.navigate(LinkItNavKey.ExploreCreators)
        navigator.navigateBack()
        assertEquals(LinkItNavKey.Explore, state.currentRoute)

        navigator.navigateBack()
        assertEquals(LinkItNavKey.Map, state.currentTopLevelRoute)
    }

    @Test
    fun backAtStartRouteKeepsRoot() {
        val state = navigationState()
        val navigator = LinkItNavigator(state)

        navigator.navigateBack()

        assertEquals(listOf<NavKey>(LinkItNavKey.Map), state.topLevelStack)
        assertEquals(LinkItNavKey.Map, state.currentTopLevelRoute)
    }

    private fun navigationState(): NavigationState {
        val topLevelRoutes = listOf(LinkItNavKey.Map, LinkItNavKey.Storage, LinkItNavKey.Explore)
        return NavigationState(
            startRoute = LinkItNavKey.Map,
            topLevelStack = NavBackStack(LinkItNavKey.Map),
            backStacks = topLevelRoutes.associateWith { NavBackStack<NavKey>(it) },
        )
    }
}
