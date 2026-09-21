package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject

/** 서로 배타적인 탐색 조건을 검증하고 기기 계정으로 영상 목록을 조회한다. */
@Inject
class GetExploreVideosUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(
        country: String? = null,
        region: String? = null,
        theme: String? = null,
        cursor: String? = null,
    ): CursorPage<DiscoverVideo> {
        require(listOfNotNull(country, region, theme).size <= 1) { "탐색 조건은 한 번에 하나만 선택할 수 있습니다." }
        require(listOfNotNull(country, region, theme).none(String::isBlank))
        require(cursor == null || theme != null) { "더보기는 테마 조회에서만 지원합니다." }
        return ensureAuthenticated.withExploreAuthentication {
            if (theme != null) {
                videoRepository.getDiscoverVideosByTheme(theme, cursor)
            } else {
                val videos = when {
                    country != null -> videoRepository.getDiscoverVideosByCountry(country)
                    region != null -> videoRepository.getDiscoverVideosByRegion(region)
                    else -> videoRepository.getDiscoverVideos()
                }
                CursorPage(videos, nextCursor = null, hasNext = false)
            }
        }
    }
}

internal suspend fun <T> EnsureAuthenticatedUseCase.withExploreAuthentication(action: suspend () -> T): T {
    invoke()
    return try {
        action()
    } catch (error: LinkTripApiException) {
        if (error.httpStatus != 401 && error.errorCode != LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED) throw error
        invoke(forceRefresh = true)
        action()
    }
}
