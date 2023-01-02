package tk.mallumo.nuliko.android.ui.controller

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tk.mallumo.compose.navigation.ComposableNavNode
import tk.mallumo.compose.navigation.LocalNavigation
import tk.mallumo.compose.navigation.viewmodel.NavigationViewModel
import tk.mallumo.compose.navigation.viewmodel.viewModel
import tk.mallumo.nuliko.android.common.GpioHW
import tk.mallumo.nuliko.android.common.Space
import tk.mallumo.nuliko.android.io.Repository
import tk.mallumo.utils.second
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

interface ControllerScope {

    val vm: ControllerVM
    val action: (ControllerVM.Action) -> Unit

    companion object {

        @Composable
        fun remember(): ControllerScope {
            val nav = LocalNavigation.current
            val vm = nav.viewModel<ControllerVM>()
            val action = ControllerVM.Action.remember(vm)
            return remember {
                object : ControllerScope {
                    override val vm: ControllerVM
                        get() = vm
                    override val action: (ControllerVM.Action) -> Unit
                        get() = action

                }
            }
        }
    }
}

class ControllerVM : NavigationViewModel() {

    override fun onRelease() = Unit

    val uiState = mutableStateMapOf<Int, Long>()

    sealed class Action(val id: Int) {
        class Start(id: Int, val duration: Duration) : Action(id)
        class Stop(id: Int) : Action(id)

        companion object {
            @Composable
            fun remember(vm: ControllerVM): (Action) -> Unit {

                return remember {
                    { act ->
                        when (act) {
                            is Start -> {
                                vm.uiState[act.id] = SystemClock.elapsedRealtime() + act.duration.inWholeMilliseconds
                                Repository.gpio.start(act.id, act.duration)
                            }

                            is Stop -> {
                                vm.uiState.remove(act.id)
                                Repository.gpio.stop(act.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@ComposableNavNode
fun ControllerUI() {
    with(ControllerScope.remember()) {


        Column(modifier = Modifier.fillMaxSize()) {
            GpioHW.all.forEach {
                GpioCard(
                    item = it,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .weight(1F)
                )
            }
            Space(16.dp)
        }
    }

}


@Composable
fun ControllerScope.GpioCard(item: GpioHW, modifier: Modifier) {
    val activeUntil = vm.uiState[item.pinId] ?: 0

    val background by animateColorAsState(
        targetValue =
        if (activeUntil > SystemClock.elapsedRealtime()) MaterialTheme.colors.secondary
        else MaterialTheme.colors.primary
    )
    Surface(modifier = modifier, color = background) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Image(
                    imageVector = item.icon,
                    colorFilter = ColorFilter.tint(Color.DarkGray),
                    contentDescription = item.name,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = item.name,
                    color = Color.Black,
                    style = MaterialTheme.typography.h3
                )
            }
            if (activeUntil > 0) CardTimer(item.pinId, activeUntil)
            else CardActions(item.pinId)
        }
    }
}

@Composable
fun ControllerScope.CardActions(pinId: Int) {
    Box(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            FloatingActionButton(onClick = { action(ControllerVM.Action.Start(pinId, 1.minutes)) }) {
                Text(text = "1 min.", Modifier.padding(8.dp))
            }
            Space(horizontal = 16.dp)
            FloatingActionButton(onClick = { action(ControllerVM.Action.Start(pinId, 5.minutes)) }) {
                Text(text = "5 min.", Modifier.padding(8.dp))
            }
            Space(horizontal = 16.dp)
            FloatingActionButton(onClick = { action(ControllerVM.Action.Start(pinId, 10.minutes)) }) {
                Text(text = "10 min.", Modifier.padding(8.dp))
            }
            Space(horizontal = 16.dp)
            FloatingActionButton(onClick = { action(ControllerVM.Action.Start(pinId, 20.minutes)) }) {
                Text(text = "20 min.", Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun ControllerScope.CardTimer(pinId: Int, activeUntil: Long) {
    fun Duration.niceTimeText(): String = buildString {
        val minutes = inWholeSeconds / 60
        if (minutes == 0L) {
            append("$inWholeSeconds sec")
        } else {
            append("$minutes min ${inWholeSeconds - (minutes * 60)} sec")
        }
    }

    var timer by remember(pinId, activeUntil) {
        mutableStateOf((activeUntil - SystemClock.elapsedRealtime()).milliseconds.niceTimeText())
    }
    Row(Modifier.fillMaxSize()) {
        Text(
            text = timer,
            color = Color.Black,
            modifier = Modifier
                .weight(1F)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h4
        )
        Button(
            onClick = { action(ControllerVM.Action.Stop(pinId)) },
            modifier = Modifier.align(Alignment.CenterVertically),
        ) {
            Text(text = "STOP")
        }
    }
    LaunchedEffect(key1 = activeUntil) {
        delay(1.second)
        while (activeUntil > SystemClock.elapsedRealtime()) {
            timer = (activeUntil - SystemClock.elapsedRealtime()).milliseconds.niceTimeText()
            delay(1.second)
        }
        timer = 0.milliseconds.niceTimeText()
        delay(1.second)
        action(ControllerVM.Action.Stop(pinId))
    }
}
