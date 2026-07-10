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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun StorageAddSavedScreen(
    onBack: () -> Unit = {},
    onCreate: () -> Unit = {},
    viewModel: StorageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    StorageAddSavedContent(
        videoUrl = uiState.videoUrl,
        onVideoUrlChange = { viewModel.onIntent(StorageIntent.UpdateVideoUrl(it)) },
        onBack = onBack,
        onCreate = onCreate,
    )
}

@Composable
fun StorageAddSavedContent(
    videoUrl: String,
    onVideoUrlChange: (String) -> Unit,
    onBack: () -> Unit = {},
    onCreate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LinkItIcon.Arrow.ChevronLeft,
                contentDescription = "뒤로가기",
                tint = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.size(22.dp).clickable(onClick = onBack),
            )
            Text(
                text = "저장 항목 추가하기",
                style = LinkItTheme.typography.headline2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("추천 영상 둘러보기", style = LinkItTheme.typography.label1NormalMedium, color = LinkItTheme.color.semantic.label.alternative)
            Text("더보기 〉", style = LinkItTheme.typography.label1NormalMedium, color = LinkItTheme.color.semantic.label.alternative)
        }
        val videos = listOf(
            VideoFixture("유부남과 함께 오사카 중...", "조회수 113만회", "🏯"),
            VideoFixture("가루들이 안 보이네요...?", "조회수 113만회", "🎎"),
            VideoFixture("가을 도쿄 여행 브이로그", "조회수 98만회", "🍁"),
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(videos) { video -> RecommendedVideoCard(video) }
        }
        Text(
            text = "영상 링크",
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.padding(start = 20.dp, top = 54.dp, bottom = 10.dp),
        )
        VideoUrlInput(videoUrl = videoUrl, onVideoUrlChange = onVideoUrlChange)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (videoUrl.isBlank()) LinkItTheme.color.semantic.interaction.disable
                    else LinkItTheme.color.semantic.inverse.background,
                )
                .clickable(enabled = videoUrl.isNotBlank(), onClick = onCreate),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "일정 생성하기",
                style = LinkItTheme.typography.body1NormalSemibold,
                color = if (videoUrl.isBlank()) LinkItTheme.color.semantic.label.disable else LinkItTheme.color.semantic.inverse.label,
            )
        }
    }
}

@Composable
private fun RecommendedVideoCard(video: VideoFixture) {
    Column(modifier = Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LinkItTheme.color.semantic.accent.background.redOrange.copy(alpha = .25f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(video.emoji, style = LinkItTheme.typography.display1Bold)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(LinkItTheme.color.semantic.material.dimmer)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Control.Copy,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.static.white,
                    modifier = Modifier.size(14.dp),
                )
                Text("링크복사", style = LinkItTheme.typography.caption3Medium, color = LinkItTheme.color.semantic.static.white)
            }
        }
        Text(
            text = video.title,
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.neutral,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = video.views,
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun VideoUrlInput(videoUrl: String, onVideoUrlChange: (String) -> Unit) {
    BasicTextField(
        value = videoUrl,
        onValueChange = onVideoUrlChange,
        textStyle = LinkItTheme.typography.body1NormalRegular.copy(color = LinkItTheme.color.semantic.label.strong),
        cursorBrush = SolidColor(LinkItTheme.color.semantic.primary.normal),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(132.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(12.dp))
                    .padding(14.dp),
            ) {
                if (videoUrl.isBlank()) {
                    Text(
                        text = "URL 를 붙여넣거나 입력해주세요.",
                        style = LinkItTheme.typography.body2NormalRegular,
                        color = LinkItTheme.color.semantic.label.assistive,
                    )
                }
                innerTextField()
            }
        },
    )
}

private data class VideoFixture(
    val title: String,
    val views: String,
    val emoji: String,
)
