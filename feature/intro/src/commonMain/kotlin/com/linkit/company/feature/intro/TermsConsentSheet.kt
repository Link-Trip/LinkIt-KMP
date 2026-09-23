package com.linkit.company.feature.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.action.LinkItActionArea
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.checkbox.LinkItCheckbox
import com.linkit.company.core.designsystem.component.sheet.LinkItModalBottomSheet
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.terms.TermsDocumentType

/**
 * 약관 동의 바텀시트(Figma `18580:37864`). 끌어 내리기·바깥 탭은 [IntroIntent.DismissTermsSheet] 로 닫힌다.
 * [TermsSheetState.dismissible] 이 false(개정 약관 재동의)면 숨김 전이를 막아 동의 전에는 닫히지 않는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConsentSheet(
    state: TermsSheetState,
    onIntent: (IntroIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // `rememberModalBottomSheetState` 는 confirmValueChange 람다를 remember 키로 쓴다.
    // 람다가 [state] 전체를 캡처하면 체크 토글마다 새 람다 → 새 SheetState(Hidden) 가 만들어져 시트가 다시 올라오므로
    // 안정적인 State 객체만 캡처해 람다 인스턴스를 유지한다.
    val dismissible by rememberUpdatedState(state.dismissible)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value -> dismissible || value != SheetValue.Hidden },
    )
    LinkItModalBottomSheet(
        onDismissRequest = { onIntent(IntroIntent.DismissTermsSheet) },
        modifier = modifier,
        sheetState = sheetState,
    ) {
        TermsConsentSheetContent(state = state, onIntent = onIntent)
    }
}

/** 시트 본문. `ModalBottomSheet` 는 별도 창에 그려지므로 골든 테스트는 이 함수를 직접 호출한다. */
@Composable
fun TermsConsentSheetContent(
    state: TermsSheetState,
    onIntent: (IntroIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LinkItTheme.color.semantic.background.normal.normal)
            .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = OnboardingStrings.TermsTitle,
                style = LinkItTheme.typography.headline1Bold,
                color = LinkItTheme.color.semantic.label.normal,
            )
            Text(
                text = OnboardingStrings.TermsSubtitle,
                style = LinkItTheme.typography.label1NormalMedium,
                // Figma 18580:37869 는 semantic 토큰 대신 #70737C(CoolNeutral50) 원색을 쓴다
                color = PaletteTokens.CoolNeutral50,
            )
        }
        Spacer(Modifier.height(16.dp))

        AllTermsRow(
            checked = state.allAgreed,
            enabled = !state.isSubmitting,
            onToggle = { onIntent(IntroIntent.ToggleAllTerms) },
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        TermsItemRow(
            title = OnboardingStrings.TermsService,
            checked = state.serviceAgreed,
            enabled = !state.isSubmitting,
            onToggle = { onIntent(IntroIntent.ToggleServiceTerms) },
            onDetail = { onIntent(IntroIntent.OpenTermsDetail(TermsDocumentType.SERVICE)) },
            checkboxTag = TermsConsentTestTags.ServiceCheckbox,
            detailTag = TermsConsentTestTags.ServiceDetail,
        )
        TermsItemRow(
            title = OnboardingStrings.TermsPrivacy,
            checked = state.privacyAgreed,
            enabled = !state.isSubmitting,
            onToggle = { onIntent(IntroIntent.TogglePrivacyTerms) },
            onDetail = { onIntent(IntroIntent.OpenTermsDetail(TermsDocumentType.PRIVACY)) },
            checkboxTag = TermsConsentTestTags.PrivacyCheckbox,
            detailTag = TermsConsentTestTags.PrivacyDetail,
        )

        LinkItActionArea(
            divider = false,
            bottomSafeArea = 0.dp,
        ) {
            LinkItButton(
                onClick = { onIntent(IntroIntent.AgreeAndStart) },
                text = OnboardingStrings.TermsStart,
                enabled = state.canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TermsConsentTestTags.Start),
            )
        }
    }
}

/** `전체 동의` 행: 채움 컨테이너(radius 12, fill.normal) + 체크박스 + body2 Bold */
@Composable
private fun AllTermsRow(
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LinkItTheme.shape.xl)
            .background(LinkItTheme.color.semantic.fill.normal)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                onValueChange = { onToggle() },
            )
            .padding(12.dp)
            .testTag(TermsConsentTestTags.AllCheckbox),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinkItCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text(
            text = OnboardingStrings.TermsAll,
            style = LinkItTheme.typography.body2NormalBold,
            color = LinkItTheme.color.semantic.label.neutral,
        )
    }
}

/** 개별 약관 행(높이 44, 좌우 24): 체크박스 + `[필수]` + 제목 + `상세보기` */
@Composable
private fun TermsItemRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onDetail: () -> Unit,
    checkboxTag: String,
    detailTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    interactionSource = null,
                    indication = null,
                    onValueChange = { onToggle() },
                )
                .testTag(checkboxTag),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinkItCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = OnboardingStrings.TermsRequired,
                    style = LinkItTheme.typography.label2Medium,
                    color = LinkItTheme.color.semantic.primary.normal,
                )
                Text(
                    text = title,
                    style = LinkItTheme.typography.label1NormalMedium,
                    color = LinkItTheme.color.semantic.label.normal,
                )
            }
        }
        Text(
            text = OnboardingStrings.TermsDetail,
            style = LinkItTheme.typography.label2Medium.copy(textDecoration = TextDecoration.Underline),
            color = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier
                .clickable(
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                    onClick = onDetail,
                )
                .testTag(detailTag),
        )
    }
}

object TermsConsentTestTags {
    const val AllCheckbox = "terms-consent-all"
    const val ServiceCheckbox = "terms-consent-service"
    const val PrivacyCheckbox = "terms-consent-privacy"
    const val ServiceDetail = "terms-consent-service-detail"
    const val PrivacyDetail = "terms-consent-privacy-detail"
    const val Start = "terms-consent-start"
}
