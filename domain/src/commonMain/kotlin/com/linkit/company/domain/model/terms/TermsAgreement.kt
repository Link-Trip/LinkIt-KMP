package com.linkit.company.domain.model.terms

/**
 * 서버가 노출 대상으로 내려준 약관과 현재 회원의 동의 상태(`GET /terms`).
 *
 * @property required 필수 약관 여부. 필수 약관이 하나라도 [agreed]=false면 동의 시트를 띄운다
 * @property version 약관 버전. 개정되면 기존 동의자도 [agreed]=false로 내려온다
 * @property detailUrl 약관 전문 웹페이지 주소(웹뷰로 노출)
 * @property agreed 현재 버전 동의 여부
 */
data class TermsAgreement(
    val type: TermsDocumentType,
    val title: String,
    val required: Boolean,
    val version: Int,
    val detailUrl: String,
    val agreed: Boolean,
)
