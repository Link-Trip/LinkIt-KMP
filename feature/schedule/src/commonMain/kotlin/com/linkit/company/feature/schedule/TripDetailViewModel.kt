package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.contract.Intent
import com.linkit.company.core.common.architecture.contract.SideEffect
import com.linkit.company.core.common.architecture.contract.UiState
import com.linkit.company.domain.usecase.GetTripPlanContentUseCase
import com.linkit.company.domain.usecase.TripPlanContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val isLoading: Boolean = true,
    val content: TripPlanContent? = null,
    val selectedDay: Int = 1,
    val errorMessage: String? = null,
) : UiState

sealed interface TripDetailIntent : Intent {
    data class Load(val tripPlanId: String, val focusedPlaceId: String? = null) : TripDetailIntent
    data class SelectDay(val day: Int) : TripDetailIntent
}

@ContributesIntoMap(AppScope::class)
@ViewModelKey(TripDetailViewModel::class)
@Inject
class TripDetailViewModel(
    private val getTripPlanContent: GetTripPlanContentUseCase,
) : ViewModel() {
    private var loadJob: Job? = null
    private val container = MviContainer<TripDetailIntent, SideEffect, TripDetailUiState>(
        initialState = TripDetailUiState(),
        onIntent = { intent ->
            when (intent) {
                is TripDetailIntent.Load -> load(intent)
                is TripDetailIntent.SelectDay -> reduce { copy(selectedDay = intent.day) }
            }
        },
    )
    val uiState = container.uiState

    fun onIntent(intent: TripDetailIntent) = container.intent(intent)

    private fun load(intent: TripDetailIntent.Load) {
        loadJob?.cancel()
        container.mviContext.reduce { TripDetailUiState() }
        loadJob = viewModelScope.launch {
            try {
                val content = getTripPlanContent(intent.tripPlanId)
                val focusedDay = content.tripPlan.items.firstOrNull {
                    intent.focusedPlaceId != null &&
                        (it.place?.id == intent.focusedPlaceId || it.id == intent.focusedPlaceId)
                }?.day
                container.mviContext.reduce {
                    copy(
                        isLoading = false,
                        content = content,
                        selectedDay = focusedDay ?: content.tripPlan.items.firstOrNull()?.day ?: 1,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                container.mviContext.reduce {
                    copy(isLoading = false, errorMessage = "일정을 불러오지 못했어요. 다시 시도해주세요.")
                }
            }
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        container.close()
        super.onCleared()
    }
}
