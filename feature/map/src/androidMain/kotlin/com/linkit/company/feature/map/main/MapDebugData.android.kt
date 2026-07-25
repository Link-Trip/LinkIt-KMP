package com.linkit.company.feature.map.main

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.linkit.company.domain.model.map.GeoCoordinate
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.model.map.TripPlanMapPlace
import com.linkit.company.domain.model.map.boundingBoxCenterOrNull
import com.linkit.company.domain.model.place.Place
import com.linkit.company.domain.model.place.PlaceCategory
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanSummary

@Composable
internal actual fun rememberMapDebugData(): List<TripPlanMapData>? {
    val context = LocalContext.current
    val isDebuggable = remember(context) {
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
    return MapDebugMockData.schedules.takeIf { isDebuggable }
}

internal object MapDebugMockData {
    val schedules = listOf(
        schedule(
            id = "mock-tokyo-shinjuku",
            title = "도쿄 신주쿠 핵심 3박 4일",
            nights = 3,
            days = 4,
            hashtags = listOf("맛집 중심", "야경", "도시 여행"),
            places = listOf(
                place(
                    id = "shinjuku-gyoen",
                    day = 1,
                    order = 1,
                    name = "신주쿠 교엔",
                    category = PlaceCategory.ATTRACTION,
                    description = "도심 한가운데 펼쳐진 정원을 천천히 산책해요.",
                    tips = "오전 개장 시간에 맞추면 한적하게 둘러보기 좋아요.",
                    address = "일본, 도쿄, 신주쿠",
                    latitude = 35.6852,
                    longitude = 139.7100,
                ),
                place(
                    id = "omoide-yokocho",
                    day = 1,
                    order = 2,
                    name = "오모이데 요코초",
                    category = PlaceCategory.EAT,
                    description = "작은 이자카야가 모인 골목에서 도쿄의 밤을 즐겨요.",
                    tips = "좌석이 적어 저녁 피크 전 방문을 추천해요.",
                    address = "일본, 도쿄, 신주쿠",
                    latitude = 35.6939,
                    longitude = 139.6996,
                ),
                place(
                    id = "tokyo-metropolitan-observatory",
                    day = 2,
                    order = 1,
                    name = "도쿄도청 전망대",
                    category = PlaceCategory.ATTRACTION,
                    description = "신주쿠 고층 빌딩과 도쿄 전경을 한눈에 감상해요.",
                    tips = "입장 마감 시간과 휴관일을 미리 확인해 주세요.",
                    address = "일본, 도쿄, 신주쿠",
                    latitude = 35.6896,
                    longitude = 139.6921,
                ),
                place(
                    id = "nakano-broadway",
                    day = 3,
                    order = 1,
                    name = "나카노 브로드웨이",
                    category = PlaceCategory.SHOPPING,
                    description = "빈티지 숍과 서브컬처 상점을 천천히 구경해요.",
                    tips = "매장별 휴무일이 달라 방문 전에 확인하면 좋아요.",
                    address = "일본, 도쿄, 신주쿠",
                    latitude = 35.7090,
                    longitude = 139.6657,
                ),
                place(
                    id = "kagurazaka",
                    day = 3,
                    order = 2,
                    name = "가구라자카",
                    category = PlaceCategory.EAT,
                    description = "돌담 골목을 걸으며 작은 디저트 가게를 찾아봐요.",
                    tips = "메인 거리에서 한 블록 들어간 골목도 함께 둘러보세요.",
                    address = "일본, 도쿄, 신주쿠",
                    latitude = 35.7038,
                    longitude = 139.7342,
                ),
            ),
        ),
        schedule(
            id = "mock-tokyo-harajuku",
            title = "하라주쿠 감성 1박 2일",
            nights = 1,
            days = 2,
            hashtags = listOf("SNS 핫플레이스", "카페", "쇼핑 중심"),
            places = listOf(
                place(
                    id = "meiji-jingu",
                    day = 1,
                    order = 1,
                    name = "메이지 신궁",
                    category = PlaceCategory.ATTRACTION,
                    description = "울창한 숲길을 지나 고요한 신궁을 둘러봐요.",
                    tips = "입구부터 본전까지 걷는 시간을 넉넉히 잡아 주세요.",
                    address = "일본, 도쿄, 하라주쿠",
                    latitude = 35.6764,
                    longitude = 139.6993,
                ),
                place(
                    id = "takeshita-street",
                    day = 1,
                    order = 2,
                    name = "다케시타 거리",
                    category = PlaceCategory.SHOPPING,
                    description = "개성 있는 패션 숍과 디저트 가게를 구경해요.",
                    tips = "주말 오후에는 혼잡하니 오전 방문이 편해요.",
                    address = "일본, 도쿄, 하라주쿠",
                    latitude = 35.6716,
                    longitude = 139.7030,
                ),
                place(
                    id = "omotesando",
                    day = 2,
                    order = 1,
                    name = "오모테산도",
                    category = PlaceCategory.EAT,
                    description = "건축과 카페를 함께 즐기며 대로를 산책해요.",
                    tips = "인기 카페는 오픈 시간에 맞춰 가는 편이 좋아요.",
                    address = "일본, 도쿄, 하라주쿠",
                    latitude = 35.6652,
                    longitude = 139.7124,
                ),
            ),
        ),
        schedule(
            id = "mock-tokyo-shibuya",
            title = "시부야 천천히 즐기는 6박 7일",
            nights = 6,
            days = 7,
            hashtags = listOf("장기 여행", "전망", "로컬 산책"),
            places = listOf(
                place(
                    id = "shibuya-scramble",
                    day = 1,
                    order = 1,
                    name = "시부야 스크램블 교차로",
                    category = PlaceCategory.ATTRACTION,
                    description = "수많은 사람이 교차하는 도쿄의 대표 장면을 만나봐요.",
                    tips = "주변 전망 공간에서 내려다보면 흐름이 잘 보여요.",
                    address = "일본, 도쿄, 시부야",
                    latitude = 35.6595,
                    longitude = 139.7005,
                ),
                place(
                    id = "shibuya-sky",
                    day = 2,
                    order = 1,
                    name = "시부야 스카이",
                    category = PlaceCategory.ATTRACTION,
                    description = "루프톱에서 도쿄의 일몰과 야경을 감상해요.",
                    tips = "일몰 시간대는 매진이 빠르니 사전 예약을 추천해요.",
                    address = "일본, 도쿄, 시부야",
                    latitude = 35.6585,
                    longitude = 139.7020,
                ),
                place(
                    id = "miyashita-park",
                    day = 3,
                    order = 1,
                    name = "미야시타 파크",
                    category = PlaceCategory.SHOPPING,
                    description = "옥상 공원과 쇼핑 공간을 이어서 둘러봐요.",
                    tips = "해 질 무렵 옥상 공원에서 잠시 쉬어가기 좋아요.",
                    address = "일본, 도쿄, 시부야",
                    latitude = 35.6618,
                    longitude = 139.7011,
                ),
                place(
                    id = "shibuya-stream",
                    day = 4,
                    order = 1,
                    name = "시부야 스트림",
                    category = PlaceCategory.EAT,
                    description = "강변 산책로를 따라 로컬 맛집을 찾아봐요.",
                    tips = "평일 점심 피크를 피하면 여유롭게 식사할 수 있어요.",
                    address = "일본, 도쿄, 시부야",
                    latitude = 35.6577,
                    longitude = 139.7039,
                ),
            ),
        ),
    )
}

private data class MockPlace(
    val id: String,
    val day: Int,
    val order: Int,
    val name: String,
    val category: PlaceCategory,
    val description: String,
    val tips: String,
    val address: String,
    val coordinate: GeoCoordinate,
)

private fun place(
    id: String,
    day: Int,
    order: Int,
    name: String,
    category: PlaceCategory,
    description: String,
    tips: String,
    address: String,
    latitude: Double,
    longitude: Double,
) = MockPlace(
    id = id,
    day = day,
    order = order,
    name = name,
    category = category,
    description = description,
    tips = tips,
    address = address,
    coordinate = GeoCoordinate(latitude, longitude),
)

private fun schedule(
    id: String,
    title: String,
    nights: Int,
    days: Int,
    hashtags: List<String>,
    places: List<MockPlace>,
): TripPlanMapData {
    val mapPlaces = places.map { mock ->
        TripPlanMapPlace(
            item = TripPlanItem(
                id = "mock-item-${mock.id}",
                travelItineraryItemId = "mock-itinerary-${mock.id}",
                day = mock.day,
                itemOrder = mock.order,
                name = mock.name,
                category = mock.category,
                description = mock.description,
                tips = mock.tips,
                place = Place(
                    id = "mock-place-${mock.id}",
                    name = mock.name,
                    googlePlaceId = "mock-google-${mock.id}",
                    address = mock.address,
                    latitude = mock.coordinate.latitude,
                    longitude = mock.coordinate.longitude,
                ),
            ),
            place = Place(
                id = "mock-place-${mock.id}",
                name = mock.name,
                googlePlaceId = "mock-google-${mock.id}",
                address = mock.address,
                latitude = mock.coordinate.latitude,
                longitude = mock.coordinate.longitude,
            ),
            coordinate = mock.coordinate,
        )
    }

    return TripPlanMapData(
        summary = TripPlanSummary(
            id = id,
            title = title,
            videoAnalysisTaskId = "mock-analysis-$id",
            youtubeUrl = "https://www.youtube.com/watch?v=$id",
            itemCount = mapPlaces.size,
            nights = nights,
            days = days,
            hashtags = hashtags,
            createdAt = "2026-07-25T09:00:00Z",
            updatedAt = "2026-07-25T09:00:00Z",
        ),
        places = mapPlaces,
        center = mapPlaces.map(TripPlanMapPlace::coordinate).boundingBoxCenterOrNull(),
    )
}
