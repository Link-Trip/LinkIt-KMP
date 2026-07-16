package com.linkit.company.data.datasource.tripplan

/**
 * DataSource 파라미터용 아이템 재배치 값 객체.
 *
 * DataSource는 domain 모델을 받지 않고, Request DTO 조립은 DataSourceImpl 책임이므로
 * 복합 파라미터는 datasource 패키지의 Param 클래스로 전달한다.
 * (domain TripPlanItemOrder → Param 변환은 RepositoryImpl 책임)
 */
data class TripPlanItemOrderParam(
    val tripPlanItemId: String,
    val day: Int,
    val itemOrder: Int,
)
