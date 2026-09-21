package com.linkit.company.feature.map.mypage.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.feature.map.mypage.MyPageStrings
import com.linkit.company.feature.map.mypage.platform.PlatformWebView

/** 웹뷰 로딩 상태. 화면 로컬 상태로 관리한다. */
enum class TermsLoadState {
    LOADING,
    CONTENT,
    ERROR,
}

/** 이용약관 상세(Figma `18287:116612`). 운영 웹페이지를 앱 안에서 표시한다. */
@Composable
fun TermsDetailScreen(
    type: TermsDocumentType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val document = remember(type) { TermsDocuments.find(type) }
    var loadState by rememberSaveable { mutableStateOf(TermsLoadState.LOADING) }
    var reloadToken by rememberSaveable { mutableIntStateOf(0) }

    TermsDetailContent(
        document = document,
        loadState = loadState,
        onBack = onBack,
        onRetry = {
            loadState = TermsLoadState.LOADING
            reloadToken += 1
        },
        modifier = modifier,
    ) {
        if (!LocalInspectionMode.current) {
            PlatformWebView(
                url = document.url,
                reloadToken = reloadToken,
                onLoadingChanged = { loading ->
                    if (loading) {
                        if (loadState != TermsLoadState.ERROR) loadState = TermsLoadState.LOADING
                    } else if (loadState == TermsLoadState.LOADING) {
                        loadState = TermsLoadState.CONTENT
                    }
                },
                onError = { loadState = TermsLoadState.ERROR },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun TermsDetailContent(
    document: TermsDocument,
    loadState: TermsLoadState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    webView: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        LinkItTopNavigation(
            title = document.title,
            navigationIcon = { TopNavigationDefaults.BackButton(onClick = onBack) },
        )
        Box(Modifier.fillMaxSize()) {
            if (loadState != TermsLoadState.ERROR) {
                webView()
            }
            when (loadState) {
                TermsLoadState.LOADING -> CircularProgressIndicator(
                    color = LinkItTheme.color.semantic.primary.normal,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("terms-detail-loading"),
                )
                TermsLoadState.ERROR -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp)
                        .testTag("terms-detail-error"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = MyPageStrings.TermsLoadError,
                        style = LinkItTheme.typography.body2NormalMedium,
                        color = LinkItTheme.color.semantic.label.alternative,
                        textAlign = TextAlign.Center,
                    )
                    LinkItButton(
                        onClick = onRetry,
                        text = MyPageStrings.TermsRetry,
                        variant = ButtonVariant.Outlined,
                        color = ButtonColor.Assistive,
                        size = ButtonSize.Medium,
                        modifier = Modifier.testTag("terms-detail-retry"),
                    )
                }
                TermsLoadState.CONTENT -> Unit
            }
        }
    }
}
