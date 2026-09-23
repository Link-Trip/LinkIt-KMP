package com.linkit.company.feature.intro

import com.linkit.company.core.common.architecture.contract.SideEffect
import com.linkit.company.domain.model.terms.TermsDocumentType

sealed interface IntroSideEffect : SideEffect {
    data object NavigateToOnboardingStart : IntroSideEffect
    data class NavigateToTermsDetail(val type: TermsDocumentType) : IntroSideEffect
    data object NavigateToHome : IntroSideEffect
}
