package com.linkit.company.feature.explore

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.linkit.company.core.designsystem.component.chip.ChipDefaults
import com.linkit.company.core.designsystem.component.chip.ChipVariant
import com.linkit.company.core.designsystem.component.chip.LinkItChip
import com.linkit.company.core.designsystem.component.navigation.LinkItTab
import com.linkit.company.core.designsystem.component.navigation.LinkItTabRow
import com.linkit.company.core.designsystem.component.navigation.TabSize
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.video.DiscoverChannel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ExploreScreen(
    onOpenCreators: () -> Unit = {},
    viewModel: ExploreViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    ExploreContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onOpenCreators = onOpenCreators,
        onOpenUrl = { url ->
            if (!openExploreUrl(url, uriHandler::openUri)) viewModel.onIntent(ExploreIntent.LinkOpenFailed)
        },
    )
}

@Composable
fun ExploreContent(
    uiState: ExploreUiState,
    onIntent: (ExploreIntent) -> Unit,
    onOpenCreators: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isCountryTab = uiState.selectedTab == ExploreTab.COUNTRY
    Column(modifier.fillMaxSize().background(LinkItTheme.color.semantic.background.normal.normal)) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("탐색", style = LinkItTheme.typography.headline2Bold, color = LinkItTheme.color.semantic.label.strong)
        }
        LinkItTabRow(size = TabSize.Small) {
            LinkItTab(uiState.selectedTab == ExploreTab.COUNTRY, { onIntent(ExploreIntent.SelectTab(ExploreTab.COUNTRY)) }, "국가별 둘러보기")
            LinkItTab(uiState.selectedTab == ExploreTab.THEME, { onIntent(ExploreIntent.SelectTab(ExploreTab.THEME)) }, "테마별 둘러보기")
        }
        ExploreLinkError(uiState.linkErrorMessage) { onIntent(ExploreIntent.DismissLinkError) }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().background(LinkItTheme.color.atomic.CoolNeutral98).testTag("explore-video-feed"),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCountryTab) 8.dp else 12.dp),
        ) {
            if (!isCountryTab) item {
                ExploreSectionHeading("테마별 추천 영상", "선택된 테마에 따라 영상을 추천해드려요")
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.selectedTab == ExploreTab.COUNTRY) {
                        item {
                            ExploreFilterChip("전체", uiState.selectedCountry == null && uiState.selectedRegion == null) {
                                onIntent(ExploreIntent.SelectCountry(null))
                            }
                        }
                        items(uiState.countries, key = { "country:${it.country}" }) { country ->
                            ExploreFilterChip(country.country, uiState.selectedCountry == country.country) {
                                onIntent(ExploreIntent.SelectCountry(country.country))
                            }
                        }
                        items(listOf("동남아시아", "유럽"), key = { "region:$it" }) { region ->
                            ExploreFilterChip(region, uiState.selectedRegion == region) { onIntent(ExploreIntent.SelectRegion(region)) }
                        }
                    } else {
                        items(ExploreTheme.entries) { theme ->
                            ExploreFilterChip(theme.label, uiState.selectedTheme == theme) { onIntent(ExploreIntent.SelectTheme(theme)) }
                        }
                    }
                }
            }
            if (isCountryTab) {
                if (uiState.isCatalogLoading || uiState.catalogErrorMessage != null) item {
                    ExploreStatus(uiState.isCatalogLoading, uiState.catalogErrorMessage, "", false) {
                        onIntent(ExploreIntent.RetryCatalog)
                    }
                }
                if (uiState.countries.isNotEmpty()) {
                    item {
                        ExploreSectionHeading("🌏 인기 여행지 둘러보기", "생성된 일정 수가 많은 나라예요", modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                    }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.countries, key = { it.country }) { country ->
                                Box(
                                    Modifier.size(width = 128.dp, height = 160.dp).clip(RoundedCornerShape(12.dp))
                                        .background(LinkItTheme.color.semantic.fill.strong)
                                        .testTag("explore-country-${country.country}")
                                        .clickable(role = Role.Button) { onIntent(ExploreIntent.SelectCountry(country.country)) },
                                ) {
                                    val thumbnail = uiState.videos.firstOrNull { it.country == country.country }?.thumbnailUrl
                                    AsyncImage(thumbnail, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                                        0f to LinkItTheme.color.semantic.static.black.copy(alpha = 0f),
                                        0.5f to LinkItTheme.color.semantic.static.black.copy(alpha = 0f),
                                        1f to LinkItTheme.color.semantic.static.black.copy(alpha = 0.7f),
                                    )))
                                    Column(Modifier.align(Alignment.BottomStart).padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(country.country, style = LinkItTheme.typography.label2Medium, color = LinkItTheme.color.semantic.static.white)
                                        Text("${country.tripPlanCount}개 일정", style = LinkItTheme.typography.caption3Regular, color = LinkItTheme.color.semantic.static.white.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
                if (uiState.channels.isNotEmpty()) {
                    item {
                        ExploreSectionHeading("인기있는 여행 크리에이터", "추천 여행 채널의 영상을 만나보세요", "더보기", onOpenCreators, Modifier.padding(top = 12.dp, bottom = 4.dp))
                    }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.channels, key = { it.channelId }) { channel ->
                                ExploreCreatorCard(channel) { onOpenUrl(channel.youtubeChannelUrl()) }
                            }
                        }
                    }
                }
            }
            if (isCountryTab) item {
                ExploreSectionHeading(
                    "추천 여행 영상",
                    uiState.selectedCountry ?: uiState.selectedRegion ?: "다음 여행을 영상으로 만나보세요",
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }
            if (uiState.isLoading) item { ExploreStatus(loading = true) }
            items(uiState.videos, key = { it.videoId }) { video ->
                Box(Modifier.padding(horizontal = 20.dp)) {
                    ExploreVideoCard(
                        title = video.title,
                        thumbnailUrl = video.thumbnailUrl,
                        channelTitle = video.channelTitle,
                        metadata = listOf("조회수 ${video.viewCount}회", formatExploreDuration(video.duration), video.publishedAt.substringBefore('T'))
                            .filter(String::isNotBlank).joinToString(" · "),
                        onClick = { onOpenUrl(video.videoUrl) },
                        imageHeight = if (isCountryTab) 112.dp else 168.dp,
                    )
                }
            }
            if (uiState.isLoadingMore || uiState.errorMessage != null || (!uiState.isLoading && uiState.videos.isEmpty())) item {
                ExploreStatus(
                    loading = uiState.isLoadingMore,
                    errorMessage = uiState.errorMessage,
                    emptyMessage = "조건에 맞는 여행 영상이 아직 없어요.",
                    empty = !uiState.isLoading && uiState.videos.isEmpty(),
                    onRetry = { onIntent(ExploreIntent.RetryVideos) },
                )
            }
            if (uiState.hasNext && !uiState.isLoadingMore && uiState.errorMessage == null) {
                item { TextButton(onClick = { onIntent(ExploreIntent.LoadMore) }, modifier = Modifier.fillMaxWidth()) { Text("영상 더보기") } }
            }
        }
    }
}

@Composable
internal fun ExploreVideoCard(
    title: String,
    thumbnailUrl: String,
    channelTitle: String,
    metadata: String,
    onClick: () -> Unit,
    imageHeight: Dp = 168.dp,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.background.normal.normal)
            .clickable(role = Role.Button, onClickLabel = "원본 영상 열기", onClick = onClick),
    ) {
        AsyncImage(thumbnailUrl, null, Modifier.fillMaxWidth().height(imageHeight).testTag("explore-video-thumbnail"), contentScale = ContentScale.Crop)
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = LinkItTheme.typography.label2Medium, color = LinkItTheme.color.semantic.label.strong, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (metadata.isNotBlank()) Text(metadata, style = LinkItTheme.typography.caption3Medium, color = LinkItTheme.color.semantic.label.alternative, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(channelTitle, style = LinkItTheme.typography.caption3Medium, color = LinkItTheme.color.semantic.label.neutral)
        }
    }
}

@Composable
internal fun ExploreCreatorCard(channel: DiscoverChannel, onClick: () -> Unit) {
    Column(
        Modifier.width(126.dp).clip(RoundedCornerShape(12.dp))
            .background(LinkItTheme.color.semantic.background.normal.normal)
            .clickable(role = Role.Button, onClickLabel = "채널 열기", onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(channel.thumbnailUrl, null, Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(channel.title, style = LinkItTheme.typography.caption1Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("구독자 ${channel.subscriberCount}명", style = LinkItTheme.typography.caption3Regular, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(width = 94.dp, height = 24.dp).clip(RoundedCornerShape(6.dp)).background(LinkItTheme.color.semantic.fill.alternative), contentAlignment = Alignment.Center) {
            Text("채널 보러가기", style = LinkItTheme.typography.caption1Medium, color = LinkItTheme.color.semantic.label.alternative)
        }
    }
}

@Composable
internal fun ExploreFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    LinkItChip(
        onClick = onClick,
        text = label,
        active = selected,
        modifier = Modifier.height(34.dp).testTag("explore-filter-$label"),
        shape = CircleShape,
        colors = ChipDefaults.colors(ChipVariant.Solid, selected).copy(
            containerColor = if (selected) LinkItTheme.color.semantic.label.strong else LinkItTheme.color.semantic.background.normal.normal,
            contentColor = if (selected) LinkItTheme.color.semantic.static.white else LinkItTheme.color.semantic.label.strong,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        textStyle = LinkItTheme.typography.label2Bold,
    )
}

@Composable
internal fun ExploreSectionHeading(title: String, subtitle: String, action: String? = null, onAction: () -> Unit = {}, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = LinkItTheme.typography.label1NormalBold, color = LinkItTheme.color.semantic.label.strong)
            Text(subtitle, style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.neutral)
        }
        if (action != null) Text(action, Modifier.clickable(role = Role.Button, onClick = onAction), style = LinkItTheme.typography.caption1Medium, color = LinkItTheme.color.semantic.primary.normal)
    }
}

@Composable
internal fun ExploreStatus(
    loading: Boolean = false,
    errorMessage: String? = null,
    emptyMessage: String = "",
    empty: Boolean = false,
    onRetry: () -> Unit = {},
) {
    if (!loading && errorMessage == null && !empty) return
    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            loading -> CircularProgressIndicator(Modifier.size(24.dp))
            errorMessage != null -> {
                Text(errorMessage, style = LinkItTheme.typography.label2Medium)
                TextButton(onClick = onRetry) { Text("다시 시도") }
            }
            empty -> Text(emptyMessage, style = LinkItTheme.typography.label2Medium)
        }
    }
}

@Composable
internal fun ExploreLinkError(message: String?, onDismiss: () -> Unit) {
    if (message == null) return
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(message, Modifier.weight(1f), style = LinkItTheme.typography.caption1Regular)
        TextButton(onClick = onDismiss) { Text("확인") }
    }
}

internal fun DiscoverChannel.youtubeChannelUrl(): String =
    if (channelId.matches(Regex("[A-Za-z0-9_-]+"))) "https://www.youtube.com/channel/$channelId" else ""

internal fun openExploreUrl(url: String, openUri: (String) -> Unit): Boolean {
    if (!url.matches(Regex("https://(?:(?:www|m)\\.)?(?:youtube\\.com|youtu\\.be)/[^\\s]+", RegexOption.IGNORE_CASE))) return false
    return try {
        openUri(url)
        true
    } catch (_: Exception) {
        false
    }
}

internal fun formatExploreDuration(duration: String): String {
    val parts = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matchEntire(duration)?.groupValues ?: return duration
    val hours = parts[1].toLongOrNull() ?: 0
    val minutes = parts[2].toLongOrNull() ?: 0
    val seconds = parts[3].toLongOrNull() ?: 0
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    else "$minutes:${seconds.toString().padStart(2, '0')}"
}
