package com.linkit.company.domain.model.video

/** 예상 비용 산정 기준 */
enum class CostBasis {
    /** 영상에서 직접 언급된 비용 기반 */
    VIDEO_MENTIONED,

    /** 일정 아이템 기반 추정 */
    ITEM_ESTIMATED,

    /** 서버에 새 기준이 추가된 경우의 폴백 */
    UNKNOWN,
    ;

    companion object {
        fun from(value: String?): CostBasis =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
