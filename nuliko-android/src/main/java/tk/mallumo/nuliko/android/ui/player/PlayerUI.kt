package tk.mallumo.nuliko.android.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import compose.assets.Svg
import compose.assets.svg.PlayCircle
import compose.assets.svg.Rotate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import tk.mallumo.compose.navigation.ComposableNavNode
import tk.mallumo.log.logERROR
import tk.mallumo.nuliko.DataState
import tk.mallumo.utils.minute

@Composable
@ComposableNavNode
fun PlayerUI() {
    with(PlayerScope.remember()) {
        Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
            Content()
        }
    }
}

@Composable
fun PlayerScope.Content() {
    Column(Modifier.fillMaxSize()) {
        CameRaTabs()
        PlayerStates()
    }

}

@Composable
fun PlayerScope.PlayerStates() {
    val uiState by vm.collectUI()
    when (val state = uiState) {
        is DataState.Error -> ErrorState(state.message)
        is DataState.Idle -> IdleState(state.entry)
        is DataState.Loading -> LoadingState()
        is DataState.Result -> ResultState(state.nnvl)
    }
}

@Composable
fun PlayerScope.CameRaTabs() {
    fun consumeAction(item: PlayerVM.CameraTab) {
        action(PlayerVM.Action.ActivateCameraTab(item))
    }
    TabRow(
        selectedTabIndex = vm.activeCamera.id - 1,
        modifier = Modifier.fillMaxWidth(),
        tabs = {
            CamTab(PlayerVM.CameraTab.Cam1, ::consumeAction)
            CamTab(PlayerVM.CameraTab.Cam2, ::consumeAction)
        })
}

@Composable
fun PlayerScope.CamTab(item: PlayerVM.CameraTab, onClick: (PlayerVM.CameraTab) -> Unit) {
    Tab(
        selected = vm.activeCamera == item,
        onClick = { onClick(item) },
        text = {
            Text(item.name)
        })
}

@Composable
fun PlayerScope.IdleState(entry: ImageBitmap?) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { action(PlayerVM.Action.Play) }) {
        if (entry != null) {
            Image(
                bitmap = entry,
                contentScale = ContentScale.Fit,
                contentDescription = "src",
                modifier = Modifier
                    .rotate(vm.rotation)
                    .fillMaxSize()

            )
        }

        Icon(
            imageVector = Svg.PlayCircle,
            contentDescription = "PLAY",
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun PlayerScope.ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun PlayerScope.LoadingState() {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { action(PlayerVM.Action.Stop) }) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun PlayerScope.ResultState(nnvl: ImageBitmap) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable {
            action(PlayerVM.Action.Stop)
            logERROR("STOP")
        }) {

        Image(
            bitmap = nnvl,
            contentScale = ContentScale.Fit,
            contentDescription = "src",
            modifier = Modifier
                .rotate(vm.rotation)
                .fillMaxSize()

        )
        FloatingActionButton(
            onClick = { action(PlayerVM.Action.Rotate) },
            modifier = Modifier
                .padding(start = 96.dp)
                .padding(vertical = 18.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(imageVector = Svg.Rotate, contentDescription = "Rotate")
        }
    }

    LaunchedEffect(Unit) {
        delay(15.minute)
        if (isActive) action(PlayerVM.Action.Stop)
    }
}
