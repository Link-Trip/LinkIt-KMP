package com.linkit.company.feature.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import kotlinx.coroutines.delay

@Composable
fun ScheduleAnalysisLoadingScreen(
    onBack: () -> Unit = {},
    onAnalysisComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(1_600)
        onAnalysisComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LinkItIcon.Arrow.ChevronLeft,
                contentDescription = "뒤로가기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.size(22.dp).clickable(onClick = onBack),
            )
            Text(
                text = "영상 일정 생성",
                style = LinkItTheme.typography.body1NormalMedium,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(LinkItTheme.color.semantic.line.normal.alternative))

        Text(
            text = "일정 생성중...",
            style = LinkItTheme.typography.caption1Semibold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier
                .padding(top = 72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LinkItTheme.color.semantic.fill.normal)
                .padding(horizontal = 30.dp, vertical = 7.dp),
        )

        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .width(188.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(LinkItTheme.color.semantic.fill.strong),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(.2f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(LinkItTheme.color.semantic.label.neutral),
            )
        }

        Text(
            text = "일정을 생성중이에요~",
            style = LinkItTheme.typography.heading2Semibold,
            color = LinkItTheme.color.semantic.label.strong,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 22.dp),
        )
        Text(
            text = "완료되면 알려드릴게요~",
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.alternative,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        LoadingIllustration(Modifier.padding(top = 24.dp).size(152.dp))
    }
}

@Composable
private fun LoadingIllustration(modifier: Modifier = Modifier) {
    val background = LinkItTheme.color.semantic.background.normal.alternative
    val line = LinkItTheme.color.semantic.line.solid.normal
    val primary = LinkItTheme.color.semantic.primary.normal
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background),
    ) {
        repeat(5) { index ->
            val y = size.height * (.18f + index * .16f)
            drawLine(line, Offset(-12f, y), Offset(size.width + 12f, y + 26f), strokeWidth = 3f)
        }
        drawCircle(primary.copy(alpha = .18f), radius = 25f, center = center)
        drawCircle(primary, radius = 7f, center = center)
    }
}
