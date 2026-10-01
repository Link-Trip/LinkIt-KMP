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
    ALREADY_IN_PROGRESS,
}

enum class TripDetailDialog {
    RENAME,
    DELETE,
}

data class ExistingScheduleUiModel(
    val tripPlanId: String,
    val title: String,
)

data class ScheduleUiState(
    val recommendedVideos: List<DiscoverVideo> = emptyList(),
    val isLoadingRecommendedVideos: Boolean = true,
    val recommendedVideosError: String? = null,
    val areRecommendedVideosExpanded: Boolean = false,
    val videoLink: String = "",
    val videoLinkError: VideoLinkError? = null,
    val isSubmittingVideoLink: Boolean = false,
    val existingSchedule: ExistingScheduleUiModel? = null,
    val tripDetailTab: TripDetailTab = TripDetailTab.ITINERARY,
    val showTripMapPreview: Boolean = true,
    val tripDetailMenuExpanded: Boolean = false,
    val tripDetailDialog: TripDetailDialog? = null,
    val tripDetailActionTripPlanId: String? = null,
    val tripDetailNameDraft: String = "",
    val renamedTripPlanId: String? = null,
    val renamedTripPlanTitle: String? = null,
    val isTripDetailActionInProgress: Boolean = false,
    val tripDetailActionError: String? = null,
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

    /** 현재 추천 영상 URL 목록(온보딩 링크 검증용). */
    val recommendedVideoUrls: List<String>
        get() = recommendedVideos.map(DiscoverVideo::videoUrl)
}
