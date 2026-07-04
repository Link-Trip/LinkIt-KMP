package com.linkit.company.core.navigation

import androidx.navigation3.runtime.NavKey

class LinkItNavigator(private val navigationState: NavigationState) {
    fun navigate(target: NavKey) {
        when (target) {
            navigationState.currentTopLevelRoute -> clearCurrentBackStack()
            in navigationState.topLevelRoutes -> switchTab(target)
            else -> pushRoute(target)
        }
    }

    fun navigateBack() {
        if (navigationState.currentRoute == navigationState.currentTopLevelRoute) {
            if (navigationState.topLevelStack.size > 1) {
                navigationState.topLevelStack.removeLastOrNull()
            }
        } else {
            if (navigationState.currentTopLevelBackStack.size > 1) {
                navigationState.currentTopLevelBackStack.removeLastOrNull()
            }
        }
    }

    private fun pushRoute(route: NavKey) {
        navigationState.currentTopLevelBackStack.apply {
            remove(route)
            add(route)
        }
    }

    private fun switchTab(target: NavKey) {
        navigationState.topLevelStack.apply {
            remove(target)
            add(target)
        }
    }

    private fun clearCurrentBackStack() {
        navigationState.currentTopLevelBackStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
