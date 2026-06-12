package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.linkit.company.core.designsystem.legacy.theme.G3
import com.linkit.company.core.designsystem.legacy.theme.G7
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.LinkItTextStyle
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlue
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlueLight
import com.linkit.company.core.designsystem.legacy.theme.Slate50
import com.linkit.company.core.designsystem.legacy.theme.Slate500
import com.linkit.company.core.designsystem.legacy.theme.TagGreenBg
import com.linkit.company.core.designsystem.legacy.theme.TagGreenText
import com.linkit.company.core.designsystem.legacy.theme.TagRedBg
import com.linkit.company.core.designsystem.legacy.theme.TagRedText
import com.linkit.company.core.designsystem.legacy.theme.White

enum class TagColorScheme(
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color? = null,
) {
    Blue(PrimaryBlueLight, PrimaryBlue),
    Green(TagGreenBg, TagGreenText),
    Red(TagRedBg, TagRedText),
    Slate(Slate50, Slate500),
    Keyword(White, G7, G3),
}

data class TagData(
    val text: String,
    val colorScheme: TagColorScheme,
)

@Composable
fun LinkItTag(
    text: String,
    colorScheme: TagColorScheme,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = if (colorScheme == TagColorScheme.Keyword) LinkItTextStyle.keywordTag else LinkItTextStyle.tagText,
) {
    val isKeyword = colorScheme == TagColorScheme.Keyword
    Box(
        modifier = modifier
            .then(
                if (isKeyword) Modifier.height(26.dp) else Modifier,
            )
            .background(
                color = colorScheme.backgroundColor,
                shape = LinkItShape.tag,
            )
            .then(
                if (colorScheme.borderColor != null) {
                    Modifier.border(1.dp, colorScheme.borderColor, LinkItShape.tag)
                } else {
                    Modifier
                },
            )
            .padding(
                horizontal = if (isKeyword) 8.dp else 6.dp,
                vertical = if (isKeyword) 6.dp else 2.dp,
            ),
        contentAlignment = if (isKeyword) Alignment.Center else Alignment.TopStart,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = colorScheme.textColor,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkItTagRow(
    tags: List<TagData>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            LinkItTag(
                text = tag.text,
                colorScheme = tag.colorScheme,
            )
        }
    }
}
