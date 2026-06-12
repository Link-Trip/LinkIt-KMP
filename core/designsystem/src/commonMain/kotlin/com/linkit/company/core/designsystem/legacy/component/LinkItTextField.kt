package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.legacy.theme.Black
import com.linkit.company.core.designsystem.legacy.theme.G1
import com.linkit.company.core.designsystem.legacy.theme.G4
import com.linkit.company.core.designsystem.legacy.theme.G6
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.TagRedText

object LinkItTextFieldDefaults {
    fun borderColor(isError: Boolean): Color =
        if (isError) TagRedText else G1

    fun borderModifier(isError: Boolean): Modifier =
        if (isError) Modifier.border(1.dp, borderColor(isError), LinkItShape.input)
        else Modifier
}

@Composable
fun LinkItTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    maxLength: Int = Int.MAX_VALUE,
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = false,
    minHeight: Int = 120,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = LinkItTextStyle.caption1,
                color = G6,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LinkItTextStyle.caption1.copy(color = Black),
            singleLine = singleLine,
            cursorBrush = SolidColor(Black),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(LinkItTextFieldDefaults.borderModifier(isError))
                        .background(G1, LinkItShape.input)
                        .height(minHeight.dp)
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = LinkItTextStyle.caption1,
                            color = G4,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (maxLength != Int.MAX_VALUE || (isError && errorMessage.isNotEmpty())) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isError && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        style = LinkItTextStyle.small,
                        color = TagRedText,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (maxLength != Int.MAX_VALUE) {
                    Text(
                        text = "${value.length}/$maxLength",
                        style = LinkItTextStyle.small,
                        color = G4,
                    )
                }
            }
        }
    }
}
