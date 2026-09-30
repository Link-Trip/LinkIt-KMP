package com.linkit.company.data.mapper

import com.linkit.company.data.dto.terms.TermsResponse
import com.linkit.company.domain.model.terms.TermsAgreement
import com.linkit.company.domain.model.terms.TermsDocumentType

/** 클라이언트가 모르는 약관 유형은 null로 돌려 목록에서 제외한다(서버에 새 유형이 추가돼도 파싱이 깨지지 않게). */
internal fun TermsResponse.toDomainOrNull(): TermsAgreement? {
    val documentType = TermsDocumentType.entries.firstOrNull { it.name == type } ?: return null
    return TermsAgreement(
        type = documentType,
        title = title,
        required = required,
        version = version,
        detailUrl = detailUrl,
        agreed = agreed,
    )
}
