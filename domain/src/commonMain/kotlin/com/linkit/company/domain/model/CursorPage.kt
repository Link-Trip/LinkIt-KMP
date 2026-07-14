package com.linkit.company.domain.model

/**
 * 커서 기반 페이지네이션의 한 페이지.
 *
 * 첫 요청은 cursor 없이, 다음 페이지는 [nextCursor]를 전달한다.
 * [hasNext]가 false면 마지막 페이지.
 */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
)
