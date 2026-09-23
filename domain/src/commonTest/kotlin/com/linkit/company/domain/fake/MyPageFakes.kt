package com.linkit.company.domain.fake

import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.model.member.NotificationSetting
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppInfoRepository
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.FeedbackRepository
import com.linkit.company.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 호출 순서를 기록하는 인증 저장소. */
internal class RecordingAuthRepository(
    private var loggedIn: Boolean = true,
) : AuthRepository {
    val events = mutableListOf<String>()

    override suspend fun login(): Auth {
        events += "login"
        loggedIn = true
        return Auth(memberId = "member-id", accessToken = "access-token")
    }

    override suspend fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        events += "logout"
        loggedIn = false
    }
}

/** 응답을 큐로 주입할 수 있는 회원 저장소. [Throwable]이면 던지고, 그 외는 반환한다. */
internal class ScriptedMemberRepository(
    withdrawResults: List<Any> = listOf(0),
    notificationResults: List<Any> = listOf(Unit),
) : MemberRepository {
    val events = mutableListOf<String>()
    val notificationCalls = mutableListOf<Boolean>()
    private val withdrawQueue = ArrayDeque(withdrawResults)
    private val notificationQueue = ArrayDeque(notificationResults)

    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting {
        events += "notification"
        notificationCalls += enabled
        val result = notificationQueue.removeFirstOrNull() ?: Unit
        if (result is Throwable) throw result
        return NotificationSetting(enabled)
    }

    override suspend fun withdraw(): Int {
        events += "withdraw"
        val result = withdrawQueue.removeFirstOrNull() ?: 0
        if (result is Throwable) throw result
        return result as Int
    }
}

internal class InMemoryAppSettingsRepository(
    initial: MapDisplayType = MapDisplayType.DEFAULT,
) : AppSettingsRepository {
    val events = mutableListOf<String>()
    val mapDisplayType = MutableStateFlow(initial)
    var notificationPrompted = false
    var clearAllCount = 0
        private set
    var onClearAll: () -> Unit = {}

    override fun observeMapDisplayType(): Flow<MapDisplayType> = mapDisplayType

    override suspend fun setMapDisplayType(type: MapDisplayType) {
        mapDisplayType.value = type
    }

    override suspend fun isNotificationPrompted(): Boolean = notificationPrompted

    override suspend fun setNotificationPrompted(prompted: Boolean) {
        notificationPrompted = prompted
    }

    override suspend fun clearAll() {
        events += "clearAll"
        onClearAll()
        clearAllCount += 1
        mapDisplayType.value = MapDisplayType.DEFAULT
        notificationPrompted = false
    }
}

internal class ScriptedFeedbackRepository(
    results: List<Any> = listOf(Unit),
) : FeedbackRepository {
    data class Sent(val type: FeedbackType, val content: String, val appInfo: AppInfo)

    val sent = mutableListOf<Sent>()
    private val queue = ArrayDeque(results)

    override suspend fun sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo) {
        sent += Sent(type, content, appInfo)
        val result = queue.removeFirstOrNull() ?: Unit
        if (result is Throwable) throw result
    }
}

internal val TestAppInfo = AppInfo(
    appVersion = "1.0",
    platform = "ANDROID",
    osVersion = "15",
    deviceModel = "Pixel 8",
)

internal class FixedAppInfoRepository(
    private val appInfo: AppInfo = TestAppInfo,
) : AppInfoRepository {
    override suspend fun getAppInfo(): AppInfo = appInfo
}
