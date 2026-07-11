package com.linkit.company.feature.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_video_1
import linkitcompany.feature.schedule.generated.resources.schedule_video_2
import linkitcompany.feature.schedule.generated.resources.schedule_video_3
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleEditScreen(
    onCreateSchedule: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ScheduleEditContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onCreateSchedule = onCreateSchedule,
        onBack = onBack,
    )
}

@Composable
fun ScheduleEditContent(
    uiState: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onCreateSchedule: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(57.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Arrow.ChevronLeft,
                    contentDescription = "뒤로가기",
                    tint = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack),
                )
                Text(
                    text = "영상 링크로 만들기",
                    style = LinkItTheme.typography.heading2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(end = 24.dp),
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp).background(LinkItTheme.color.semantic.background.normal.alternative),
                contentAlignment = Alignment.Center,
            ) {
                Text("그래픽", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.assistive)
            }

            Text(
                text = "추천영상",
                style = LinkItTheme.typography.caption1Bold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 16.dp, top = 26.dp),
            )
            RecommendedVideos(
                selectedIndex = uiState.copiedRecommendedIndex,
                onCopy = { onIntent(ScheduleIntent.CopyRecommendedLink(it)) },
            )

            CopyLinkButton(
                onClick = { onIntent(ScheduleIntent.CopyRecommendedLink(0)) },
                modifier = Modifier.padding(start = 20.dp, top = 33.dp),
            )

            Text(
                text = "영상 링크",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.padding(start = 20.dp, top = 5.dp),
            )
            VideoLinkField(
                value = uiState.videoLink,
                onValueChange = { onIntent(ScheduleIntent.UpdateVideoLink(it)) },
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 24.dp, bottom = 8.dp),
            )

            Spacer(Modifier.weight(1f))
            if (!uiState.showInvalidLinkMessage) {
                CreateButton(
                    enabled = uiState.canCreate,
                    onClick = {
                        onIntent(ScheduleIntent.SubmitVideoLink)
                        if (uiState.videoLink.contains("youtu")) onCreateSchedule()
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
            } else {
                Spacer(Modifier.height(64.dp))
            }
        }

        if (uiState.showInvalidLinkMessage) {
            InvalidLinkMessage(
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp, vertical = 14.dp),
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
        val videos = listOf(
            RecommendedVideo("유부남과 함께 오사카 좋은 놀이공원 가보기 【오사카上】", Res.drawable.schedule_video_1),
            RecommendedVideo("'가루들이 안 보이네요..?'...", Res.drawable.schedule_video_2),
            RecommendedVideo("'가족 여행' 오키나와 브이로그", Res.drawable.schedule_video_3),
        )
        videos.forEachIndexed { index, video ->
            Column(Modifier.width(160.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (selectedIndex == index) 2.dp else 0.dp,
                            color = LinkItTheme.color.semantic.primary.normal,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onCopy(index) },
                ) {
                    Image(
                        painter = painterResource(video.thumbnail),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
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
                    text = video.title,
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
private fun CopyLinkButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "복사한 링크 붙여넣기",
            style = LinkItTheme.typography.caption1Medium,
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
            .height(82.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(8.dp))
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
            .height(50.dp)
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
            text = "메시지에 마침표를 찍어요.",
            style = LinkItTheme.typography.body2NormalSemibold,
            color = LinkItTheme.color.semantic.static.white,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

private data class RecommendedVideo(val title: String, val thumbnail: DrawableResource)
