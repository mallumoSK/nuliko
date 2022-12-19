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

public val Svg.Reload: ImageVector
    get() {
        if (_reload != null) {
            return _reload!!
        }
        _reload = Builder(name = "Reload", defaultWidth = 256.676.dp, defaultHeight = 256.676.dp,
                viewportWidth = 256.676f, viewportHeight = 256.676f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(240.455f, 170.828f)
                lineToRelative(-40.0f, -29.0f)
                verticalLineTo(70.441f)
                horizontalLineTo(89.781f)
                verticalLineToRelative(30.439f)
                lineTo(19.221f, 50.441f)
                lineTo(89.781f, 0.0f)
                verticalLineToRelative(30.441f)
                horizontalLineToRelative(150.674f)
                verticalLineTo(170.828f)
                close()
                moveTo(237.455f, 206.236f)
                lineToRelative(-70.559f, -50.441f)
                verticalLineToRelative(30.441f)
                horizontalLineTo(56.221f)
                verticalLineTo(114.85f)
                lineToRelative(-40.0f, -29.0f)
                verticalLineToRelative(140.387f)
                horizontalLineToRelative(150.676f)
                verticalLineToRelative(30.44f)
                lineTo(237.455f, 206.236f)
                close()
            }
        }
        .build()
        return _reload!!
    }

private var _reload: ImageVector? = null
