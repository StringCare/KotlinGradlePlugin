package components

import models.Configuration
import models.ResourceFile
import models.SAttribute
import models.StringEntity
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

fun locateFiles(projectPath: String, configuration: Configuration): List<ResourceFile> = File(projectPath).walkTopDown()
    .filterIndexed { _, file ->
        file.validForConfiguration(configuration)
    }.map { it.resourceFile(configuration)!! }.toList()

fun backupFiles(projectPath: String, configuration: Configuration): List<File> {
    val resourceFiles = mutableListOf<File>()
    val files = locateFiles(projectPath, configuration)
    files.forEach { resource ->
        resourceFiles.add(resource.backup(projectPath))
    }
    return resourceFiles
}

fun restoreFiles(projectPath: String): List<File> {
    val resourceFiles = File("$projectPath${File.separator}$resourceBackup").walkTopDown().toList()
    resourceFiles.filter { file ->
        !file.isDirectory
    }.map {
        it.restore(projectPath)
    }
    return resourceFiles
}

fun parseXML(file: File, module: String, debug: Boolean): List<StringEntity> {
    if (debug) {
        val content = file.inputStream().readBytes().toString(Charsets.UTF_8)
        PrintUtils.print(module, content, true)
    }
    val entities = mutableListOf<StringEntity>()
    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(file.inputStream())
    // reading player tags
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        var name = ""
        val attributes = mutableListOf<SAttribute>()
        for (a in 0 until node.attributes.length) {
            val attribute = node.attributes.item(a)
            for (n in 0 until attribute.childNodes.length) {
                val attr = attribute.childNodes.item(n)
                // can set value
                if (attribute.nodeName == "name") name = attr.nodeValue
                attributes.add(SAttribute(attribute.nodeName, attr.nodeValue))
            }
        }
        entities.add(StringEntity(name, attributes, node.firstChild.nodeValue))
    }
    return entities
}

fun obfuscate(mainModule: String, key: String, entity: StringEntity): StringEntity {
    val obfuscation = Stark.obfuscate(mainModule, key, entity.value.toByteArray()).toReadableString()
    return StringEntity(entity.name, entity.attributes, obfuscation)
}

fun reveal(mainModule: String, key: String, entity: StringEntity): StringEntity {
    val arr: ByteArray = entity.value.split(", ").map { it.toInt().toByte() }.toByteArray()
    val original = String(Stark.reveal(mainModule, key, arr))
    return StringEntity(entity.name, entity.attributes, original)
}
