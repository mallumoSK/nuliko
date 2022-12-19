package tk.mallumo.nuliko.android.common

import androidx.compose.ui.graphics.vector.*
import compose.assets.*
import compose.assets.svg.*

data class GpioHW(
    val name: String,
    val icon: ImageVector,
    val pinId: Int
) {
    companion object {
        val all by lazy {
            listOf(
                GpioHW(
                    name = "Svetlo 1",
                    icon = Svg.Bulb,
                    pinId = 23,
                ),
                GpioHW(
                    name = "Svetlo 2",
                    icon = Svg.Bulb,
                    pinId = 24,
                ),
                GpioHW(
                    name = "Sirena",
                    icon = Svg.Alarm_bell,
                    pinId = 18,
                )
            )
        }
    }
}
