package com.linkit.company.domain.model.map

import com.linkit.company.domain.model.place.Place
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanSummary

/** 저장 일정 하나를 지도와 일정 목록에서 함께 사용하기 위한 데이터. */
data class TripPlanMapData(
    val summary: TripPlanSummary,
    val places: List<TripPlanMapPlace>,
    /** 지도에 표시할 수 있는 장소가 없으면 null이다. */
    val center: GeoCoordinate?,
)

/** 유효한 좌표를 가진 일정 아이템과 장소. */
data class TripPlanMapPlace(
    val item: TripPlanItem,
    val place: Place,
    val coordinate: GeoCoordinate,
)
