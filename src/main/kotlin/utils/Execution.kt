package utils

import models.Configuration
import models.Extension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.io.BufferedReader
import java.io.InputStreamReader

fun Project.absolutePath(): String = this.file(wrapper).absolutePath.replace(wrapper, emptyChar)
fun Project.createExtension(): Extension = this.extensions.create(extensionName, Extension::class.java)
fun Project.createConfiguration(): NamedDomainObjectContainer<Configuration> =
    this.container<Configuration>(Configuration::class.java)

fun execute(command: String, processing: (line: String) -> Unit = {}) {
    // Runtime.getRuntime().exec(command)
    val `is` = Runtime.getRuntime().exec(command).inputStream
    val isr = InputStreamReader(`is`)
    val buff = BufferedReader(isr)
    while (true) {
        val line = buff.readLine() ?: break
        processing(line)
        System.out.println(line)
    }
}
