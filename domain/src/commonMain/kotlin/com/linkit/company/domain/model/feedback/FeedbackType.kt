package com.linkit.company.domain.model.feedback

/** 의견 보내기 유형. 미선택으로 전송하면 [ETC]로 기록한다. */
enum class FeedbackType {
    SUGGESTION,
    BUG,
    ETC,
}
