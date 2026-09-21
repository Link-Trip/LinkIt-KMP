package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.FixedAppInfoRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedFeedbackRepository
import com.linkit.company.domain.fake.TestAppInfo
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SendFeedbackUseCaseTest {

    @Test
    fun trimsContentAndDefaultsTypeToEtcAndAttachesAppInfo() = runImmediateSuspend {
        val feedback = ScriptedFeedbackRepository()

        createUseCase(feedback = feedback)(type = null, content = "  좋아요  ")

        val sent = feedback.sent.single()
        assertEquals(FeedbackType.ETC, sent.type)
        assertEquals("좋아요", sent.content)
        assertEquals(TestAppInfo, sent.appInfo)
    }

    @Test
    fun keepsSelectedType() = runImmediateSuspend {
        val feedback = ScriptedFeedbackRepository()

        createUseCase(feedback = feedback)(type = FeedbackType.BUG, content = "버그")

        assertEquals(FeedbackType.BUG, feedback.sent.single().type)
    }

    @Test
    fun rejectsBlankContentWithoutCallingServer() = runImmediateSuspend {
        val feedback = ScriptedFeedbackRepository()

        assertFailsWith<IllegalArgumentException> {
            createUseCase(feedback = feedback)(type = null, content = "   ")
        }
        assertEquals(0, feedback.sent.size)
    }

    @Test
    fun rejectsContentLongerThan200() = runImmediateSuspend {
        val feedback = ScriptedFeedbackRepository()

        assertFailsWith<IllegalArgumentException> {
            createUseCase(feedback = feedback)(type = null, content = "a".repeat(201))
        }
        assertEquals(0, feedback.sent.size)
    }

    @Test
    fun retriesOnceAfterUnauthorized() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val feedback = ScriptedFeedbackRepository(
            results = listOf(apiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401), Unit),
        )

        createUseCase(auth = auth, feedback = feedback)(type = null, content = "재시도")

        assertEquals(2, feedback.sent.size)
        assertEquals(listOf("logout", "login"), auth.events)
    }

    @Test
    fun propagatesDailyLimitWithoutRetry() = runImmediateSuspend {
        val feedback = ScriptedFeedbackRepository(
            results = listOf(apiException(LinkTripErrorCode.FEEDBACK_DAILY_LIMIT_EXCEEDED, 429)),
        )

        val error = assertFailsWith<LinkTripApiException> {
            createUseCase(feedback = feedback)(type = null, content = "초과")
        }

        assertEquals(LinkTripErrorCode.FEEDBACK_DAILY_LIMIT_EXCEEDED, error.errorCode)
        assertEquals(1, feedback.sent.size)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository = RecordingAuthRepository(),
        feedback: ScriptedFeedbackRepository,
    ) = SendFeedbackUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        feedbackRepository = feedback,
        appInfoRepository = FixedAppInfoRepository(),
    )

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}
