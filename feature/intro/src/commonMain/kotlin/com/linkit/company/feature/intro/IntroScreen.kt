package com.linkit.company.feature.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import kotlinx.coroutines.delay
import linkitcompany.feature.intro.generated.resources.Res
import linkitcompany.feature.intro.generated.resources.intro_globe
import org.jetbrains.compose.resources.painterResource

/**
 * 인트로 애니메이션 화면. [IntroSplashDurationMillis] 뒤 [onSplashFinished] 만 호출하고
 * 목적지 판단은 [IntroViewModel] 이 한다(research R5·R11).
 *
 * 앱 초기화 완료 토스트는 온보딩 시작 화면([OnboardingStartScreen])으로 옮겨졌다.
 */
@Composable
fun IntroScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(IntroSplashDurationMillis)
        onSplashFinished()
    }

    IntroContent(modifier = modifier)
}

@Composable
fun IntroContent(
    modifier: Modifier = Modifier,
) {
    val background = PaletteTokens.PaleBlue95
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        Image(
            painter = painterResource(Res.drawable.intro_globe),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-1).dp)
                .size(375.dp),
        )

        Text(
            text = "인트로 애니메이션",
            style = LinkItTheme.typography.heading2Semibold.copy(
                fontFamily = nanumSquare,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 25.2.sp,
            ),
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 132.dp),
        )
    }
}
