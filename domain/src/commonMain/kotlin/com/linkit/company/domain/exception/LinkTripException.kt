package com.linkit.company.domain.exception

/**
 * LinkTrip 서버가 실패 응답(`{code, message}`)으로 내려주는 에러 코드.
 *
 * 서버에 새 코드가 추가되어도 파싱이 깨지지 않도록, 매칭되지 않는 값은 [UNKNOWN]으로 폴백한다.
 */
enum class LinkTripErrorCode {
    BAD_REQUEST_VALIDATION,
    BAD_REQUEST_MISSING_IDEMPOTENCY_KEY,
    BAD_REQUEST_YOUTUBE_URL,
    BAD_REQUEST_VIDEO,
    BAD_REQUEST_DISCOVER_QUERY,
    BAD_REQUEST_FEEDBACK_TYPE,
    BAD_REQUEST_PLATFORM,
    UNAUTHORIZED_AUTHENTICATION_FAILED,
    UNAUTHORIZED_TOKEN_EXPIRED,
    UNAUTHORIZED_TOKEN_INVALID,
    FORBIDDEN_TRIP_PLAN,
    NOT_FOUND_TRIP_PLAN,
    NOT_FOUND_VIDEO_ANALYSIS_TASK,
    NOT_FOUND_MEMBER,
    DUPLICATE_REQUEST,
    TOO_MANY_REQUESTS,
    FEEDBACK_DAILY_LIMIT_EXCEEDED,
    UNKNOWN,
    ;

    companion object {
        fun from(code: String?): LinkTripErrorCode =
            entries.firstOrNull { it.name == code } ?: UNKNOWN
    }
}

/**
 * LinkTrip API가 4xx/5xx로 실패했을 때 던져지는 예외.
 *
 * ViewModel/UseCase는 [errorCode]로 분기해 사용자 노출 메시지를 결정한다.
 */
class LinkTripApiException(
    val errorCode: LinkTripErrorCode,
    val httpStatus: Int,
    override val message: String,
) : Exception(message)
