package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.theme.CardBackground
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.Slate500
import com.linkit.company.core.designsystem.legacy.theme.Slate700

@Composable
fun LinkItTransportInfo(
    transportName: String,
    duration: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, LinkItShape.input)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = transportName,
                style = LinkItTextStyle.caption1.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Slate700,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = duration,
                style = LinkItTextStyle.small,
                color = Slate500,
            )
        }
    }
}
