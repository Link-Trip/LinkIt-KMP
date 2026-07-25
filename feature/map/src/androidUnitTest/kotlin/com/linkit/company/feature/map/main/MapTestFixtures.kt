package com.linkit.company.feature.map.main

internal object MapTestFixtures {
    const val SeoulScheduleId = "schedule-seoul"
    const val BusanScheduleId = "schedule-busan"
    const val PalaceMarkerId = "$SeoulScheduleId::item-palace"
    const val MarketMarkerId = "$SeoulScheduleId::item-market"
    const val BeachMarkerId = "$BusanScheduleId::item-beach"

    val schedules = listOf(
        MapScheduleUiModel(
            id = SeoulScheduleId,
            title = "서울 골목과 궁궐 3박 4일",
            youtubeUrl = "https://www.youtube.com/watch?v=seoul",
            itemCount = 2,
            nights = 3,
            days = 4,
            hashtags = listOf("#도시여행", "#맛집"),
            regionLabel = "서울특별시",
            centerLatitude = 37.5756,
            centerLongitude = 126.9849,
            places = listOf(
                MapPlaceUiModel(
                    markerId = PalaceMarkerId,
                    scheduleId = SeoulScheduleId,
                    placeId = "place-palace",
                    itemId = "item-palace",
                    day = 1,
                    itemOrder = 1,
                    name = "경복궁",
                    categoryLabel = "관광",
                    description = "한복을 입고 조선의 궁궐과 주변 골목을 둘러봐요.",
                    tips = "오전 일찍 방문하면 비교적 여유로워요.",
                    address = "대한민국, 서울특별시 종로구 사직로 161",
                    latitude = 37.5796,
                    longitude = 126.9770,
                ),
                MapPlaceUiModel(
                    markerId = MarketMarkerId,
                    scheduleId = SeoulScheduleId,
                    placeId = "place-market",
                    itemId = "item-market",
                    day = 2,
                    itemOrder = 2,
                    name = "광장시장",
                    categoryLabel = "맛집",
                    description = "빈대떡과 마약김밥 등 서울의 시장 음식을 즐겨요.",
                    tips = "현금과 카드를 함께 준비하면 편해요.",
                    address = "대한민국, 서울특별시 종로구 창경궁로 88",
                    latitude = 37.5700,
                    longitude = 126.9996,
                ),
            ),
        ),
        MapScheduleUiModel(
            id = BusanScheduleId,
            title = "부산 바다 따라 1박 2일",
            youtubeUrl = "https://www.youtube.com/watch?v=busan",
            itemCount = 1,
            nights = 1,
            days = 2,
            hashtags = listOf("#힐링", "#바다"),
            regionLabel = "부산광역시",
            centerLatitude = 35.1587,
            centerLongitude = 129.1604,
            places = listOf(
                MapPlaceUiModel(
                    markerId = BeachMarkerId,
                    scheduleId = BusanScheduleId,
                    placeId = "place-beach",
                    itemId = "item-beach",
                    day = 1,
                    itemOrder = 1,
                    name = "광안리해수욕장",
                    categoryLabel = "관광",
                    description = "광안대교가 보이는 해변을 산책하며 야경을 감상해요.",
                    tips = "해 질 무렵부터 야경까지 이어서 보기 좋아요.",
                    address = "대한민국, 부산광역시 수영구 광안해변로 219",
                    latitude = 35.1532,
                    longitude = 129.1187,
                ),
            ),
        ),
    )

    fun contentState(
        selectedScheduleId: String? = null,
        selectedPlaceMarkerId: String? = null,
    ) = MapUiState(
        loadState = MapLoadState.CONTENT,
        schedules = schedules,
        selectedScheduleId = selectedScheduleId,
        selectedPlaceMarkerId = selectedPlaceMarkerId,
    )
}
