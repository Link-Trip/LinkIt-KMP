package com.linkit.company.data.dto.member

import kotlinx.serialization.Serializable

@Serializable
data class WithdrawMemberResponse(
    val deletedTripPlanCount: Int,
)
