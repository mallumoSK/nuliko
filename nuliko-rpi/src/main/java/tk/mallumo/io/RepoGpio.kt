package tk.mallumo.io

import kotlinx.coroutines.*
import tk.mallumo.system.Gpio

class RepoGpio : ImplRepo() {
    private val gpioMap = mutableMapOf<Int, Gpio>()
    private val jobMap = mutableMapOf<Int, Job?>()

    override val scope: CoroutineScope = CoroutineScope(CoroutineName("gpio") + Dispatchers.IO)

    private fun getPin(id: Int): Gpio {
        return gpioMap.getOrPut(id) {
            Gpio.pin(
                address = id,
                mode = Gpio.Mode.OUT,
                initialState = Gpio.State.LOW
            ).apply {
                open()
            }
        }
    }

    fun pinSetup(id: Int, on: Boolean, durationMs: Int) {
        if (on) pinON(id, durationMs)
        else pinOFF(id)
    }

    private fun pinON(id: Int, durationMs: Int) = getPin(id).apply {
        jobMap[address]?.cancel()
        jobMap[address] = scope.launch {
            high()
            delay(durationMs.toLong())
            low()
        }
    }

    private fun pinOFF(id: Int) = getPin(id).apply {
        jobMap[address]?.cancel()
        low()
    }
}
