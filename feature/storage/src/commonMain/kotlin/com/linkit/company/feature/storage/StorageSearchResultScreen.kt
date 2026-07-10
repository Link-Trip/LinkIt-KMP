package com.linkit.company.feature.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun StorageSearchResultScreen(
    onBack: () -> Unit = {},
    onOpenItem: () -> Unit = {},
) {
    StorageSearchResultContent(
        query = "일본",
        onBack = onBack,
        onOpenItem = onOpenItem,
    )
}

@Composable
fun StorageSearchResultContent(
    query: String,
    onBack: () -> Unit = {},
    onOpenItem: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        SearchResultTopBar(query = query, onBack = onBack)
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "\"$query\"",
                style = LinkItTheme.typography.body1NormalSemibold,
                color = LinkItTheme.color.semantic.primary.normal,
            )
            Text(
                text = " 검색결과",
                style = LinkItTheme.typography.body1NormalRegular,
                color = LinkItTheme.color.semantic.label.alternative,
            )
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ResultFilter("◉ 지역")
            ResultFilter("♣ 여행 테마")
            ResultFilter("＄ 비용")
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("총 8개 항목", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
            Text("최신순", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
        }
        val results = listOf(
            ResultFixture("도쿄 신주쿠 여행", "🗻", 0),
            ResultFixture("도쿄 나가노 여행", "🏔️", 1),
            ResultFixture("도쿄 시부야 여행", "🗼", 2),
            ResultFixture("도쿄 나기노 여행", "🏛️", 3),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(results) { item ->
                SearchResultCard(item = item, onClick = onOpenItem)
            }
        }
    }
}

@Composable
private fun SearchResultTopBar(query: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, top = 16.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "뒤로가기",
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Row(
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = query,
                style = LinkItTheme.typography.body1NormalRegular,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = LinkItIcon.Utility.Search,
                contentDescription = "검색",
                tint = LinkItTheme.color.semantic.label.assistive,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ResultFilter(text: String) {
    Text(
        text = "$text ⌄",
        style = LinkItTheme.typography.label1NormalSemibold,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.fill.normal)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}

@Composable
private fun SearchResultCard(item: ResultFixture, onClick: () -> Unit) {
    val background = when (item.accent) {
        0 -> LinkItTheme.color.semantic.accent.background.cyan
        1 -> LinkItTheme.color.semantic.accent.background.lightBlue
        2 -> LinkItTheme.color.semantic.accent.background.redOrange
        else -> LinkItTheme.color.semantic.accent.background.violet
    }.copy(alpha = .18f)
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(98.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Text(item.emoji, style = LinkItTheme.typography.display2Bold)
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SearchTag("# 먹방여행")
                SearchTag("# SNS 핫플레이스")
            }
            Text(
                text = item.title,
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(top = 7.dp),
            )
            Text(
                text = "▣ 3박4일    ＄ 82만원",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "AI 한줄 요약된 여행지 정보",
                style = LinkItTheme.typography.caption2Regular,
                color = LinkItTheme.color.semantic.label.alternative,
            )
        }
        Icon(
            imageVector = LinkItIcon.Utility.MoreHorizontal,
            contentDescription = "더보기",
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SearchTag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption3Medium,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier
            .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp),
    )
}

private data class ResultFixture(
    val title: String,
    val emoji: String,
    val accent: Int,
)
