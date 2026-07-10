package com.linkit.company.feature.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun StorageSearchScreen(
    onSearch: (String) -> Unit = {},
    viewModel: StorageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    StorageSearchContent(
        query = uiState.query,
        onQueryChange = { viewModel.onIntent(StorageIntent.UpdateQuery(it)) },
        onSearch = onSearch,
    )
}

@Composable
fun StorageSearchContent(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Text(
            text = "보관함",
            style = LinkItTheme.typography.headline2Bold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 20.dp),
        )
        ActiveSearchField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onSearch(query) },
        )
        SearchSuggestions(
            suggestions = listOf(
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
                "작성영역자동완성영역",
            ),
            onSelected = {
                onQueryChange(it)
                onSearch(it)
            },
        )
    }
}

@Composable
private fun ActiveSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = LinkItTheme.typography.body1NormalRegular.copy(
            color = LinkItTheme.color.semantic.label.strong,
        ),
        cursorBrush = SolidColor(LinkItTheme.color.semantic.primary.normal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 2.dp,
                        color = LinkItTheme.color.semantic.primary.light,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "검색어를 입력해주세요",
                            style = LinkItTheme.typography.body1NormalRegular,
                            color = LinkItTheme.color.semantic.label.assistive,
                        )
                    }
                    innerTextField()
                }
                Icon(
                    imageVector = LinkItIcon.Utility.Search,
                    contentDescription = "검색",
                    tint = LinkItTheme.color.semantic.label.assistive,
                    modifier = Modifier.size(23.dp).clickable(onClick = onSearch),
                )
            }
        },
    )
}

@Composable
private fun SearchSuggestions(
    suggestions: List<String>,
    onSelected: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = LinkItTheme.color.semantic.line.normal.alternative,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        items(suggestions) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .clickable { onSelected(suggestion) }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LinkItIcon.Location.Location,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.label.assistive,
                    modifier = Modifier.size(21.dp),
                )
                Text(
                    text = suggestion,
                    style = LinkItTheme.typography.body1NormalSemibold,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
    }
}
