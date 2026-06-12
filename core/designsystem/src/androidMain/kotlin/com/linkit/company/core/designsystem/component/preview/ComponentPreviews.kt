package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.legacy.component.LinkItButton
import com.linkit.company.core.designsystem.legacy.component.LinkItDaySelector
import com.linkit.company.core.designsystem.legacy.component.LinkItDivider
import com.linkit.company.core.designsystem.legacy.component.LinkItFilterChip
import com.linkit.company.core.designsystem.legacy.component.LinkItFolderCard
import com.linkit.company.core.designsystem.legacy.component.LinkItScheduleCard
import com.linkit.company.core.designsystem.legacy.component.LinkItScheduleListItem
import com.linkit.company.core.designsystem.legacy.component.LinkItTab
import com.linkit.company.core.designsystem.legacy.component.LinkItTag
import com.linkit.company.core.designsystem.legacy.component.LinkItTagRow
import com.linkit.company.core.designsystem.legacy.component.LinkItTextField
import com.linkit.company.core.designsystem.legacy.component.LinkItTopAppBar
import com.linkit.company.core.designsystem.legacy.component.LinkItTransportInfo
import com.linkit.company.core.designsystem.legacy.component.LinkItVideoCard
import com.linkit.company.core.designsystem.legacy.component.TagColorScheme
import com.linkit.company.core.designsystem.legacy.component.TagData
import com.linkit.company.core.designsystem.legacy.theme.G2
import com.linkit.company.core.designsystem.theme.LinkItTheme

// region Divider

@Preview(showBackground = true)
@Composable
private fun LinkItDividerPreview() {
    LinkItTheme {
        LinkItDivider()
    }
}

// endregion

// region Tag

@Preview(showBackground = true)
@Composable
private fun LinkItTagColorSchemesPreview() {
    LinkItTheme {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TagColorScheme.entries.forEach { scheme ->
                LinkItTag(text = scheme.name, colorScheme = scheme)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTagRowPreview() {
    LinkItTheme {
        LinkItTagRow(
            tags = listOf(
                TagData("박물관", TagColorScheme.Blue),
                TagData("조용한", TagColorScheme.Green),
                TagData("SNS 핫플", TagColorScheme.Red),
            ),
        )
    }
}

// endregion

// region Button

@Preview(showBackground = true)
@Composable
private fun LinkItButtonEnabledPreview() {
    LinkItTheme {
        LinkItButton(text = "일정 생성하기", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItButtonDisabledPreview() {
    LinkItTheme {
        LinkItButton(text = "일정 생성하기", onClick = {}, enabled = false)
    }
}

// endregion

// region TopAppBar

@Preview(showBackground = true)
@Composable
private fun LinkItTopAppBarPreview() {
    LinkItTheme {
        LinkItTopAppBar(title = "도쿄 신주쿠 여행")
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTopAppBarWithBackPreview() {
    LinkItTheme {
        LinkItTopAppBar(
            title = "마이페이지",
            showBackButton = true,
            onBackClick = {},
        )
    }
}

// endregion

// region TextField

@Preview(showBackground = true)
@Composable
private fun LinkItTextFieldEmptyPreview() {
    LinkItTheme {
        LinkItTextField(
            value = "",
            onValueChange = {},
            label = "영상 링크",
            placeholder = "URL 를 붙여넣거나 입력해주세요.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTextFieldWithValuePreview() {
    LinkItTheme {
        LinkItTextField(
            value = "https://youtube.com/watch?v=example",
            onValueChange = {},
            label = "영상 링크",
            placeholder = "URL 를 붙여넣거나 입력해주세요.",
            maxLength = 200,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTextFieldErrorPreview() {
    LinkItTheme {
        LinkItTextField(
            value = "잘못된 링크",
            onValueChange = {},
            label = "영상 링크",
            placeholder = "URL 를 붙여넣거나 입력해주세요.",
            isError = true,
            errorMessage = "올바른 URL을 입력해주세요",
        )
    }
}

// endregion

// region FilterChip

@Preview(showBackground = true)
@Composable
private fun LinkItFilterChipDefaultPreview() {
    LinkItTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinkItFilterChip(label = "지역", onClick = {})
            LinkItFilterChip(label = "여행 테마", onClick = {})
            LinkItFilterChip(label = "비용", onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItFilterChipSelectedPreview() {
    LinkItTheme {
        LinkItFilterChip(label = "지역", onClick = {}, selected = true)
    }
}

// endregion

// region Tab

@Preview(showBackground = true)
@Composable
private fun LinkItTabFirstSelectedPreview() {
    LinkItTheme {
        LinkItTab(
            tabs = listOf("여행 일정", "영상 요약"),
            selectedIndex = 0,
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItTabSecondSelectedPreview() {
    LinkItTheme {
        LinkItTab(
            tabs = listOf("여행 일정", "영상 요약"),
            selectedIndex = 1,
            onTabSelected = {},
        )
    }
}

// endregion

// region DaySelector

@Preview(showBackground = true)
@Composable
private fun LinkItDaySelectorPreview() {
    LinkItTheme {
        LinkItDaySelector(
            totalDays = 7,
            selectedDay = 1,
            onDaySelected = {},
            availableDays = 4,
        )
    }
}

// endregion

// region TransportInfo

@Preview(showBackground = true)
@Composable
private fun LinkItTransportInfoPreview() {
    LinkItTheme {
        LinkItTransportInfo(
            transportName = "신칸센 고속열차",
            duration = "30분",
        )
    }
}

// endregion

// region ScheduleCard

@Preview(showBackground = true)
@Composable
private fun LinkItScheduleCardPreview() {
    LinkItTheme {
        LinkItScheduleCard(
            image = {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(G2),
                )
            },
            tags = listOf(
                TagData("박물관", TagColorScheme.Blue),
                TagData("조용한", TagColorScheme.Green),
            ),
            title = "루브르 박물관",
            address = "뤼 드 리볼리, 75001 파리",
            description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
            onDetailClick = {},
        )
    }
}

// endregion

// region ScheduleListItem

@Preview(showBackground = true)
@Composable
private fun LinkItScheduleListItemPreview() {
    LinkItTheme {
        LinkItScheduleListItem(
            thumbnail = {
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 100.dp)
                        .background(G2),
                )
            },
            tags = listOf(
                TagData("# 먹방여행", TagColorScheme.Keyword),
                TagData("# SNS 핫플레이스", TagColorScheme.Keyword),
            ),
            title = "도쿄 신주쿠 여행",
            duration = "3박4일",
            cost = "82만원",
            summary = "AI 한줄 요약된 여행지 정보",
            onClick = {},
            onMoreClick = {},
        )
    }
}

// endregion

// region FolderCard

@Preview(showBackground = true)
@Composable
private fun LinkItFolderCardPreview() {
    LinkItTheme {
        LinkItFolderCard(
            thumbnail = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .background(G2),
                )
            },
            name = "교통",
            itemCount = 6,
            onClick = {},
            onMoreClick = {},
            modifier = Modifier.width(160.dp),
        )
    }
}

// endregion

// region VideoCard

@Preview(showBackground = true)
@Composable
private fun LinkItVideoCardPreview() {
    LinkItTheme {
        LinkItVideoCard(
            thumbnail = {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(90.dp)
                        .background(G2),
                )
            },
            title = "유부남과 함께 오사카 좋은 놀이공원 가보기",
            viewCount = "조회수 113만회",
            onClick = {},
            onCopyLinkClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LinkItVideoCardNoCopyPreview() {
    LinkItTheme {
        LinkItVideoCard(
            thumbnail = {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(90.dp)
                        .background(G2),
                )
            },
            title = "유부남과 함께 오사카 좋은 놀이공원 가보기",
            viewCount = "조회수 113만회",
            onClick = {},
        )
    }
}

// endregion
