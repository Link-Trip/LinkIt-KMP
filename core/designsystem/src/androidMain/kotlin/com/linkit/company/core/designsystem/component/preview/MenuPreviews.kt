package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.menu.LinkItMenu
import com.linkit.company.core.designsystem.component.menu.LinkItMenuItem
import com.linkit.company.core.designsystem.component.menu.MenuItemPadding
import com.linkit.company.core.designsystem.component.menu.MenuItemVariant
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItMenuPreview() {
    LinkItTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Normal 변형
            LinkItMenu {
                LinkItMenuItem(text = "텍스트", onClick = {})
                LinkItMenuItem(text = "선택됨", onClick = {}, selected = true)
                LinkItMenuItem(text = "텍스트", onClick = {})
                LinkItMenuItem(text = "비활성", onClick = {}, enabled = false)
                LinkItMenuItem(text = "텍스트", onClick = {})
            }

            // Checkbox 변형
            LinkItMenu {
                LinkItMenuItem(text = "텍스트", onClick = {}, variant = MenuItemVariant.Checkbox)
                LinkItMenuItem(text = "선택됨", onClick = {}, variant = MenuItemVariant.Checkbox, selected = true)
                LinkItMenuItem(text = "텍스트", onClick = {}, variant = MenuItemVariant.Checkbox)
                LinkItMenuItem(
                    text = "비활성",
                    onClick = {},
                    variant = MenuItemVariant.Checkbox,
                    enabled = false,
                )
                LinkItMenuItem(text = "텍스트", onClick = {}, variant = MenuItemVariant.Checkbox)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItMenuCompactPreview() {
    LinkItTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LinkItMenu {
                repeat(3) {
                    LinkItMenuItem(
                        text = "텍스트",
                        onClick = {},
                        padding = MenuItemPadding.Compact,
                    )
                }
            }
        }
    }
}
