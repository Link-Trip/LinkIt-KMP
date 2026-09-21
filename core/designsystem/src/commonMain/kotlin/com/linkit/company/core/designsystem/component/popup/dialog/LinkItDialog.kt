package com.linkit.company.core.designsystem.component.popup.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonColors
import com.linkit.company.core.designsystem.component.button.ButtonDefaults
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * 제목, 설명, 확인 액션(과 선택적 보조 액션)으로 구성된 공통 정보 다이얼로그.
 *
 * 포커스를 갖는 모달 [Popup]을 사용하므로 노출 중에는 뒤 화면으로 포인터 입력이 전달되지 않는다.
 *
 * @param secondaryText 보조(Outlined) 액션 문구. null 이면 확인 액션만 표시한다.
 * @param onSecondaryClick 보조 액션 탭 콜백
 * @param confirmColors 확인 버튼 색. 파괴적 동작이면 [DialogDefaults.negativeConfirmColors] 를 넘긴다.
 * @param actionsEnabled false 면 확인·보조·닫기·뒤로가기 입력을 모두 막는다(진행 중 상태).
 */
@Composable
fun LinkItDialog(
    title: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    showCloseButton: Boolean = true,
    secondaryText: String? = null,
    onSecondaryClick: () -> Unit = onDismissRequest,
    confirmColors: ButtonColors = ButtonDefaults.colors(ButtonVariant.Solid, ButtonColor.Primary),
    actionsEnabled: Boolean = true,
) {
    val dismiss: () -> Unit = { if (actionsEnabled) onDismissRequest() }
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = dismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DialogDefaults.dimmerColor)
                .semantics { dialog() },
            contentAlignment = Alignment.Center,
        ) {
            val shape = LinkItTheme.shape.xl
            Box(
                modifier = modifier
                    .padding(horizontal = DialogDefaults.HorizontalMargin)
                    .widthIn(max = DialogDefaults.MaxWidth)
                    .fillMaxWidth()
                    .shadow(DialogDefaults.ShadowElevation, shape)
                    .clip(shape)
                    .background(DialogDefaults.containerColor),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DialogDefaults.ContentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = title,
                            style = LinkItTheme.typography.body1NormalBold,
                            color = DialogDefaults.titleColor,
                            textAlign = TextAlign.Center,
                        )
                        if (description != null) {
                            Text(
                                text = description,
                                modifier = Modifier.padding(top = DialogDefaults.ContentSpacing),
                                style = LinkItTheme.typography.label2Medium,
                                color = DialogDefaults.descriptionColor,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DialogDefaults.ActionPadding),
                        verticalArrangement = Arrangement.spacedBy(DialogDefaults.ActionSpacing),
                    ) {
                        LinkItButton(
                            onClick = onConfirmClick,
                            text = confirmText,
                            modifier = Modifier.fillMaxWidth(),
                            size = ButtonSize.Medium,
                            enabled = actionsEnabled,
                            colors = confirmColors,
                        )
                        if (secondaryText != null) {
                            LinkItButton(
                                onClick = onSecondaryClick,
                                text = secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                variant = ButtonVariant.Outlined,
                                color = ButtonColor.Assistive,
                                size = ButtonSize.Medium,
                                enabled = actionsEnabled,
                                colors = DialogDefaults.secondaryColors(),
                            )
                        }
                    }
                }

                if (showCloseButton) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(DialogDefaults.CloseButtonInset)
                            .size(DialogDefaults.CloseButtonSize)
                            .clickable(
                                role = Role.Button,
                                enabled = actionsEnabled,
                                onClick = onDismissRequest,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = LinkItIcon.Utility.Close,
                            contentDescription = "닫기",
                            tint = DialogDefaults.closeIconColor,
                            modifier = Modifier.size(DialogDefaults.CloseIconSize),
                        )
                    }
                }
            }
        }
    }
}
