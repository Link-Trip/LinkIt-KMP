package com.linkit.company.data.datasource.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.linkit.company.data.DataScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * 튜토리얼 단계를 메모리 [MutableStateFlow]로 보유하므로 `@SingleIn(DataScope::class)`가 필수다.
 * 인트로·홈·일정 Activity가 같은 인스턴스를 봐야 단계 전이가 화면 간에 전달된다.
 */
@Inject
@ContributesBinding(DataScope::class)
@SingleIn(DataScope::class)
class OnboardingLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : OnboardingLocalDataSource {

    private val tutorialStep = MutableStateFlow<String?>(null)

    override suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun saveOnboardingCompleted(value: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = value }
    }

    override suspend fun getTermsAgreedAt(): Long? {
        return dataStore.data.first()[KEY_TERMS_AGREED_AT]
    }

    override suspend fun saveTermsAgreedAt(value: Long) {
        dataStore.edit { it[KEY_TERMS_AGREED_AT] = value }
    }

    override fun observeTutorialStep(): Flow<String?> = tutorialStep.asStateFlow()

    override suspend fun saveTutorialStep(value: String?) {
        tutorialStep.value = value
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ONBOARDING_COMPLETED)
            preferences.remove(KEY_TERMS_AGREED_AT)
        }
        tutorialStep.value = null
    }

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_TERMS_AGREED_AT = longPreferencesKey("terms_agreed_at")
    }
}
