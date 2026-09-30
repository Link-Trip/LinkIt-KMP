package com.linkit.company.feature.intro.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.rememberNavigationState
import com.linkit.company.core.ui.terms.TermsDetailScreen
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.feature.intro.IntroIntent
import com.linkit.company.feature.intro.IntroScreen
import com.linkit.company.feature.intro.IntroSideEffect
import com.linkit.company.feature.intro.IntroViewModel
import com.linkit.company.feature.intro.OnboardingStartScreen
import com.linkit.company.feature.intro.TermsConsentSheet
import dev.zacsweers.metrox.viewmodel.metroViewModel

/**
 * 인트로 Activity의 단일 백스택 호스트: `Intro` → `OnboardingStart` → `TermsDetail(type)`.
 *
 * - `OnboardingStart` 로 갈 때 `Intro` 를 백스택에서 제거해 시작 화면이 루트가 된다.
 *   루트에서의 시스템 뒤로가기는 NavDisplay 가 처리하지 않으므로 Activity 종료로 이어진다(스펙 US1-6).
 * - 약관 시트 상태는 [IntroViewModel] 에 있어 상세 화면을 다녀와도 유지된다(FR-009).
 * - 온보딩을 마친 회원에게 개정 약관 재동의가 필요하면 `Intro` 위에 시트를 띄우고, 동의 후 메인으로 간다.
 *
 * @param showResetCompletedToast 앱 초기화 직후 진입 여부. [OnboardingStartScreen] 에 전달한다
 * @param onNavigateToHome 메인 화면으로 이동(Android: `HomeNavigator` + `finish()`, iOS: 콜백)
 */
@Composable
fun IntroNavDisplay(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    showResetCompletedToast: Boolean = false,
    viewModel: IntroViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationState = rememberNavigationState(
        savedStateConfiguration = LinkItSavedStateConfiguration,
        startRoute = LinkItNavKey.Intro,
        topLevelRoutes = setOf(LinkItNavKey.Intro),
    )
    val backStack = navigationState.currentTopLevelBackStack
    val latestOnNavigateToHome by rememberUpdatedState(onNavigateToHome)

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                IntroSideEffect.NavigateToOnboardingStart -> {
                    backStack.add(LinkItNavKey.OnboardingStart)
                    backStack.remove(LinkItNavKey.Intro)
                }
                is IntroSideEffect.NavigateToTermsDetail -> {
                    val route = LinkItNavKey.TermsDetail(effect.type.name)
                    backStack.remove(route)
                    backStack.add(route)
                }
                IntroSideEffect.NavigateToHome -> latestOnNavigateToHome()
            }
        }
    }

    val entryProvider = entryProvider<NavKey> {
        entry<LinkItNavKey.Intro> {
            IntroScreen(onSplashFinished = { viewModel.onIntent(IntroIntent.SplashFinished) })
            uiState.termsSheet?.let { sheet ->
                TermsConsentSheet(state = sheet, onIntent = viewModel::onIntent)
            }
        }
        entry<LinkItNavKey.OnboardingStart> {
            OnboardingStartScreen(
                uiState = uiState,
                onIntent = viewModel::onIntent,
                showResetCompletedToast = showResetCompletedToast,
            )
        }
        entry<LinkItNavKey.TermsDetail> { route ->
            TermsDetailScreen(
                type = TermsDocumentType.valueOf(route.type),
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
            )
        }
    }

    LinkItNavDisplay(
        modifier = modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider,
    )
}
