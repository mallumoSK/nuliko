package tk.mallumo.nuliko.android.ui.player

import android.widget.ImageButton
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import compose.assets.*
import compose.assets.svg.*
import tk.mallumo.compose.navigation.*
import tk.mallumo.compose.navigation.viewmodel.*
import tk.mallumo.log.*
import tk.mallumo.nuliko.*
import tk.mallumo.nuliko.android.io.*

abstract class PlayerScope {
    abstract val vm: PlayerVM
    abstract val action: (PlayerVM.Action) -> Unit

    companion object {
        @Composable
        fun remember(): PlayerScope {
            val nav = LocalNavigation.current
            val vm = nav.viewModel<PlayerVM>()
            val action = PlayerVM.Action.remember(vm = vm)
            return remember {
                object : PlayerScope() {
                    override val vm: PlayerVM
                        get() = vm
                    override val action: (PlayerVM.Action) -> Unit
                        get() = action

                }
            }
        }
    }
}

class PlayerVM : NavigationViewModel() {

    @Composable
    fun collectUI() = Repository.player.collect()

    var rotation by mutableStateOf(0F)
        private set

    sealed interface Action {
        object Play : Action
        object Stop : Action
        object Rotate : Action

        companion object {


            @Composable
            fun remember(vm: PlayerVM): (Action) -> Unit {

                return remember {
                    { act ->
                        when (act) {
                            Play -> vm.play()
                            Stop -> vm.stop()
                            Rotate -> vm.rotate()
                        }
                    }
                }
            }
        }

    }

    private fun rotate() {
        rotation = if (rotation + 90F > 360F) 0F
        else rotation + 90
    }

    private fun stop() {
        Repository.player.stop()
    }

    private fun play() {
        Repository.player.start()
    }

    override fun onRelease() {
        Repository.player.stop()
    }
}

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
    val uiState by vm.collectUI()
    when (val state = uiState) {
        is DataState.Error -> ErrorState(state.message)
        is DataState.Idle -> IdleState()
        is DataState.Loading -> LoadingState()
        is DataState.Result -> ResultState(state.nnvl)
    }
}

@Composable
fun PlayerScope.IdleState() {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { action(PlayerVM.Action.Play) }) {
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
    Box(modifier = Modifier.fillMaxSize()) {
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
            modifier = Modifier.padding(start = 96.dp, top = 18.dp)
        ) {
            Icon(imageVector = Svg.Rotate, contentDescription = "Rotate")
        }
    }
}
