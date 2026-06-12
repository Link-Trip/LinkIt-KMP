package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.legacy.icon.LinkItIcons
import com.linkit.company.core.designsystem.legacy.theme.Black
import com.linkit.company.core.designsystem.legacy.theme.ChipBackground
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlue
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlueBackground
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlueBorder

object FilterChipDefaults {
    fun containerColor(selected: Boolean): Color =
        if (selected) PrimaryBlueBackground else ChipBackground

    fun contentColor(selected: Boolean): Color =
        if (selected) PrimaryBlue else Black

    fun borderModifier(selected: Boolean): Modifier =
        if (selected) Modifier.border(1.dp, PrimaryBlueBorder, LinkItShape.pill)
        else Modifier
}

@Composable
fun LinkItFilterChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val contentColor = FilterChipDefaults.contentColor(selected)

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(LinkItShape.pill)
            .background(FilterChipDefaults.containerColor(selected))
            .then(FilterChipDefaults.borderModifier(selected))
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        leadingIcon?.invoke()
        Text(
            text = label,
            style = LinkItTextStyle.chipLabel,
            color = contentColor,
        )
        Icon(
            imageVector = LinkItIcons.ChevronDown,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor,
        )
    }
}
