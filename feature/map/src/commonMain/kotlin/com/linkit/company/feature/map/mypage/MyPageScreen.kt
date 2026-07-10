package com.linkit.company.feature.map.mypage

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun MyPageScreen(
    onBack: () -> Unit = {},
    onExternalItem: () -> Unit = {},
    viewModel: MyPageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    MyPageContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onExternalItem = onExternalItem,
    )
}

@Composable
fun MyPageContent(
    uiState: MyPageUiState,
    onIntent: (MyPageIntent) -> Unit,
    onBack: () -> Unit = {},
    onExternalItem: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Arrow.ChevronLeft,
                    contentDescription = "뒤로가기",
                    tint = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.size(23.dp).clickable(onClick = onBack),
                )
                Text(
                    text = "마이페이지",
                    style = LinkItTheme.typography.headline2Semibold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(LinkItTheme.color.semantic.line.normal.alternative))
            SectionLabel("지도 설정", top = 24)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MapTypeCard(
                    label = "기본",
                    selected = uiState.selectedMapType == MyPageMapType.DEFAULT,
                    satellite = false,
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(MyPageIntent.SelectMapType(MyPageMapType.DEFAULT)) },
                )
                MapTypeCard(
                    label = "위성",
                    selected = uiState.selectedMapType == MyPageMapType.SATELLITE,
                    satellite = true,
                    modifier = Modifier.weight(1f),
                    onClick = { onIntent(MyPageIntent.SelectMapType(MyPageMapType.SATELLITE)) },
                )
            }
            SectionLabel("도움말 & 지원", top = 10)
            SettingRow("피드백 보내기", onExternalItem)
            SettingRow("버그 신고하기", onExternalItem)
            SettingRow("이용약관", onExternalItem)
            SectionLabel("앱 설정", top = 32)
            SettingRow("앱 초기화") { onIntent(MyPageIntent.ShowResetDialog) }
        }

        if (uiState.showResetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LinkItTheme.color.semantic.material.dimmer),
            )
            ResetDialog(
                onDismiss = { onIntent(MyPageIntent.DismissResetDialog) },
                onReset = { onIntent(MyPageIntent.DismissResetDialog) },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, top: Int) {
    Text(
        text = text,
        style = LinkItTheme.typography.body1NormalSemibold,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier.padding(start = 20.dp, top = top.dp),
    )
}

@Composable
private fun MapTypeCard(
    label: String,
    selected: Boolean,
    satellite: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        val mapBackground = if (satellite) LinkItTheme.color.semantic.label.neutral else LinkItTheme.color.semantic.background.normal.alternative
        val roadColor = if (satellite) LinkItTheme.color.semantic.line.solid.strong else LinkItTheme.color.semantic.static.white
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(mapBackground)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) LinkItTheme.color.semantic.label.strong else androidx.compose.ui.graphics.Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                ),
        ) {
            drawLine(roadColor, Offset(-20f, 25f), Offset(size.width + 20f, size.height - 6f), strokeWidth = 7f)
            drawLine(roadColor, Offset(size.width * .2f, size.height + 8f), Offset(size.width * .65f, -8f), strokeWidth = 6f)
            drawLine(roadColor, Offset(-12f, size.height * .7f), Offset(size.width + 12f, size.height * .35f), strokeWidth = 4f)
        }
        Text(
            text = label,
            style = LinkItTheme.typography.body2NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SettingRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Communication.Message,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = LinkItTheme.typography.body1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronRight,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.label.assistive,
            modifier = Modifier.size(18.dp),
        )
    }
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(1.dp).background(LinkItTheme.color.semantic.line.normal.alternative))
}

@Composable
private fun ResetDialog(
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 38.dp)
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = LinkItIcon.Utility.Close,
                contentDescription = "닫기",
                tint = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.size(22.dp).clickable(onClick = onDismiss),
            )
        }
        Text(
            text = "정말 앱을 초기화 하시겠어요?",
            style = LinkItTheme.typography.heading2Semibold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            text = "앱을 초기화하면 다시 복구할 수 없어요",
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.padding(top = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LinkItTheme.color.semantic.primary.normal)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Text("이전으로 돌아가기", style = LinkItTheme.typography.body1NormalMedium, color = LinkItTheme.color.semantic.static.white)
        }
        Text(
            text = "초기화",
            style = LinkItTheme.typography.body1NormalMedium,
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.padding(top = 18.dp, bottom = 4.dp).clickable(onClick = onReset),
        )
    }
}
