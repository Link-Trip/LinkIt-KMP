package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.video.ExploreCatalog
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject

@Inject
class GetExploreCatalogUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(): ExploreCatalog = ensureAuthenticated.withExploreAuthentication {
        ExploreCatalog(
            countries = videoRepository.getDiscoverCountries(),
            channels = videoRepository.getDiscoverChannels(),
        )
    }
}
