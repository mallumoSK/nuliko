package compose.assets

import androidx.compose.ui.graphics.vector.ImageVector
import compose.assets.svg.Alarm_bell
import compose.assets.svg.ArrowBack
import compose.assets.svg.Bulb
import compose.assets.svg.Clear
import compose.assets.svg.Fullscreen
import compose.assets.svg.Ladybug
import compose.assets.svg.PlayCircle
import compose.assets.svg.Reload
import compose.assets.svg.Rotate
import kotlin.collections.List as ____KtList

public object Svg

private var __All: ____KtList<ImageVector>? = null

public val Svg.All: ____KtList<ImageVector>
  get() {
    if (__All != null) {
      return __All!!
    }
    __All= listOf(PlayCircle, Fullscreen, Reload, Ladybug, Clear, Rotate, ArrowBack, Bulb,
        Alarm_bell)
    return __All!!
  }
