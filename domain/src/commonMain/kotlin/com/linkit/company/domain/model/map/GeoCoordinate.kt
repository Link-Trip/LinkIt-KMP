package com.linkit.company.domain.model.map

/**
 * 플랫폼 지도 SDK에 의존하지 않는 위경도 좌표.
 *
 * 서버처럼 신뢰할 수 없는 입력은 [fromOrNull]을 사용해 유효한 좌표만 변환한다.
 */
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude.isFinite() && latitude in MIN_LATITUDE..MAX_LATITUDE) {
            "latitude must be finite and between $MIN_LATITUDE and $MAX_LATITUDE"
        }
        require(longitude.isFinite() && longitude in MIN_LONGITUDE..MAX_LONGITUDE) {
            "longitude must be finite and between $MIN_LONGITUDE and $MAX_LONGITUDE"
        }
    }

    companion object {
        private const val MIN_LATITUDE = -90.0
        private const val MAX_LATITUDE = 90.0
        private const val MIN_LONGITUDE = -180.0
        private const val MAX_LONGITUDE = 180.0

        fun fromOrNull(
            latitude: Double?,
            longitude: Double?,
        ): GeoCoordinate? {
            if (latitude == null || longitude == null) return null
            if (!latitude.isFinite() || latitude !in MIN_LATITUDE..MAX_LATITUDE) return null
            if (!longitude.isFinite() || longitude !in MIN_LONGITUDE..MAX_LONGITUDE) return null

            return GeoCoordinate(latitude = latitude, longitude = longitude)
        }
    }
}

/**
 * 좌표들의 위도/경도 최솟값과 최댓값으로 만든 경계 사각형의 중앙을 계산한다.
 */
fun Iterable<GeoCoordinate>.boundingBoxCenterOrNull(): GeoCoordinate? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null

    val first = iterator.next()
    var minLatitude = first.latitude
    var maxLatitude = first.latitude
    var minLongitude = first.longitude
    var maxLongitude = first.longitude

    while (iterator.hasNext()) {
        val coordinate = iterator.next()
        minLatitude = minOf(minLatitude, coordinate.latitude)
        maxLatitude = maxOf(maxLatitude, coordinate.latitude)
        minLongitude = minOf(minLongitude, coordinate.longitude)
        maxLongitude = maxOf(maxLongitude, coordinate.longitude)
    }

    return GeoCoordinate(
        latitude = (minLatitude + maxLatitude) / 2,
        longitude = (minLongitude + maxLongitude) / 2,
    )
}
