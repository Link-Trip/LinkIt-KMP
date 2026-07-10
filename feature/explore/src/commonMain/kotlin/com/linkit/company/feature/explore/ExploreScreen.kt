package com.linkit.company.feature.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.component.navigation.LinkItTab
import com.linkit.company.core.designsystem.component.navigation.LinkItTabRow
import com.linkit.company.core.designsystem.component.navigation.TabSize
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.explore.generated.resources.Res
import linkitcompany.feature.explore.generated.resources.explore_channel_avatar
import linkitcompany.feature.explore.generated.resources.explore_fuji
import linkitcompany.feature.explore.generated.resources.explore_kyoto
import linkitcompany.feature.explore.generated.resources.explore_osaka
import linkitcompany.feature.explore.generated.resources.explore_tokyo
import linkitcompany.feature.explore.generated.resources.explore_video_mushroom
import linkitcompany.feature.explore.generated.resources.explore_video_osaka
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val ExploreBackground = Color(0xFFF4F4F5)
private val ExploreText = Color(0xFF1F2127)
private val ExploreSubText = Color(0xFF5D6470)
private val ExploreBlue = Color(0xFF388AFE)

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ExploreContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun ExploreContent(
    uiState: ExploreUiState,
    onIntent: (ExploreIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        ExploreToolbar()
        ExploreTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = { onIntent(ExploreIntent.SelectTab(it)) },
        )
        when (uiState.selectedTab) {
            ExploreTab.COUNTRY -> CountryExploreContent(
                selectedCountry = uiState.selectedCountry,
                onCountrySelected = { onIntent(ExploreIntent.SelectCountry(it)) },
                modifier = Modifier.weight(1f),
            )

            ExploreTab.THEME -> ThemeExploreContent(
                selectedTheme = uiState.selectedTheme,
                onThemeSelected = { onIntent(ExploreIntent.SelectTheme(it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExploreToolbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "탐색",
            style = LinkItTheme.typography.headline2Bold,
            color = LinkItTheme.color.semantic.label.strong,
        )
    }
}

@Composable
private fun ExploreTabs(
    selectedTab: ExploreTab,
    onTabSelected: (ExploreTab) -> Unit,
) {
    LinkItTabRow(size = TabSize.Small) {
        LinkItTab(
            selected = selectedTab == ExploreTab.COUNTRY,
            onClick = { onTabSelected(ExploreTab.COUNTRY) },
            text = "국가별 둘러보기",
        )
        LinkItTab(
            selected = selectedTab == ExploreTab.THEME,
            onClick = { onTabSelected(ExploreTab.THEME) },
            text = "테마별 둘러보기",
        )
    }
}

@Composable
private fun CountryExploreContent(
    selectedCountry: ExploreCountry,
    onCountrySelected: (ExploreCountry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(ExploreBackground),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            CountryFilters(
                selectedCountry = selectedCountry,
                onCountrySelected = onCountrySelected,
            )
        }
        item {
            DestinationSection(selectedCountry)
        }
        item {
            CreatorSection(selectedCountry)
        }
        item {
            TrendingVideoSection(selectedCountry)
        }
    }
}

@Composable
private fun CountryFilters(
    selectedCountry: ExploreCountry,
    onCountrySelected: (ExploreCountry) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ExploreCountry.entries) { country ->
            CountryChip(
                country = country,
                selected = country == selectedCountry,
                onClick = { onCountrySelected(country) },
            )
        }
    }
}

@Composable
private fun CountryChip(
    country: ExploreCountry,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(if (selected) ExploreText else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = country.flag,
            fontSize = 13.sp,
            lineHeight = 16.sp,
        )
        Text(
            text = country.label,
            style = LinkItTheme.typography.label2Bold,
            color = if (selected) Color.White else Color.Black,
            maxLines = 1,
        )
    }
}

@Composable
private fun DestinationSection(selectedCountry: ExploreCountry) {
    val destinations = listOf(
        Destination("도쿄", "네온사인, 애니메이션 문화, 미슐랭 스타 라멘", Res.drawable.explore_tokyo),
        Destination("교토", "고대 사원, 대나무 숲, 게이샤 거리", Res.drawable.explore_kyoto),
        Destination("오사카", "길거리 음식의 중심지이자 활기찬 밤문화", Res.drawable.explore_osaka),
        Destination("Mt. Fuji", "Japan's iconic peak and surrounding lakes", Res.drawable.explore_fuji),
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeading(
            title = if (selectedCountry == ExploreCountry.ALL) {
                "🌏 인기 여행지 둘러보기"
            } else {
                "🌏 ${selectedCountry.label} 인기 여행지 둘러보기"
            },
            subtitle = if (selectedCountry == ExploreCountry.ALL) {
                "어디로 떠나볼까요?"
            } else {
                "${selectedCountry.label} 어디로 떠나볼까요?"
            },
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(destinations) { destination ->
                DestinationCard(destination)
            }
        }
    }
}

@Composable
private fun DestinationCard(destination: Destination) {
    Box(
        modifier = Modifier
            .size(width = 128.dp, height = 160.dp)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Image(
            painter = painterResource(destination.image),
            contentDescription = destination.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.72f),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = destination.title,
                style = LinkItTheme.typography.label2Medium,
                color = Color.White,
                maxLines = 1,
            )
            Text(
                text = destination.description,
                style = LinkItTheme.typography.caption3Medium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CreatorSection(selectedCountry: ExploreCountry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeading(
            title = if (selectedCountry == ExploreCountry.ALL) {
                "인기있는 여행 크리에이터"
            } else {
                "${selectedCountry.label}여행, 이런 유튜버는 어때요?"
            },
            subtitle = if (selectedCountry == ExploreCountry.ALL) {
                "나랑 맞는 크리에이터는 누가 있을까요?"
            } else {
                "${selectedCountry.label}여행 에서 추천하는 여행 유튜버예요"
            },
            action = "더보기",
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(3) {
                CreatorCard()
            }
        }
    }
}

@Composable
private fun CreatorCard() {
    Column(
        modifier = Modifier
            .width(126.dp)
            .height(128.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF7F7F8)),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "빠니보틀",
                style = LinkItTheme.typography.caption1Medium,
                color = ExploreText,
            )
            Text(
                text = "240만 구독자",
                style = LinkItTheme.typography.caption3Regular,
                color = ExploreSubText,
            )
        }
        Box(
            modifier = Modifier
                .width(94.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF7F7F8)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "영상 보러가기",
                style = LinkItTheme.typography.caption1Medium,
                color = Color(0x9C37383C),
            )
        }
    }
}

@Composable
private fun TrendingVideoSection(selectedCountry: ExploreCountry) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(
            title = if (selectedCountry == ExploreCountry.ALL) {
                "많이 분석된 여행 영상"
            } else {
                "많이 요약된 ${selectedCountry.label}여행 영상"
            },
            subtitle = if (selectedCountry == ExploreCountry.ALL) {
                "사람들이 많이 분석한 영상이에요"
            } else {
                "한국 사람들이 많이 분석한 ${selectedCountry.label}관련 영상이에요"
            },
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VideoCard(image = painterResource(Res.drawable.explore_video_osaka))
            VideoCard(image = painterResource(Res.drawable.explore_video_mushroom))
            if (selectedCountry != ExploreCountry.ALL) {
                VideoCard(image = painterResource(Res.drawable.explore_video_osaka))
                VideoCard(image = painterResource(Res.drawable.explore_video_mushroom))
            }
        }
    }
}

@Composable
private fun VideoCard(
    image: Painter,
    imageHeight: androidx.compose.ui.unit.Dp = 112.dp,
    showAnalysisCount: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
        ) {
            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                if (showAnalysisCount) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "40회 분석됨",
                            style = LinkItTheme.typography.caption1Bold,
                            color = Color.White,
                        )
                    }
                } else {
                    Box(Modifier.size(1.dp))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LinkItIcon.Control.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ExploreText,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "오사카 여행 전 필수시청! 오사카 핵심 요약 완벽 가이드 [맛집, 숙소추천, 가볼만한곳, 주유패스, 공항에서시내가는법, 일본여행]",
                    style = LinkItTheme.typography.label2Medium,
                    color = ExploreText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    VideoMetaText("조회수 25만회")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(10.dp)
                            .background(Color(0x667B8696)),
                    )
                    VideoMetaText("2개월 전")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.explore_channel_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = "타이거 투어",
                    style = LinkItTheme.typography.caption3Medium,
                    color = Color(0xFF64748B),
                )
            }
        }
    }
}

@Composable
private fun VideoMetaText(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption3Medium,
        color = ExploreText.copy(alpha = 0.4f),
    )
}

@Composable
private fun ThemeExploreContent(
    selectedTheme: ExploreTheme,
    onThemeSelected: (ExploreTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(ExploreBackground),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionHeading(
                title = "테마별 추천 영상",
                subtitle = "선택된 테마에 따라 영상을 추천해드려요",
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ExploreTheme.entries) { theme ->
                    ThemeFilter(
                        theme = theme,
                        selected = theme != ExploreTheme.ALL && theme == selectedTheme,
                        onClick = { onThemeSelected(theme) },
                    )
                }
            }
        }
        items(3) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                VideoCard(
                    image = painterResource(
                        if (it % 2 == 0) Res.drawable.explore_fuji else Res.drawable.explore_kyoto,
                    ),
                    imageHeight = 168.dp,
                    showAnalysisCount = false,
                )
            }
        }
    }
}

@Composable
private fun ThemeFilter(
    theme: ExploreTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(CircleShape)
            .background(if (selected) ExploreText else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "🍜", fontSize = 13.sp, lineHeight = 16.sp)
        Text(
            text = theme.label,
            style = LinkItTheme.typography.label2Bold,
            color = if (selected) Color.White else Color.Black,
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String,
    action: String? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = LinkItTheme.typography.label1NormalBold.copy(letterSpacing = 0.sp),
                color = ExploreText,
            )
            Text(
                text = subtitle,
                style = LinkItTheme.typography.caption1Regular.copy(
                    lineHeight = 17.sp,
                    letterSpacing = 0.sp,
                ),
                color = ExploreSubText,
            )
        }
        if (action != null) {
            Text(
                text = action,
                style = LinkItTheme.typography.caption1Bold,
                color = ExploreBlue,
            )
        }
    }
}

private data class Destination(
    val title: String,
    val description: String,
    val image: DrawableResource,
)
