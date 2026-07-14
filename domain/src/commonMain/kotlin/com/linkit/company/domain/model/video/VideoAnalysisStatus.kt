package com.linkit.company.domain.model.video

enum class VideoAnalysisStatus {
    /** 분석 진행 중 — 폴링 필요 */
    PENDING,

    /** 분석 완료 */
    COMPLETED,

    /** 여행 영상이 아니거나 분석 불가능한 영상 */
    INVALID,

    /** 분석 실패 — 동일 URL 재요청 시 재분석 */
    FAILED,

    /** 서버에 새 상태가 추가된 경우의 폴백 */
    UNKNOWN,
    ;

    companion object {
        fun from(value: String?): VideoAnalysisStatus =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
