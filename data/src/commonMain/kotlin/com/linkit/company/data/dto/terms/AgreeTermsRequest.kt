package com.linkit.company.data.dto.terms

import kotlinx.serialization.Serializable

@Serializable
internal data class AgreeTermsRequest(
    val types: List<String>,
)
