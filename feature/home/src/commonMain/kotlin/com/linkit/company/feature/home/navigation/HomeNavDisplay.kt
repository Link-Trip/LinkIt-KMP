package com.linkit.company.feature.home.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.savedstate.serialization.SavedStateConfiguration
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItNavigator
import com.linkit.company.core.navigation.LocalLinkItNavigator
import com.linkit.company.core.navigation.rememberNavigationState
import com.linkit.company.core.navigation.toDecoratedEntries
import com.linkit.company.feature.explore.navigation.exploreEntry
import com.linkit.company.feature.map.main.navigation.mapEntry
import com.linkit.company.feature.storage.navigation.storageEntry

@Composable
fun LinkItNavigationBar(
    currentTab: NavKey,
    onTabSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = LinkItTheme.color.semantic.line.solid.normal)
        NavigationBar(
            modifier = Modifier.height(60.dp),
            containerColor = LinkItTheme.color.semantic.static.white,
            tonalElevation = 0.dp,
        ) {
            for ((key, tab) in TopLevelRoutes) {
                val isSelected = currentTab == key
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(key) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = LinkItTheme.typography.caption3Medium,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LinkItTheme.color.semantic.label.normal,
                        selectedTextColor = LinkItTheme.color.semantic.label.normal,
                        unselectedIconColor = LinkItTheme.color.semantic.label.alternative,
                        unselectedTextColor = LinkItTheme.color.semantic.label.alternative,
                        indicatorColor = LinkItTheme.color.semantic.static.white,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavDisplay(
    savedStateConfiguration: SavedStateConfiguration,
    navigateToScheduleEdit: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val navigationState =
        rememberNavigationState(
            savedStateConfiguration = savedStateConfiguration,
            startRoute = LinkItNavKey.Map,
            topLevelRoutes = TopLevelRoutes.keys
        )

    val navigator = remember(navigationState) { LinkItNavigator(navigationState) }

    val entryProvider = entryProvider {
        mapEntry(
            navigateToScheduleEdit = {
                navigateToScheduleEdit()
            },
        )
        storageEntry()
        exploreEntry()
    }

    CompositionLocalProvider(LocalLinkItNavigator provides navigator) {
        Scaffold(
            modifier = modifier.fillMaxSize().systemBarsPadding(),
            bottomBar = {
                LinkItNavigationBar(
                    currentTab = navigationState.currentTopLevelRoute,
                    onTabSelected = { key -> navigator.navigate(key) },
                )
            }
        ) { paddingValues ->
            LinkItNavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onBack = navigator::navigateBack,
                entries = navigationState.toDecoratedEntries(entryProvider),
            )
        }
    }
}
