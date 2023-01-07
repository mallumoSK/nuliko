package tk.mallumo.nuliko.android.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tk.mallumo.compose.navigation.viewmodel.NavigationViewModel
import tk.mallumo.nuliko.android.io.Repository

class PlayerVM : NavigationViewModel() {

    var activeCamera by mutableStateOf<CameraTab>(CameraTab.Brana)
        private set

    sealed class CameraTab(val index: Int, val id: Int, val name: String) {
        object Brana : CameraTab(0, 2, "Brana")
        object Vzadu : CameraTab(1, 1, "Vzadu")
    }

    @Composable
    fun collectUI() = Repository.player.collect()

    var rotation by mutableStateOf(360F)
        private set

    sealed interface Action {
        object Play : Action
        object Stop : Action
        object Rotate : Action
        class ActivateCameraTab(val tab: CameraTab) : Action
        companion object {


            @Composable
            fun remember(vm: PlayerVM): (Action) -> Unit {

                return androidx.compose.runtime.remember {
                    { act ->
                        when (act) {
                            Play -> vm.play()
                            Stop -> vm.stop()
                            Rotate -> vm.rotate()
                            is ActivateCameraTab -> vm.activateCameraTab(act.tab)
                        }
                    }
                }
            }
        }

    }

    private fun activateCameraTab(tab: CameraTab) {
        if (tab != activeCamera) {
            stop(true)
            activeCamera = tab
        }
    }

    private fun rotate() {
        rotation = if (rotation - 90F < 0F) 360F
        else rotation - 90
    }

    private fun stop(resetImage: Boolean = false) {
        Repository.player.stop(activeCamera.id, resetImage)
    }

    private fun play() {
        Repository.player.start(activeCamera.id)
    }

    override fun onRelease() {
        Repository.player.stop(activeCamera.id)
    }
}