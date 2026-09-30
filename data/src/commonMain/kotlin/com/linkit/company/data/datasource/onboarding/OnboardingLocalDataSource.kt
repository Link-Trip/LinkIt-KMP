package com.linkit.company.data.datasource.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingLocalDataSource {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun saveOnboardingCompleted(value: Boolean)

    /** 약관 동의 시각(epoch millis). 없으면 null. */
    suspend fun getTermsAgreedAt(): Long?
    suspend fun saveTermsAgreedAt(value: Long)

    /** 튜토리얼 단계 enum name. 메모리에만 있고 초기값은 null. */
    fun observeTutorialStep(): Flow<String?>
    suspend fun saveTutorialStep(value: String?)

    /** `onboarding_completed`·`terms_agreed_at` 제거 + 튜토리얼 단계 null. */
    suspend fun clearAll()
}
