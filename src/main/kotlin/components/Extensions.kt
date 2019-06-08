package components

import groovy.json.StringEscapeUtils
import models.Configuration
import models.Extension
import models.ResourceFile
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.io.File

fun String.runCommand(runner: (command: String, result: String) -> Unit = { _, _ -> }): String {
    val result = execute(this)
    runner(this, result)
    return result
}

fun String.escape(): String = Regex.escape(this)
fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
fun String.removeNewLines(): String = this.replace("\n", "")

fun File.validForConfiguration(configuration: Configuration): Boolean {
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains("/$folder/".replace("//", "/"))
            && !this.absolutePath.contains("/$resourceBackup/")) {
            valid = true
        }
    }
    if (valid) {
        valid = false
        configuration.stringFiles.forEach { file ->
            if (this.absolutePath.contains("/$file".replace("//", "/"))) {
                valid = true
            }
        }
    }
    return valid
}

fun File.resourceFile(configuration: Configuration): ResourceFile? {
    var sourceFolder = ""
    var validFile: File? = null
    var valid = false
    configuration.srcFolders.forEach { folder ->
        if (this.absolutePath.contains("/$folder/".replace("//", "/"))
            && !this.absolutePath.contains("/$resourceBackup/")
        ) {
            sourceFolder = folder
            valid = true
        }
    }
    if (valid) {
        valid = false
        configuration.stringFiles.forEach { file ->
            if (this.absolutePath.contains("/$file".replace("//", "/"))) {
                valid = true
                validFile = this
            }
        }
    }
    return if (valid) ResourceFile(validFile!!, sourceFolder, configuration.name) else null
}

fun Project.absolutePath(): String = this.file(wrapperWindows).absolutePath.replace(
    wrapperWindows,
    emptyChar
)

fun Project.createExtension(): Extension = this.extensions.create(extensionName, Extension::class.java)
fun Project.createConfiguration(): NamedDomainObjectContainer<Configuration> =
    this.container<Configuration>(Configuration::class.java)

fun Process.outputString() = this.inputStream.bufferedReader().use { it.readText() }

fun defaultConfig(): Configuration {
    return Configuration("app", listOf("strings.xml"), listOf("src${File.separator}main"))
}

fun ResourceFile.backup(projectPath: String): File {
    val cleanPath = "$projectPath${File.separator}$resourceBackup${File.separator}${this.module}" +
            "${File.separator}${this.sourceFolder}${this.file.absolutePath.split(this.sourceFolder)[1]}"
                .replace("${File.separator}${File.separator}", File.separator)

    val backupFile = File(cleanPath)
    this.file.copyTo(backupFile)
    return backupFile
}

fun File.restore(projectPath: String): File {
    val cleanPath = "$projectPath${File.separator}${this.absolutePath.split(resourceBackup)[1]}"
                .replace("${File.separator}${File.separator}", File.separator)

    val restore = File(cleanPath)
    this.copyTo(restore, true)
    return restore
}

fun ByteArray.toReadableString(): String {
    val builder = StringBuilder()
    this.forEachIndexed { index, byte ->
        if (index == this.size - 1) {
            builder.append(byte)
        } else {
            builder.append("$byte, ")
        }
    }
    return builder.toString()
}

