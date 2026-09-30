package com.linkit.company.feature.map.mypage.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.navigation.LinkItTopNavigation
import com.linkit.company.core.designsystem.component.navigation.TopNavigationDefaults
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.ui.terms.TermsViewModel
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.feature.map.mypage.MyPageStrings
import dev.zacsweers.metrox.viewmodel.metroViewModel

/** 이용약관 목록(Figma `18287:116697`). 4개 문서를 순서대로 나열한다. */
@Composable
fun TermsListScreen(
    onBack: () -> Unit,
    onOpenDocument: (TermsDocumentType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = metroViewModel(),
) {
    val documents by viewModel.documents.collectAsState()
    TermsListContent(
        documents = documents,
        onBack = onBack,
        onOpenDocument = onOpenDocument,
        modifier = modifier,
    )
}

@Composable
fun TermsListContent(
    documents: List<TermsDocument>,
    onBack: () -> Unit,
    onOpenDocument: (TermsDocumentType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        LinkItTopNavigation(
            title = MyPageStrings.TermsTitle,
            navigationIcon = { TopNavigationDefaults.BackButton(onClick = onBack) },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            documents.forEachIndexed { index, document ->
                if (index > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LinkItTheme.color.semantic.line.solid.neutral),
                    )
                }
                TermsRow(
                    title = document.title,
                    onClick = { onOpenDocument(document.type) },
                    modifier = Modifier.testTag("terms-row-${document.type.name.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun TermsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LinkItIcon.Utility.Document,
                contentDescription = null,
                tint = LinkItTheme.color.semantic.label.normal,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = title,
                style = LinkItTheme.typography.body2NormalMedium,
                color = LinkItTheme.color.semantic.label.normal,
            )
        }
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronRightTight,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.height(20.dp),
        )
    }
}
