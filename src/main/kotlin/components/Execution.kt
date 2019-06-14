package components

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

fun executeWith(runtime: Runtime, command: String): String = execute(runtime, command)

fun execute(command: String): String = execute(Runtime.getRuntime(), command)

private fun execute(runtime: Runtime, command: String): String {
    return try {
        when (getOs()) {
            Os.WINDOWS -> {
                val com = mutableListOf<String>()
                command.replace("\n", " ").split(" ").forEach {
                    if (it.trim().isNotEmpty()) {
                        com.add(it)
                    }
                }
                runtime.exec(arrayOf("cmd", "/c", com.joinToString(" ").replace("&&", "&"))).outputString()
            }
            Os.OSX -> {
                runtime.exec(
                    arrayOf(
                        "/bin/bash",
                        "-c",
                        command
                    )
                ).outputString()
            }
        }
    } catch (e: IOException) {
        ""
    }
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}

