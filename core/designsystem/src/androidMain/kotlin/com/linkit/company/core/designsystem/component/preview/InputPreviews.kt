package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.textarea.LinkItTextArea
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true)
@Composable
private fun LinkItTextAreaPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Inactive (placeholder)
            LinkItTextArea(
                value = "",
                onValueChange = {},
                label = "주제",
                placeholder = "메시지를 입력해 주세요.",
                supportingText = "메시지에 마침표를 찍어요.",
            )
            // Active (값 입력)
            LinkItTextArea(
                value = "입력된 내용",
                onValueChange = {},
                label = "주제",
                placeholder = "메시지를 입력해 주세요.",
                supportingText = "메시지에 마침표를 찍어요.",
            )
            // Negative (오류)
            LinkItTextArea(
                value = "입력된 내용",
                onValueChange = {},
                label = "주제",
                placeholder = "메시지를 입력해 주세요.",
                supportingText = "메시지에 마침표를 찍어요.",
                isError = true,
            )
            // Disabled
            LinkItTextArea(
                value = "입력된 내용",
                onValueChange = {},
                label = "주제",
                placeholder = "메시지를 입력해 주세요.",
                supportingText = "메시지에 마침표를 찍어요.",
                enabled = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTextAreaInteractivePreview() {
    LinkItTheme {
        var text by remember { mutableStateOf("") }
        LinkItTextArea(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = "주제",
            placeholder = "메시지를 입력해 주세요.",
            supportingText = "메시지에 마침표를 찍어요.",
        )
    }
}
