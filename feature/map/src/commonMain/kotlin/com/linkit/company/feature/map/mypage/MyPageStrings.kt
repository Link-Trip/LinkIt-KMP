package com.linkit.company.feature.map.mypage

/** 마이페이지 사용자 노출 문자열 (contracts/domain-contracts.md §8, Figma 확정). */
internal object MyPageStrings {
    const val Title = "마이페이지"

    const val NotificationCardTitle = "기기 알림이 꺼져있어요"
    const val NotificationCardBody = "알림이 꺼져있으면 영상 분석이 \n완료되어도 바로 알 수 없어요!"
    const val NotificationCardAction = "설정하러 가기"

    const val SectionMap = "지도 설정"
    const val MapDefault = "기본"
    const val MapSatellite = "위성"
    const val SectionNotification = "알림설정"
    const val RowNotification = "알림"
    const val SectionHelp = "도움말 & 지원"
    const val RowFeedback = "의견 보내기"
    const val RowTerms = "이용약관"
    const val SectionApp = "앱 설정"
    const val RowReset = "앱 초기화"

    const val ToastMapSatellite = "위성 지도로 변경되었습니다."
    const val ToastMapDefault = "기본 지도로 변경되었습니다."
    const val ToastFeedbackSuccess = "의견 전송이 완료되었습니다."
    const val ToastFeedbackLimit = "최대 의견 전송 횟수를 초과했습니다."
    const val ToastFeedbackFailure = "전송에 실패했습니다. 다시 시도해주세요."
    const val ToastResetFailure = "앱 초기화에 실패했습니다. 다시 시도해주세요."

    const val ResetDialogTitle = "정말 앱을 초기화 하시겠어요?"
    const val ResetDialogDescription = "앱을 초기화하면 다시 복구할 수 없어요"
    const val ResetDialogConfirm = "초기화"
    const val ResetDialogCancel = "돌아가기"

    const val FeedbackTitle = "의견 보내기"
    const val FeedbackBody = "보내주신 의견은 서비스 개선에 활용돼요.\n(개별 답변은 어려워요 🥹)\n스팸 방지를 위해 하루 최대 5회까지 전송할 수 있어요!"
    const val FeedbackTypeSuggestion = "제안"
    const val FeedbackTypeBug = "오류·버그"
    const val FeedbackTypeEtc = "기타"
    const val FeedbackPlaceholder = "핑고를 쓰면서 느낀 점이나 \n발견한 문제를 알려주세요!"
    const val FeedbackSend = "보내기"

    const val TermsTitle = "이용약관"
    const val TermsService = "서비스 이용약관"
    const val TermsPrivacy = "개인정보 처리방침"
    const val TermsOpenSource = "오픈소스 라이센스 고지"
    const val TermsLocation = "위치기반 서비스 이용약관"
    const val TermsLoadError = "약관을 불러오지 못했어요"
    const val TermsRetry = "다시 시도"
}
