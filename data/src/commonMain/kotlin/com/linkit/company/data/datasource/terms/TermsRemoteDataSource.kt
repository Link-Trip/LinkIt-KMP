package com.linkit.company.data.datasource.terms

import com.linkit.company.data.dto.terms.TermsResponse

interface TermsRemoteDataSource {
    suspend fun getTerms(): List<TermsResponse>

    /** @param types 서버 약관 유형 문자열(`SERVICE`, `PRIVACY`) */
    suspend fun agree(types: List<String>)
}
