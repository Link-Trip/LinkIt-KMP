package com.linkit.company.feature.schedule

import com.linkit.company.domain.model.video.DiscoverVideo

internal val ScheduleRecommendationFixtures = listOf(
    "오사카 공원 여행",
    "도쿄 골목 여행",
    "유명 신혼 여행지에 혼자 당당히 여행가는 사람【몰디브】",
    "서울의 문화 명소",
    "부산 바다 여행",
).mapIndexed { index, title ->
    DiscoverVideo(
        videoId = "recommended-$index",
        videoUrl = "https://youtu.be/recommended-$index",
        title = title,
        description = "여행 영상 $index",
        thumbnailUrl = "",
        channelId = "channel-$index",
        channelTitle = "여행 채널",
        viewCount = 12345L + index,
        likeCount = 123L,
        duration = "PT10M",
        publishedAt = "2026-09-21T00:00:00Z",
        region = "ASIA",
        country = "KR",
        city = "서울",
        theme = "문화",
    )
}
