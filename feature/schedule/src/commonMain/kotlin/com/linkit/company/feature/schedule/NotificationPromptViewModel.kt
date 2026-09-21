package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 알림 안내 바텀시트 노출 이력을 앱 설정 저장소(DataStore)로 읽고 쓴다.
 * 앱 초기화 시 함께 지워져 첫 설치처럼 다시 안내한다.
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(NotificationPromptViewModel::class)
@Inject
class NotificationPromptViewModel(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    /** null = 아직 읽는 중. 읽기 전에는 시트를 띄우지 않는다. */
    private val _isPrompted = MutableStateFlow<Boolean?>(null)
    val isPrompted: StateFlow<Boolean?> = _isPrompted.asStateFlow()

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
