package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.category.CategorySize
import com.linkit.company.core.designsystem.component.category.LinkItCategory
import com.linkit.company.core.designsystem.component.category.LinkItCategoryItem
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItCategoryPreview() {
    val categories = listOf("텍스트", "텍스트", "텍스트", "텍스트", "텍스트", "텍스트", "텍스트", "텍스트")
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CategorySize.entries.forEach { size ->
                var selected by remember { mutableIntStateOf(0) }
                LinkItCategory(size = size, horizontalPadding = true) {
                    categories.forEachIndexed { index, text ->
                        LinkItCategoryItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            text = text,
                        )
                    }
                }
            }
        }
    }
}
