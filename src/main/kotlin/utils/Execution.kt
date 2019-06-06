package utils

import groovy.json.StringEscapeUtils
import models.Configuration
import models.Extension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun Project.absolutePath(): String = this.file(wrapper).absolutePath.replace(wrapper, emptyChar)
fun Project.createExtension(): Extension = this.extensions.create(extensionName, Extension::class.java)
fun Project.createConfiguration(): NamedDomainObjectContainer<Configuration> =
    this.container<Configuration>(Configuration::class.java)

fun Process.outputString() = this.inputStream.bufferedReader().use { it.readText() }

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

fun String.run(runner: (command: String, result: String) -> Unit) {
    runner(this, execute(this))
}

fun String.escape(): String = Regex.escape(this)
fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
fun String.removeNewLines(): String = this.replace("\n", "")
