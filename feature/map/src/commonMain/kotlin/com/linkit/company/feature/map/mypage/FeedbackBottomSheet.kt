package com.linkit.company.feature.map.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.chip.ChipColors
import com.linkit.company.core.designsystem.component.chip.LinkItChip
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.component.textarea.LinkItTextArea
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.feedback.FeedbackType

/**
 * 의견 보내기 바텀시트(Figma `18197:38784` Modal/Bottom Sheet).
 *
 * 시트 밖 탭·뒤로가기·닫기 아이콘으로 닫히며, 전송 중에는 닫히지 않는다.
 * 콘텐츠는 [FeedbackSheetContent] 로 분리해 스크린샷 테스트가 시트 윈도우 없이 캡처할 수 있게 한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedbackBottomSheet(
    state: FeedbackSheetState,
    onIntent: (MyPageIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(MyPageIntent.CloseFeedbackSheet) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius),
        containerColor = LinkItTheme.color.semantic.background.elevated.normal,
        scrimColor = LinkItTheme.color.semantic.material.dimmer,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        FeedbackSheetContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.imePadding(),
        )
    }
}

@Composable
internal fun FeedbackSheetContent(
    state: FeedbackSheetState,
    onIntent: (MyPageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .testTag("feedback-sheet"),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ContentTopPadding)
                    .padding(horizontal = ContentHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LinkItTheme.color.semantic.background.normal.alternative),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = MyPageIllustrations.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = MyPageStrings.FeedbackTitle,
                            style = LinkItTheme.typography.heading2Bold,
                            color = LinkItTheme.color.semantic.label.normal,
                        )
                        Text(
                            text = MyPageStrings.FeedbackBody,
                            style = LinkItTheme.typography.body2ReadingRegular,
                            color = LinkItTheme.color.semantic.label.neutral,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeedbackTypeChip(FeedbackType.SUGGESTION, MyPageStrings.FeedbackTypeSuggestion, state, onIntent)
                    FeedbackTypeChip(FeedbackType.BUG, MyPageStrings.FeedbackTypeBug, state, onIntent)
                    FeedbackTypeChip(FeedbackType.ETC, MyPageStrings.FeedbackTypeEtc, state, onIntent)
                }

                LinkItTextArea(
                    value = state.content,
                    onValueChange = { onIntent(MyPageIntent.ChangeFeedbackContent(it)) },
                    placeholder = MyPageStrings.FeedbackPlaceholder,
                    enabled = !state.isSending,
                    maxLength = FeedbackSheetState.MaxLength,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback-sheet-input"),
                )
            }

            // Action area: Figma 18197:38820 — 위 20, 버튼 46, 아래는 시스템 하단 여백(iOS 홈 인디케이터 34)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ActionTopSpacing)
                    .padding(start = ContentHorizontalPadding, end = ContentHorizontalPadding, top = ActionAreaTopPadding)
                    .navigationBarsPadding(),
            ) {
                LinkItButton(
                    onClick = { onIntent(MyPageIntent.SendFeedback) },
                    text = MyPageStrings.FeedbackSend,
                    enabled = state.canSend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback-sheet-send"),
                )
            }
        }

        // Modal/Resource/Navigation: 닫기 아이콘을 우측 상단에 겹쳐 배치 (아이콘 중심 y=30, 우측 16)
        TopNavigationDefaults.IconButton(
            icon = LinkItIcon.Utility.Close,
            onClick = { onIntent(MyPageIntent.CloseFeedbackSheet) },
            contentDescription = "닫기",
            enabled = !state.isSending,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = CloseButtonTopInset, end = CloseButtonEndInset)
                .testTag("feedback-sheet-close"),
        )
    }
}

/** Figma Filter 칩: pill(999), 12×8, 13sp Bold. 선택 시 PaleBlue/99 면 + PaleBlue/80 선 + primary 텍스트. */
@Composable
private fun FeedbackTypeChip(
    type: FeedbackType,
    label: String,
    state: FeedbackSheetState,
    onIntent: (MyPageIntent) -> Unit,
) {
    val active = state.selectedType == type
    val semantic = LinkItTheme.color.semantic
    val atomic = LinkItTheme.color.atomic
    LinkItChip(
        onClick = { onIntent(MyPageIntent.SelectFeedbackType(type)) },
        text = label,
        active = active,
        enabled = !state.isSending,
        colors = ChipColors(
            containerColor = if (active) atomic.PaleBlue99 else semantic.background.normal.alternative,
            contentColor = if (active) semantic.primary.normal else semantic.label.normal,
            borderColor = if (active) atomic.PaleBlue80 else Color.Unspecified,
            disabledContainerColor = semantic.interaction.disable,
            disabledContentColor = semantic.label.disable,
            disabledBorderColor = Color.Unspecified,
        ),
        shape = LinkItTheme.shape.rounded,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        textStyle = LinkItTheme.typography.label2Bold,
        modifier = Modifier.testTag("feedback-chip-${type.name.lowercase()}"),
    )
}

private val SheetCornerRadius = 12.dp
private val CloseButtonTopInset = 10.dp
private val CloseButtonEndInset = 8.dp
private val ContentTopPadding = 40.dp
private val ContentHorizontalPadding = 20.dp
private val ActionTopSpacing = 10.dp
private val ActionAreaTopPadding = 20.dp
