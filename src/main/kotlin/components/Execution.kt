package components

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

fun executeWith(runtime: Runtime, command: String): String = execute(runtime, command)

fun execute(command: String): String = execute(Runtime.getRuntime(), command)

private fun execute(runtime: Runtime, command: String): String {
    return try {
        runtime.exec(
            arrayOf(
                "/bin/bash",
                "-c",
                command
            )
        ).outputString()
    } catch (e: IOException) {
        ""
    }
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}

