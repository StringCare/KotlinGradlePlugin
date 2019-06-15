package components

import models.ExecutionResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

fun executeWith(runtime: Runtime, command: String): ExecutionResult = execute(runtime, command)

fun execute(command: String): ExecutionResult = execute(Runtime.getRuntime(), command)

private fun execute(runtime: Runtime, command: String): ExecutionResult {
    return try {
        when (getOs()) {
            Os.WINDOWS -> {
                ExecutionResult(
                    command.normalize(),
                    runtime.exec(
                        arrayOf(
                            "cmd",
                            "/c",
                            command.normalize()
                        )
                    ).outputString()
                )
            }
            Os.OSX -> {
                ExecutionResult(
                    command,
                    runtime.exec(
                        arrayOf(
                            "/bin/bash",
                            "-c",
                            command
                        )
                    ).outputString()
                )
            }
        }
    } catch (e: IOException) {
        ExecutionResult(command.normalize(), "")
    }
}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}

