package com.linkit.company.feature.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.ui.platform.BlockSystemBack
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_analysis_complete_character
import org.jetbrains.compose.resources.painterResource

/**
 * 온보딩 분석 완료 화면(Figma `17789:48125`, FR-027).
 *
 * `생성된 일정 확인하기` → [ScheduleIntent.ConfirmOnboardingSchedule] → [ScheduleSideEffect.FinishOnboarding] → [onFinish].
 * 시스템 뒤로가기는 차단한다.
 */
@Composable
fun ScheduleAnalysisCompleteScreen(
    onFinish: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            if (effect == ScheduleSideEffect.FinishOnboarding) onFinish()
        }
    }

    ScheduleAnalysisCompleteContent(
        onConfirm = { viewModel.onIntent(ScheduleIntent.ConfirmOnboardingSchedule) },
        modifier = modifier,
    )
}

/** 화면 본문. 골든 테스트가 직접 호출한다. */
@Composable
fun ScheduleAnalysisCompleteContent(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BlockSystemBack()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            // Figma: 일러스트 240 + 문구 블록을 화면 중앙에서 31dp 위에 배치
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = CompleteContentOffset),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.schedule_analysis_complete_character),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(CompleteIllustrationWidth)
                        .height(CompleteIllustrationHeight),
                )
                Text(
                    text = ScheduleCompleteStrings.Title + "\n" + ScheduleCompleteStrings.Body,
                    style = LinkItTheme.typography.body1NormalSemibold,
                    color = LinkItTheme.color.semantic.label.normal.copy(alpha = CompleteTextAlpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                )
            }
        }

        LinkItActionArea(divider = false) {
            LinkItButton(
                onClick = onConfirm,
                text = ScheduleCompleteStrings.Confirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(ScheduleCompleteTestTags.Confirm),
            )
        }
        Spacer(Modifier.height(0.dp))
    }
}

internal object ScheduleCompleteStrings {
    const val Title = "축하해요 첫일정 분석이 완료됐어요!"
    const val Body = "짜여진 일정을 저장하고 쉽게 관리해보세요"
    const val Confirm = "생성된 일정 확인하기"
}

object ScheduleCompleteTestTags {
    const val Confirm = "schedule-complete-confirm"
}

private val CompleteIllustrationWidth = 293.dp
private val CompleteIllustrationHeight = 240.dp
private val CompleteContentOffset = 62.dp
private const val CompleteTextAlpha = .8f
