package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.icon.LinkItIcons
import com.linkit.company.core.designsystem.legacy.theme.Black
import com.linkit.company.core.designsystem.legacy.theme.G6
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle

@Composable
fun LinkItFolderCard(
    thumbnail: @Composable () -> Unit,
    name: String,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(LinkItShape.input),
        ) {
            thumbnail()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = LinkItTextStyle.sectionTitle,
                    color = Black,
                )
                Text(
                    text = "${itemCount}개",
                    style = LinkItTextStyle.small,
                    color = G6,
                )
            }
            if (onMoreClick != null) {
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = LinkItIcons.MoreVert,
                        contentDescription = "더보기",
                        modifier = Modifier.size(20.dp),
                        tint = G6,
                    )
                }
            }
        }
    }
}
