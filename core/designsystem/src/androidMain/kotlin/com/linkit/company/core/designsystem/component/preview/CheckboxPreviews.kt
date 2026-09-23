package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.checkbox.LinkItCheckbox
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItCheckboxPreview() {
    LinkItTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinkItCheckbox(checked = false, onCheckedChange = {})
            LinkItCheckbox(checked = true, onCheckedChange = {})
            LinkItCheckbox(checked = false, onCheckedChange = {}, enabled = false)
            LinkItCheckbox(checked = true, onCheckedChange = null)
        }
    }
}
