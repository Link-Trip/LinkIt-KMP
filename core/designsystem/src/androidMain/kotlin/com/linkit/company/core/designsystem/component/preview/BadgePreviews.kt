package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.badge.BadgeColor
import com.linkit.company.core.designsystem.component.badge.BadgeSize
import com.linkit.company.core.designsystem.component.badge.BadgeVariant
import com.linkit.company.core.designsystem.component.badge.LinkItBadge
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItBadgePreview() {
    LinkItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BadgeColor.entries.forEach { color ->
                BadgeVariant.entries.forEach { variant ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BadgeSize.entries.forEach { size ->
                            LinkItBadge(
                                text = "텍스트",
                                variant = variant,
                                size = size,
                                color = color,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItBadgeAccentCustomizePreview() {
    LinkItTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgeVariant.entries.forEach { variant ->
                LinkItBadge(
                    text = "텍스트",
                    variant = variant,
                    size = BadgeSize.Medium,
                    color = BadgeColor.Accent,
                    accentBackgroundColor = LinkItTheme.color.semantic.accent.foreground.green,
                    accentContentColor = LinkItTheme.color.semantic.accent.foreground.green,
                )
            }
        }
    }
}
