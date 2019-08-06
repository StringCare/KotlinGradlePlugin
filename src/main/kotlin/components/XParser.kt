package components

import StringCare.*
import models.ResourceFile
import models.SAttribute
import models.StringEntity
import java.io.File

fun locateFiles(projectPath: String, configuration: Configuration): List<ResourceFile> {
    if (configuration.debug) {
        println("== FILES FOUND ======================================")
    }
    return File(projectPath).walkTopDown()
        .filterIndexed { _, file ->
            file.validForConfiguration(configuration.normalize())
        }.map {
            it.resourceFile(configuration.normalize())!!
        }.toList()
}

fun backupFiles(projectPath: String, configuration: Configuration): List<ResourceFile> {
    val files = locateFiles(projectPath, configuration.normalize())
    files.forEach { resource ->
        resource.backup()
    }
    return files
}

fun restoreFiles(projectPath: String, module: String): List<File> {
    val resourceFiles = File("${StringCare.tempFolder}${File.separator}$module")
        .walkTopDown().toList().filter { file ->
            !file.isDirectory
        }.map {
            it.restore(projectPath)
        }
    StringCare.resetFolder()
    return resourceFiles
}

fun parseXML(file: File): List<StringEntity> {
    val entities = mutableListOf<StringEntity>()

    val doc = file.getXML()
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        var name = ""
        val attributes = mutableListOf<SAttribute>()
        var obfuscate = false
        var androidTreatment = true
        var containsHtml = false
        for (a in 0 until node.attributes.length) {
            val attribute = node.attributes.item(a)
            for (n in 0 until attribute.childNodes.length) {
                val attr = attribute.childNodes.item(n)
                if (attribute.nodeName == "name") name = attr.nodeValue
                if (attribute.nodeName == "hidden" && attr.nodeValue != "false") {
                    obfuscate = true
                }
                if (attribute.nodeName == "androidTreatment" && attr.nodeValue == "false") {
                    androidTreatment = false
                }
                if (attribute.nodeName == "containsHtml" && attr.nodeValue != "false") {
                    containsHtml = true
                }
                attributes.add(SAttribute(attribute.nodeName, attr.nodeValue))
            }
        }
        if (obfuscate) {
            entities.add(
                StringEntity(
                    name, attributes, when {
                        containsHtml -> node.extractHtml()
                        else -> node.textContent
                    }, "string", i, androidTreatment
                )
            )
        }
    }
    return entities
}

fun modifyXML(file: File, mainModule: String, key: String, debug: Boolean, mockId: String = "") {
    val stringEntities = parseXML(file)
    if (debug) {
        PrintUtils.print(null, file.getContent(), true)
    }

    val doc = file.getXML()
    val nList = doc.getElementsByTagName("string")
    for (i in 0 until nList.length) {
        val node = nList.item(i)
        val entity = stringEntities.find {
            it.tag == "string" && it.index == i
        }
        entity?.let {
            node.textContent = obfuscate(mainModule, key, it, mockId).value
        }
    }

    file.updateXML(doc)
    file.removeAttributes()
    if (debug) {
        PrintUtils.print(null, file.getContent(), true)
    }
}

fun obfuscate(mainModule: String, key: String, entity: StringEntity, mockId: String = ""): StringEntity {
    val obfuscation = Stark.obfuscate(
        mainModule, key, when (entity.androidTreatment) {
            true -> entity.value.androidTreatment()
            false -> entity.value.unescape()
        }.toByteArray(),
        mockId
    ).toReadableString()
    return StringEntity(entity.name, entity.attributes, obfuscation, entity.tag, entity.index, entity.androidTreatment)
}

fun reveal(mainModule: String, key: String, entity: StringEntity): StringEntity {
    val arr: ByteArray = entity.value.split(", ").map { it.toInt().toByte() }.toByteArray()
    val original = String(Stark.reveal(mainModule, key, arr))
    return StringEntity(entity.name, entity.attributes, original, entity.tag, entity.index, entity.androidTreatment)
}

