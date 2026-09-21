package com.linkit.company.feature.map.mypage.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 앱 안에서 웹페이지를 표시하는 플랫폼 웹뷰.
 *
 * @param url 표시할 주소
 * @param reloadToken 값이 바뀌면 같은 [url] 을 다시 불러온다(재시도)
 * @param onLoadingChanged 페이지 로딩 시작(true)·종료(false) 콜백
 * @param onError 페이지를 불러오지 못했을 때 콜백. 이후 [onLoadingChanged] `false` 가 뒤따른다
 *
 * 본문 안의 링크는 같은 도메인이면 웹뷰 안에서 열고, 외부 도메인이면 기기 브라우저로 넘긴다.
 */
@Composable
internal expect fun PlatformWebView(
    url: String,
    reloadToken: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
)
