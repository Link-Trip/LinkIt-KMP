package com.linkit.company.feature.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_analysis_video
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleAnalysisLoadingScreen(
    videoTitle: String? = null,
    thumbnailUrl: String? = null,
    onBack: () -> Unit = {},
    onReturnHome: () -> Unit = {},
    showNotificationPermissionSheet: Boolean = false,
    onAllowNotifications: () -> Unit = {},
    onDismissNotificationPrompt: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            LinkItTopNavigation(
                title = "일정 생성",
                navigationIcon = {
                    TopNavigationDefaults.BackButton(onClick = onBack)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AttachedVideoCard(
                    videoTitle = videoTitle,
                    thumbnailUrl = thumbnailUrl,
                    modifier = Modifier.padding(top = 20.dp),
                )

                Text(
                    text = "영상 첨부 완료",
                    style = LinkItTheme.typography.caption1Bold,
                    color = LinkItTheme.color.semantic.primary.normal,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LinkItTheme.color.semantic.background.normal.alternative)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                Text(
                    text = "분석을 시작합니다",
                    style = LinkItTheme.typography.heading1Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = "일정 생성이 완료되면 알려드릴게요!",
                    style = LinkItTheme.typography.label1NormalMedium,
                    color = LinkItTheme.color.semantic.label.alternative,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
                LinkItButton(
                    onClick = onAllowNotifications,
                    text = "알림 허용하기",
                    variant = ButtonVariant.Outlined,
                    size = ButtonSize.Medium,
                    leadingIcon = LinkItIcon.Utility.BellFill,
                    modifier = Modifier.padding(top = 20.dp),
                )
                Spacer(Modifier.height(20.dp))
            }

            LinkItActionArea(
                divider = false,
                bottomSafeArea = 0.dp,
            ) {
                LinkItButton(
                    onClick = onReturnHome,
                    text = "메인화면으로 돌아가기",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (showNotificationPermissionSheet) {
            NotificationPermissionSheet(
                onAllow = onAllowNotifications,
                onDismiss = onDismissNotificationPrompt,
            )
        }
    }
}

@Composable
private fun AttachedVideoCard(
    videoTitle: String?,
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
) {
    val fallbackPainter = painterResource(Res.drawable.schedule_analysis_video)
    val displayTitle = videoTitle?.takeIf(String::isNotBlank) ?: DefaultVideoTitle

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = thumbnailUrl?.takeIf(String::isNotBlank),
            contentDescription = displayTitle,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            fallback = fallbackPainter,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp)),
        )
        Text(
            text = displayTitle,
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

private const val DefaultVideoTitle = "[알파메일 독신남 정혁이랑 결혼하실 분? | 독신의 삶 ep.06]"

@Composable
private fun NotificationPermissionSheet(
    onAllow: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.material.dimmer)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .clickable(onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth()) {
                TopNavigationDefaults.IconButton(
                    icon = LinkItIcon.Utility.Close,
                    onClick = onDismiss,
                    contentDescription = "닫기",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 40.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LinkItTheme.color.semantic.background.normal.alternative),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = LinkItIcon.Utility.BellFill,
                            contentDescription = null,
                            tint = LinkItTheme.color.semantic.status.cautionary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Text(
                        text = "완료되면 바로 알려드릴게요!",
                        style = LinkItTheme.typography.heading2Bold,
                        color = LinkItTheme.color.semantic.label.strong,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                    Text(
                        text = "알림을 허용하면 앱을 닫고 있어도\n완료 즉시 알려드려요",
                        style = LinkItTheme.typography.label1NormalMedium,
                        color = LinkItTheme.color.semantic.label.alternative,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            LinkItActionArea(
                divider = false,
                bottomSafeArea = 0.dp,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                LinkItButton(
                    onClick = onAllow,
                    text = "알림 허용하기",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable(onClick = onDismiss),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "나중에 할게요",
                        style = LinkItTheme.typography.label1NormalMedium,
                        color = LinkItTheme.color.semantic.label.alternative,
                    )
                }
            }
        }
    }
}
