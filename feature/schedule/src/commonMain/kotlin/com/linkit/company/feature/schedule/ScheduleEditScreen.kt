package com.linkit.company.feature.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ScheduleEditScreen(
    onCreateSchedule: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ScheduleEditContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onCreateSchedule = onCreateSchedule,
    )
}

@Composable
fun ScheduleEditContent(
    uiState: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onCreateSchedule: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "유튜브 영상 링크를 넣고\n여행 일정을 만들어보세요",
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 98.dp),
            )

            Text(
                text = "추천영상",
                style = LinkItTheme.typography.body1NormalSemibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 16.dp, top = 40.dp),
            )
            RecommendedVideos(
                selectedIndex = uiState.copiedRecommendedIndex,
                onCopy = { onIntent(ScheduleIntent.CopyRecommendedLink(it)) },
            )

            CopyLinkButton(
                onClick = { onIntent(ScheduleIntent.CopyRecommendedLink(0)) },
                modifier = Modifier.padding(start = 20.dp, top = 92.dp),
            )

            Text(
                text = "영상 링크",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            )
            VideoLinkField(
                value = uiState.videoLink,
                onValueChange = { onIntent(ScheduleIntent.UpdateVideoLink(it)) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )

            Spacer(Modifier.weight(1f))
            if (!uiState.showInvalidLinkMessage) {
                CreateButton(
                    enabled = uiState.canCreate,
                    onClick = {
                        onIntent(ScheduleIntent.SubmitVideoLink)
                        if (uiState.videoLink.contains("youtu")) onCreateSchedule()
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                )
            } else {
                Spacer(Modifier.height(104.dp))
            }
        }

        if (uiState.showInvalidLinkMessage) {
            InvalidLinkMessage(
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 40.dp),
            )
        }
    }
}

@Composable
private fun RecommendedVideos(
    selectedIndex: Int?,
    onCopy: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(start = 16.dp, top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val titles = listOf(
            "유부남과 함께 오사카 좋...",
            "'가루들이 안 보이네요..?'...",
            "'가족 여행' 오키나와 브이로그",
        )
        titles.forEachIndexed { index, title ->
            Column(Modifier.width(160.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(videoBrush(index))
                        .border(
                            width = if (selectedIndex == index) 2.dp else 0.dp,
                            color = LinkItTheme.color.semantic.primary.normal,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onCopy(index) },
                ) {
                    Text(
                        text = listOf("오사카", "도쿄", "오키나와")[index],
                        style = LinkItTheme.typography.body1NormalSemibold,
                        color = LinkItTheme.color.semantic.static.white,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LinkItTheme.color.semantic.material.dimmer)
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = LinkItIcon.Control.Copy,
                            contentDescription = null,
                            tint = LinkItTheme.color.semantic.static.white,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "링크복사",
                            style = LinkItTheme.typography.caption2Medium,
                            color = LinkItTheme.color.semantic.static.white,
                            modifier = Modifier.padding(start = 3.dp),
                        )
                    }
                }
                Text(
                    text = title,
                    style = LinkItTheme.typography.body2NormalMedium,
                    color = LinkItTheme.color.semantic.label.strong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Text(
                    text = "조회수 113만회",
                    style = LinkItTheme.typography.body2NormalRegular,
                    color = LinkItTheme.color.semantic.label.alternative,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun videoBrush(index: Int): Brush {
    val primary = LinkItTheme.color.semantic.primary.normal
    val accent = when (index) {
        0 -> LinkItTheme.color.semantic.accent.background.lime
        1 -> LinkItTheme.color.semantic.accent.background.violet
        else -> LinkItTheme.color.semantic.accent.background.lightBlue
    }
    return Brush.linearGradient(listOf(accent, primary, LinkItTheme.color.semantic.inverse.background))
}

@Composable
private fun CopyLinkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, LinkItTheme.color.semantic.line.normal.strong, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "복사한 링크 붙여넣기",
            style = LinkItTheme.typography.body2NormalMedium,
            color = LinkItTheme.color.semantic.label.alternative,
        )
    }
}

@Composable
private fun VideoLinkField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = LinkItTheme.color.semantic.label.strong,
            fontSize = LinkItTheme.typography.body2NormalRegular.fontSize,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(12.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = "URL 를 붙여넣거나 입력해주세요.",
                        style = LinkItTheme.typography.body2NormalRegular,
                        color = LinkItTheme.color.semantic.label.assistive,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun CreateButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (enabled) LinkItTheme.color.semantic.primary.normal
                else LinkItTheme.color.semantic.interaction.disable,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "일정 생성하기",
            style = LinkItTheme.typography.body1NormalSemibold,
            color = if (enabled) LinkItTheme.color.semantic.static.white else LinkItTheme.color.semantic.label.disable,
        )
    }
}

@Composable
private fun InvalidLinkMessage(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LinkItTheme.color.semantic.label.alternative)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(18.dp).clip(RoundedCornerShape(9.dp)).background(LinkItTheme.color.semantic.status.negative),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "!",
                color = LinkItTheme.color.semantic.static.white,
                style = LinkItTheme.typography.caption2Medium,
            )
        }
        Text(
            text = "유효한 영상 링크가 아닙니다",
            style = LinkItTheme.typography.body2NormalSemibold,
            color = LinkItTheme.color.semantic.static.white,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
