package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.UiState
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.video.DiscoverVideo

enum class TripDetailTab {
    ITINERARY,
    SUMMARY,
}

enum class VideoLinkError {
    WRONG_FORMAT,
    INVALID_LINK,
}

data class ExistingScheduleUiModel(
    val tripPlanId: String,
    val title: String,
)

/** 추천 영상 목록 로드 상태(FR-019). 빈 목록·실패는 [Error] 로 안내와 `다시 시도` 를 보여준다. */
sealed interface RecommendedVideosState {
    data object Loading : RecommendedVideosState
    data class Content(val videos: List<DiscoverVideo>) : RecommendedVideosState
    data object Error : RecommendedVideosState
}

data class ScheduleUiState(
    val videoLink: String = "",
    val videoLinkError: VideoLinkError? = null,
    val isSubmittingVideoLink: Boolean = false,
    val existingSchedule: ExistingScheduleUiModel? = null,
    val tripDetailTab: TripDetailTab = TripDetailTab.ITINERARY,
    val showTripMapPreview: Boolean = true,
    /** 서버 추천 영상(`VideoRepository.getOnboardingVideos()`, 상위 8개) */
    val recommendedVideos: RecommendedVideosState = RecommendedVideosState.Loading,
    /** 튜토리얼 단계(`OnboardingRepository.observeTutorialStep()`). null 이면 일반 모드 */
    val tutorialStep: TutorialStep? = null,
    /** 진입 후 `TutorialGuideDelayMillis` 지연 뒤 true → 3단계 코치마크 표시(FR-020) */
    val isGuideVisible: Boolean = false,
    /** `복사한 링크 붙여넣기` 활성 조건(FR-022) */
    val hasClipboardText: Boolean = false,
    /** 앱 안에서 `링크복사` 직후에만 세팅되는 클립보드 토스트 URL(FR-021) */
    val clipboardToastUrl: String? = null,
    /** 온보딩 오류 안내 토스트(3초 후 자동 해제) */
    val errorToast: String? = null,
) : UiState {
    val canCreate: Boolean
        get() = videoLink.isNotBlank() && videoLinkError == null && !isSubmittingVideoLink

    /** 온보딩 모드: 건너뛰기 노출, 추천 링크만 허용 */
    val isOnboardingMode: Boolean
        get() = tutorialStep != null

    /** 추천 영상 URL 목록(온보딩 링크 검증용). Content 가 아니면 빈 목록 */
    val recommendedVideoUrls: List<String>
        get() = (recommendedVideos as? RecommendedVideosState.Content)?.videos?.map(DiscoverVideo::videoUrl).orEmpty()
}
