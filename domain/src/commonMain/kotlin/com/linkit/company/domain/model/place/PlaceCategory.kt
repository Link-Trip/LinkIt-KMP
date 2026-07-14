package com.linkit.company.domain.model.place

enum class PlaceCategory {
    /** 음식점, 카페, 길거리 음식 */
    EAT,

    /** 관광지, 박물관, 사찰, 공원 */
    ATTRACTION,

    /** 쇼핑몰, 면세점, 기념품점 */
    SHOPPING,

    /** 공항, 기차역, 버스터미널 */
    TRANSPORTATION_HUB,

    /** 지하철, 택시, 버스 이동 */
    TRANSPORTATION_TRANSIT,

    /** 서버에 새 카테고리가 추가된 경우의 폴백 */
    UNKNOWN,
    ;

    companion object {
        fun from(value: String?): PlaceCategory =
            entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
