package com.linkit.company.feature.map.main

import com.linkit.company.domain.model.map.GeoCoordinate
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.model.map.TripPlanMapPlace
import com.linkit.company.domain.model.place.Place
import com.linkit.company.domain.model.place.PlaceCategory
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapUiStateTest {
    @Test
    fun selectionAndSelectedPlaceAreDerivedFromIds() {
        val noSelection = MapTestFixtures.contentState()
        val scheduleSelection = MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
        )
        val placeSelection = MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
            selectedPlaceMarkerId = MapTestFixtures.MarketMarkerId,
        )

        assertEquals(MapSelection.NONE, noSelection.selection)
        assertEquals(MapSelection.SCHEDULE, scheduleSelection.selection)
        assertEquals(MapSelection.PLACE, placeSelection.selection)
        assertEquals("광장시장", placeSelection.selectedPlace?.name)
        assertEquals(1, placeSelection.selectedPlaceIndex)
    }

    @Test
    fun filtersSchedulesByRegionStyleAndDurationTogether() {
        val state = MapTestFixtures.contentState().copy(
            selectedRegion = "서울특별시",
            selectedStyle = MapTravelStyleFilter.FOOD,
            durationFilter = MapDurationFilter.THREE_NIGHTS_FOUR_DAYS,
        )

        assertEquals(listOf(MapTestFixtures.SeoulScheduleId), state.filteredSchedules.map { it.id })
        assertEquals(listOf("서울특별시", "부산광역시"), state.availableRegions)
        assertEquals(
            listOf(
                "맛집 중심",
                "쇼핑 중심",
                "명소 탐방 중심",
                "자연·풍경 위주",
                "문화·역사 탐방",
                "액티비티",
                "힐링",
            ),
            MapTravelStyleFilter.entries.map(MapTravelStyleFilter::label),
        )
    }

    @Test
    fun durationFiltersMatchEachTripLengthWithoutOverlap() {
        assertEquals(true, MapDurationFilter.DAY_TRIP.accepts(1))
        assertEquals(true, MapDurationFilter.ONE_NIGHT_TWO_DAYS.accepts(2))
        assertEquals(true, MapDurationFilter.TWO_NIGHTS_THREE_DAYS.accepts(3))
        assertEquals(true, MapDurationFilter.THREE_NIGHTS_FOUR_DAYS.accepts(4))
        assertEquals(true, MapDurationFilter.FOUR_NIGHTS_FIVE_DAYS.accepts(5))
        assertEquals(true, MapDurationFilter.FIVE_NIGHTS_OR_MORE.accepts(6))
        assertEquals(false, MapDurationFilter.FIVE_NIGHTS_OR_MORE.accepts(5))
    }

    @Test
    fun selectedPlaceIsNullWhenMarkerDoesNotBelongToSelectedSchedule() {
        val state = MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
            selectedPlaceMarkerId = MapTestFixtures.BeachMarkerId,
        )

        assertNull(state.selectedPlace)
        assertEquals(0, state.selectedPlaceIndex)
    }

    @Test
    fun mapDataMapperSortsPlacesAndMapsServerFields() {
        val mapData = TripPlanMapData(
            summary = TripPlanSummary(
                id = "schedule-42",
                title = "제주 동쪽 2박 3일",
                videoAnalysisTaskId = "analysis-42",
                youtubeUrl = "https://www.youtube.com/watch?v=jeju",
                itemCount = 2,
                nights = 2,
                days = 3,
                hashtags = listOf("#자연", "#드라이브"),
                createdAt = "2026-07-20T09:00:00Z",
                updatedAt = "2026-07-21T09:00:00Z",
            ),
            places = listOf(
                mapPlace(
                    itemId = "item-cafe",
                    day = 2,
                    itemOrder = 1,
                    name = "세화 카페",
                    category = PlaceCategory.EAT,
                    placeId = "place-cafe",
                    address = "대한민국, 제주특별자치도 제주시 구좌읍",
                    latitude = 33.5250,
                    longitude = 126.8580,
                ),
                mapPlace(
                    itemId = "item-forest",
                    day = 1,
                    itemOrder = 2,
                    name = "비자림",
                    category = PlaceCategory.ATTRACTION,
                    placeId = "place-forest",
                    address = "대한민국, 제주특별자치도 제주시 구좌읍",
                    latitude = 33.4913,
                    longitude = 126.8114,
                ),
            ),
            center = GeoCoordinate(latitude = 33.5081, longitude = 126.8347),
        )

        val result = mapData.toMapScheduleUiModel()

        assertEquals("schedule-42", result.id)
        assertEquals("제주시", result.regionLabel)
        assertEquals(listOf("제주시"), result.regions)
        assertEquals(33.5081, result.centerLatitude)
        assertEquals(126.8347, result.centerLongitude)
        assertEquals(listOf("비자림", "세화 카페"), result.places.map { it.name })
        assertEquals(listOf("관광", "맛집"), result.places.map { it.categoryLabel })
        assertEquals("schedule-42::item-forest", result.places.first().markerId)
    }

    @Test
    fun regionFilterUsesCityAndExcludesCountryAndDistrictSegments() {
        assertEquals("도쿄", "일본, 도쿄, 신주쿠구".toCityRegionLabelOrNull())
        assertEquals("파리", "프랑스, 파리".toCityRegionLabelOrNull())
        assertEquals("서울특별시", "대한민국, 서울특별시 종로구 사직로 161".toCityRegionLabelOrNull())
        assertEquals("제주시", "대한민국, 제주특별자치도 제주시 구좌읍".toCityRegionLabelOrNull())
    }

    private fun mapPlace(
        itemId: String,
        day: Int,
        itemOrder: Int,
        name: String,
        category: PlaceCategory,
        placeId: String,
        address: String,
        latitude: Double,
        longitude: Double,
    ): TripPlanMapPlace {
        val place = Place(
            id = placeId,
            name = name,
            googlePlaceId = "google-$placeId",
            address = address,
            latitude = latitude,
            longitude = longitude,
        )
        val item = TripPlanItem(
            id = itemId,
            travelItineraryItemId = "itinerary-$itemId",
            day = day,
            itemOrder = itemOrder,
            name = name,
            category = category,
            description = "$name 설명",
            tips = "$name 팁",
            place = place,
        )
        return TripPlanMapPlace(
            item = item,
            place = place,
            coordinate = GeoCoordinate(latitude = latitude, longitude = longitude),
        )
    }
}
