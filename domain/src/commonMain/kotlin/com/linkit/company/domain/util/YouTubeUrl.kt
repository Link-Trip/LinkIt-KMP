package com.linkit.company.domain.util

/**
 * YouTube 영상 주소 정규화·식별.
 *
 * `youtu.be/{id}`, `youtube.com/watch?v={id}`, `youtube.com/{shorts|live|embed}/{id}` 를 지원한다.
 * 같은 영상을 가리키는 표기 차이(짧은 주소, 추가 매개변수 등)는 [videoIdOrNull] 동일성으로 같은 링크로 본다.
 */
object YouTubeUrl {

    /** 앞뒤 공백을 제거한 요청용 주소. 서버에는 이 값을 보낸다. */
    fun normalize(url: String): String = url.trim()

    /** 지원하는 YouTube 주소면 영상 ID, 아니면 null. */
    fun videoIdOrNull(url: String): String? {
        val normalized = normalize(url)

        ShortUrl.matchEntire(normalized)?.let { match ->
            return match.groupValues[1]
        }
        PathUrl.matchEntire(normalized)?.let { match ->
            return match.groupValues[1]
        }
        WatchUrl.matchEntire(normalized)?.let { match ->
            return match.groupValues[1]
                .split('&')
                .firstOrNull { it.startsWith("v=") }
                ?.substringAfter("v=")
                ?.takeIf(String::isNotBlank)
        }

        return null
    }

    private val ShortUrl = Regex(
        pattern = """https?://(?:www\.)?youtu\.be/([A-Za-z0-9_-]+)(?:[/?#&].*)?""",
        option = RegexOption.IGNORE_CASE,
    )

    private val PathUrl = Regex(
        pattern = """https?://(?:www\.|m\.)?youtube\.com/(?:shorts|live|embed)/([A-Za-z0-9_-]+)(?:[/?#&].*)?""",
        option = RegexOption.IGNORE_CASE,
    )

    private val WatchUrl = Regex(
        pattern = """https?://(?:www\.|m\.)?youtube\.com/watch\?([^#\s]+)(?:#.*)?""",
        option = RegexOption.IGNORE_CASE,
    )
}
