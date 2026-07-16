package com.linkit.company.feature.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.storage.generated.resources.Res
import linkitcompany.feature.storage.generated.resources.storage_related_video
import org.jetbrains.compose.resources.painterResource

@Composable
fun StorageDetailScreen(
    onBack: () -> Unit = {},
    onCreateSchedule: () -> Unit = {},
    viewModel: StorageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    StorageDetailContent(
        selectedDay = uiState.selectedDay,
        onDaySelected = { viewModel.onIntent(StorageIntent.SelectDay(it)) },
        onBack = onBack,
        onCreateSchedule = onCreateSchedule,
    )
}

@Composable
fun StorageDetailContent(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onBack: () -> Unit = {},
    onCreateSchedule: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DetailTopBar(onBack = onBack) }
        item { VideoHero() }
        item { AiSummary() }
        item {
            Text(
                text = "타임라인",
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            )
        }
        item { DaySelector(selectedDay = selectedDay, onDaySelected = onDaySelected) }
        item {
            Text(
                text = "${selectedDay}일차",
                style = LinkItTheme.typography.headline2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 14.dp),
            )
        }
        items(3) { index ->
            TimelinePlaceCard(index = index, expanded = index == 1)
        }
        item {
            Text(
                text = "함께 볼만한 영상",
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.neutral,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 14.dp),
            )
        }
        item { RelatedVideos() }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 34.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LinkItTheme.color.semantic.interaction.disable)
                    .clickable(onClick = onCreateSchedule),
                contentAlignment = Alignment.Center,
            ) {
                Text("일정 만들기", style = LinkItTheme.typography.body1NormalSemibold, color = LinkItTheme.color.semantic.label.alternative)
            }
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "뒤로가기",
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Text(
            text = "파리 여행",
            style = LinkItTheme.typography.heading2Medium,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Icon(LinkItIcon.Control.Trash, "삭제", tint = LinkItTheme.color.semantic.label.assistive, modifier = Modifier.size(20.dp))
        Icon(LinkItIcon.Control.Bookmark, "보관", tint = LinkItTheme.color.semantic.label.assistive, modifier = Modifier.padding(start = 12.dp).size(20.dp))
    }
}

@Composable
private fun VideoHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .height(188.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LinkItTheme.color.semantic.fill.strong),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(LinkItTheme.color.semantic.background.elevated.normal),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", color = LinkItTheme.color.semantic.label.alternative)
        }
    }
}

@Composable
private fun AiSummary() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(LinkItTheme.color.semantic.fill.strong))
            Text("AI 분석 요약", style = LinkItTheme.typography.body1NormalSemibold, color = LinkItTheme.color.semantic.label.alternative, modifier = Modifier.padding(start = 7.dp))
            Spacer(Modifier.weight(1f))
            Text("프랑스", style = LinkItTheme.typography.label1NormalMedium, color = LinkItTheme.color.semantic.primary.normal)
        }
        Text(
            text = "이 영상은 산토리니에서 즐기는 완벽한 48시간 해안 휴양지를 소개합니다. 이 여정은 상징적인 사진 촬영 명소와 숨겨진 현지 맛집을 조화롭게 구성했습니다.",
            style = LinkItTheme.typography.body2ReadingRegular,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryInfo("▣ 일정 3박4일", Modifier.weight(1f))
            SummaryInfo("＄ 예산 82만원", Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryInfo(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption1Medium,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.background.normal.normal)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
private fun DaySelector(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        items((1..5).toList()) { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${day}일차", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedDay == day) LinkItTheme.color.semantic.label.alternative else LinkItTheme.color.semantic.background.normal.normal)
                        .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, CircleShape)
                        .clickable { onDaySelected(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.toString(),
                        style = LinkItTheme.typography.body2NormalMedium,
                        color = if (selectedDay == day) LinkItTheme.color.semantic.static.white else LinkItTheme.color.semantic.label.assistive,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelinePlaceCard(index: Int, expanded: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp)) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(LinkItTheme.color.semantic.fill.normal),
            contentAlignment = Alignment.Center,
        ) {
            Text((index + 1).toString(), style = LinkItTheme.typography.caption2Medium, color = LinkItTheme.color.semantic.label.alternative)
        }
        Column(
            modifier = Modifier
                .padding(start = 9.dp)
                .weight(1f)
                .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            Row {
                Box(
                    modifier = Modifier.size(74.dp).clip(RoundedCornerShape(9.dp)).background(LinkItTheme.color.semantic.fill.strong),
                )
                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        DetailTag("박물관")
                        DetailTag("조용한")
                    }
                    Text("루브르 박물관", style = LinkItTheme.typography.body1NormalSemibold, color = LinkItTheme.color.semantic.label.strong, modifier = Modifier.padding(top = 6.dp))
                    Text("⌖ 뤼 드 리볼리, 75001 파리", style = LinkItTheme.typography.caption2Regular, color = LinkItTheme.color.semantic.label.alternative, modifier = Modifier.padding(top = 3.dp))
                }
                Icon(LinkItIcon.Utility.MoreVertical, "더보기", tint = LinkItTheme.color.semantic.label.assistive, modifier = Modifier.size(18.dp))
            }
            if (expanded) {
                Text(
                    text = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
                    style = LinkItTheme.typography.caption1Regular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Text(
                text = if (expanded) "간략히 보기 ︿" else "자세히 보기 ﹀",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.assistive,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
            )
        }
    }
}

@Composable
private fun DetailTag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption3Medium,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(LinkItTheme.color.semantic.fill.normal).padding(horizontal = 7.dp, vertical = 4.dp),
    )
}

@Composable
private fun RelatedVideos() {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(3) {
            Column(modifier = Modifier.width(160.dp)) {
                Image(
                    painter = painterResource(Res.drawable.storage_related_video),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(86.dp).clip(RoundedCornerShape(10.dp)),
                )
                Text("Paracosm", style = LinkItTheme.typography.body2NormalMedium, color = LinkItTheme.color.semantic.label.alternative, modifier = Modifier.padding(top = 8.dp))
                Text("Album · Absolutely", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.assistive)
            }
        }
    }
}
