package com.linkit.company.core.designsystem.component.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun LinkItScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = LinkItTheme.color.semantic.background.normal.alternative,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = containerColor,
    ) { paddingValues ->
        // TODO : ContentMargin 적용 필요
        val innerPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            start = 0.dp,
            end = 0.dp,
            bottom = paddingValues.calculateBottomPadding(),
        )
        content(innerPadding)
    }
}
