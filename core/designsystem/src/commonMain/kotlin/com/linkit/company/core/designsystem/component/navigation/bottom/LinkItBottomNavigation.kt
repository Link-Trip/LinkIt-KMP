package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 화면 하단에 위치하는 탭 바.
 *
 * Figma "Pingo v3.0.3 - Navigation / Bottom Navigation" 을 구현한 것으로, 상단 구분선과 흰색 배경,
 * 하단 시스템 영역([windowInsets]) 여백을 포함한다. 탭은 [LinkItBottomNavigationItem] 으로 채운다.
 *
 * @param platform 간격·여백·구분선 두께를 결정하는 플랫폼. 기본값은 [BottomNavigationPlatform.Android].
 * @param windowInsets 하단 홈 인디케이터 등 시스템 영역 여백. 기본값은 navigationBars.
 * @param content [RowScope] 에서 [LinkItBottomNavigationItem] 들을 가로로 배치한다.
 */
@Composable
fun LinkItBottomNavigation(
    modifier: Modifier = Modifier,
    platform: BottomNavigationPlatform = BottomNavigationPlatform.Android,
    containerColor: Color = BottomNavigationDefaults.containerColor,
    windowInsets: WindowInsets = BottomNavigationDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        HorizontalDivider(
            thickness = BottomNavigationDefaults.dividerThickness(platform),
            color = BottomNavigationDefaults.dividerColor,
        )
        CompositionLocalProvider(LocalBottomNavigationPlatform provides platform) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/**
 * [LinkItBottomNavigation] 안에 배치하는 탭 하나.
 *
 * [selected] 상태에 따라 [selectedIcon]/[icon] 과 색을 전환한다. 보통 채워진(Fill) 아이콘을
 * [selectedIcon] 으로, 외곽선 아이콘을 [icon] 으로 전달한다.
 *
 * @param selectedIcon 선택 시 표시할 아이콘. 미지정 시 [icon] 을 그대로 사용한다.
 */
@Composable
fun RowScope.LinkItBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector = icon,
    enabled: Boolean = true,
    selectedColor: Color = BottomNavigationDefaults.selectedColor,
    unselectedColor: Color = BottomNavigationDefaults.unselectedColor,
) {
    val platform = LocalBottomNavigationPlatform.current
    val contentColor = if (selected) selectedColor else unselectedColor
    Column(
        modifier = modifier
            .weight(1f)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
            )
            .padding(vertical = BottomNavigationDefaults.itemVerticalPadding(platform)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            BottomNavigationDefaults.itemContentGap(platform),
            Alignment.CenterVertically,
        ),
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = null,
            modifier = Modifier.size(BottomNavigationDefaults.IconSize),
            tint = contentColor,
        )
        Text(
            text = label,
            style = BottomNavigationDefaults.labelStyle,
            color = contentColor,
            maxLines = 1,
        )
    }
}
