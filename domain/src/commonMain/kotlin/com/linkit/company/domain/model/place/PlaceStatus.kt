package com.linkit.company.domain.model.place

/** 일정 아이템의 Google Places 정보 연결 상태 */
enum class PlaceStatus {
    FOUND,
    PENDING,
    SEARCHING,
    NOT_FOUND,
    NOT_REQUIRED,

    /** 서버에 새 상태가 추가된 경우의 폴백 */
    UNKNOWN,
    ;

    companion object {
        fun from(value: String?): PlaceStatus =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
