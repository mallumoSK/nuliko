package tk.mallumo.nuliko.android.ui.player

import androidx.compose.runtime.Composable
import tk.mallumo.compose.navigation.LocalNavigation
import tk.mallumo.compose.navigation.viewmodel.viewModel

abstract class PlayerScope {
    abstract val vm: PlayerVM
    abstract val action: (PlayerVM.Action) -> Unit

    companion object {
        @Composable
        fun remember(): PlayerScope {
            val nav = LocalNavigation.current
            val vm = nav.viewModel<PlayerVM>()
            val action = PlayerVM.Action.remember(vm = vm)
            return androidx.compose.runtime.remember {
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