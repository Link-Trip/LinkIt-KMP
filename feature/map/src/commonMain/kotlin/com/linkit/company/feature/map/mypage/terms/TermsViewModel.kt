package com.linkit.company.feature.map.mypage.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.repository.TermsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 약관 목록을 제공한다. 단순 조회라 Repository를 직접 호출한다. 목록·상세 화면이 각자 인스턴스를 가진다. */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(TermsViewModel::class)
@Inject
class TermsViewModel(
    private val termsRepository: TermsRepository,
) : ViewModel() {
    private val _documents = MutableStateFlow<List<TermsDocument>>(emptyList())
    val documents: StateFlow<List<TermsDocument>> = _documents.asStateFlow()

    init {
        viewModelScope.launch {
            _documents.value = termsRepository.getTermsDocuments()
        }
    }
}
