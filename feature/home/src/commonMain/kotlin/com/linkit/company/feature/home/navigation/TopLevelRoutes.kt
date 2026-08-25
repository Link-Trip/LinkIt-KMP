package com.linkit.company.feature.home.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.navigation.LinkItNavKey

val TopLevelRoutes: Map<LinkItNavKey, TopLevelTab> = mapOf(
    LinkItNavKey.Map to TopLevelTab("홈", LinkItIcon.Control.Home, LinkItIcon.Control.HomeFill),
    LinkItNavKey.Storage to TopLevelTab("보관함", LinkItIcon.Utility.Folder, LinkItIcon.Utility.FolderFill),
    LinkItNavKey.Explore to TopLevelTab("탐색", LinkItIcon.Location.Compass, LinkItIcon.Location.CompassFill),
)

data class TopLevelTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)
