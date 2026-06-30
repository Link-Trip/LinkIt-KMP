package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.button.LinkItIconButton
import com.linkit.company.core.designsystem.component.chip.ChipSize
import com.linkit.company.core.designsystem.component.chip.ChipVariant
import com.linkit.company.core.designsystem.component.chip.LinkItChip
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItButtonStylePreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            listOf(true, false).forEach { enabled ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ButtonVariant.entries.forEach { variant ->
                        ButtonColor.entries.forEach { color ->
                            LinkItButton(
                                onClick = {},
                                text = "텍스트",
                                variant = variant,
                                color = color,
                                enabled = enabled,
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
private fun LinkItButtonSizePreview() {
    LinkItTheme {
        Row(
            modifier = Modifier
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            ButtonSize.entries.forEach { size ->
                LinkItButton(
                    onClick = {},
                    text = "텍스트",
                    size = size,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItButtonWithIconPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinkItButton(
                onClick = {},
                text = "텍스트",
                leadingIcon = LinkItIcon.Utility.Search,
            )
            LinkItButton(
                onClick = {},
                text = "텍스트",
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Primary,
                trailingIcon = LinkItIcon.Arrow.ChevronRight,
            )
            LinkItButton(
                onClick = {},
                text = "텍스트",
                color = ButtonColor.Assistive,
                shadow = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItIconButtonPreview() {
    LinkItTheme {
        Row(
            modifier = Modifier
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButtonVariant.entries.forEach { variant ->
                ButtonSize.entries.forEach { size ->
                    LinkItIconButton(
                        onClick = {},
                        icon = LinkItIcon.Utility.Search,
                        contentDescription = "검색",
                        variant = variant,
                        size = size,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun LinkItActionAreaPreview() {
    LinkItTheme {
        LinkItActionArea {
            LinkItButton(
                onClick = {},
                text = "메인 액션",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun LinkItActionAreaWithExtraPreview() {
    LinkItTheme {
        LinkItActionArea(
            heading = "헤딩",
            description = "필요한 경우 설명을 덧붙입니다.",
            headingIcon = LinkItIcon.Utility.CircleInfo,
        ) {
            LinkItButton(
                onClick = {},
                text = "메인 액션",
                modifier = Modifier.fillMaxWidth(),
            )
            LinkItButton(
                onClick = {},
                text = "보조 액션",
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Assistive,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItChipPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChipVariant.entries.forEach { variant ->
                listOf(false, true).forEach { active ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        ChipSize.entries.forEach { size ->
                            LinkItChip(
                                onClick = {},
                                text = "텍스트",
                                variant = variant,
                                size = size,
                                active = active,
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
private fun LinkItChipStatePreview() {
    LinkItTheme {
        Row(
            modifier = Modifier
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var active by remember { mutableStateOf(true) }
            LinkItChip(
                onClick = { active = !active },
                text = "토글",
                active = active,
                leadingIcon = LinkItIcon.Utility.Search,
            )
            LinkItChip(
                onClick = {},
                text = "비활성",
                variant = ChipVariant.Outlined,
                enabled = false,
            )
        }
    }
}
