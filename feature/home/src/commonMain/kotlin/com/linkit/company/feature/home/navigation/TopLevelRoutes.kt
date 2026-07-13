package com.linkit.company.feature.home.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.navigation.LinkItNavKey

val TopLevelRoutes = mapOf(
    LinkItNavKey.Map to TopLevelTab("지도", LinkItIcon.Location.Map),
    LinkItNavKey.Storage to TopLevelTab("보관함", LinkItIcon.Utility.Archive),
    LinkItNavKey.Explore to TopLevelTab("탐색", LinkItIcon.Utility.Search),
)

data class TopLevelTab(
    val label: String,
    val icon: ImageVector,
)
