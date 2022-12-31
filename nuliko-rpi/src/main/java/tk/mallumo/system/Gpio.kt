package tk.mallumo.system

import tk.mallumo.log.logERROR
import tk.mallumo.utils.*
import java.io.Closeable

class Gpio private constructor(
    @Suppress("MemberVisibilityCanBePrivate") val address: Int,
    @Suppress("MemberVisibilityCanBePrivate") private var mode: Mode,
    private val initialState: State
) : Closeable {

    private val port = "gpio$address"

    sealed class State(private val value: Int) {
        object LOW : State(0)
        object HIGH : State(1)

        override fun toString(): String = value.toString()
    }

    sealed class Mode(private val value: String) {
        object IN : Mode("in")
        object OUT : Mode("out")

        override fun toString(): String = value
    }

    companion object {
        fun pin(address: Int, mode: Mode, initialState: State = State.LOW) = Gpio(address, mode, initialState)
    }

    fun low() {
        logERROR("GPIO $port OFF")

        shellSH {
            if (!isConnected()) throw IllegalStateException("not opened")
            run("echo \"${State.LOW}\" > /sys/class/gpio/$port/value")
        }

    }

    fun high() {
        logERROR("GPIO $port ON")
        shellSH {
            if (!isConnected()) throw IllegalStateException("not opened")
            run("echo \"${State.HIGH}\" > /sys/class/gpio/$port/value")
        }

    }


    fun Shell.setMode(mode: Mode) {
        if (!isConnected()) throw IllegalStateException("not opened")
        this@Gpio.mode = mode

        run("echo \"$initialState\" > /sys/class/gpio/$port/value")
        run("echo \"$mode\" > /sys/class/gpio/$port/direction")
    }

    fun open() {
        shellSH {
            connect()
            run("echo \"0\" > /sys/class/gpio/$port/active_low")
            setMode(mode)
            run("echo \"$initialState\" > /sys/class/gpio/$port/value")
        }
    }

    private fun Shell.connect() {
        if (!isConnected()) {
            run("echo $address > /sys/class/gpio/export")
        }
    }

    override fun close() {
        shellSH {
            disconnect()
        }
    }

    private fun Shell.isConnected(): Boolean {
        return run("ls /sys/class/gpio")
            .output
            .map { it.split(" ") }
            .flatten()
            .any { it == port }
    }

    private fun Shell.disconnect() {
        if (isConnected()) {
            run("echo $address > /sys/class/gpio/unexport")
        }
    }
}
