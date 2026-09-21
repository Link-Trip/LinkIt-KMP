package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.TermsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * 약관 4종의 웹페이지 주소를 보유한다. 원격 조회 없이 상수로 제공한다.
 *
 * URL은 운영 확정 전 자리 표시 값이다. 확정되면 이 파일만 바꾸면 된다.
 */
@Inject
@ContributesBinding(DataScope::class)
class TermsRepositoryImpl : TermsRepository {

    override suspend fun getTermsDocuments(): List<TermsDocument> {
        return TermsDocumentType.entries.map { type ->
            when (type) {
                TermsDocumentType.SERVICE -> TermsDocument(type, "서비스 이용약관", "$BaseUrl/service")
                TermsDocumentType.PRIVACY -> TermsDocument(type, "개인정보 처리방침", "$BaseUrl/privacy")
                TermsDocumentType.OPEN_SOURCE -> TermsDocument(type, "오픈소스 라이센스 고지", "$BaseUrl/oss")
                TermsDocumentType.LOCATION -> TermsDocument(type, "위치기반 서비스 이용약관", "$BaseUrl/location")
            }
        }
    }

    private companion object {
        const val BaseUrl = "https://linktrip.cloud/terms"
    }
}
