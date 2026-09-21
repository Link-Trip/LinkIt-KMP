package com.linkit.company.feature.map.mypage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

/**
 * 마이페이지 전용 다색 일러스트(Figma 이모지 스타일 아이콘).
 *
 * 단색 `LinkItIcon` 과 달리 고유 색을 가지므로 `Image` 로 그린다.
 */
internal object MyPageIllustrations {

    /** 알림 안내 카드의 종(🔔). Figma `18525:38230`, 20×20 박스 안 16.5×17.73. */
    val Bell: ImageVector by lazy {
        ImageVector.Builder(
            name = "MyPageBell",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            group(translationX = 1.75f, translationY = 1.136f) {
                path("M11.0502 14.9268C11.0502 16.4733 9.7967 17.7268 8.2502 17.7268C6.7037 17.7268 5.4502 16.4733 5.4502 14.9268H11.0502Z", Color(0xFFFF9E00))
                path("M6.56201 1.6885C6.56201 0.756 7.31801 0 8.25051 0C9.18301 0 9.93901 0.756 9.93901 1.6885H6.56251H6.56201Z", Color(0xFFFF9E00))
                path("M1.30228 8.27515C1.30228 4.28465 4.66628 1.08015 8.71278 1.34215C12.4058 1.58115 15.1983 4.81265 15.1983 8.51315V11.5526L16.3803 13.5996C16.7208 14.1896 16.2953 14.9271 15.6138 14.9271H0.886285C0.205285 14.9271 -0.220715 14.1896 0.119785 13.5996L1.30178 11.5526L1.30228 8.27515Z", Color(0xFFFFC000))
            }
        }.build()
    }

    /** 의견 보내기 시트의 받은 편지(📨). Figma `18212:34868`, 32×32 박스 안 26.67×24.77. */
    val Inbox: ImageVector by lazy {
        ImageVector.Builder(
            name = "MyPageInbox",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            group(translationX = 2.667f, translationY = 3.613f) {
                path("M23.8128 7.5695C23.3936 6.5879 22.5192 5.9007 21.4992 5.6959V10.5927C21.4992 11.1151 21.3104 11.5879 21.0072 11.9663C20.6024 12.4703 19.9888 12.7999 19.292 12.7999H7.3744C6.6776 12.7999 6.064 12.4703 5.6592 11.9663C5.356 11.5887 5.1672 11.1151 5.1672 10.5927V5.6959C4.1472 5.9007 3.272 6.5879 2.8536 7.5695L0 14.2559H26.6664L23.8128 7.5695Z", Color(0xFF2085F7))
                path("M0 14.2561V22.0529C0 23.5553 1.2184 24.7737 2.7208 24.7737H23.9456C25.448 24.7737 26.6664 23.5553 26.6664 22.0529V14.2561H0Z", Color(0xFF64A7FF))
                path("M5.66036 11.9666L10.8532 7.82744L5.16836 3.08984V10.593C5.16836 11.1154 5.35716 11.5882 5.66036 11.9666Z", Color(0xFFD0D5DA))
                path("M15.8145 7.82744L21.0073 11.9666C21.3105 11.589 21.4993 11.1154 21.4993 10.593V3.08984L15.8145 7.82744Z", Color(0xFFD0D5DA))
                path("M10.8528 7.82725L11.8312 8.64245C12.736 9.34405 13.932 9.34405 14.836 8.64245L15.8144 7.82725L21.4992 3.08965L14.836 7.68085C13.9312 8.30405 12.7352 8.30405 11.8312 7.68085L5.16797 3.08965L10.8528 7.82725Z", Color(0xFFAFB7C0))
                path("M14.8358 8.64254C13.931 9.34414 12.735 9.34414 11.831 8.64254L10.8526 7.82734L5.65977 11.9665C6.06457 12.4705 6.67817 12.8001 7.37497 12.8001H19.2926C19.9894 12.8001 20.603 12.4705 21.0078 11.9665L15.815 7.82734L14.8358 8.64254Z", Color(0xFFE5E9EE))
                path("M11.8308 7.6808C12.7356 8.304 13.9316 8.304 14.8356 7.6808L21.4988 3.0896V2.2072C21.4988 0.988 20.5108 0 19.2916 0H7.374C6.1556 0 5.1668 0.988 5.1668 2.2072V3.0896L11.8308 7.6808Z", Color(0xFFE5E9EE))
            }
        }.build()
    }

    private fun ImageVector.Builder.path(pathData: String, fill: Color) {
        addPath(
            pathData = PathParser().parsePathString(pathData).toNodes(),
            fill = SolidColor(fill),
        )
    }
}
