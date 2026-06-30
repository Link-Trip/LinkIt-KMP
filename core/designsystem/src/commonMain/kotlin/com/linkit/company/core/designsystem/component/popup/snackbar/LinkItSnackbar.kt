package com.linkit.company.core.designsystem.component.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

@Composable
fun LinkItSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
    shape: Shape = SnackbarDefaults.shape,
    containerColor: Color = SnackbarDefaults.containerColor,
    overlayColor: Color = SnackbarDefaults.overlayColor,
    contentColor: Color = SnackbarDefaults.contentColor,
    headingTextStyle: TextStyle = SnackbarDefaults.headingTextStyle,
    descriptionTextStyle: TextStyle = SnackbarDefaults.descriptionTextStyle,
) {
    Box(
        modifier = modifier
            .widthIn(max = SnackbarDefaults.MaxWidth)
            .clip(shape),
    ) {
        Spacer(Modifier.matchParentSize().background(containerColor))
        Spacer(Modifier.matchParentSize().background(overlayColor))

        Row(
            modifier = Modifier
                .heightIn(min = SnackbarDefaults.MinContentHeight)
                .padding(SnackbarDefaults.ContentPadding),
            horizontalArrangement = Arrangement.spacedBy(SnackbarDefaults.ActionSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SnackbarDefaults.ContentSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(SnackbarDefaults.IconSize),
                        tint = contentColor,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = message,
                        style = headingTextStyle,
                        color = contentColor,
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = descriptionTextStyle,
                            color = contentColor,
                        )
                    }
                }
            }

            if (actionLabel != null) {
                SnackbarAction(
                    label = actionLabel,
                    onClick = onActionClick,
                )
            }
        }
    }
}

@Composable
private fun SnackbarAction(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(SnackbarDefaults.shape)
            .clickable(
                interactionSource = null,
                indication = InteractionDefaults.indication(color = SnackbarDefaults.actionColor),
                onClick = onClick,
            )
            .padding(SnackbarDefaults.ActionPadding),
        style = SnackbarDefaults.actionTextStyle,
        color = SnackbarDefaults.actionColor,
    )
}
