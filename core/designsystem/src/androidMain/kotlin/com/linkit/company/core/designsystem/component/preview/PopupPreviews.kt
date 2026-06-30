package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.popup.LinkItSnackbar
import com.linkit.company.core.designsystem.component.popup.LinkItToast
import com.linkit.company.core.designsystem.component.popup.ToastVariant
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true, backgroundColor = 0xFF6B7280)
@Composable
private fun LinkItToastPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ToastVariant.entries.forEach { variant ->
                LinkItToast(
                    text = "메시지에 마침표를 찍어요.",
                    variant = variant,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF6B7280)
@Composable
private fun LinkItToastWithLeadingIconPreview() {
    LinkItTheme {
        LinkItToast(
            text = "메시지에 마침표를 찍어요.",
            variant = ToastVariant.Normal,
            leadingIcon = LinkItIcon.Utility.Bell,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF6B7280)
@Composable
private fun LinkItSnackbarPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinkItSnackbar(
                message = "메시지에 마침표를 찍어요.",
            )
            LinkItSnackbar(
                message = "메시지에 마침표를 찍어요.",
                description = "설명은 필요할 때만 써요.",
            )
            LinkItSnackbar(
                message = "메시지에 마침표를 찍어요.",
                leadingIcon = LinkItIcon.Utility.CircleInfo,
                actionLabel = "텍스트",
            )
            LinkItSnackbar(
                message = "메시지에 마침표를 찍어요.",
                description = "설명은 필요할 때만 써요.",
                leadingIcon = LinkItIcon.Utility.CircleInfo,
                actionLabel = "텍스트",
            )
        }
    }
}
