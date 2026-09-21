package com.linkit.company.feature.schedule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
import com.linkit.company.domain.model.video.DiscoverVideo
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_video_link_hero
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleEditScreen(
    onCreateSchedule: (videoTitle: String?, thumbnailUrl: String?) -> Unit = { _, _ -> },
    onOpenExistingSchedule: (tripPlanId: String, title: String) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ScheduleSideEffect.NavigateToAnalysis -> {
                    onCreateSchedule(effect.videoTitle, effect.thumbnailUrl)
                }
                is ScheduleSideEffect.OpenExistingSchedule -> {
                    onOpenExistingSchedule(effect.tripPlanId, effect.title)
                }
                ScheduleSideEffect.TripPlanDeleted -> Unit
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
                    uiState = uiState,
                    onRetry = { onIntent(ScheduleIntent.LoadRecommendedVideos) },
                    onToggleExpanded = { onIntent(ScheduleIntent.ToggleRecommendedVideos) },
                    onSelect = { onIntent(ScheduleIntent.UpdateVideoLink(it)) },
                    onCopy = { youtubeUrl ->
                        clipboardManager.setText(AnnotatedString(youtubeUrl))
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
            .padding(horizontal = 20.dp)
            .height(180.dp)
            .testTag("video-link-hero")
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
    uiState: ScheduleUiState,
    onRetry: () -> Unit,
    onToggleExpanded: () -> Unit,
    onSelect: (youtubeUrl: String) -> Unit,
    onCopy: (youtubeUrl: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 24.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "추천영상",
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
        )
        if (uiState.recommendedVideos.size > RecommendedPreviewCount) Text(
            text = if (uiState.areRecommendedVideosExpanded) "접기" else "더보기",
            style = LinkItTheme.typography.caption1Bold,
            color = LinkItTheme.color.semantic.primary.normal,
            modifier = Modifier.clickable(onClick = onToggleExpanded)
                .testTag("recommended-video-expand"),
        )
    }

    when {
        uiState.isLoadingRecommendedVideos -> Box(
            Modifier.fillMaxWidth().height(130.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = LinkItTheme.color.semantic.primary.normal,
                modifier = Modifier.size(24.dp).testTag("recommended-video-loading"),
            )
        }
        uiState.recommendedVideosError != null -> Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                uiState.recommendedVideosError,
                style = LinkItTheme.typography.caption1Regular,
                color = LinkItTheme.color.semantic.label.alternative,
            )
            LinkItButton(onClick = onRetry, text = "다시 시도", size = ButtonSize.Small)
        }
        uiState.recommendedVideos.isEmpty() -> Text(
            text = "추천 영상이 아직 없어요.",
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
        uiState.areRecommendedVideosExpanded -> Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag(RecommendedVideoListTestTag),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.recommendedVideos.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { video ->
                        RecommendedVideoCard(
                            video = video,
                            selectionEnabled = !uiState.isSubmittingVideoLink,
                            onSelect = { onSelect(video.videoUrl) },
                            onCopy = { onCopy(video.videoUrl) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        else -> LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                .testTag(RecommendedVideoListTestTag),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.recommendedVideos.take(RecommendedPreviewCount), key = { it.videoId }) { video ->
                RecommendedVideoCard(
                    video = video,
                    selectionEnabled = !uiState.isSubmittingVideoLink,
                    onSelect = { onSelect(video.videoUrl) },
                    onCopy = { onCopy(video.videoUrl) },
                )
            }
        }
    }
}

@Composable
private fun RecommendedVideoCard(
    video: DiscoverVideo,
    selectionEnabled: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.width(160.dp).clickable(enabled = selectionEnabled, onClick = onSelect).testTag("recommended-video-${video.videoId}")) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LinkItTheme.color.semantic.background.normal.alternative),
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LinkItTheme.color.semantic.material.dimmer)
                    .clickable(onClick = onCopy)
                    .testTag("recommended-video-copy-${video.videoId}")
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Control.Copy,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.static.white,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "링크복사",
                    style = LinkItTheme.typography.caption1Medium,
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
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "조회수 ${video.viewCount.toString().reversed().chunked(3).joinToString(",").reversed()}회",
            style = LinkItTheme.typography.caption1Medium,
            color = LinkItTheme.color.semantic.label.alternative,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp),
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
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        LinkItButton(
            onClick = onPaste,
            text = "복사한 링크 붙여넣기",
            enabled = !uiState.isSubmittingVideoLink,
            size = ButtonSize.Small,
            color = ButtonColor.Assistive,
            modifier = Modifier.padding(vertical = 10.dp),
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
        VideoLinkError.ALREADY_IN_PROGRESS -> "이미 생성 중인 일정이 있어요. 메인 화면에서 진행 상태를 확인해 주세요."
    }

private const val RecommendedPreviewCount = 3

internal const val RecommendedVideoListTestTag = "recommended-video-list"
