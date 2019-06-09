package components

import models.Configuration
import models.ResourceFile
import models.SAttribute
import models.StringEntity
import java.io.File

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
        PrintUtils.print(module, file.getContent(), true)
    }
    val entities = mutableListOf<StringEntity>()

    val doc = file.getXML()
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        var name = ""
        val attributes = mutableListOf<SAttribute>()
        var obfuscate = false
        for (a in 0 until node.attributes.length) {
            val attribute = node.attributes.item(a)
            for (n in 0 until attribute.childNodes.length) {
                val attr = attribute.childNodes.item(n)
                // can set value
                if (attribute.nodeName == "name") name = attr.nodeValue
                if (attribute.nodeName == "hidden" && attr.nodeValue != "false") {
                    obfuscate = true
                }
                attributes.add(SAttribute(attribute.nodeName, attr.nodeValue))
            }
        }
        if (obfuscate) {
            entities.add(StringEntity(name, attributes, node.firstChild.nodeValue, "string", i))
        }
    }
    return entities
}

fun modifyXML(file: File, module: String, key: String, debug: Boolean) {
    val stringEntities = parseXML(file, module, debug)
    if (debug) {
        PrintUtils.print(module, file.getContent(), true)
    }

    val doc = file.getXML()
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        val entity = stringEntities.find {
            it.tag == "string" && it.index == i
        }
        entity?.let {
            node.firstChild.nodeValue = obfuscate(module, key, it).value
        }
    }

    file.updateXML(doc)
    file.removeHiddenAttributes()
}

fun obfuscate(mainModule: String, key: String, entity: StringEntity): StringEntity {
    val obfuscation = Stark.obfuscate(mainModule, key, entity.value.toByteArray()).toReadableString()
    return StringEntity(entity.name, entity.attributes, obfuscation, entity.tag, entity.index)
}

fun reveal(mainModule: String, key: String, entity: StringEntity): StringEntity {
    val arr: ByteArray = entity.value.split(", ").map { it.toInt().toByte() }.toByteArray()
    val original = String(Stark.reveal(mainModule, key, arr))
    return StringEntity(entity.name, entity.attributes, original, entity.tag, entity.index)
}

