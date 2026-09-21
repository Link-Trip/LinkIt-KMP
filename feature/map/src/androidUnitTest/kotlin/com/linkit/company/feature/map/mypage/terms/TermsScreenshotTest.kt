package com.linkit.company.feature.map.mypage.terms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class TermsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 18287:116697 — 약관 목록 4행 */
    @Test
    fun terms_list() = capture {
        TermsListContent(documents = SampleDocuments, onBack = {}, onOpenDocument = {})
    }

    /** 18287:116612 — 상세 로딩 중(웹뷰는 인스펙션 모드에서 생략) */
    @Test
    fun terms_detailLoading() = capture {
        TermsDetailContent(
            document = SampleDocuments.first(),
            loadState = TermsLoadState.LOADING,
            onBack = {},
            onRetry = {},
        )
    }

    /** 상세 실패: 안내 문구 + 다시 시도 (Figma 미정의, FR-021a) */
    @Test
    fun terms_detailError() = capture {
        TermsDetailContent(
            document = SampleDocuments.first(),
            loadState = TermsLoadState.ERROR,
            onBack = {},
            onRetry = {},
        )
    }

    private companion object {
        /** TermsRepositoryImpl과 동일한 4종. 스크린샷은 data 모듈에 의존하지 않도록 여기서 정의한다. */
        val SampleDocuments = listOf(
            TermsDocument(TermsDocumentType.SERVICE, "서비스 이용약관", "https://linktrip.cloud/terms/service"),
            TermsDocument(TermsDocumentType.PRIVACY, "개인정보 처리방침", "https://linktrip.cloud/terms/privacy"),
            TermsDocument(TermsDocumentType.OPEN_SOURCE, "오픈소스 라이센스 고지", "https://linktrip.cloud/terms/oss"),
            TermsDocument(TermsDocumentType.LOCATION, "위치기반 서비스 이용약관", "https://linktrip.cloud/terms/location"),
        )
    }

    private fun capture(content: @Composable () -> Unit) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) { content() }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
