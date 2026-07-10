package com.linkit.company.feature.intro

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface IntroIntent : Intent {
    data object ZoomToSeoul : IntroIntent
}
