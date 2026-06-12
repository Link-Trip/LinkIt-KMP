package com.linkit.company.core.designsystem.legacy.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.legacy.theme.Black
import com.linkit.company.core.designsystem.legacy.theme.BorderDefault
import com.linkit.company.core.designsystem.legacy.theme.LinkItFontFamily
import com.linkit.company.core.designsystem.legacy.theme.LinkItShape
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlue
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlueBackground
import com.linkit.company.core.designsystem.legacy.theme.PrimaryBlueBorder
import com.linkit.company.core.designsystem.legacy.theme.Slate500
import com.linkit.company.core.designsystem.legacy.theme.White
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

object DaySelectorDefaults {
    fun backgroundColor(isSelected: Boolean): Color =
        if (isSelected) PrimaryBlueBackground else White

    fun borderColor(isSelected: Boolean): Color =
        if (isSelected) PrimaryBlueBorder else BorderDefault

    fun dayLabelColor(isSelected: Boolean): Color =
        if (isSelected) PrimaryBlueBorder else Slate500

    fun dayNumberColor(isSelected: Boolean): Color =
        if (isSelected) PrimaryBlue else Black

    const val DisabledAlpha = 0.3f

    val dayLabelStyle = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.5.sp,
    )

    val dayNumberStyle = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 21.6.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun LinkItDaySelector(
    totalDays: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableDays: Int = totalDays,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        itemsIndexed(List(totalDays) { it + 1 }) { _, day ->
            val isSelected = day == selectedDay
            val isDisabled = day > availableDays

            Column(
                modifier = Modifier
                    .size(52.dp)
                    .alpha(if (isDisabled) DaySelectorDefaults.DisabledAlpha else 1f)
                    .clip(LinkItShape.card)
                    .background(DaySelectorDefaults.backgroundColor(isSelected))
                    .border(1.dp, DaySelectorDefaults.borderColor(isSelected), LinkItShape.card)
                    .clickable(enabled = !isDisabled) { onDaySelected(day) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "${day}일차",
                    style = DaySelectorDefaults.dayLabelStyle,
                    color = DaySelectorDefaults.dayLabelColor(isSelected),
                )
                Text(
                    text = "$day",
                    style = DaySelectorDefaults.dayNumberStyle,
                    color = DaySelectorDefaults.dayNumberColor(isSelected),
                )
            }
        }
    }
}
