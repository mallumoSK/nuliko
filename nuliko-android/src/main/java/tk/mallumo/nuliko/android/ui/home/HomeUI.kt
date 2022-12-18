package tk.mallumo.nuliko.android.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import tk.mallumo.compose.navigation.*

@Composable
@ComposableNavNode
fun HomeUI() {
    Row(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(2F)
        ) {
            NavigationChildPlayer(startupNode = Node.PlayerUI)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1F)
        ) {
            NavigationChildController(startupNode = Node.ControllerUI)
        }
    }
}
