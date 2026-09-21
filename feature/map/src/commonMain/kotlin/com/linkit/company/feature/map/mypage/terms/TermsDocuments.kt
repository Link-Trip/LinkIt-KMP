package com.linkit.company.feature.map.mypage.terms

import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.feature.map.mypage.MyPageStrings

/**
 * 약관 4종 카탈로그. 목록 순서는 [TermsDocumentType] 선언 순서를 따른다.
 *
 * URL은 운영 확정 전 자리 표시 값이다. TODO(#45): `TermsRepository` 로 이관.
 */
internal object TermsDocuments {
    val all: List<TermsDocument> = TermsDocumentType.entries.map { type -> type.toDocument() }

    fun find(type: TermsDocumentType): TermsDocument = all.first { it.type == type }

    private fun TermsDocumentType.toDocument(): TermsDocument = when (this) {
        TermsDocumentType.SERVICE -> TermsDocument(this, MyPageStrings.TermsService, "https://linktrip.cloud/terms/service")
        TermsDocumentType.PRIVACY -> TermsDocument(this, MyPageStrings.TermsPrivacy, "https://linktrip.cloud/terms/privacy")
        TermsDocumentType.OPEN_SOURCE -> TermsDocument(this, MyPageStrings.TermsOpenSource, "https://linktrip.cloud/terms/oss")
        TermsDocumentType.LOCATION -> TermsDocument(this, MyPageStrings.TermsLocation, "https://linktrip.cloud/terms/location")
    }
}
