package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.icon.LinkItIcons
import com.linkit.company.core.designsystem.legacy.theme.CardBackground
import com.linkit.company.core.designsystem.legacy.theme.CardBorder
import com.linkit.company.core.designsystem.legacy.theme.HeadingBlack
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlue
import com.linkit.company.core.designsystem.legacy.theme.Slate500
import com.linkit.company.core.designsystem.legacy.theme.Slate600

@Composable
fun LinkItScheduleCard(
    image: @Composable () -> Unit,
    tags: List<TagData>,
    title: String,
    address: String,
    description: String,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, LinkItShape.scheduleCard)
            .background(CardBackground, LinkItShape.scheduleCard)
            .border(1.dp, CardBorder, LinkItShape.scheduleCard)
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: image + tags + title + address
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(LinkItShape.card),
            ) {
                image()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (tags.isNotEmpty()) {
                    LinkItTagRow(tags = tags)
                }
                Text(
                    text = title,
                    style = LinkItTextStyle.heading,
                    color = HeadingBlack,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = address,
                        style = LinkItTextStyle.small,
                        color = Slate500,
                    )
                }
            }
        }

        // Description
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = LinkItTextStyle.body,
                color = Slate600,
            )
        }

        // Detail button
        Row(
            modifier = Modifier.clickable(onClick = onDetailClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "자세히 보기",
                style = LinkItTextStyle.caption1.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = PrimaryBlue,
            )
            Icon(
                imageVector = LinkItIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = PrimaryBlue,
            )
        }
    }
}
