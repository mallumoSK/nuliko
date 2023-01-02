package tk.mallumo.nuliko.android.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import compose.assets.Svg
import compose.assets.svg.Fullscreen
import tk.mallumo.compose.navigation.*

@Composable
@ComposableNavNode
fun HomeUI() {
    var isFullscreen by remember {
        mutableStateOf(false)
    }
    val ctx = LocalContext.current
    val display = remember {
        ctx.resources.displayMetrics.let { res ->
            DpSize(
                width = (res.widthPixels.toFloat() / res.density).dp,
                height = (res.heightPixels.toFloat() / res.density).dp,
            )
        }
    }
    val part1of3 = remember {
        (display.width.value / 3F).dp
    }

    val part2of3 = remember {
        (part1of3.value * 2F).dp
    }

    val leftPanelWidth by animateDpAsState(
        if (isFullscreen) display.width
        else part2of3
    )
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(leftPanelWidth)
        ) {
            NavigationChildPlayer(startupNode = Node.PlayerUI)
            FloatingActionButton(
                onClick = { isFullscreen = !isFullscreen },
                modifier = Modifier.padding(18.dp)
                    .align(Alignment.BottomStart)
            ) {
                Icon(
                    imageVector = Svg.Fullscreen,
                    contentDescription = "Fullscreen"
                )
            }
        }

        AnimatedVisibility(
            visible = !isFullscreen,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(part1of3)
        ) {
            NavigationChildController(startupNode = Node.ControllerUI)
        }
    }
}
