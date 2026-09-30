package com.linkit.company.feature.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.badge.BadgeSize
import com.linkit.company.core.designsystem.component.badge.LinkItBadge
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.popup.LinkItToast
import com.linkit.company.core.designsystem.component.popup.ToastVariant
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.intro.generated.resources.Res
import linkitcompany.feature.intro.generated.resources.onboarding_start_character
import org.jetbrains.compose.resources.painterResource

/** 앱 초기화 완료 토스트 문구 (specs/001-mypage-screen contracts §8, #48 `IntroScreen`에서 이동). */
const val ResetCompletedToastMessage = "앱 초기화가 완료되었습니다."

/**
 * 온보딩 시작 화면(Figma `18058:86645`). 캐릭터 일러스트 + 후킹 문구 + 버튼 2개.
 *
 * [uiState]의 `termsSheet`가 있으면 약관 동의 바텀시트([TermsConsentSheet])를 모달로 띄운다.
 *
 * @param showResetCompletedToast 앱 초기화 직후 진입이면 `true`(001 FR-026, `IntroActivity.EXTRA_SHOW_RESET_TOAST`)
 */
@Composable
fun OnboardingStartScreen(
    uiState: IntroUiState,
    onIntent: (IntroIntent) -> Unit,
    modifier: Modifier = Modifier,
    showResetCompletedToast: Boolean = false,
) {
    OnboardingStartContent(
        onSeeHowTo = { onIntent(IntroIntent.TapSeeHowTo) },
        onStartNow = { onIntent(IntroIntent.TapStartNow) },
        showResetCompletedToast = showResetCompletedToast,
        modifier = modifier,
    )

    uiState.termsSheet?.let { sheet ->
        TermsConsentSheet(state = sheet, onIntent = onIntent)
    }
}

/** 시트를 제외한 시작 화면 본문. 골든 테스트가 직접 호출한다. */
@Composable
fun OnboardingStartContent(
    onSeeHowTo: () -> Unit,
    onStartNow: () -> Unit,
    modifier: Modifier = Modifier,
    showResetCompletedToast: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // 작은 화면에서는 본문만 스크롤하고 하단 버튼은 고정한다
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(StartTopSpacing))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StartIllustrationAreaHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.onboarding_start_character),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(top = StartIllustrationOffset)
                            .width(StartIllustrationWidth)
                            .height(StartIllustrationHeight),
                    )
                }
                Spacer(Modifier.height(24.dp))
                LinkItBadge(
                    text = OnboardingStrings.StartBadge,
                    size = BadgeSize.Medium,
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = OnboardingStrings.StartTitle,
                        style = LinkItTheme.typography.headline1Bold,
                        color = LinkItTheme.color.semantic.label.normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = OnboardingStrings.StartSubtitle,
                        style = LinkItTheme.typography.label1ReadingMedium,
                        color = LinkItTheme.color.semantic.label.normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            if (showResetCompletedToast) {
                LinkItToast(
                    text = ResetCompletedToastMessage,
                    variant = ToastVariant.Positive,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .testTag(OnboardingStartTestTags.ResetToast),
                )
            }
        }

        LinkItActionArea(
            divider = false,
            bottomSafeArea = 0.dp,
        ) {
            LinkItButton(
                onClick = onSeeHowTo,
                text = OnboardingStrings.StartPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(OnboardingStartTestTags.SeeHowTo),
            )
            LinkItButton(
                onClick = onStartNow,
                text = OnboardingStrings.StartSecondary,
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Assistive,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(OnboardingStartTestTags.StartNow),
            )
        }
    }
}

object OnboardingStartTestTags {
    const val SeeHowTo = "onboarding-start-see-how-to"
    const val StartNow = "onboarding-start-start-now"
    const val ResetToast = "onboarding-start-reset-toast"
}

// Figma 18058:86645: 콘텐츠 시작 y=174(상태바 38 포함) → 136, 일러스트 영역 240, 이미지 320x261(+10 오프셋)
private val StartTopSpacing = 136.dp
private val StartIllustrationAreaHeight = 240.dp
private val StartIllustrationWidth = 320.dp
private val StartIllustrationHeight = 240.dp
private val StartIllustrationOffset = 10.dp
