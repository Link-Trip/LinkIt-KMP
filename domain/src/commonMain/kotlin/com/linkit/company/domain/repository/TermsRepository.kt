package com.linkit.company.domain.repository

import com.linkit.company.domain.model.terms.TermsDocument

interface TermsRepository {

    /** 열람 가능한 약관 4종을 고정 순서로 돌려준다. */
    suspend fun getTermsDocuments(): List<TermsDocument>
}
