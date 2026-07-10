package com.linkit.company.feature.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.explore.generated.resources.Res
import linkitcompany.feature.explore.generated.resources.explore_creator_avatar
import linkitcompany.feature.explore.generated.resources.explore_fuji
import linkitcompany.feature.explore.generated.resources.explore_kyoto
import org.jetbrains.compose.resources.painterResource

private val CreatorScreenBackground = Color(0xFFF4F4F5)
private val CreatorScreenText = Color(0xFF1F2127)
private val CreatorScreenSubText = Color(0xFF7B8696)
private val CreatorScreenBlue = Color(0xFF388AFE)

@Composable
fun RecommendedCreatorsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RecommendedCreatorsContent(onBack = onBack, modifier = modifier)
}

@Composable
fun RecommendedCreatorsContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreatorScreenBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = LinkItIcon.Arrow.ChevronLeft,
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onBack),
                tint = CreatorScreenText,
            )
            Text(
                text = "추천 여행 유튜버",
                style = LinkItTheme.typography.headline2Bold,
                color = CreatorScreenText,
            )
        }

        CreatorSelector()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(4) { index ->
                ExploreVideoCard(
                    image = painterResource(if (index % 2 == 0) Res.drawable.explore_fuji else Res.drawable.explore_kyoto),
                    imageHeight = 168.dp,
                    showAnalysisCount = false,
                )
            }
        }
    }
}

@Composable
private fun CreatorSelector() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(List(5) { "홍길동" }) { index, name ->
            Column(
                modifier = Modifier
                    .width(88.dp)
                    .height(118.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (index == 0) Color(0xFFF7FBFF) else Color.White)
                    .then(
                        if (index == 0) {
                            Modifier.border(1.dp, CreatorScreenBlue, RoundedCornerShape(12.dp))
                        } else {
                            Modifier.border(1.dp, Color(0xFFE6E7EB), RoundedCornerShape(12.dp))
                        },
                    )
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.explore_creator_avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = name,
                    style = LinkItTheme.typography.label2Bold,
                    color = CreatorScreenText,
                )
                Text(
                    text = "80만 구독자",
                    style = LinkItTheme.typography.caption3Regular,
                    color = CreatorScreenSubText,
                )
            }
        }
    }
}
