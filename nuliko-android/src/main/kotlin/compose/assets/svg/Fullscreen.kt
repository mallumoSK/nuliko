package compose.assets.svg

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import compose.assets.Svg

public val Svg.Fullscreen: ImageVector
    get() {
        if (_fullscreen != null) {
            return _fullscreen!!
        }
        _fullscreen = Builder(name = "Fullscreen", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(21.0f, 11.0f)
                lineToRelative(0.0f, -8.0f)
                lineToRelative(-8.0f, 0.0f)
                lineToRelative(3.29f, 3.29f)
                lineToRelative(-10.0f, 10.0f)
                lineToRelative(-3.29f, -3.29f)
                lineToRelative(0.0f, 8.0f)
                lineToRelative(8.0f, 0.0f)
                lineToRelative(-3.29f, -3.29f)
                lineToRelative(10.0f, -10.0f)
                close()
            }
        }
        .build()
        return _fullscreen!!
    }

private var _fullscreen: ImageVector? = null
