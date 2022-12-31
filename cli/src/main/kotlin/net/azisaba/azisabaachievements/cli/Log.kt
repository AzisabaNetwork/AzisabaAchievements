package net.azisaba.azisabaachievements.cli

import net.azisaba.azisabaachievements.api.Logger
import java.io.OutputStream
import java.io.PrintStream

object Log : Logger {
    private val REAL_STDOUT : PrintStream = System.out

    init {
        System.setOut(LoggedPrintStream(System.out, ::info))
        System.setErr(LoggedPrintStream(System.out, ::error))
    }

    private fun format(msg: String, vararg args: Any?): String =
        msg.replace("{}", "%s").format(*args)

    fun debug(message: String) {
        REAL_STDOUT.println("${Colors.GRAY}[DEBUG] $message${Colors.RESET}")
    }

    override fun info(message: String) {
        REAL_STDOUT.println("${Colors.WHITE}[INFO] $message${Colors.RESET}")
    }

    override fun info(message: String, p1: Any?) {
        info(format(message, p1))
    }

    override fun info(message: String, p1: Any?, p2: Any?) {
        info(format(message, p1, p2))
    }

    override fun info(message: String, vararg params: Any?) {
        info(format(message, *params))
    }

    override fun info(message: String, throwable: Throwable?) {
        info(message)
        throwable?.printStackTrace()
    }

    override fun warn(message: String) {
        REAL_STDOUT.println("${Colors.YELLOW}[WARN] $message${Colors.RESET}")
    }

    override fun warn(message: String, p1: Any?) {
        warn(format(message, p1))
    }

    override fun warn(message: String, p1: Any?, p2: Any?) {
        warn(format(message, p1, p2))
    }

    override fun warn(message: String, vararg params: Any?) {
        warn(format(message, *params))
    }

    override fun warn(message: String, throwable: Throwable?) {
        warn(message)
        throwable?.printStackTrace()
    }

    override fun error(message: String) {
        REAL_STDOUT.println("${Colors.RED}[ERROR] $message${Colors.RESET}")
    }

    override fun error(message: String, p1: Any?) {
        error(format(message, p1))
    }

    override fun error(message: String, p1: Any?, p2: Any?) {
        error(format(message, p1, p2))
    }

    override fun error(message: String, vararg params: Any?) {
        error(format(message, *params))
    }

    override fun error(message: String, throwable: Throwable?) {
        error(message)
        throwable?.printStackTrace()
    }

    private class LoggedPrintStream(
        outputStream: OutputStream,
        private val log: (String) -> Unit,
    ) : PrintStream(outputStream) {
        override fun println(x: String?) {
            log(x.toString())
        }

        override fun println(x: Any?) {
            log(x.toString())
        }
    }
}
