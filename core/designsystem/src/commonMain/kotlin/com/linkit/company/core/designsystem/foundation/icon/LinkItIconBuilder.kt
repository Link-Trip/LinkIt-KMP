package com.linkit.company.core.designsystem.foundation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Figma 아이콘 세트("Pingo v3.0.3" - 1 Icon)를 ImageVector로 옮기기 위한 헬퍼.
 *
 * SVG `<path d>` 문자열을 [PathParser]로 직접 파싱해 수작업 변환 없이 ImageVector를 만든다.
 * 아이콘은 단색이며 fill 색상은 사용처에서 `Icon(tint = ...)`로 덮어쓴다.
 *
 * @param widthDp 아이콘 고유 너비. Tight 계열처럼 가로 폭이 좁은 아이콘은 [viewportWidth]와 함께 12로 지정한다.
 * @param viewportWidth SVG `viewBox`의 너비. 대부분 24이며, Tight 계열은 12다.
 */
internal fun linkItIcon(
    name: String,
    widthDp: Float = 24f,
    heightDp: Float = 24f,
    viewportWidth: Float = 24f,
    viewportHeight: Float = 24f,
    block: ImageVector.Builder.() -> Unit,
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = widthDp.dp,
        defaultHeight = heightDp.dp,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    ).apply(block).build()

/**
 * SVG path `d` 문자열을 ImageVector path로 추가한다.
 *
 * @param pathData SVG `<path d="...">` 값
 * @param pathFillType SVG `fill-rule="evenodd"`면 [PathFillType.EvenOdd], 그 외 [PathFillType.NonZero]
 */
internal fun ImageVector.Builder.addPath(
    pathData: String,
    pathFillType: PathFillType = PathFillType.NonZero,
    fill: Color = Color.Black,
) {
    addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        pathFillType = pathFillType,
        fill = SolidColor(fill),
    )
}
