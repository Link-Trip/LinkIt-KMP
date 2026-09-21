package com.linkit.company.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun RecommendedCreatorsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    RecommendedCreatorsContent(
        onBack = onBack,
        modifier = modifier,
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onOpenUrl = { url ->
            if (!openExploreUrl(url, uriHandler::openUri)) viewModel.onIntent(ExploreIntent.LinkOpenFailed)
        },
    )
}

@Composable
fun RecommendedCreatorsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: ExploreUiState = ExploreUiState(),
    onIntent: (ExploreIntent) -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(LinkItTheme.color.semantic.background.normal.alternative)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(LinkItTheme.color.semantic.background.normal.normal).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(LinkItIcon.Arrow.ChevronLeft, "뒤로가기") }
            Text("추천 여행 유튜버", style = LinkItTheme.typography.headline2Bold)
        }
        ExploreLinkError(uiState.linkErrorMessage) { onIntent(ExploreIntent.DismissLinkError) }
        ExploreStatus(
            loading = uiState.isCatalogLoading,
            errorMessage = uiState.catalogErrorMessage,
            empty = uiState.channels.isEmpty(),
            emptyMessage = "추천 크리에이터가 아직 없어요.",
            onRetry = { onIntent(ExploreIntent.RetryCatalog) },
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.channels, key = { it.channelId }) { channel ->
                val selected = uiState.selectedChannelId == channel.channelId
                Column(
                    Modifier.width(112.dp).clip(RoundedCornerShape(12.dp))
                        .background(LinkItTheme.color.semantic.background.normal.normal)
                        .border(1.dp, if (selected) LinkItTheme.color.semantic.primary.normal else LinkItTheme.color.semantic.line.solid.neutral, RoundedCornerShape(12.dp))
                        .clickable(role = Role.Tab) { onIntent(ExploreIntent.SelectChannel(channel.channelId)) }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AsyncImage(channel.thumbnailUrl, null, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    Text(channel.title, style = LinkItTheme.typography.label2Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("구독자 ${channel.subscriberCount}명", style = LinkItTheme.typography.caption3Regular, maxLines = 1)
                }
            }
        }
        val channel = uiState.selectedChannel
        if (channel != null) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("구독자 ${channel.subscriberCount}명", style = LinkItTheme.typography.caption1Medium)
                TextButton(onClick = { onOpenUrl(channel.youtubeChannelUrl()) }) { Text("채널 열기") }
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (channel.recentVideos.isEmpty()) item { ExploreStatus(empty = true, emptyMessage = "아직 등록된 영상이 없어요.") }
                items(channel.recentVideos.distinctBy { it.videoId }, key = { it.videoId }) { video ->
                    ExploreVideoCard(
                        title = video.title,
                        thumbnailUrl = video.thumbnailUrl,
                        channelTitle = channel.title,
                        metadata = video.publishedAt.substringBefore('T'),
                        onClick = { onOpenUrl(video.videoUrl) },
                    )
                }
            }
        }
    }
}
