package com.linkit.company.feature.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun StorageScreen(
    onSearch: () -> Unit = {},
    onOpenFolder: () -> Unit = {},
    onAddSavedItem: () -> Unit = {},
    onAddManualItem: () -> Unit = {},
    viewModel: StorageViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    StorageContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onSearch = onSearch,
        onOpenFolder = onOpenFolder,
        onAddSavedItem = onAddSavedItem,
        onAddManualItem = onAddManualItem,
    )
}

@Composable
fun StorageContent(
    uiState: StorageUiState,
    onIntent: (StorageIntent) -> Unit,
    onSearch: () -> Unit = {},
    onOpenFolder: () -> Unit = {},
    onAddSavedItem: () -> Unit = {},
    onAddManualItem: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "보관함",
                style = LinkItTheme.typography.headline2Bold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 20.dp),
            )
            SearchBar(onClick = onSearch)
            FolderGrid(onOpenFolder = onOpenFolder)
        }

        if (uiState.isAddMenuVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LinkItTheme.color.semantic.material.dimmer.copy(alpha = .22f))
                    .clickable { onIntent(StorageIntent.DismissAddMenu) },
            )
            StorageAddMenu(
                onAddSavedItem = onAddSavedItem,
                onAddManualItem = onAddManualItem,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 88.dp),
            )
        }

        FloatingAddButton(
            expanded = uiState.isAddMenuVisible,
            onClick = { onIntent(StorageIntent.ToggleAddMenu) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
        )
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = LinkItTheme.color.semantic.line.normal.normal,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "검색어를 입력해주세요",
            style = LinkItTheme.typography.body1NormalRegular,
            color = LinkItTheme.color.semantic.label.assistive,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = LinkItIcon.Utility.Search,
            contentDescription = "보관함 검색",
            tint = LinkItTheme.color.semantic.label.assistive,
            modifier = Modifier.size(23.dp),
        )
    }
}

@Composable
private fun FolderGrid(onOpenFolder: () -> Unit) {
    val folders = listOf(
        FolderFixture("교통", 6),
        FolderFixture("식사", 5),
        FolderFixture("관광지", 3),
        FolderFixture("쇼핑", 4),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        items(folders) { folder ->
            FolderCard(folder = folder, onClick = onOpenFolder)
        }
        item { AddFolderTile() }
    }
}

@Composable
private fun FolderCard(folder: FolderFixture, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(LinkItTheme.color.semantic.background.normal.alternative),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = LinkItTheme.typography.caption1Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                )
                Text(
                    text = "${folder.count}개",
                    style = LinkItTheme.typography.caption1Regular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(
                imageVector = LinkItIcon.Utility.MoreHorizontal,
                contentDescription = "${folder.name} 더보기",
                tint = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AddFolderTile() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .border(
                    width = 1.dp,
                    color = com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens.Lavender80,
                    shape = RoundedCornerShape(6.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlusGlyph(24)
                Text(
                    text = "폴더 추가하기",
                    style = LinkItTheme.typography.body2NormalRegular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun StorageAddMenu(
    onAddSavedItem: () -> Unit,
    onAddManualItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(162.dp)
            .shadow(8.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(LinkItTheme.color.semantic.inverse.background)
            .padding(vertical = 7.dp),
    ) {
        AddMenuItem(LinkItIcon.Utility.Archive, "저장 항목 추가하기", onAddSavedItem)
        AddMenuItem(LinkItIcon.Control.Write, "직접 추가하기", onAddManualItem)
    }
}

@Composable
private fun AddMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.inverse.label,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = LinkItTheme.typography.label1NormalMedium,
            color = LinkItTheme.color.semantic.inverse.label,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

@Composable
private fun FloatingAddButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(LinkItTheme.color.semantic.inverse.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (expanded) {
            Icon(
                imageVector = LinkItIcon.Utility.Close,
                contentDescription = "추가 메뉴 닫기",
                tint = LinkItTheme.color.semantic.inverse.label,
                modifier = Modifier.size(24.dp),
            )
        } else {
            PlusGlyph(24, inverse = true)
        }
    }
}

@Composable
private fun PlusGlyph(size: Int, inverse: Boolean = false) {
    val color = if (inverse) LinkItTheme.color.semantic.inverse.label else LinkItTheme.color.semantic.label.alternative
    Canvas(Modifier.size(size.dp)) {
        val half = this.size.width / 2f
        val inset = this.size.width * .27f
        drawLine(color, androidx.compose.ui.geometry.Offset(inset, half), androidx.compose.ui.geometry.Offset(this.size.width - inset, half), strokeWidth = 1.6f)
        drawLine(color, androidx.compose.ui.geometry.Offset(half, inset), androidx.compose.ui.geometry.Offset(half, this.size.height - inset), strokeWidth = 1.6f)
    }
}

private data class FolderFixture(val name: String, val count: Int)
