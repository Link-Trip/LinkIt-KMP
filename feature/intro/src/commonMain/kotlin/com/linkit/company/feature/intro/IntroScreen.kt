package com.linkit.company.feature.intro

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
    ) {
        when (uiState.stage) {
            IntroStage.GLOBE -> DottedGlobe(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 12.dp)
                    .size(304.dp),
            )
            IntroStage.SEOUL -> SeoulMap(Modifier.fillMaxSize())
        }

        Text(
            text = if (uiState.stage == IntroStage.GLOBE) "인트로 애니메이션" else "내 여행이 시작되는 곳",
            style = LinkItTheme.typography.heading2Semibold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 132.dp),
        )
    }
}

@Composable
private fun DottedGlobe(modifier: Modifier = Modifier) {
    val globe = LinkItTheme.color.semantic.inverse.background
    val blue = LinkItTheme.color.semantic.primary.normal
    val paleBlue = LinkItTheme.color.semantic.primary.light

    Canvas(modifier) {
        val radius = size.minDimension * .46f
        val center = center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(globe, Color.Black),
                center = Offset(center.x * .78f, center.y * .72f),
                radius = radius * 1.35f,
            ),
            radius = radius,
            center = center,
        )
        drawCircle(
            color = blue.copy(alpha = .22f),
            radius = radius * 1.06f,
            center = center,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(globe, Color.Black),
                center = Offset(center.x * .78f, center.y * .72f),
                radius = radius * 1.35f,
            ),
            radius = radius,
            center = center,
        )

        val step = radius / 22f
        for (row in -20..20) {
            for (column in -20..20) {
                val x = column / 20f
                val y = row / 20f
                if (x * x + y * y >= .88f) continue
                if (!isAsiaPacific(x, y)) continue

                val depth = sqrt(1f - x * x - y * y)
                val wave = sin((column + row) * .62f) * 1.4f
                drawCircle(
                    color = paleBlue.copy(alpha = .48f + depth * .5f),
                    radius = 1.15f + depth * .7f,
                    center = Offset(
                        x = center.x + column * step + wave,
                        y = center.y + row * step,
                    ),
                )
            }
        }

        for (ring in 1..6) {
            val ringRadius = radius * (.26f + ring * .105f)
            for (index in 0 until 44) {
                if (index % 3 == 0) continue
                val angle = index * .143f + ring * .18f
                val x = center.x + cos(angle) * ringRadius
                val y = center.y + sin(angle) * ringRadius * .31f
                drawCircle(
                    color = blue.copy(alpha = .32f),
                    radius = .8f,
                    center = Offset(x, y),
                )
            }
        }
    }
}

private fun isAsiaPacific(x: Float, y: Float): Boolean {
    val asia = y in -.58f..-.02f && x in -.62f..57f &&
        !(x < -.2f && y > -.2f) && !(x > .35f && y < -.38f)
    val southEastAsia = y in -.04f..32f && x in -.02f..58f &&
        abs(sin(x * 18f + y * 7f)) > .28f
    val australia = y in .31f..62f && x in .18f..67f &&
        (x - .43f) * (x - .43f) + (y - .46f) * (y - .46f) < .08f
    val island = y in -.1f..42f && x in .5f..72f && abs(sin(y * 28f)) > .7f
    return asia || southEastAsia || australia || island
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
