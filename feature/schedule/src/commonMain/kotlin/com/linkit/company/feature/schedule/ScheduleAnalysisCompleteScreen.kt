package com.linkit.company.feature.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun ScheduleAnalysisCompleteScreen(
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(192.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(207.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LinkItTheme.color.semantic.background.normal.alternative),
        ) {
            MapLines(Modifier.fillMaxSize())
            SaveMenu(Modifier.align(Alignment.TopCenter).padding(top = 35.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 117.dp, bottom = 23.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LinkItTheme.color.semantic.label.strong),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LinkItIcon.Control.BookmarkFill,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.static.white,
                    modifier = Modifier.size(21.dp),
                )
            }
        }

        Text(
            text = "축하해요 일정 분석이 완료됐어요!",
            style = LinkItTheme.typography.body2NormalSemibold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 30.dp),
        )
        Text(
            text = "짜여진 일정을 저장하고 쉽게 관리해보세요~",
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.padding(top = 5.dp),
        )

        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(LinkItTheme.color.semantic.label.strong)
                .clickable(onClick = onConfirm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "확인",
                style = LinkItTheme.typography.body1NormalSemibold,
                color = LinkItTheme.color.semantic.static.white,
            )
        }
    }
}

@Composable
private fun SaveMenu(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(158.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LinkItTheme.color.semantic.label.strong)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        SaveMenuRow("전체일정 저장하기", true)
        Box(Modifier.fillMaxWidth().height(1.dp).background(LinkItTheme.color.semantic.line.normal.strong))
        SaveMenuRow("선택항목 저장하기", false)
    }
}

@Composable
private fun SaveMenuRow(text: String, bookmark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(39.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (bookmark) LinkItIcon.Control.BookmarkFill else LinkItIcon.Control.Bookmark,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.static.white,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.static.white,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

@Composable
private fun MapLines(modifier: Modifier = Modifier) {
    val line = LinkItTheme.color.semantic.line.solid.normal
    Canvas(modifier) {
        repeat(5) { index ->
            val y = size.height * (.14f + index * .19f)
            drawLine(
                color = line,
                start = Offset(-20f, y),
                end = Offset(size.width + 20f, y + if (index % 2 == 0) 38f else -32f),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
        }
    }
}
