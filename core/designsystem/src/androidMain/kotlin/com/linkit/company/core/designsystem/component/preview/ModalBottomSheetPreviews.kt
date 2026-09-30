package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.sheet.LinkItModalBottomSheetDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * `ModalBottomSheet` 는 별도 창에 그려져 프리뷰에 잡히지 않으므로,
 * 컨테이너 모양(핸들·라운드·배경)만 같은 토큰으로 재현한다.
 */
@Preview(showBackground = true, backgroundColor = 0xFF6B7280, widthDp = 375)
@Composable
private fun LinkItModalBottomSheetContainerPreview() {
    LinkItTheme {
        Column(
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .clip(LinkItModalBottomSheetDefaults.shape)
                .background(LinkItModalBottomSheetDefaults.containerColor),
        ) {
            LinkItModalBottomSheetDefaults.Handle()
            Text(
                text = "시트 제목",
                style = LinkItTheme.typography.headline1Bold,
                color = LinkItTheme.color.semantic.label.normal,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            LinkItButton(
                onClick = {},
                text = "확인",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            )
        }
    }
}
