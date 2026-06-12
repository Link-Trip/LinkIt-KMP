package com.linkit.company.core.designsystem.legacy.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.component.LinkItButton
import com.linkit.company.core.designsystem.legacy.component.LinkItDaySelector
import com.linkit.company.core.designsystem.legacy.component.LinkItDivider
import com.linkit.company.core.designsystem.legacy.component.LinkItFilterChip
import com.linkit.company.core.designsystem.legacy.component.LinkItFolderCard
import com.linkit.company.core.designsystem.legacy.component.LinkItScheduleCard
import com.linkit.company.core.designsystem.legacy.component.LinkItScheduleListItem
import com.linkit.company.core.designsystem.legacy.component.LinkItTab
import com.linkit.company.core.designsystem.legacy.component.LinkItTag
import com.linkit.company.core.designsystem.legacy.component.LinkItTextField
import com.linkit.company.core.designsystem.legacy.component.LinkItTopAppBar
import com.linkit.company.core.designsystem.legacy.component.LinkItTransportInfo
import com.linkit.company.core.designsystem.legacy.component.LinkItVideoCard
import com.linkit.company.core.designsystem.legacy.component.TagColorScheme
import com.linkit.company.core.designsystem.legacy.component.TagData
import com.linkit.company.core.designsystem.legacy.theme.G4
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlue

@Composable
fun DesignShowcaseScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            LinkItTopAppBar(
                title = "디자인 컴포넌트",
                showBackButton = true,
                onBackClick = onBack,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            // ── Tag ──
            item { SectionHeader("Tag") }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TagColorScheme.entries.forEach { scheme ->
                            LinkItTag(text = scheme.name, colorScheme = scheme)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinkItTag(text = "박물관", colorScheme = TagColorScheme.Blue)
                        LinkItTag(text = "조용한", colorScheme = TagColorScheme.Green)
                        LinkItTag(text = "SNS 핫플", colorScheme = TagColorScheme.Red)
                        LinkItTag(text = "# 먹방여행", colorScheme = TagColorScheme.Keyword)
                    }
                }
            }

            // ── Button ──
            item { SectionHeader("Button") }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LinkItButton(text = "일정 생성하기", onClick = {})
                    LinkItButton(text = "일정 생성하기", onClick = {}, enabled = false)
                }
            }

            // ── TextField ──
            item { SectionHeader("TextField") }
            item {
                var textValue by remember { mutableStateOf("") }
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LinkItTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        label = "영상 링크",
                        placeholder = "URL 를 붙여넣거나 입력해주세요.",
                        maxLength = 200,
                    )
                    LinkItTextField(
                        value = "잘못된 링크",
                        onValueChange = {},
                        label = "에러 상태",
                        placeholder = "",
                        isError = true,
                        errorMessage = "올바른 URL을 입력해주세요",
                        minHeight = 60,
                    )
                }
            }

            // ── FilterChip ──
            item { SectionHeader("FilterChip") }
            item {
                var selectedChip by remember { mutableIntStateOf(-1) }
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("지역", "여행 테마", "비용").forEachIndexed { index, label ->
                        LinkItFilterChip(
                            label = label,
                            onClick = {
                                selectedChip = if (selectedChip == index) -1 else index
                            },
                            selected = selectedChip == index,
                        )
                    }
                }
            }

            // ── Tab ──
            item { SectionHeader("Tab") }
            item {
                var selectedTab by remember { mutableIntStateOf(0) }
                LinkItTab(
                    tabs = listOf("여행 일정", "영상 요약"),
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            // ── DaySelector ──
            item { SectionHeader("DaySelector") }
            item {
                var selectedDay by remember { mutableIntStateOf(1) }
                LinkItDaySelector(
                    totalDays = 7,
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    availableDays = 4,
                )
            }

            // ── Divider ──
            item { SectionHeader("Divider") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkItDivider()
                    LinkItDivider(startIndent = 16.dp)
                }
            }

            // ── TransportInfo ──
            item { SectionHeader("TransportInfo") }
            item {
                LinkItTransportInfo(
                    transportName = "신칸센 고속열차",
                    duration = "30분",
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // ── ScheduleCard ──
            item { SectionHeader("ScheduleCard") }
            item {
                LinkItScheduleCard(
                    image = { PlaceholderBox(Modifier.size(80.dp)) },
                    tags = listOf(
                        TagData("박물관", TagColorScheme.Blue),
                        TagData("조용한", TagColorScheme.Green),
                    ),
                    title = "루브르 박물관",
                    address = "뤼 드 리볼리, 75001 파리",
                    description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
                    onDetailClick = {},
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // ── ScheduleListItem ──
            item { SectionHeader("ScheduleListItem") }
            item {
                Column {
                    LinkItScheduleListItem(
                        thumbnail = { PlaceholderBox(Modifier.size(width = 80.dp, height = 100.dp)) },
                        tags = listOf(
                            TagData("# 먹방여행", TagColorScheme.Keyword),
                            TagData("# SNS 핫플레이스", TagColorScheme.Keyword),
                        ),
                        title = "도쿄 신주쿠 여행",
                        duration = "3박4일",
                        cost = "82만원",
                        summary = "AI 한줄 요약된 여행지 정보",
                        onClick = {},
                        onMoreClick = {},
                    )
                    LinkItDivider(startIndent = 16.dp)
                    LinkItScheduleListItem(
                        thumbnail = { PlaceholderBox(Modifier.size(width = 80.dp, height = 100.dp)) },
                        tags = listOf(
                            TagData("# 먹방여행", TagColorScheme.Keyword),
                        ),
                        title = "도쿄 나가노 여행",
                        duration = "2박3일",
                        cost = "65만원",
                        summary = null,
                        onClick = {},
                        onMoreClick = {},
                    )
                }
            }

            // ── TopAppBar ──
            item { SectionHeader("TopAppBar") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinkItTopAppBar(title = "제목만 있는 앱바")
                    LinkItTopAppBar(
                        title = "뒤로가기 있는 앱바",
                        showBackButton = true,
                        onBackClick = {},
                    )
                }
            }

            // ── VideoCard ──
            item { SectionHeader("VideoCard") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(3) { index ->
                        LinkItVideoCard(
                            thumbnail = {
                                PlaceholderBox(
                                    Modifier
                                        .width(160.dp)
                                        .height(90.dp),
                                )
                            },
                            title = "여행 영상 제목 ${index + 1}",
                            viewCount = "조회수 ${(index + 1) * 50}만회",
                            onClick = {},
                            onCopyLinkClick = if (index == 0) {
                                {}
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            // ── FolderCard ──
            item { SectionHeader("FolderCard") }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    listOf("교통" to 6, "식사" to 5, "관광지" to 3).forEach { (name, count) ->
                        LinkItFolderCard(
                            thumbnail = {
                                PlaceholderBox(
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.2f),
                                )
                            },
                            name = name,
                            itemCount = count,
                            onClick = {},
                            onMoreClick = {},
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = LinkItTextStyle.heading,
        color = PrimaryBlue,
        modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 12.dp),
    )
}

@Composable
private fun PlaceholderBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(G4))
}
