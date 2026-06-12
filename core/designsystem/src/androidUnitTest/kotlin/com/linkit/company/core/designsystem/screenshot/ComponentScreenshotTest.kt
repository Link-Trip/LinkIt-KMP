package com.linkit.company.core.designsystem.screenshot

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.component.BottomNavTab
import com.linkit.company.core.designsystem.component.LinkItBottomNavigationBar
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class ComponentScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    // region Button

    @Test
    fun linkItButton_enabled() {
        composeRule.setContent {
            LinkItTheme { LinkItButton(text = "일정 생성하기", onClick = {}) }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItButton_disabled() {
        composeRule.setContent {
            LinkItTheme { LinkItButton(text = "일정 생성하기", onClick = {}, enabled = false) }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region Tag

    @Test
    fun linkItTag_allColorSchemes() {
        composeRule.setContent {
            LinkItTheme {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TagColorScheme.entries.forEach { scheme ->
                        LinkItTag(text = scheme.name, colorScheme = scheme)
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItTagRow() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region TopAppBar

    @Test
    fun linkItTopAppBar_default() {
        composeRule.setContent {
            LinkItTheme { LinkItTopAppBar(title = "도쿄 신주쿠 여행") }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItTopAppBar_withBackButton() {
        composeRule.setContent {
            LinkItTheme {
                LinkItTopAppBar(
                    title = "마이페이지",
                    showBackButton = true,
                    onBackClick = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region TextField

    @Test
    fun linkItTextField_empty() {
        composeRule.setContent {
            LinkItTheme {
                LinkItTextField(
                    value = "",
                    onValueChange = {},
                    label = "영상 링크",
                    placeholder = "URL 를 붙여넣거나 입력해주세요.",
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItTextField_withValue() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItTextField_error() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region FilterChip

    @Test
    fun linkItFilterChip_default() {
        composeRule.setContent {
            LinkItTheme {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkItFilterChip(label = "지역", onClick = {})
                    LinkItFilterChip(label = "여행 테마", onClick = {})
                    LinkItFilterChip(label = "비용", onClick = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItFilterChip_selected() {
        composeRule.setContent {
            LinkItTheme {
                LinkItFilterChip(label = "지역", onClick = {}, selected = true)
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region Tab

    @Test
    fun linkItTab_firstSelected() {
        composeRule.setContent {
            LinkItTheme {
                LinkItTab(
                    tabs = listOf("여행 일정", "영상 요약"),
                    selectedIndex = 0,
                    onTabSelected = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun linkItTab_secondSelected() {
        composeRule.setContent {
            LinkItTheme {
                LinkItTab(
                    tabs = listOf("여행 일정", "영상 요약"),
                    selectedIndex = 1,
                    onTabSelected = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region Divider

    @Test
    fun linkItDivider() {
        composeRule.setContent {
            LinkItTheme { LinkItDivider() }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region DaySelector

    @Test
    fun linkItDaySelector() {
        composeRule.setContent {
            LinkItTheme {
                LinkItDaySelector(
                    totalDays = 7,
                    selectedDay = 1,
                    onDaySelected = {},
                    availableDays = 4,
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region BottomNavigationBar

    @Test
    fun linkItBottomNavigationBar_map() {
        composeRule.setContent {
            LinkItTheme {
                LinkItBottomNavigationBar(
                    selectedTab = BottomNavTab.Map,
                    onTabSelected = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region TransportInfo

    @Test
    fun linkItTransportInfo() {
        composeRule.setContent {
            LinkItTheme {
                LinkItTransportInfo(
                    transportName = "신칸센 고속열차",
                    duration = "30분",
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region ScheduleCard

    @Test
    fun linkItScheduleCard() {
        composeRule.setContent {
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
                    description = "세계 각지의 유물이 모여있는 박물관 입니다.",
                    onDetailClick = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region ScheduleListItem

    @Test
    fun linkItScheduleListItem() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region FolderCard

    @Test
    fun linkItFolderCard() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    // endregion

    // region VideoCard

    @Test
    fun linkItVideoCard() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage()
    }

    // endregion
}
