package com.linkit.company.feature.explore

import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.RecentVideo

/** 테스트에서만 사용하는 명시적 응답 fixture. 원격 이미지 의존 없이 화면 내용을 검증한다. */
internal fun exploreFixtureState() = ExploreUiState(
    countries = listOf(DiscoverCountry("일본", 12), DiscoverCountry("태국", 4)),
    channels = listOf(exploreChannel("creator1"), exploreChannel("creator2")),
    selectedChannelId = "creator1",
    videos = listOf(exploreVideo("one"), exploreVideo("two")),
    isLoading = false,
    isCatalogLoading = false,
)

internal fun exploreVideo(id: String) = DiscoverVideo(
    videoId = id,
    videoUrl = "https://www.youtube.com/watch?v=$id",
    title = "실제 응답 영상 $id",
    description = "영상 설명",
    thumbnailUrl = "",
    channelId = "creator1",
    channelTitle = "여행 크리에이터",
    viewCount = 12345,
    likeCount = 123,
    duration = "PT12M34S",
    publishedAt = "2026-09-21T00:00:00Z",
    region = "아시아",
    country = "일본",
    city = "도쿄",
    theme = "맛집여행",
)

internal fun exploreChannel(id: String) = DiscoverChannel(
    channelId = id,
    title = "크리에이터 $id",
    description = "여행 영상 채널",
    thumbnailUrl = "",
    subscriberCount = 1234,
    videoCount = 30,
    recentVideos = listOf(RecentVideo("recent-$id", "최근 영상 $id", "", "2026-09-20", "https://youtu.be/recent-$id")),
)
