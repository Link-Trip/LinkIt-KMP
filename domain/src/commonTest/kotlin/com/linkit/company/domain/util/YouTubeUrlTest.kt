package com.linkit.company.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class YouTubeUrlTest {

    @Test
    fun normalizeTrimsSurroundingWhitespace() {
        assertEquals("https://youtu.be/AbC_123", YouTubeUrl.normalize("  https://youtu.be/AbC_123 \n"))
    }

    @Test
    fun extractsIdFromShortWatchAndPathUrls() {
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://youtu.be/AbC_123"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://www.youtube.com/watch?v=AbC_123"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://m.youtube.com/shorts/AbC_123"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://youtube.com/live/AbC_123"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://www.youtube.com/embed/AbC_123"))
    }

    @Test
    fun ignoresExtraParametersAndFragments() {
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://youtu.be/AbC_123?t=10&feature=share"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull("https://www.youtube.com/watch?feature=share&v=AbC_123&t=5#top"))
        assertEquals("AbC_123", YouTubeUrl.videoIdOrNull(" https://youtube.com/shorts/AbC_123/ "))
    }

    @Test
    fun sameVideoWithDifferentNotationSharesId() {
        val short = YouTubeUrl.videoIdOrNull("https://youtu.be/AbC_123")
        val watch = YouTubeUrl.videoIdOrNull("https://www.youtube.com/watch?v=AbC_123&list=PL1")
        assertEquals(short, watch)
    }

    @Test
    fun rejectsNonYoutubeOrMalformedUrls() {
        assertNull(YouTubeUrl.videoIdOrNull("https://example.com/watch?v=AbC_123"))
        assertNull(YouTubeUrl.videoIdOrNull("https://www.youtube.com/watch?list=PL1"))
        assertNull(YouTubeUrl.videoIdOrNull("youtu.be/AbC_123"))
        assertNull(YouTubeUrl.videoIdOrNull("https://youtu.be/AbC_123 extra"))
        assertNull(YouTubeUrl.videoIdOrNull(""))
    }
}
