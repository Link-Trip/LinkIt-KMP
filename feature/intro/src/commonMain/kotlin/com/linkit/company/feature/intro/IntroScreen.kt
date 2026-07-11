package com.linkit.company.feature.intro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.intro.generated.resources.Res
import linkitcompany.feature.intro.generated.resources.intro_globe
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun IntroScreen(
    onNavigateToHome: () -> Unit = {},
    viewModel: IntroViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.stage) {
        when (uiState.stage) {
            IntroStage.GLOBE -> {
                delay(1_500)
                viewModel.onIntent(IntroIntent.ZoomToSeoul)
            }
            IntroStage.SEOUL -> {
                delay(900)
                onNavigateToHome()
            }
        }
    }

    IntroContent(uiState = uiState)
}

@Composable
fun IntroContent(
    uiState: IntroUiState,
    modifier: Modifier = Modifier,
) {
    val background = PaletteTokens.PaleBlue95
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        when (uiState.stage) {
            IntroStage.GLOBE -> Image(
                painter = painterResource(Res.drawable.intro_globe),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-1).dp)
                    .size(375.dp),
            )
            IntroStage.SEOUL -> SeoulMap(Modifier.fillMaxSize())
        }

        Text(
            text = if (uiState.stage == IntroStage.GLOBE) "인트로 애니메이션" else "내 여행이 시작되는 곳",
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

@Composable
private fun SeoulMap(modifier: Modifier = Modifier) {
    val road = LinkItTheme.color.semantic.static.white
    val minorRoad = LinkItTheme.color.semantic.line.solid.normal
    val primary = LinkItTheme.color.semantic.primary.normal
    Canvas(modifier) {
        repeat(9) { index ->
            val y = size.height * (.12f + index * .095f)
            drawLine(
                color = road,
                start = Offset(-30f, y),
                end = Offset(size.width + 30f, y + if (index % 2 == 0) 92f else -66f),
                strokeWidth = 15f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = minorRoad,
                start = Offset(-30f, y),
                end = Offset(size.width + 30f, y + if (index % 2 == 0) 92f else -66f),
                strokeWidth = 2f,
            )
        }
        drawCircle(primary.copy(alpha = .18f), radius = 42f, center = center)
        drawCircle(primary, radius = 11f, center = center)
        drawCircle(road, radius = 4f, center = center)
    }
}
