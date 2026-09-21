package com.linkit.company.feature.intro

import androidx.compose.ui.window.ComposeUIViewController
import com.linkit.company.core.designsystem.theme.LinkItTheme

fun IntroViewController(
    onComplete: () -> Unit,
    showResetCompletedToast: Boolean = false,
) = ComposeUIViewController {
    LinkItTheme {
        IntroScreen(
            onNavigateToHome = onComplete,
            showResetCompletedToast = showResetCompletedToast,
        )
    }
}
