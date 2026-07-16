package com.linkit.company.domain.model.place

data class Place(
    val id: String,
    val name: String,
    val googlePlaceId: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
) {
    /** 좌표가 없는 장소(지오코딩 실패 등)는 지도에 표시할 수 없다 */
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null
}
