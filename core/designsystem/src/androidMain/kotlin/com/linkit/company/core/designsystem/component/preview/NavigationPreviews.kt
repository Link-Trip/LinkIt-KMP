package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.navigation.BottomNavigationPlatform
import com.linkit.company.core.designsystem.component.navigation.LinkItBottomNavigation
import com.linkit.company.core.designsystem.component.navigation.LinkItBottomNavigationItem
import com.linkit.company.core.designsystem.component.navigation.LinkItTab
import com.linkit.company.core.designsystem.component.navigation.LinkItTabRow
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TabSize
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.component.navigation.TopNavigationVariant
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItTopNavigationPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TopNavigationVariant.entries.forEach { variant ->
                LinkItTopNavigation(
                    title = "제목",
                    variant = variant,
                    navigationIcon = {
                        TopNavigationDefaults.BackButton(onClick = {})
                    },
                    actions = {
                        TopNavigationDefaults.IconButton(
                            icon = LinkItIcon.Utility.MoreVertical,
                            onClick = {},
                        )
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTopNavigationActionsPreview() {
    LinkItTheme {
        LinkItTopNavigation(
            title = "제목",
            variant = TopNavigationVariant.Normal,
            navigationIcon = {
                TopNavigationDefaults.BackButton(onClick = {})
            },
            actions = {
                TopNavigationDefaults.IconButton(
                    icon = LinkItIcon.Utility.Search,
                    onClick = {},
                )
                TopNavigationDefaults.IconButton(
                    icon = LinkItIcon.Utility.MoreVertical,
                    onClick = {},
                )
            },
            modifier = Modifier.background(LinkItTheme.color.semantic.background.normal.normal),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItBottomNavigationPreview() {
    val items = listOf(
        Triple("홈", LinkItIcon.Control.Home, LinkItIcon.Control.HomeFill),
        Triple("보관함", LinkItIcon.Utility.Folder, LinkItIcon.Utility.FolderFill),
        Triple("탐색", LinkItIcon.Location.Compass, LinkItIcon.Location.CompassFill),
    )
    LinkItTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BottomNavigationPlatform.entries.forEach { platform ->
                var selected by remember { mutableIntStateOf(0) }
                LinkItBottomNavigation(platform = platform) {
                    items.forEachIndexed { index, (label, icon, selectedIcon) ->
                        LinkItBottomNavigationItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            icon = icon,
                            selectedIcon = selectedIcon,
                            label = label,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTabRowPreview() {
    val tabs = listOf("텍스트", "텍스트", "텍스트", "텍스트")
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            listOf(TabSize.Medium, TabSize.Small).forEach { size ->
                var selected by remember { mutableIntStateOf(2) }
                LinkItTabRow(size = size) {
                    tabs.forEachIndexed { index, text ->
                        LinkItTab(
                            selected = selected == index,
                            onClick = { selected = index },
                            text = text,
                            enabled = index != 1,
                        )
                    }
                }
            }
        }
    }
}
