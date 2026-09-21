package com.linkit.company.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.usecase.GetExploreCatalogUseCase
import com.linkit.company.domain.usecase.GetExploreVideosUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ExploreViewModel::class)
@Inject
class ExploreViewModel(
    private val getExploreVideos: GetExploreVideosUseCase,
    private val getExploreCatalog: GetExploreCatalogUseCase,
) : ViewModel() {
    private val container = MviContainer<ExploreIntent, ExploreSideEffect, ExploreUiState>(
        initialState = ExploreUiState(),
        onIntent = { handleIntent(it) },
    )
    private var videosJob: Job? = null
    private var catalogJob: Job? = null
    private var videosGeneration = 0
    private val requestedCursors = mutableSetOf<String>()
    val uiState = container.uiState

    init {
        loadVideos()
        loadCatalog()
    }

    fun onIntent(intent: ExploreIntent) = container.intent(intent)

    private fun MviContext<ExploreUiState, ExploreSideEffect>.handleIntent(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.SelectCountry -> {
                reduce { copy(selectedCountry = intent.country, selectedRegion = null) }
                loadVideos()
            }
            is ExploreIntent.SelectRegion -> {
                reduce { copy(selectedCountry = null, selectedRegion = intent.region) }
                loadVideos()
            }
            is ExploreIntent.SelectTab -> {
                if (currentState.selectedTab == intent.tab) return
                reduce { copy(selectedTab = intent.tab) }
                loadVideos()
            }
            is ExploreIntent.SelectTheme -> {
                reduce { copy(selectedTheme = intent.theme) }
                loadVideos()
            }
            is ExploreIntent.SelectChannel -> {
                if (currentState.channels.any { it.channelId == intent.channelId }) {
                    reduce { copy(selectedChannelId = intent.channelId) }
                }
            }
            ExploreIntent.RetryVideos -> loadVideos(append = currentState.hasNext && currentState.nextCursor != null)
            ExploreIntent.RetryCatalog -> loadCatalog()
            ExploreIntent.LoadMore -> loadVideos(append = true)
            ExploreIntent.LinkOpenFailed -> reduce { copy(linkErrorMessage = "링크를 열지 못했어요. 잠시 후 다시 시도해 주세요.") }
            ExploreIntent.DismissLinkError -> reduce { copy(linkErrorMessage = null) }
        }
    }

    private fun loadCatalog() {
        if (catalogJob?.isActive == true) return
        container.mviContext.reduce { copy(isCatalogLoading = true, catalogErrorMessage = null) }
        catalogJob = viewModelScope.launch {
            try {
                val catalog = getExploreCatalog()
                container.mviContext.reduce {
                    copy(
                        countries = catalog.countries.distinctBy { it.country },
                        channels = catalog.channels.distinctBy { it.channelId },
                        selectedChannelId = selectedChannelId?.takeIf { id -> catalog.channels.any { it.channelId == id } }
                            ?: catalog.channels.firstOrNull()?.channelId,
                        isCatalogLoading = false,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                container.mviContext.reduce {
                    copy(isCatalogLoading = false, catalogErrorMessage = "여행지와 크리에이터를 불러오지 못했어요.")
                }
            }
        }
    }

    private fun loadVideos(append: Boolean = false) {
        val snapshot = uiState.value
        val cursor = if (append) snapshot.nextCursor else null
        if (append && (snapshot.isLoading || snapshot.isLoadingMore || !snapshot.hasNext || cursor == null)) return
        videosJob?.cancel()
        val generation = ++videosGeneration
        if (!append) requestedCursors.clear()
        container.mviContext.reduce {
            copy(
                videos = if (append) videos else emptyList(),
                isLoading = !append,
                isLoadingMore = append,
                errorMessage = null,
                nextCursor = if (append) nextCursor else null,
                hasNext = append && hasNext,
            )
        }
        videosJob = viewModelScope.launch {
            try {
                val page = getExploreVideos(
                    country = snapshot.selectedCountry.takeIf { snapshot.selectedTab == ExploreTab.COUNTRY },
                    region = snapshot.selectedRegion.takeIf { snapshot.selectedTab == ExploreTab.COUNTRY },
                    theme = snapshot.selectedTheme.query.takeIf { snapshot.selectedTab == ExploreTab.THEME },
                    cursor = cursor,
                )
                if (generation != videosGeneration) return@launch
                cursor?.let(requestedCursors::add)
                container.mviContext.reduce {
                    copy(
                        videos = ((if (append) videos else emptyList()) + page.items).distinctBy { it.videoId },
                        nextCursor = page.nextCursor,
                        hasNext = snapshot.selectedTab == ExploreTab.THEME && snapshot.selectedTheme.query != null &&
                            page.hasNext && !page.nextCursor.isNullOrBlank() && page.nextCursor !in requestedCursors,
                        isLoading = false,
                        isLoadingMore = false,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (generation != videosGeneration) return@launch
                container.mviContext.reduce {
                    copy(isLoading = false, isLoadingMore = false, errorMessage = "영상을 불러오지 못했어요. 다시 시도해 주세요.")
                }
            }
        }
    }

    override fun onCleared() {
        videosGeneration += 1
        videosJob?.cancel()
        catalogJob?.cancel()
        container.close()
        super.onCleared()
    }
}
