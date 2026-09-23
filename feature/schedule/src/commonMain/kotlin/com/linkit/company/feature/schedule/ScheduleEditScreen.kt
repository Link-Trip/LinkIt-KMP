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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
import com.linkit.company.core.designsystem.component.coachmark.CoachMarkPointer
import com.linkit.company.core.designsystem.component.coachmark.LinkItCoachMark
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.component.popup.LinkItToast
import com.linkit.company.core.designsystem.component.popup.ToastDefaults
import com.linkit.company.core.designsystem.component.popup.ToastVariant
import com.linkit.company.core.designsystem.component.textarea.LinkItTextArea
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.ui.onboarding.OnboardingSkipButton
import com.linkit.company.core.ui.onboarding.OnboardingSkipButtonDefaults
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.video.DiscoverVideo
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.delay
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_video_link_hero
import org.jetbrains.compose.resources.painterResource

/**
 * @param onNavigateToAnalysisComplete 온보딩 일정 생성 성공(분석 중 화면 생략)
 * @param onFinishOnboarding 건너뛰기 → Activity 종료로 메인 복귀
 */
@Composable
fun ScheduleEditScreen(
    onCreateSchedule: (videoTitle: String?, thumbnailUrl: String?) -> Unit = { _, _ -> },
    onOpenExistingSchedule: (tripPlanId: String, title: String) -> Unit = { _, _ -> },
    onNavigateToAnalysisComplete: () -> Unit = {},
    onFinishOnboarding: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ScheduleSideEffect.NavigateToAnalysis -> onCreateSchedule(effect.videoTitle, effect.thumbnailUrl)
                is ScheduleSideEffect.OpenExistingSchedule -> onOpenExistingSchedule(effect.tripPlanId, effect.title)
                is ScheduleSideEffect.WriteClipboard -> clipboardManager.setText(AnnotatedString(effect.text))
                ScheduleSideEffect.NavigateToAnalysisComplete -> onNavigateToAnalysisComplete()
                ScheduleSideEffect.FinishOnboarding -> onFinishOnboarding()
            }
        }
    }

    // 클립보드 읽기는 포그라운드에서만: 진입·포커스 복귀 시 존재 여부만 확인한다 (research R10)
    val isWindowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(isWindowFocused) {
        if (isWindowFocused) {
            viewModel.onIntent(ScheduleIntent.ClipboardAvailabilityChanged(clipboardManager.hasText()))
        }
    }

    ScheduleEditContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        readClipboardText = { clipboardManager.getText()?.text?.takeIf(String::isNotBlank) },
    )
}

/**
 * @param readClipboardText 붙여넣기 칩 탭 시 클립보드 텍스트를 읽는다. 읽기 실패·빈 값이면 null
 */
@Composable
fun ScheduleEditContent(
    uiState: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    readClipboardText: () -> String? = { null },
) {
    var copyButtonBounds by remember { mutableStateOf<Rect?>(null) }
    var pasteChipBounds by remember { mutableStateOf<Rect?>(null) }

    // 진입 후 잠시 뒤 3단계 코치마크 (FR-020). 지연 전에 이미 복사했으면 단계가 넘어가 있어 표시하지 않는다
    LaunchedEffect(Unit) {
        delay(TutorialGuideDelayMillis)
        onIntent(ScheduleIntent.GuideDelayElapsed)
    }
    LaunchedEffect(uiState.clipboardToastUrl) {
        if (uiState.clipboardToastUrl != null) {
            delay(ScheduleToastDurationMillis)
            onIntent(ScheduleIntent.DismissClipboardToast)
        }
    }
    LaunchedEffect(uiState.errorToast) {
        if (uiState.errorToast != null) {
            delay(ScheduleToastDurationMillis)
            onIntent(ScheduleIntent.DismissErrorToast)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            LinkItTopNavigation(
                title = ScheduleEditStrings.Title,
                navigationIcon = {
                    if (!uiState.isOnboardingMode) {
                        TopNavigationDefaults.BackButton(onClick = onBack)
                    }
                },
                actions = {
                    if (uiState.isOnboardingMode) {
                        OnboardingSkipButton(
                            onClick = { onIntent(ScheduleIntent.SkipOnboarding) },
                            modifier = Modifier
                                .padding(end = SkipButtonNavigationEndPadding)
                                .testTag(ScheduleEditTestTags.Skip),
                        )
                    }
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                VideoLinkHero()
                RecommendedVideos(
                    state = uiState.recommendedVideos,
                    onCopy = { url -> onIntent(ScheduleIntent.CopyRecommendedLink(url)) },
                    onRetry = { onIntent(ScheduleIntent.LoadRecommendedVideos) },
                    onFirstCopyButtonPositioned = { copyButtonBounds = it },
                )
                VideoLinkInput(
                    uiState = uiState,
                    onPaste = {
                        readClipboardText()?.let { onIntent(ScheduleIntent.PasteFromClipboard(it)) }
                    },
                    onValueChange = { onIntent(ScheduleIntent.UpdateVideoLink(it)) },
                    onPasteChipPositioned = { pasteChipBounds = it },
                )
                Spacer(Modifier.height(16.dp))
            }

            LinkItActionArea(
                divider = false,
                bottomSafeArea = 0.dp,
            ) {
                LinkItButton(
                    onClick = { onIntent(ScheduleIntent.SubmitVideoLink) },
                    text = ScheduleEditStrings.Create,
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

        // 튜토리얼 3·4단계 코치마크 (FR-020, FR-023). FREE 이면 오버레이 없음
        when (uiState.tutorialStep) {
            TutorialStep.COPY_LINK -> if (uiState.isGuideVisible) {
                LinkItCoachMark(
                    targetBounds = copyButtonBounds,
                    message = ScheduleEditStrings.Step3,
                    pointer = CoachMarkPointer.Leading,
                    targetCornerRadius = 4.dp,
                    onTargetClick = {
                        uiState.recommendedVideoUrls.firstOrNull()?.let { onIntent(ScheduleIntent.CopyRecommendedLink(it)) }
                    },
                )
            }
            TutorialStep.PASTE_LINK -> LinkItCoachMark(
                targetBounds = pasteChipBounds,
                message = ScheduleEditStrings.Step4,
                pointer = CoachMarkPointer.Leading,
                targetCornerRadius = 8.dp,
                onTargetClick = {
                    readClipboardText()?.let { onIntent(ScheduleIntent.PasteFromClipboard(it)) }
                        ?: uiState.clipboardToastUrl?.let { onIntent(ScheduleIntent.ApplyClipboardToast) }
                },
            )
            else -> Unit
        }

        // 건너뛰기는 코치마크 위 레이어에 다시 그려 항상 눌리게 한다
        if (uiState.tutorialStep == TutorialStep.COPY_LINK && uiState.isGuideVisible ||
            uiState.tutorialStep == TutorialStep.PASTE_LINK
        ) {
            OnboardingSkipButton(
                onClick = { onIntent(ScheduleIntent.SkipOnboarding) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = OnboardingSkipButtonDefaults.TopMargin,
                        end = OnboardingSkipButtonDefaults.EndMargin,
                    ),
            )
        }

        uiState.clipboardToastUrl?.let { url ->
            ClipboardToast(
                url = url,
                onApply = { onIntent(ScheduleIntent.ApplyClipboardToast) },
                onDismiss = { onIntent(ScheduleIntent.DismissClipboardToast) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth(),
            )
        }
        uiState.errorToast?.let { message ->
            LinkItToast(
                text = message,
                variant = ToastVariant.Negative,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .testTag(ScheduleEditTestTags.ErrorToast),
            )
        }
    }
}

@Composable
private fun VideoLinkHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
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

/** 추천 영상(FR-019): 로딩 / 가로 목록 / 실패 + 다시 시도 */
@Composable
private fun RecommendedVideos(
    state: RecommendedVideosState,
    onCopy: (youtubeUrl: String) -> Unit,
    onRetry: () -> Unit,
    onFirstCopyButtonPositioned: (Rect) -> Unit,
) {
    Text(
        text = ScheduleEditStrings.Recommended,
        style = LinkItTheme.typography.label1NormalMedium,
        color = PaletteTokens.PingoNeutral700,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 12.dp, end = 20.dp),
    )

    when (state) {
        RecommendedVideosState.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(RecommendedVideoCardHeight)
                .testTag(ScheduleEditTestTags.RecommendedLoading),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = LinkItTheme.color.semantic.primary.normal,
                modifier = Modifier.size(28.dp),
            )
        }
        RecommendedVideosState.Error -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(RecommendedVideoCardHeight)
                .testTag(ScheduleEditTestTags.RecommendedError),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = ScheduleEditStrings.RecommendedError,
                style = LinkItTheme.typography.label2Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                textAlign = TextAlign.Center,
            )
            LinkItButton(
                onClick = onRetry,
                text = ScheduleEditStrings.Retry,
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Assistive,
                size = ButtonSize.Small,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag(ScheduleEditTestTags.RecommendedRetry),
            )
        }
        is RecommendedVideosState.Content -> LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .testTag(RecommendedVideoListTestTag),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = state.videos,
                key = { _, video -> video.videoId },
            ) { index, video ->
                RecommendedVideoCard(
                    video = video,
                    onCopy = { onCopy(video.videoUrl) },
                    onCopyButtonPositioned = if (index == 0) onFirstCopyButtonPositioned else null,
                )
            }
        }
    }
}

@Composable
private fun RecommendedVideoCard(
    video: DiscoverVideo,
    onCopy: () -> Unit,
    onCopyButtonPositioned: ((Rect) -> Unit)?,
) {
    Column(Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LinkItTheme.color.semantic.fill.normal),
        ) {
            if (!LocalInspectionMode.current) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ToastDefaults.containerColor)
                    .background(ToastDefaults.overlayColor)
                    .clickable(role = Role.Button, onClick = onCopy)
                    .then(
                        if (onCopyButtonPositioned != null) {
                            Modifier.onGloballyPositioned { onCopyButtonPositioned(it.boundsInRoot()) }
                        } else {
                            Modifier
                        },
                    )
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
                    text = ScheduleEditStrings.Copy,
                    style = LinkItTheme.typography.caption1Medium,
                    color = LinkItTheme.color.semantic.static.white,
                )
            }
        }
        Text(
            text = video.title,
            style = LinkItTheme.typography.label1NormalMedium,
            color = PaletteTokens.PingoNeutral700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = video.viewCount.toViewCountLabel(),
            style = LinkItTheme.typography.caption1Medium,
            color = PaletteTokens.PingoNeutral400,
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
    onPasteChipPositioned: (Rect) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 20.dp, top = 34.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // `복사한 링크 붙여넣기` 칩: 클립보드에 텍스트가 있을 때만 활성 (FR-022)
        LinkItButton(
            onClick = onPaste,
            text = ScheduleEditStrings.Paste,
            size = ButtonSize.Small,
            color = ButtonColor.Assistive,
            enabled = uiState.hasClipboardText,
            modifier = Modifier
                .onGloballyPositioned { onPasteChipPositioned(it.boundsInRoot()) }
                .testTag(ScheduleEditTestTags.PasteChip),
        )
        LinkItTextArea(
            value = uiState.videoLink,
            onValueChange = onValueChange,
            label = ScheduleEditStrings.LinkLabel,
            placeholder = ScheduleEditStrings.LinkPlaceholder,
            supportingText = uiState.videoLinkError?.supportingText,
            isError = uiState.videoLinkError != null,
            enabled = !uiState.isSubmittingVideoLink,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 클립보드 토스트(Figma `Clibboard Toast`): 제목 + URL + 닫기. 본문 탭 → 링크 입력 (FR-021) */
@Composable
private fun ClipboardToast(
    url: String,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = modifier
            .shadow(2.dp, LinkItTheme.shape.lg)
            .clip(LinkItTheme.shape.lg)
            .testTag(ScheduleEditTestTags.ClipboardToast),
    ) {
        Spacer(Modifier.matchParentSize().background(ToastDefaults.containerColor))
        Spacer(Modifier.matchParentSize().background(ToastDefaults.overlayColor))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onApply)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ScheduleEditStrings.ClipboardToastTitle,
                    style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
                    color = LinkItTheme.color.semantic.static.white,
                )
                Icon(
                    imageVector = LinkItIcon.Utility.Close,
                    contentDescription = "닫기",
                    tint = LinkItTheme.color.semantic.static.white,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .testTag(ScheduleEditTestTags.ClipboardToastClose),
                )
            }
            Text(
                text = url,
                style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
                color = PaletteTokens.PingoNeutral100,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
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

/** 조회수 표기: 1만 이상은 `113만회`, 그 미만은 `9,800회` */
internal fun Long.toViewCountLabel(): String {
    val body = when {
        this >= 10_000 -> "${this / 10_000}만회"
        else -> "${this.toString().reversed().chunked(3).joinToString(",").reversed()}회"
    }
    return "조회수 $body"
}

/** 사용자 노출 문자열 (contracts/domain-contracts.md §9). `RecommendedError`·토스트 신규 문구는 디자인 확인 필요(T055) */
internal object ScheduleEditStrings {
    const val Title = "영상 링크로 만들기"
    const val Skip = OnboardingSkipButtonDefaults.Text
    const val Recommended = "추천영상"
    const val Copy = "링크복사"
    const val Paste = "복사한 링크 붙여넣기"
    const val LinkLabel = "영상 링크"
    const val LinkPlaceholder = "URL 를 붙여넣거나 입력해주세요."
    const val Create = "일정 생성하기"
    const val Step3 = "링크를 복사해요"
    const val Step4 = "붙여넣어요~"
    const val ClipboardToastTitle = "클립보드에 복사한 링크"
    const val RecommendedError = "추천 영상을 불러오지 못했어요"
    const val Retry = "다시 시도"
    const val ToastInvalid = "유효한 영상 링크가 아닙니다"
    const val ToastNotRecommended = "온보딩에서는 추천 영상 링크만 사용할 수 있어요"
    const val ToastNotReady = "미리 준비된 일정을 가져오지 못했어요. 다시 시도해주세요."
}

object ScheduleEditTestTags {
    const val Skip = "schedule-edit-skip"
    const val PasteChip = "schedule-edit-paste-chip"
    const val RecommendedLoading = "schedule-edit-recommended-loading"
    const val RecommendedError = "schedule-edit-recommended-error"
    const val RecommendedRetry = "schedule-edit-recommended-retry"
    const val ClipboardToast = "schedule-edit-clipboard-toast"
    const val ClipboardToastClose = "schedule-edit-clipboard-toast-close"
    const val ErrorToast = "schedule-edit-error-toast"
}

internal const val RecommendedVideoListTestTag = "recommended-video-list"

/** 진입 후 3단계 코치마크까지의 지연. 디자인 미정(임시 600ms, research R11) */
internal const val TutorialGuideDelayMillis = 600L

/** 클립보드·오류 토스트 표시 시간(Map `ScheduleActionFeedbackDurationMillis` 와 동일) */
internal const val ScheduleToastDurationMillis = 3_000L

private val RecommendedVideoCardHeight = 134.dp

/** 상단 네비 `actions` 슬롯 안에서 `건너뛰기` 오른쪽 여백. 슬롯 자체 패딩과 합쳐 화면 가장자리에서 20dp 가 되게 한다 */
private val SkipButtonNavigationEndPadding = 12.dp
