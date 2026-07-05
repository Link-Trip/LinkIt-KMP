package com.linkit.company.feature.map.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun MapScreen(
    navigateToScheduleEdit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.alternative),
    ) {
        GoogleMapBackground(modifier = Modifier.fillMaxSize())
    }
}
