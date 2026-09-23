package com.linkit.company.feature.intro

import com.linkit.company.core.common.architecture.contract.Intent
import com.linkit.company.domain.model.terms.TermsDocumentType

sealed interface IntroIntent : Intent {
    /** 인트로 애니메이션 종료. 목적지(온보딩 시작/메인)는 ViewModel이 결정한다 */
    data object SplashFinished : IntroIntent

    /** `30초 만에 사용법 보기` */
    data object TapSeeHowTo : IntroIntent

    /** `바로 시작하기` */
    data object TapStartNow : IntroIntent

    data object ToggleAllTerms : IntroIntent
    data object ToggleServiceTerms : IntroIntent
    data object TogglePrivacyTerms : IntroIntent

    /** `상세보기` — 체크 상태는 유지된다(FR-009) */
    data class OpenTermsDetail(val type: TermsDocumentType) : IntroIntent

    /** 시트 끌어 내리기·바깥 탭 — 아무것도 기록하지 않는다(FR-011) */
    data object DismissTermsSheet : IntroIntent

    /** `동의하고 시작하기` */
    data object AgreeAndStart : IntroIntent
}
