package com.linkit.company.domain.model.terms

/**
 * 열람 가능한 약관 문서.
 *
 * @property title 앱에 표시할 제목
 * @property url 운영 측이 관리하는 웹페이지 주소
 */
data class TermsDocument(
    val type: TermsDocumentType,
    val title: String,
    val url: String,
)
