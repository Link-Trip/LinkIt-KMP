package com.linkit.company.domain.repository

import com.linkit.company.domain.model.onboarding.TutorialStep
import kotlinx.coroutines.flow.Flow

/**
 * 최초 진입 상태 저장소: 온보딩 완료 여부, 약관 동의 시각, 튜토리얼 단계.
 *
 * 완료 여부와 약관 동의 시각은 기기에 저장되어 재실행 후에도 유지되고, 튜토리얼 단계는 프로세스 메모리에만 있다.
 * 앱 초기화 시 [clearAll]로 첫 설치 상태로 되돌린다.
 */
interface OnboardingRepository {

    /** 저장값이 없으면 false. */
    suspend fun isOnboardingCompleted(): Boolean

    suspend fun setOnboardingCompleted(completed: Boolean)

    /** 약관 동의 시각이 기록되어 있으면 true. */
    suspend fun isTermsAgreed(): Boolean

    suspend fun setTermsAgreed(agreedAtEpochMillis: Long)

    /** 튜토리얼 단계. 초기값과 튜토리얼이 아닐 때는 null. */
    fun observeTutorialStep(): Flow<TutorialStep?>

    suspend fun setTutorialStep(step: TutorialStep?)

    /** 완료 여부·약관 동의 시각을 제거하고 튜토리얼 단계를 null로 되돌린다. */
    suspend fun clearAll()
}
