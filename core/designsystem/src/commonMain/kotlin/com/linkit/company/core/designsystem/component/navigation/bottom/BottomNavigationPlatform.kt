package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * [LinkItBottomNavigation]의 플랫폼별 표시 형태.
 *
 * 아이콘과 라벨 사이 간격, 탭의 상하 여백, 상단 구분선 두께가 플랫폼에 따라 달라진다.
 * (LinkIt 은 Android·iOS 네이티브 앱이므로 Figma 의 Web Mobile 변형은 제외했다.)
 */
enum class BottomNavigationPlatform {
    iOS,
    Android,
}

/** 하위 [LinkItBottomNavigationItem] 들이 참조하는 현재 바의 플랫폼. */
internal val LocalBottomNavigationPlatform =
    staticCompositionLocalOf { BottomNavigationPlatform.Android }
