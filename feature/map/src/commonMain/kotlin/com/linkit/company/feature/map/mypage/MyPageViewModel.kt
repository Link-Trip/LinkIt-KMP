package com.linkit.company.feature.map.mypage

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MyPageViewModel::class)
@Inject
class MyPageViewModel : ViewModel() {
    private val container = MviContainer<MyPageIntent, MyPageSideEffect, MyPageUiState>(
        initialState = MyPageUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: MyPageIntent) = container.intent(intent)

    private fun MviContext<MyPageUiState, MyPageSideEffect>.handleIntent(intent: MyPageIntent) {
        when (intent) {
            is MyPageIntent.SelectMapType -> reduce { copy(selectedMapType = intent.type) }
            MyPageIntent.ShowResetDialog -> reduce { copy(showResetDialog = true) }
            MyPageIntent.DismissResetDialog -> reduce { copy(showResetDialog = false) }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}
