package com.linkit.company.feature.storage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.storage.generated.resources.Res
import linkitcompany.feature.storage.generated.resources.storage_search_tokyo_1
import linkitcompany.feature.storage.generated.resources.storage_search_tokyo_2
import linkitcompany.feature.storage.generated.resources.storage_search_tokyo_3
import linkitcompany.feature.storage.generated.resources.storage_search_tokyo_4
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
    val results = listOf(
        ResultFixture("도쿄 신주쿠 여행", Res.drawable.storage_search_tokyo_1),
        ResultFixture("도쿄 나가노 여행", Res.drawable.storage_search_tokyo_2),
        ResultFixture("도쿄 시부야 여행", Res.drawable.storage_search_tokyo_3),
        ResultFixture("도쿄 나가노 여행", Res.drawable.storage_search_tokyo_4),
    )
    val countStyle = LinkItTheme.typography.label1NormalBold.copy(
        fontSize = 13.sp,
        lineHeight = 17.55.sp,
        letterSpacing = (-.13).sp,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        SearchResultTopBar(query = query, onBack = onBack)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
            Text(
                text = "“$query”",
                style = LinkItTheme.typography.label1NormalBold,
                color = LinkItTheme.color.semantic.primary.normal,
            )
            Text(
                text = " 검색결과",
                style = LinkItTheme.typography.label1NormalBold,
                color = LinkItTheme.color.semantic.label.strong,
            )
        }
        Column(modifier = Modifier.fillMaxSize().alpha(.3f).padding(top = 16.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ResultFilter("지역", LinkItIcon.Location.Globe)
                ResultFilter("여행 테마", LinkItIcon.Utility.Category)
                ResultFilter("비용", LinkItIcon.Utility.Money)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp)
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .alpha(.4f),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("총 8개 항목", style = countStyle, color = LinkItTheme.color.semantic.label.strong)
                Text("최신순", style = countStyle, color = LinkItTheme.color.semantic.label.strong)
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { item ->
                    SearchResultCard(item = item, onClick = onOpenItem)
                }
            }
        }
    }
}

@Composable
private fun SearchResultTopBar(query: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "뒤로가기",
            tint = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.size(24.dp).clickable(onClick = onBack),
        )
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = query,
                style = LinkItTheme.typography.body1NormalRegular,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
            Icon(
                imageVector = LinkItIcon.Utility.Search,
                contentDescription = "검색",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.size(24.dp).alpha(.4f),
            )
        }
    }
}

@Composable
private fun ResultFilter(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.fill.normal)
            .padding(start = 12.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinkItTheme.color.semantic.label.strong, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = LinkItTheme.typography.label1NormalBold.copy(
                fontSize = 13.sp,
                lineHeight = 18.2.sp,
                letterSpacing = 0.sp,
            ),
            color = LinkItTheme.color.semantic.label.strong,
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronDownSmall,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SearchResultCard(item: ResultFixture, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().height(125.dp).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(item.thumbnail),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(80.dp).height(100.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(
                modifier = Modifier.weight(1f).height(100.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(26.dp)) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SearchTag("# 먹방여행", 64.dp)
                        SearchTag("# SNS 핫플레이스", 98.dp)
                    }
                    Icon(
                        imageVector = LinkItIcon.Utility.MoreHorizontal,
                        contentDescription = "더보기",
                        tint = LinkItTheme.color.semantic.label.strong,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Text(
                    text = item.title,
                    style = LinkItTheme.typography.label1NormalBold.copy(
                        fontSize = 15.sp,
                        lineHeight = 21.75.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = LinkItTheme.color.semantic.label.strong,
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.height(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ResultMeta(LinkItIcon.Utility.Calendar, "3박4일")
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(12.dp)
                                .background(LinkItTheme.color.semantic.line.solid.normal),
                        )
                        ResultMeta(LinkItIcon.Utility.Money, "82만원")
                    }
                    Text(
                        text = "AI 한줄 요약된 여행지 정보",
                        style = LinkItTheme.typography.caption1Bold.copy(lineHeight = 17.4.sp, letterSpacing = 0.sp),
                        color = LinkItTheme.color.semantic.label.alternative,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(LinkItTheme.color.semantic.line.normal.alternative),
        )
    }
}

@Composable
private fun SearchTag(text: String, width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(26.dp)
            .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.caption3Regular.copy(letterSpacing = (-.1).sp),
            color = LinkItTheme.color.semantic.label.alternative,
        )
    }
}

@Composable
private fun ResultMeta(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Bold.copy(lineHeight = 16.2.sp, letterSpacing = .174.sp),
            color = LinkItTheme.color.semantic.label.alternative,
        )
    }
}

private data class ResultFixture(
    val title: String,
    val thumbnail: DrawableResource,
)
