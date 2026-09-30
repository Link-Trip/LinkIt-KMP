package com.linkit.company.domain.repository

import com.linkit.company.domain.model.terms.TermsAgreement
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType

interface TermsRepository {

    /**
     * 열람 가능한 약관 4종을 고정 순서로 돌려준다.
     * 서버 약관 목록을 조회한 뒤라면 서비스·개인정보 약관의 주소는 서버 `detailUrl`로 대체된다.
     */
    suspend fun getTermsDocuments(): List<TermsDocument>

    /** 서버가 노출 대상으로 정한 약관과 로그인한 회원의 동의 여부. 인증이 필요하다. */
    suspend fun getTermsAgreements(): List<TermsAgreement>

    /** 약관 동의를 서버에 기록한다. 이미 동의한 약관이 섞여도 성공한다(멱등). */
    suspend fun agreeTerms(types: List<TermsDocumentType>)
}
