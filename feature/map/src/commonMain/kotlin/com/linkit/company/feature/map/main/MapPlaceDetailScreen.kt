package com.linkit.company.feature.map.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItNavKey
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_place_photo
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapPlaceDetailScreen(
    route: LinkItNavKey.PlaceDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LinkItIcon.Arrow.ChevronLeft,
                contentDescription = "뒤로가기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.size(24.dp).clickable(onClick = onBack),
            )
            Text(
                text = "장소 상세",
                style = LinkItTheme.typography.heading2Bold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Image(
                painter = painterResource(Res.drawable.map_place_photo),
                contentDescription = route.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(240.dp),
            )

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text(
                    text = route.categoryLabel,
                    style = LinkItTheme.typography.caption1Medium,
                    color = LinkItTheme.color.semantic.accent.foreground.blue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LinkItTheme.color.semantic.accent.foreground.blue.copy(alpha = .08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Text(
                    text = route.name,
                    style = LinkItTheme.typography.headline2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = route.address.ifBlank { "주소 정보 없음" },
                    style = LinkItTheme.typography.body2NormalRegular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 6.dp),
                )

                PlaceDetailSection(
                    title = "장소 정보",
                    body = route.description.ifBlank { "등록된 장소 설명이 없어요." },
                    modifier = Modifier.padding(top = 28.dp),
                )
                if (route.tips.isNotBlank()) {
                    PlaceDetailSection(
                        title = "여행 팁",
                        body = route.tips,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }

                Text(
                    text = "위치",
                    style = LinkItTheme.typography.heading2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    StaticMapBackground(
                        mapType = MapType.DEFAULT,
                        modifier = Modifier.fillMaxSize(),
                        markers = listOf(
                            MapMarkerUiModel(
                                id = route.placeId,
                                lat = route.latitude,
                                lng = route.longitude,
                                label = route.name,
                                type = MapMarkerType.PLACE,
                                selected = true,
                            ),
                        ),
                        initialCamera = MapCameraUiModel(
                            center = MapCoordinateUiModel(route.latitude, route.longitude),
                            zoom = 14f,
                        ),
                        contentPaddingBottom = 0.dp,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun PlaceDetailSection(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = LinkItTheme.typography.heading2Bold,
            color = LinkItTheme.color.semantic.label.strong,
        )
        Text(
            text = body,
            style = LinkItTheme.typography.body2NormalRegular,
            color = LinkItTheme.color.semantic.label.neutral,
        )
    }
}
