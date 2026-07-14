package com.linkit.company.data.mapper

import com.linkit.company.data.dto.video.DiscoverChannelResponse
import com.linkit.company.data.dto.video.DiscoverVideoCursorResponse
import com.linkit.company.data.dto.video.DiscoverVideoResponse
import com.linkit.company.data.dto.video.RecentVideoResponse
import com.linkit.company.data.dto.video.ScheduleItemResponse
import com.linkit.company.data.dto.video.TimelineResponse
import com.linkit.company.data.dto.video.VideoAnalyzeResponse
import com.linkit.company.domain.model.CostBasis
import com.linkit.company.domain.model.CursorPage
import com.linkit.company.domain.model.DiscoverChannel
import com.linkit.company.domain.model.DiscoverVideo
import com.linkit.company.domain.model.PlaceCategory
import com.linkit.company.domain.model.PlaceStatus
import com.linkit.company.domain.model.RecentVideo
import com.linkit.company.domain.model.ScheduleItem
import com.linkit.company.domain.model.VideoAnalysis
import com.linkit.company.domain.model.VideoAnalysisStatus
import com.linkit.company.domain.model.VideoTimeline

internal fun VideoAnalyzeResponse.toDomain(): VideoAnalysis {
    return VideoAnalysis(
        id = id,
        youtubeUrl = youtubeUrl,
        isValid = valid,
        status = VideoAnalysisStatus.from(status),
        summary = summary.orEmpty(),
        estimatedMinCost = estimatedMinCost,
        estimatedMaxCost = estimatedMaxCost,
        costBasis = costBasis?.let { CostBasis.from(it) },
        placeEnrichmentCompleted = placeEnrichmentCompleted,
        timelines = timelines.map { it.toDomain() },
        itineraryItems = itineraryItems.map { it.toDomain() },
    )
}

internal fun TimelineResponse.toDomain(): VideoTimeline {
    return VideoTimeline(
        timestampSeconds = timestampSeconds,
        timestamp = timestamp,
        timestampUrl = timestampUrl,
        description = description,
    )
}

internal fun ScheduleItemResponse.toDomain(): ScheduleItem {
    return ScheduleItem(
        id = id,
        day = day,
        order = order,
        category = PlaceCategory.from(category),
        name = name,
        description = description.orEmpty(),
        tips = tips.orEmpty(),
        place = place?.toDomain(),
        placeStatus = PlaceStatus.from(placeStatus),
    )
}

internal fun DiscoverVideoCursorResponse.toDomain(): CursorPage<DiscoverVideo> {
    return CursorPage(
        items = videos.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )
}

internal fun DiscoverVideoResponse.toDomain(): DiscoverVideo {
    return DiscoverVideo(
        videoId = videoId,
        videoUrl = videoUrl,
        title = title,
        description = description.orEmpty(),
        thumbnailUrl = thumbnailUrl,
        channelId = channelId,
        channelTitle = channelTitle,
        viewCount = viewCount,
        likeCount = likeCount,
        duration = duration,
        publishedAt = publishedAt,
        region = region.orEmpty(),
        country = country.orEmpty(),
        city = city.orEmpty(),
        theme = theme.orEmpty(),
    )
}

internal fun DiscoverChannelResponse.toDomain(): DiscoverChannel {
    return DiscoverChannel(
        channelId = channelId,
        title = title,
        description = description.orEmpty(),
        thumbnailUrl = thumbnailUrl,
        subscriberCount = subscriberCount,
        videoCount = videoCount,
        recentVideos = recentVideos.map { it.toDomain() },
    )
}

internal fun RecentVideoResponse.toDomain(): RecentVideo {
    return RecentVideo(
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        publishedAt = publishedAt,
        videoUrl = videoUrl,
    )
}
