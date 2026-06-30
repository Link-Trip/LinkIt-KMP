package com.linkit.company.core.designsystem.component.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.LinkItButton

/**
 * 화면 하단에 핵심 행동을 모아 배치하는 액션 영역.
 *
 * Figma "Pingo v3.0.3 - Action / Action Area" 를 구현한 것으로, 하단 배치를 기본으로 한다.
 * [heading] 또는 [description] 이 있으면 액션 위에 설명(Extra) 영역과 상단 구분선이 함께 표시된다.
 *
 * 액션 버튼은 [content] 슬롯에 [LinkItButton] 을 `Modifier.fillMaxWidth()` 로 배치해 사용한다.
 *
 * @param heading 설명 영역 제목. `null` 이면 설명 영역을 그리지 않는다.
 * @param description 설명 영역 본문.
 * @param headingIcon 헤딩 앞 아이콘.
 * @param divider 상단 구분선 표시 여부. 기본값은 설명 영역이 있을 때 `true`.
 * @param bottomSafeArea 하단 안전 영역 높이. 0dp 로 두면 여백 없이 그린다.
 * @param content 액션 버튼들을 세로로 배치하는 슬롯.
 */
@Composable
fun LinkItActionArea(
    modifier: Modifier = Modifier,
    heading: String? = null,
    description: String? = null,
    headingIcon: ImageVector? = null,
    divider: Boolean = heading != null || description != null,
    containerColor: Color = ActionAreaDefaults.containerColor,
    actionSpacing: Dp = ActionAreaDefaults.ActionSpacing,
    bottomSafeArea: Dp = ActionAreaDefaults.BottomSafeAreaHeight,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        if (divider) {
            HorizontalDivider(color = ActionAreaDefaults.dividerColor)
        }

        if (heading != null || description != null) {
            ActionAreaDescription(
                heading = heading,
                description = description,
                headingIcon = headingIcon,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ActionAreaDefaults.ActionsPadding),
            verticalArrangement = Arrangement.spacedBy(actionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )

        if (bottomSafeArea > 0.dp) {
            Spacer(Modifier.fillMaxWidth().height(bottomSafeArea))
        }
    }
}

@Composable
private fun ActionAreaDescription(
    heading: String?,
    description: String?,
    headingIcon: ImageVector?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ActionAreaDefaults.DescriptionPadding),
        verticalArrangement = Arrangement.spacedBy(ActionAreaDefaults.HeadingSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (heading != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    ActionAreaDefaults.HeadingIconSpacing,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (headingIcon != null) {
                    Icon(
                        imageVector = headingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(ActionAreaDefaults.HeadingIconSize),
                        tint = ActionAreaDefaults.headingColor,
                    )
                }
                Text(
                    text = heading,
                    style = ActionAreaDefaults.headingTextStyle,
                    color = ActionAreaDefaults.headingColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (description != null) {
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                style = ActionAreaDefaults.descriptionTextStyle,
                color = ActionAreaDefaults.descriptionColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
