package com.linkit.company.feature.map.main

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface MapIntent : Intent {
    data object RetryLoad : MapIntent
    data class SelectSchedule(val scheduleId: String) : MapIntent
    data class SelectPlace(
        val scheduleId: String,
        val markerId: String,
    ) : MapIntent
    data object ClearSelection : MapIntent
    data object ClosePlace : MapIntent
    data object ShowPreviousPlace : MapIntent
    data object ShowNextPlace : MapIntent
    data object ToggleCreateMenu : MapIntent
    data class ToggleScheduleMenu(val scheduleId: String) : MapIntent
    data object DismissScheduleMenu : MapIntent
    data class ShowRenameScheduleDialog(val scheduleId: String) : MapIntent
    data class ShowDeleteScheduleDialog(val scheduleId: String) : MapIntent
    data class UpdateScheduleName(val value: String) : MapIntent
    data object ConfirmScheduleRename : MapIntent
    data object ConfirmScheduleDelete : MapIntent
    data object DismissScheduleDialog : MapIntent
    data object DismissScheduleActionFeedback : MapIntent
    data object ShowComingSoonDialog : MapIntent
    data object DismissComingSoonDialog : MapIntent
    data object ToggleMapType : MapIntent

    /** 생성 메뉴 `영상 링크로 만들기` 선택. 메뉴를 닫고 튜토리얼 2단계면 3단계로 넘긴 뒤 화면이 이동한다 */
    data object SelectCreateFromVideo : MapIntent

    /** 일정 상세를 열었다 → `확인후`로 기록 (FR-030) */
    data class ScheduleOpened(val scheduleId: String) : MapIntent

    /** 튜토리얼 우상단 `건너뛰기` → 온보딩 완료 기록 후 일반 모드 */
    data object SkipOnboarding : MapIntent

    /** 저장 일정 목록 재조회(튜토리얼 복귀 등 내부용) */
    data object RefreshSchedules : MapIntent
    data class ToggleFilter(val filter: MapFilterType) : MapIntent
    data class SelectRegion(val region: String?) : MapIntent
    data class SelectStyle(val style: MapTravelStyleFilter?) : MapIntent
    data class SelectDuration(val duration: MapDurationFilter) : MapIntent
    data object RequestCurrentLocation : MapIntent
    data class CurrentLocationResolved(
        val latitude: Double,
        val longitude: Double,
    ) : MapIntent
    data class CurrentLocationUnavailable(val message: String) : MapIntent
    data class CameraChanged(
        val latitude: Double,
        val longitude: Double,
        val zoom: Float,
    ) : MapIntent
    data class MapCenterLocationResolved(
        val latitude: Double,
        val longitude: Double,
        val label: String,
    ) : MapIntent
}
