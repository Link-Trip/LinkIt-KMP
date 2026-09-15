package com.linkit.company.feature.map.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.popup.LinkItSnackbar
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.video.VideoScheduleCreationState

@Composable
internal fun BoxScope.MapVideoCreationNotice(
    state: VideoScheduleCreationState,
    onRetry: () -> Unit,
    onAcknowledge: (String) -> Unit,
    onOpenSchedule: (String, String) -> Unit,
    onCreateAgain: () -> Unit,
    onShowInProgress: () -> Unit,
) {
    val noticeModifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .fillMaxWidth()
        .semantics { liveRegion = LiveRegionMode.Polite }
        .testTag("map-video-creation-notice")
    when (state) {
        VideoScheduleCreationState.Idle -> Unit
        is VideoScheduleCreationState.InProgress -> Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(CircleShape)
                .background(LinkItTheme.color.semantic.background.elevated.normal)
                .border(BorderStroke(1.dp, LinkItTheme.color.semantic.primary.normal), CircleShape)
                .clickable(onClick = onShowInProgress)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("map-video-creation-progress"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = LinkItTheme.color.semantic.primary.normal,
            )
            Text(
                text = "일정 생성중...",
                style = LinkItTheme.typography.caption1Bold,
                color = LinkItTheme.color.semantic.label.normal,
            )
        }
        is VideoScheduleCreationState.Completed -> LinkItSnackbar(
            message = "일정 생성이 완료되었습니다.",
            leadingIcon = LinkItIcon.Utility.CircleCheckFill,
            actionLabel = "확인하기",
            onActionClick = {
                onAcknowledge(state.taskId)
                onOpenSchedule(state.tripPlanId, state.title)
            },
            modifier = noticeModifier,
        )
        is VideoScheduleCreationState.Failed -> LinkItSnackbar(
            message = "일정 생성에 실패했어요",
            description = state.message,
            actionLabel = "다시 만들기",
            onActionClick = onCreateAgain,
            modifier = noticeModifier,
        )
        is VideoScheduleCreationState.Error -> LinkItSnackbar(
            message = "일정 생성 상태를 확인하지 못했어요",
            description = state.message,
            actionLabel = "다시 확인",
            onActionClick = onRetry,
            modifier = noticeModifier,
        )
    }
}

internal fun MapScheduleUiModel.estimatedCostLabel(): String {
    val minimum = estimatedMinCost?.takeIf { it >= 0 }
    val maximum = estimatedMaxCost?.takeIf { it >= 0 }
    if (minimum == null && maximum == null) return "비용 정보 없음"
    val useTenThousand = listOfNotNull(minimum, maximum).all { it >= 10_000 && it % 10_000 == 0 }
    val unit = if (useTenThousand) "만원" else "원"
    fun Int.amount() = (if (useTenThousand) this / 10_000 else this).formatAmount()
    return when {
        minimum == null -> "최대 ${maximum!!.amount()}$unit"
        maximum == null -> "최소 ${minimum.amount()}$unit"
        minimum != maximum ->
            "${minOf(minimum, maximum).amount()}~${maxOf(minimum, maximum).amount()}$unit"
        else -> "${minimum.amount()}$unit"
    }
}

private fun Int.formatAmount(): String = toString().reversed().chunked(3).joinToString(",").reversed()
