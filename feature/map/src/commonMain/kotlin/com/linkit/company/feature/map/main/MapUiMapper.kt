package com.linkit.company.feature.map.main

import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.model.place.PlaceCategory

internal fun TripPlanMapData.toMapScheduleUiModel(): MapScheduleUiModel {
    val placeModels = places
        .sortedWith(compareBy({ it.item.day }, { it.item.itemOrder }))
        .map { mapPlace ->
            MapPlaceUiModel(
                markerId = "${summary.id}::${mapPlace.item.id}",
                scheduleId = summary.id,
                placeId = mapPlace.place.id,
                itemId = mapPlace.item.id,
                day = mapPlace.item.day,
                itemOrder = mapPlace.item.itemOrder,
                name = mapPlace.item.name.ifBlank { mapPlace.place.name },
                categoryLabel = mapPlace.item.category.toDisplayLabel(),
                description = mapPlace.item.description,
                tips = mapPlace.item.tips,
                address = mapPlace.place.address,
                latitude = mapPlace.coordinate.latitude,
                longitude = mapPlace.coordinate.longitude,
            )
        }

    return MapScheduleUiModel(
        id = summary.id,
        title = summary.title,
        youtubeUrl = summary.youtubeUrl,
        itemCount = summary.itemCount,
        nights = summary.nights,
        days = summary.days,
        hashtags = summary.hashtags,
        regionLabel = placeModels
            .firstOrNull()
            ?.address
            ?.toRegionLabelOrNull()
            ?: "위치 미등록",
        centerLatitude = center?.latitude,
        centerLongitude = center?.longitude,
        places = placeModels,
    )
}

private fun String.toRegionLabelOrNull(): String? {
    KoreanAdministrativeArea.find(this)?.value?.let { return it }

    return split(',')
        .asReversed()
        .asSequence()
        .map(String::trim)
        .firstOrNull { segment ->
            segment.isNotBlank() &&
                segment.length <= 30 &&
                segment.none(Char::isDigit)
        }
}

private val KoreanAdministrativeArea = Regex(
    "서울특별시|부산광역시|대구광역시|인천광역시|광주광역시|대전광역시|" +
        "울산광역시|세종특별자치시|경기도|강원특별자치도|충청북도|충청남도|" +
        "전북특별자치도|전라북도|전라남도|경상북도|경상남도|제주특별자치도",
)

private fun PlaceCategory.toDisplayLabel(): String = when (this) {
    PlaceCategory.EAT -> "맛집"
    PlaceCategory.ATTRACTION -> "관광"
    PlaceCategory.SHOPPING -> "쇼핑"
    PlaceCategory.TRANSPORTATION_HUB -> "교통 거점"
    PlaceCategory.TRANSPORTATION_TRANSIT -> "이동"
    PlaceCategory.UNKNOWN -> "기타"
}
