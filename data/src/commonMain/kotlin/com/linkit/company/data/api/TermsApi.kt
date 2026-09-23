package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.terms.AgreeTermsRequest
import com.linkit.company.data.dto.terms.TermsListResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

internal interface TermsApi {

    /** 노출 대상 약관 목록과 로그인한 회원의 동의 여부. 개정 시 기존 동의자도 `agreed=false`. */
    @GET("terms")
    suspend fun getTerms(): ApiResponse<TermsListResponse>

    /**
     * 약관 동의 기록(멱등). 필수 약관 누락 시 400 `BAD_REQUEST_TERMS_REQUIRED`,
     * 미지원 유형은 400 `BAD_REQUEST_TERMS_TYPE`.
     */
    @POST("terms/agreement")
    @Headers("Content-Type: application/json")
    suspend fun agree(@Body request: AgreeTermsRequest): ApiResponse<Unit>
}
