package com.linkit.company.feature.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.component.textarea.LinkItTextArea
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_video_1
import linkitcompany.feature.schedule.generated.resources.schedule_video_2
import linkitcompany.feature.schedule.generated.resources.schedule_video_3
import linkitcompany.feature.schedule.generated.resources.schedule_video_link_hero
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleEditScreen(
    onCreateSchedule: () -> Unit = {},
    onOpenExistingSchedule: (tripPlanId: String, title: String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ScheduleSideEffect.NavigateToAnalysis -> onCreateSchedule()
                is ScheduleSideEffect.OpenExistingSchedule -> {
                    onOpenExistingSchedule(effect.tripPlanId, effect.title)
                }
            }
        }
    }

    ScheduleEditContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

@Composable
fun ScheduleEditContent(
    uiState: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            LinkItTopNavigation(
                title = "영상 링크로 만들기",
                navigationIcon = {
                    TopNavigationDefaults.BackButton(onClick = onBack)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                VideoLinkHero()
                RecommendedVideos(
                    selectedIndex = uiState.copiedRecommendedIndex,
                    onCopy = { index, youtubeUrl ->
                        clipboardManager.setText(AnnotatedString(youtubeUrl))
                        onIntent(ScheduleIntent.CopyRecommendedLink(index))
                    },
                )
                VideoLinkInput(
                    uiState = uiState,
                    onPaste = {
                        clipboardManager.getText()?.text
                            ?.takeIf(String::isNotBlank)
                            ?.let { onIntent(ScheduleIntent.UpdateVideoLink(it)) }
                    },
                    onValueChange = { onIntent(ScheduleIntent.UpdateVideoLink(it)) },
                )
                Spacer(Modifier.height(16.dp))
            }

            LinkItActionArea(
                divider = false,
                bottomSafeArea = 0.dp,
            ) {
                LinkItButton(
                    onClick = { onIntent(ScheduleIntent.SubmitVideoLink) },
                    text = "일정 생성하기",
                    enabled = uiState.canCreate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        uiState.existingSchedule?.let {
            ExistingSchedulePopup(
                onOpenExisting = { onIntent(ScheduleIntent.OpenExistingSchedule) },
                onCreateNew = { onIntent(ScheduleIntent.CreateDuplicateVideoSchedule) },
                onDismiss = { onIntent(ScheduleIntent.DismissExistingSchedule) },
            )
        }
    }
}

@Composable
private fun VideoLinkHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.schedule_video_link_hero),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RecommendedVideos(
    selectedIndex: Int?,
    onCopy: (index: Int, youtubeUrl: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 12.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "추천영상",
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
        )
        Text(
            text = "더보기",
            style = LinkItTheme.typography.caption1Bold,
            color = LinkItTheme.color.semantic.primary.normal,
        )
    }

    val videos = RecommendedVideoItems
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        videos.forEachIndexed { index, video ->
            RecommendedVideoCard(
                video = video,
                selected = selectedIndex == index,
                onCopy = { onCopy(index, video.youtubeUrl) },
            )
        }
        Spacer(Modifier.width(12.dp))
    }
}

@Composable
private fun RecommendedVideoCard(
    video: RecommendedVideo,
    selected: Boolean,
    onCopy: () -> Unit,
) {
    Column(Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onCopy),
        ) {
            Image(
                painter = painterResource(video.thumbnail),
                contentDescription = video.title,
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
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Control.Copy,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.static.white,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (selected) "복사완료" else "링크복사",
                    style = LinkItTheme.typography.caption2Medium,
                    color = LinkItTheme.color.semantic.static.white,
                )
            }
        }
        Text(
            text = video.title,
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = video.viewCount,
            style = LinkItTheme.typography.caption1Medium,
            color = LinkItTheme.color.semantic.label.alternative,
            maxLines = 1,
        )
    }
}

@Composable
private fun VideoLinkInput(
    uiState: ScheduleUiState,
    onPaste: () -> Unit,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        LinkItButton(
            onClick = onPaste,
            text = "복사한 링크 붙여넣기",
            size = ButtonSize.Small,
            color = ButtonColor.Assistive,
        )
        LinkItTextArea(
            value = uiState.videoLink,
            onValueChange = onValueChange,
            label = "영상 링크",
            placeholder = "URL 를 붙여넣거나 입력해주세요.",
            supportingText = uiState.videoLinkError?.supportingText,
            isError = uiState.videoLinkError != null,
            enabled = !uiState.isSubmittingVideoLink,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ExistingSchedulePopup(
    onOpenExisting: () -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.material.dimmer)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .clickable(onClick = {}),
        ) {
            Box(Modifier.fillMaxWidth()) {
                TopNavigationDefaults.IconButton(
                    icon = LinkItIcon.Utility.Close,
                    onClick = onDismiss,
                    contentDescription = "닫기",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp),
                )
                Column(
                    modifier = Modifier.padding(start = 32.dp, top = 48.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "이미 이 영상으로 만든 일정이 있어요!",
                        style = LinkItTheme.typography.body1NormalBold,
                        color = LinkItTheme.color.semantic.label.strong,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "그래도 같은 영상으로 일정을 만드시겠어요?",
                        style = LinkItTheme.typography.label2Medium,
                        color = LinkItTheme.color.semantic.label.alternative,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinkItButton(
                    onClick = onOpenExisting,
                    text = "기존 일정 보기",
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                LinkItButton(
                    onClick = onCreateNew,
                    text = "새로운 일정 만들기",
                    variant = ButtonVariant.Outlined,
                    color = ButtonColor.Assistive,
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private val VideoLinkError.supportingText: String
    get() = when (this) {
        VideoLinkError.WRONG_FORMAT -> "올바른 링크 형식이 아닙니다."
        VideoLinkError.INVALID_LINK -> "유효한 링크가 아닙니다."
    }

private data class RecommendedVideo(
    val title: String,
    val viewCount: String,
    val youtubeUrl: String,
    val thumbnail: DrawableResource,
)

private val RecommendedVideoItems = listOf(
    RecommendedVideo(
        title = "유부남과 함께 오사카 좋은 놀이공원 가보기 【오사카上】",
        viewCount = "조회수 113만회",
        youtubeUrl = "https://youtu.be/Qj1JXqY4-0I",
        thumbnail = Res.drawable.schedule_video_1,
    ),
    RecommendedVideo(
        title = "'가루들이 안 보이네요..?' 맛있는 여행 이야기",
        viewCount = "조회수 93만회",
        youtubeUrl = "https://youtu.be/0e3GPea1Tyg",
        thumbnail = Res.drawable.schedule_video_2,
    ),
    RecommendedVideo(
        title = "'가족 여행' 오키나와 브이로그",
        viewCount = "조회수 81만회",
        youtubeUrl = "https://youtu.be/s6JQUr5Ypw0",
        thumbnail = Res.drawable.schedule_video_3,
    ),
)
