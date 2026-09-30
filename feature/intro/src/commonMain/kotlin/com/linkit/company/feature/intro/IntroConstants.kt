package com.linkit.company.feature.intro

/** 인트로 애니메이션 유지 시간. 디자인 확정(`~ms`) 시 값만 바꾼다(research R11). */
internal const val IntroSplashDurationMillis = 1_500L

/** 온보딩 화면 사용자 노출 문자열 (contracts/domain-contracts.md §9, Figma 확정). */
internal object OnboardingStrings {
    const val StartBadge = "여행 영상, 이제 저장만 하지 마세요"
    const val StartTitle = "보고있던 여행 영상을\n내 일정으로 만들어보세요!"
    const val StartSubtitle = "영상을 분석해 마커와 일정으로 정리해드릴게요 ⭐"
    const val StartPrimary = "30초 만에 사용법 보기"
    const val StartSecondary = "바로 시작하기"

    const val TermsTitle = "핑고를 시작하려면 동의가 필요해요"
    const val TermsSubtitle = "필수 항목에 동의해야 서비스를 이용할 수 있어요"
    const val TermsAll = "전체 동의"
    const val TermsRequired = "[필수]"
    const val TermsService = "서비스 이용약관 동의"
    const val TermsPrivacy = "개인정보 수집·이용 동의"
    const val TermsDetail = "상세보기"
    const val TermsStart = "동의하고 시작하기"
}
