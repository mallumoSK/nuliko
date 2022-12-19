package compose.assets.svg

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import compose.assets.Svg

public val Svg.Rotate: ImageVector
    get() {
        if (_rotate != null) {
            return _rotate!!
        }
        _rotate = Builder(name = "Rotate", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(6.35f, 6.35f)
                    curveTo(7.8f, 4.9f, 9.79f, 4.0f, 12.0f, 4.0f)
                    curveTo(16.42f, 4.0f, 19.99f, 7.58f, 19.99f, 12.0f)
                    curveTo(19.99f, 16.42f, 16.42f, 20.0f, 12.0f, 20.0f)
                    curveTo(8.27f, 20.0f, 5.16f, 17.45f, 4.27f, 14.0f)
                    lineTo(6.35f, 14.0f)
                    curveTo(7.17f, 16.33f, 9.39f, 18.0f, 12.0f, 18.0f)
                    curveTo(15.31f, 18.0f, 18.0f, 15.31f, 18.0f, 12.0f)
                    curveTo(18.0f, 8.69f, 15.31f, 6.0f, 12.0f, 6.0f)
                    curveTo(10.34f, 6.0f, 8.86f, 6.69f, 7.78f, 7.78f)
                    lineTo(11.0f, 11.0f)
                    lineTo(4.0f, 11.0f)
                    lineTo(4.0f, 4.0f)
                    lineTo(6.35f, 6.35f)
                    close()
                }
            }
        }
        .build()
        return _rotate!!
    }

private var _rotate: ImageVector? = null
