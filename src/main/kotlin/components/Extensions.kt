package components

import StringCare.*
import groovy.json.StringEscapeUtils
import models.ResourceFile
import org.apache.xerces.dom.DeferredElementImpl
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.plugins.DslObject
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun String.runCommand(runner: (command: String, result: String) -> Unit = { _, _ -> }): String {
    val result = execute(this)
    runner(this, result)
    return result
}

fun String.escape(): String = Regex.escape(this)
fun String.unescape(): String = StringEscapeUtils.unescapeJava(this)
fun String.removeNewLines(): String = this.replace("\n", "")
fun String.androidTreatment(): String {
    val va = this.split(" ")
    val values = mutableListOf<String>()
    va.forEach { value ->
        if (value.trim().isNotBlank()) {
            values.add(value.trim())
        }
    }
    return values.joinToString(separator = " ")
}

fun File.validForConfiguration(configuration: Configuration): Boolean {
    var valid = this.absolutePath.contains("/${configuration.name}/")
            && !this.absolutePath.contains("/$resourceBackup/")
    if (valid) {
        valid = false
        configuration.srcFolders.forEach { folder ->
            if (this.absolutePath.contains("/$folder/".replace("//", "/"))
                && !this.absolutePath.contains("/$resourceBackup/")
            ) {
                valid = true
            }
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
    return if (valid) ResourceFile(validFile!!, sourceFolder, configuration.name!!) else null
}

fun Project.absolutePath(): String = this.file(wrapperWindows).absolutePath.replace(
    wrapperWindows,
    emptyChar
)

fun Project.createExtension(): Extension {
    val extension = this.extensions.create(extensionName, Extension::class.java)
    extension.modules = this.container<Configuration>(Configuration::class.java)
    return extension
}

fun Process.outputString() = this.inputStream.bufferedReader().use { it.readText() }

fun defaultConfig(): Configuration {
    return Configuration("app").apply {
        stringFiles.add("strings.xml")
        srcFolders.add("src${File.separator}main")
    }
}

fun ResourceFile.backup(projectPath: String): File {
    val cleanPath = "$projectPath${File.separator}$resourceBackup${File.separator}${this.module}" +
            "${File.separator}${this.sourceFolder}${this.file.absolutePath.split(this.sourceFolder)[1]}"
                .replace("${File.separator}${File.separator}", File.separator)

    val backupFile = File(cleanPath)
    this.file.copyTo(backupFile, true)
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

fun File.getXML(): Document {
    val inputStream = FileInputStream(this)
    val reader = InputStreamReader(inputStream, "UTF-8")
    val inputSource = InputSource(reader)
    inputSource.encoding = "UTF-8"

    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    return docBuilder.parse(inputSource)
}

fun File.updateXML(document: Document) {
    val output = StringWriter()
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.transform(DOMSource(document), StreamResult(output))
    val xml = output.toString()
    FileWriter(this.absolutePath).use { it.write(xml) }
}

fun File.removeAttributes() {
    val content = this.getContent()
        .replace("hidden=\"true\"", "")
        .replace("hidden=\"false\"", "")
        .replace("containsHtml=\"true\"", "")
        .replace("containsHtml=\"false\"", "")
        .replace("androidTreatment=\"true\"", "")
        .replace("androidTreatment=\"false\"", "")
    FileWriter(this.absolutePath).use { it.write(content) }
    updateXML(this.getXML())
}


fun File.getContent() = this.inputStream().readBytes().toString(Charsets.UTF_8)

fun Task.getModuleName(): String? {
    val path = this.project.path
    return if (path.isEmpty()) null else path.split(":")[path.split(":").size - 1]
}

fun Task.dataFound(): Boolean = this.name.contains(pre)
        && this.name.contains(build)
        && this.name != "$pre$build"
        && !this.name.contains(test)

fun Task.onMergeResourcesStarts(): Boolean = this.name.contains(merge)
        && this.name.contains(resources)
        && !this.name.contains(test)

fun Task.onMergeResourcesFinish(): Boolean = this.name.contains(merge)
        && this.name.contains(resources)
        && !this.name.contains(test)

fun Task.dataFoundVariant(): String = this.name.substring(pre.length)
    .substring(0, this.name.substring(pre.length).length - build.length)

fun Task.onMergeResourcesStartsVariant(): String = this.name.substring(merge.length)
    .substring(0, this.name.substring(merge.length).length - resources.length)

fun Task.onMergeResourcesFinishVariant(): String = this.name.substring(merge.length)
    .substring(0, this.name.substring(merge.length).length - resources.length)

fun Node.extractHtml(): String {
    val stringBuilder = StringBuilder()
    for (i in 0 until this.childNodes.length) {
        val item = this.childNodes.item(i)
        val type = item.getType()
        when (type) {
            StringType.BR -> stringBuilder.append("<br>${item.textContent}</br>")
            StringType.I -> stringBuilder.append("<i>${item.textContent}</i>")
            StringType.STRONG -> stringBuilder.append("<strong>${item.textContent}</strong>")
            StringType.TEXT -> stringBuilder.append(item.textContent)
        }

    }
    return stringBuilder.toString()
}

enum class StringType {
    BR,
    TEXT,
    I,
    STRONG
}

fun Node.getType(): StringType {
    return when {
        this.toString().contains("[br:") -> StringType.BR
        this.toString().contains("[i:") -> StringType.I
        this.toString().contains("[strong:") -> StringType.STRONG
        this.toString().contains("[#text") -> StringType.TEXT
        else -> StringType.TEXT
    }
}

// [#text:
//        ]