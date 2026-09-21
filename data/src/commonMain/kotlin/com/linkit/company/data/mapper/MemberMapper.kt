package com.linkit.company.data.mapper

import com.linkit.company.data.dto.member.NotificationSettingResponse
import com.linkit.company.domain.model.member.NotificationSetting

internal fun NotificationSettingResponse.toDomain(): NotificationSetting {
    return NotificationSetting(enabled = enabled)
}
