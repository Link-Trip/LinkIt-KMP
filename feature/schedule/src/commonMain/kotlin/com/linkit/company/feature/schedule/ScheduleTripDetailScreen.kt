package com.linkit.company.feature.schedule

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.schedule.generated.resources.Res
import linkitcompany.feature.schedule.generated.resources.schedule_map_background
import linkitcompany.feature.schedule.generated.resources.schedule_map_calendar
import linkitcompany.feature.schedule.generated.resources.schedule_map_money
import linkitcompany.feature.schedule.generated.resources.schedule_map_schedule_thumbnail
import linkitcompany.feature.schedule.generated.resources.schedule_map_selected_area
import linkitcompany.feature.schedule.generated.resources.schedule_map_thumb_1
import linkitcompany.feature.schedule.generated.resources.schedule_map_thumb_2
import linkitcompany.feature.schedule.generated.resources.schedule_map_thumb_3
import linkitcompany.feature.schedule.generated.resources.schedule_map_thumb_4
import linkitcompany.feature.schedule.generated.resources.schedule_map_thumb_5
import linkitcompany.feature.schedule.generated.resources.schedule_place_photo
import linkitcompany.feature.schedule.generated.resources.schedule_summary_video
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleTripDetailScreen(
    onBack: () -> Unit = {},
    viewModel: ScheduleViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ScheduleTripDetailContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

@Composable
fun ScheduleTripDetailContent(
    uiState: ScheduleUiState,
    onIntent: (ScheduleIntent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (uiState.showTripMapPreview && uiState.tripDetailTab == TripDetailTab.ITINERARY) {
        ScheduleSelectedMapPreview(modifier = modifier, onBack = onBack)
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        TripDetailTopBar(
            title = "도쿄 신주쿠 여행",
            onBack = onBack,
            showMore = uiState.tripDetailTab == TripDetailTab.SUMMARY,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            TripTabs(
                selected = uiState.tripDetailTab,
                onSelect = { onIntent(ScheduleIntent.SelectTripDetailTab(it)) },
            )
            when (uiState.tripDetailTab) {
                TripDetailTab.ITINERARY -> ItineraryContent()
                TripDetailTab.SUMMARY -> SummaryContent()
            }
        }
    }
}

@Composable
private fun TripDetailTopBar(title: String, onBack: () -> Unit, showMore: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "뒤로가기",
            tint = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.size(22.dp).clickable(onClick = onBack),
        )
        Text(
            text = title,
            style = LinkItTheme.typography.body1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
        )
        if (showMore) {
            Text(
                text = "•••",
                style = LinkItTheme.typography.body1NormalSemibold,
                color = LinkItTheme.color.semantic.label.neutral,
            )
        }
    }
}

@Composable
private fun ScheduleSelectedMapPreview(modifier: Modifier, onBack: () -> Unit) {
    Box(modifier.fillMaxSize().background(LinkItTheme.color.semantic.background.normal.normal)) {
        Image(
            painter = painterResource(Res.drawable.schedule_map_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(Res.drawable.schedule_map_selected_area),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset(43.dp, 94.dp).width(273.dp).height(325.dp),
        )
        ScheduleMapPhotoMarker(Res.drawable.schedule_map_thumb_4, Modifier.offset(218.dp, 99.dp))
        ScheduleMapPhotoMarker(Res.drawable.schedule_map_thumb_2, Modifier.offset(93.dp, 147.dp))
        ScheduleMapPhotoMarker(Res.drawable.schedule_map_thumb_5, Modifier.offset(64.dp, 299.dp))
        ScheduleMapPhotoMarker(Res.drawable.schedule_map_thumb_1, Modifier.offset(200.dp, 243.dp))
        ScheduleMapPhotoMarker(Res.drawable.schedule_map_thumb_3, Modifier.offset(258.dp, 376.dp))
        Text(
            text = "도쿄 신주쿠 여행",
            style = LinkItTheme.typography.label2Medium,
            color = LinkItTheme.color.semantic.label.strong,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .offset(102.dp, 284.dp)
                .width(118.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens.PingoMapSelectionBackground)
                .border(1.dp, com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens.PingoMapSelectionBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
        ScheduleMapSheet(
            onBack = onBack,
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = 30.dp),
        )
    }
}

@Composable
private fun ScheduleMapPhotoMarker(resource: DrawableResource, modifier: Modifier) {
    val pointerColor = com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens.PingoNeutral600
    Column(modifier = modifier.width(38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LinkItTheme.color.semantic.background.elevated.normal)
                .border(2.dp, pointerColor, RoundedCornerShape(4.dp))
                .padding(2.dp),
        )
        Canvas(Modifier.width(11.dp).height(10.dp)) {
            val pointer = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(pointer, pointerColor)
        }
    }
}

@Composable
private fun ScheduleMapSheet(onBack: () -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxWidth().height(246.dp)) {
        Box(Modifier.fillMaxWidth().height(57.dp), contentAlignment = Alignment.TopCenter) {
            Text(
                text = "일본, 도쿄",
                style = LinkItTheme.typography.caption1Bold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(LinkItTheme.color.semantic.static.white.copy(alpha = .6f))
                    .border(1.dp, LinkItTheme.color.semantic.static.white, RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        Column(
            Modifier.fillMaxWidth().height(189.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(LinkItTheme.color.semantic.background.elevated.normal),
        ) {
            Box(Modifier.fillMaxWidth().height(15.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(48.dp).height(3.dp).clip(CircleShape).background(LinkItTheme.color.semantic.label.alternative.copy(alpha = .2f)))
            }
            Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(LinkItIcon.Arrow.ChevronLeft, "뒤로가기", modifier = Modifier.size(24.dp).clickable(onClick = onBack))
                Text("도쿄 신주쿠 여행", style = LinkItTheme.typography.heading2Bold, modifier = Modifier.padding(start = 4.dp).weight(1f))
                Icon(LinkItIcon.Utility.MoreHorizontal, "더보기", modifier = Modifier.size(24.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Image(
                    painter = painterResource(Res.drawable.schedule_map_schedule_thumbnail),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(60.dp).height(75.dp).clip(RoundedCornerShape(8.dp)),
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ScheduleMapTag("맛집 중심")
                        ScheduleMapTag("쇼핑 중심")
                    }
                    Text("도쿄 신주쿠 여행", style = LinkItTheme.typography.body2NormalBold, modifier = Modifier.padding(top = 6.dp))
                    Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScheduleMapMeta(Res.drawable.schedule_map_calendar, "3박4일")
                        ScheduleMapMeta(Res.drawable.schedule_map_money, "82만원")
                    }
                    Text("AI 한줄 요약된 여행지 정보", style = LinkItTheme.typography.caption1Bold, color = LinkItTheme.color.semantic.label.neutral)
                }
            }
        }
    }
}

@Composable
private fun ScheduleMapTag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption2Bold,
        color = LinkItTheme.color.semantic.label.alternative,
        modifier = Modifier.border(1.dp, LinkItTheme.color.semantic.line.normal.neutral, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 5.dp),
    )
}

@Composable
private fun ScheduleMapMeta(resource: DrawableResource, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(resource), null, Modifier.size(16.dp))
        Text(text, style = LinkItTheme.typography.caption1Bold, color = LinkItTheme.color.semantic.label.alternative)
    }
}

@Composable
private fun TripTabs(selected: TripDetailTab, onSelect: (TripDetailTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 24.dp),
    ) {
        TripTab("여행 일정", selected == TripDetailTab.ITINERARY, Modifier.weight(1f)) {
            onSelect(TripDetailTab.ITINERARY)
        }
        TripTab("영상 요약", selected == TripDetailTab.SUMMARY, Modifier.weight(1f)) {
            onSelect(TripDetailTab.SUMMARY)
        }
    }
}

@Composable
private fun TripTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.body2NormalSemibold,
            color = if (selected) LinkItTheme.color.semantic.primary.normal else LinkItTheme.color.semantic.label.assistive,
        )
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(LinkItTheme.color.semantic.primary.normal),
            )
        }
    }
}

@Composable
private fun ItineraryContent() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            text = "3박4일 일정",
            style = LinkItTheme.typography.body2NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier
                .padding(top = 0.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(LinkItTheme.color.semantic.fill.normal)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        DayChips()
        Text(
            text = "1일차",
            style = LinkItTheme.typography.body1NormalSemibold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 18.dp),
        )
        ItineraryTimelineNode()
        ItineraryPlaceCard(
            title = "루브르 박물관",
            categories = "음식점",
            description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
        )
        TransferCard()
        ItineraryTimelineNode()
        ItineraryPlaceCard(
            title = "루브르 박물관",
            categories = "음식점",
            description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
        )
        ItineraryTimelineNode()
        ItineraryPlaceCard(
            title = "루브르 박물관",
            categories = "음식점",
            description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun DayChips() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (1..7).forEach { day ->
            val enabled = day <= 4
            val selected = day == 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) LinkItTheme.color.semantic.primary.light.copy(alpha = .18f) else LinkItTheme.color.semantic.background.normal.normal)
                    .border(
                        width = if (selected) 1.dp else 1.dp,
                        color = if (selected) LinkItTheme.color.semantic.primary.normal else LinkItTheme.color.semantic.line.normal.alternative,
                        shape = RoundedCornerShape(12.dp),
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "${day}일차",
                    style = LinkItTheme.typography.caption2Medium,
                    color = when {
                        selected -> LinkItTheme.color.semantic.primary.normal
                        enabled -> LinkItTheme.color.semantic.label.alternative
                        else -> LinkItTheme.color.semantic.label.disable
                    },
                )
                Text(
                    text = "$day",
                    style = LinkItTheme.typography.body1NormalMedium,
                    color = when {
                        selected -> LinkItTheme.color.semantic.primary.normal
                        enabled -> LinkItTheme.color.semantic.label.strong
                        else -> LinkItTheme.color.semantic.label.disable
                    },
                )
            }
        }
    }
}

@Composable
private fun ItineraryTimelineNode() {
    Box(
        modifier = Modifier.padding(start = 1.dp, top = 8.dp).size(18.dp).clip(CircleShape).background(LinkItTheme.color.semantic.primary.normal),
        contentAlignment = Alignment.Center,
    ) {
        Text("1", style = LinkItTheme.typography.caption2Medium, color = LinkItTheme.color.semantic.static.white)
    }
}

@Composable
private fun ItineraryPlaceCard(
    title: String,
    categories: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .padding(start = 28.dp)
            .offset(y = (-14).dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(14.dp),
    ) {
        Row {
            Image(
                painter = painterResource(Res.drawable.schedule_place_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 72.dp, height = 64.dp).clip(RoundedCornerShape(8.dp)),
            )
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    text = categories,
                    style = LinkItTheme.typography.caption1Medium,
                    color = LinkItTheme.color.semantic.primary.strong,
                )
                Text(
                    text = title,
                    style = LinkItTheme.typography.heading2Semibold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    text = "⌾ 뤼 드 리볼리, 75001 파리",
                    style = LinkItTheme.typography.caption1Regular,
                    color = LinkItTheme.color.semantic.label.assistive,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = description,
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "자세히 보기 ›",
            style = LinkItTheme.typography.body2NormalMedium,
            color = LinkItTheme.color.semantic.primary.normal,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun TransferCard() {
    Row(
        modifier = Modifier
            .padding(start = 28.dp, bottom = 12.dp)
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🚆", style = LinkItTheme.typography.body1NormalMedium)
        Text(
            text = "신칸센 고속열차",
            style = LinkItTheme.typography.caption1Medium,
            color = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        )
        Text("◷ 30분", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
    }
}

@Composable
private fun SummaryContent() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        VideoHero()
        Text(
            text = "오사카 여행 전 필수시청! 오사카 핵심 요약 완벽 가이드 [맛집, 숙소추천, 가볼만한곳, 주유패스, 공항에서시내가는법, 일본여행]",
            style = LinkItTheme.typography.body1NormalMedium,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 16.dp),
        )
        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Brush.linearGradient(listOf(LinkItTheme.color.semantic.primary.normal, LinkItTheme.color.semantic.secondary.normal))))
            Text("Sarah Kim  ·  2.3M views", style = LinkItTheme.typography.body2NormalRegular, color = LinkItTheme.color.semantic.label.alternative, modifier = Modifier.padding(start = 8.dp))
        }
        AiSummaryBox()
        SectionTitle("관련 태그")
        TagRows()
        SectionTitle("여행 정보")
        TravelInfoRow("▣", "여행 기간", "3박 4일")
        TravelInfoRow("▤", "비용", "150 ~ 200 만원")
        TravelInfoRow("●", "여행 스타일", "음식 & 문화 • 도시 탐험")
        SectionTitle("영상 타임라인")
        Timeline()
        Spacer(Modifier.height(48.dp))
    }
    SummaryBottomNavigation()
}

@Composable
private fun VideoHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(10.dp)),
    ) {
        Image(
            painter = painterResource(Res.drawable.schedule_summary_video),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.align(Alignment.Center).size(44.dp).clip(CircleShape).background(LinkItTheme.color.semantic.material.dimmer),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", color = LinkItTheme.color.semantic.static.white)
        }
        Text(
            text = "12:45",
            style = LinkItTheme.typography.caption1Medium,
            color = LinkItTheme.color.semantic.static.white,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LinkItTheme.color.semantic.material.dimmer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun AiSummaryBox() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LinkItTheme.color.semantic.primary.light.copy(alpha = .14f))
            .border(1.dp, LinkItTheme.color.semantic.primary.light, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Text("✣  AI 요약정보", style = LinkItTheme.typography.body2NormalSemibold, color = LinkItTheme.color.semantic.label.strong)
        Text(
            text = "신주쿠에서 최고의 주말을 경험하세요. 이 영상은 숨겨진 라멘 명소, 고층에서 바라보는 도시 전경, 그리고 추억의 골목(오모이데 요코초)의 활기찬 야간 생활에 초점을 맞춥니다.",
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.neutral,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.body2NormalSemibold,
        color = LinkItTheme.color.semantic.label.strong,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
    )
}

@Composable
private fun TagRows() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag("#도쿄여행")
            Tag("#먹방브이로그")
            Tag("#시부야밤거리")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag("#일본겨울")
            Tag("#혼자여행")
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption1Medium,
        color = LinkItTheme.color.semantic.primary.strong,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LinkItTheme.color.semantic.primary.light.copy(alpha = .14f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun TravelInfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LinkItTheme.color.semantic.background.normal.alternative)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(LinkItTheme.color.semantic.fill.normal), contentAlignment = Alignment.Center) {
            Text(icon, color = LinkItTheme.color.semantic.label.neutral)
        }
        Column(Modifier.padding(start = 12.dp)) {
            Text(label, style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.assistive)
            Text(value, style = LinkItTheme.typography.body1NormalMedium, color = LinkItTheme.color.semantic.label.strong)
        }
    }
}

@Composable
private fun Timeline() {
    val items = List(7) { "0:00" to "인트로 & 시부야 도착" }
    items.forEach { (time, description) ->
        Row(Modifier.height(62.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(LinkItTheme.color.semantic.primary.normal))
                Box(Modifier.width(1.dp).weight(1f).background(LinkItTheme.color.semantic.line.solid.normal))
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(time, style = LinkItTheme.typography.body2NormalMedium, color = LinkItTheme.color.semantic.primary.normal)
                Text(description, style = LinkItTheme.typography.body2NormalRegular, color = LinkItTheme.color.semantic.label.neutral)
            }
        }
    }
}

@Composable
private fun SummaryBottomNavigation() {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).border(1.dp, LinkItTheme.color.semantic.line.normal.alternative).padding(horizontal = 46.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomItem(LinkItIcon.Control.HomeFill, "지도", true)
        BottomItem(LinkItIcon.Utility.Folder, "보관함", false)
        BottomItem(LinkItIcon.Location.Compass, "탐색", false)
    }
}

@Composable
private fun BottomItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) LinkItTheme.color.semantic.label.strong else LinkItTheme.color.semantic.label.assistive,
            modifier = Modifier.size(20.dp),
        )
        Text(label, style = LinkItTheme.typography.caption2Medium, color = if (selected) LinkItTheme.color.semantic.label.strong else LinkItTheme.color.semantic.label.assistive)
    }
}
