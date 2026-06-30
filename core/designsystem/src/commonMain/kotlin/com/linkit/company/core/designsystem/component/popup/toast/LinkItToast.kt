package com.linkit.company.core.designsystem.component.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun LinkItToast(
    text: String,
    modifier: Modifier = Modifier,
    variant: ToastVariant = ToastVariant.Normal,
    leadingIcon: ImageVector? = ToastDefaults.leadingIcon(variant),
    shape: Shape = ToastDefaults.shape,
    containerColor: Color = ToastDefaults.containerColor,
    overlayColor: Color = ToastDefaults.overlayColor,
    contentColor: Color = ToastDefaults.contentColor,
    textStyle: TextStyle = ToastDefaults.textStyle,
) {
    Box(
        modifier = modifier
            .widthIn(max = ToastDefaults.MaxWidth)
            .clip(shape),
    ) {
        Spacer(Modifier.matchParentSize().background(containerColor))
        Spacer(Modifier.matchParentSize().background(overlayColor))

        Row(
            modifier = Modifier
                .heightIn(min = ToastDefaults.MinContentHeight)
                .padding(ToastDefaults.ContentPadding),
            horizontalArrangement = Arrangement.spacedBy(ToastDefaults.IconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                ToastLeadingIcon(
                    icon = leadingIcon,
                    variant = variant,
                    contentColor = contentColor,
                )
            }
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 5.dp),
                style = textStyle,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun ToastLeadingIcon(
    icon: ImageVector,
    variant: ToastVariant,
    contentColor: Color,
) {
    if (variant == ToastVariant.Normal) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ToastDefaults.IconSize),
            tint = contentColor,
        )
    } else {
        Box(
            modifier = Modifier.size(ToastDefaults.IconSize),
            contentAlignment = Alignment.Center,
        ) {
            Spacer(
                modifier = Modifier
                    .size(ToastDefaults.IconSize / 2)
                    .clip(CircleShape)
                    .background(LinkItTheme.color.semantic.static.white),
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ToastDefaults.IconSize),
                tint = ToastDefaults.leadingIconTint(variant),
            )
        }
    }
}
