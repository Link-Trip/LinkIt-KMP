package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.theme.Black
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.OverlayDark
import com.linkit.company.core.designsystem.legacy.theme.TextSecondary
import com.linkit.company.core.designsystem.legacy.theme.White

@Composable
fun LinkItVideoCard(
    thumbnail: @Composable () -> Unit,
    title: String,
    viewCount: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyLinkClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(90.dp)
                .clip(LinkItShape.input),
        ) {
            thumbnail()
            if (onCopyLinkClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                        .background(
                            color = OverlayDark,
                            shape = LinkItShape.tag,
                        )
                        .clickable(onClick = onCopyLinkClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "링크복사",
                        style = LinkItTextStyle.caption1.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = White,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = LinkItTextStyle.sectionTitle.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = viewCount,
                style = LinkItTextStyle.small,
                color = TextSecondary,
            )
        }
    }
}
