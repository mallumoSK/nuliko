package tk.mallumo.nuliko.android.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import compose.assets.*
import compose.assets.svg.*
import tk.mallumo.compose.navigation.*
import tk.mallumo.nuliko.android.ui.player.*

@Composable
@ComposableNavNode
fun HomeUI() {
    var isFullscreen by remember {
        mutableStateOf(false)
    }
    Row(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(2F)
        ) {
            NavigationChildPlayer(startupNode = Node.PlayerUI)
            FloatingActionButton(
                onClick = { isFullscreen = !isFullscreen },
                modifier = Modifier.padding(18.dp)
            ) {
                Icon(imageVector = Svg.Fullscreen, contentDescription = "Fullscreen")
            }
        }
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = expandHorizontally(),
            exit = shrinkHorizontally(),
            modifier = Modifier
                .fillMaxHeight()
                .weight(1F)
        ) {
            NavigationChildController(startupNode = Node.ControllerUI)
        }
    }
}
