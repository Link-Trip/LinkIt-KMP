package com.linkit.company.core.designsystem.component.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 정보를 주제나 그룹으로 나누어 구분하고 단일 선택으로 접근하게 하는 카테고리 묶음.
 *
 * Figma "Pingo v3.0.3 - Navigation / Category" 를 구현한 것으로, [LinkItCategoryItem] 들을
 * 가로로 배치한다. 항목이 화면을 넘치면 [scrollable] 로 가로 스크롤한다.
 *
 * 묶음에서 내려준 [size] 는 [LinkItCategoryScope] 를 통해 각 아이템에 공통 적용된다.
 *
 * @param size 아이템 크기 규격. 기본값은 [CategorySize.Medium].
 * @param horizontalPadding `true` 면 묶음 좌우에 [CategoryDefaults.HorizontalPadding] 여백을 둔다.
 * @param scrollable `true` 면 항목을 가로 스크롤한다.
 * @param trailing 묶음 오른쪽 끝에 고정 배치하는 슬롯(예: 더보기 아이콘 버튼).
 * @param content [LinkItCategoryScope] 에서 [LinkItCategoryItem] 들을 배치한다.
 */
@Composable
fun LinkItCategory(
    modifier: Modifier = Modifier,
    size: CategorySize = CategorySize.Medium,
    horizontalPadding: Boolean = false,
    scrollable: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable LinkItCategoryScope.() -> Unit,
) {
    val scope = remember(size) { LinkItCategoryScope(size) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .then(if (scrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                .then(
                    if (horizontalPadding) {
                        Modifier.padding(horizontal = CategoryDefaults.HorizontalPadding)
                    } else {
                        Modifier
                    },
                ),
            horizontalArrangement = Arrangement.spacedBy(CategoryDefaults.ItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            content = { scope.content() },
        )
        if (trailing != null) {
            Spacer(Modifier.width(CategoryDefaults.TrailingSpacing))
            trailing()
        }
    }
}

/**
 * [LinkItCategory] 가 아이템에 공통 값을 전달하기 위한 스코프. 현재는 [size] 를 내려준다.
 */
@Immutable
class LinkItCategoryScope internal constructor(
    val size: CategorySize,
)

/**
 * [LinkItCategory] 안에 배치하는 카테고리 아이템 하나.
 *
 * 선택 시 면을 채운 강조 스타일, 비선택 시 외곽선 스타일로 표시한다. 크기는 묶음의 [LinkItCategoryScope.size] 를 따른다.
 *
 * @param selected 선택 여부.
 * @param onClick 선택(클릭) 콜백.
 * @param text 아이템 라벨.
 * @param enabled `false` 면 비활성 색을 쓰고 클릭을 막는다.
 * @param leadingIcon 텍스트 앞 아이콘.
 * @param trailingIcon 텍스트 뒤 아이콘.
 */
@Composable
fun LinkItCategoryScope.LinkItCategoryItem(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: CategoryColors = CategoryDefaults.colors(),
    shape: Shape = CategoryDefaults.shape(size),
    contentPadding: PaddingValues = CategoryDefaults.contentPadding(size),
    textStyle: TextStyle = CategoryDefaults.textStyle(size, selected),
) {
    val contentColor = colors.contentColor(selected, enabled)
    val borderColor = colors.borderColor(selected)
    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.containerColor(selected))
            .then(if (borderColor.isSpecified) Modifier.border(CategoryDefaults.BorderWidth, borderColor, shape) else Modifier)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(CategoryDefaults.contentSpacing(size), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            CategoryItemIcon(leadingIcon, contentColor)
        }
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingIcon != null) {
            CategoryItemIcon(trailingIcon, contentColor)
        }
    }
}

@Composable
private fun CategoryItemIcon(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(CategoryDefaults.IconSize),
        tint = tint,
    )
}
