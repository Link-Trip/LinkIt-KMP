package com.linkit.company.data.mapper

import com.linkit.company.data.core.defaultJson
import com.linkit.company.data.dto.video.YouTubeOEmbedResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.decodeFromString

class YouTubeVideoMetadataMapperTest {
    @Test
    fun mapsYouTubeOEmbedResponse() {
        val response = defaultJson().decodeFromString<YouTubeOEmbedResponse>(
            """
            {
              "title": "유부남과 함께 오사카 좋은 놀이공원 가보기",
              "thumbnail_url": "https://i.ytimg.com/vi/OrGmEVTD04I/hqdefault.jpg"
            }
            """.trimIndent(),
        )

        val metadata = response.toDomain()

        assertEquals("유부남과 함께 오사카 좋은 놀이공원 가보기", metadata.title)
        assertEquals(
            "https://i.ytimg.com/vi/OrGmEVTD04I/hqdefault.jpg",
            metadata.thumbnailUrl,
        )
    }
}
