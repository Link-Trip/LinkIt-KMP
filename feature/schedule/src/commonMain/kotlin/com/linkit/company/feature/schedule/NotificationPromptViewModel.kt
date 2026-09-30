package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.OnboardingRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 알림 안내 바텀시트 노출 이력을 앱 설정 저장소(DataStore)로 읽고 쓴다.
 * 앱 초기화 시 함께 지워져 첫 설치처럼 다시 안내한다.
 *
 * 튜토리얼 중(온보딩 모드)에는 코치마크 위에 시트가 겹치지 않도록 띄우지 않는다 (research R8, SC-005).
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(NotificationPromptViewModel::class)
@Inject
class NotificationPromptViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    onboardingRepository: OnboardingRepository,
) : ViewModel() {
    /** null = 아직 읽는 중. 읽기 전에는 시트를 띄우지 않는다. */
    private val _isPrompted = MutableStateFlow<Boolean?>(null)
    val isPrompted: StateFlow<Boolean?> = _isPrompted.asStateFlow()

    /** null = 아직 읽는 중. 튜토리얼 단계가 있으면 true. 읽기 전에는 시트를 띄우지 않는다. */
    val isOnboardingMode: StateFlow<Boolean?> = onboardingRepository.observeTutorialStep()
        .map<Any?, Boolean?> { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    init {
        viewModelScope.launch {
            _isPrompted.value = appSettingsRepository.isNotificationPrompted()
        }
    }

    fun markPrompted() {
        _isPrompted.value = true
        viewModelScope.launch { appSettingsRepository.setNotificationPrompted(true) }
    }
}
