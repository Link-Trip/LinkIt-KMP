package com.linkit.company.feature.intro

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.linkit.company.core.common.AppGraph
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.feature.intro.navigation.IntroNavDisplay
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

/**
 * @param onComplete 메인 화면으로 이동해야 할 때(온보딩 완료·건너뛰기·이미 완료된 사용자) 호출
 * @param showResetCompletedToast 앱 초기화 직후 진입이면 `true`
 */
@Suppress("UNUSED")
fun IntroViewController(
    appGraph: AppGraph,
    onComplete: () -> Unit,
    showResetCompletedToast: Boolean = false,
) = ComposeUIViewController {
    CompositionLocalProvider(
        LocalMetroViewModelFactory provides appGraph.metroViewModelFactory,
    ) {
        LinkItTheme {
            IntroNavDisplay(
                showResetCompletedToast = showResetCompletedToast,
                onNavigateToHome = onComplete,
            )
        }
    }
}
