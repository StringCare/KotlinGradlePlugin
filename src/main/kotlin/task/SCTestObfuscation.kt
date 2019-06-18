package task

import StringCare.Companion.absoluteProjectPath
import StringCare.Companion.moduleMap
import components.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SCTestObfuscation : DefaultTask() {

    @Input
    var variant: String? = null

    @Input
    var module: String? = null

    @Input
    var debug: Boolean? = null

    @TaskAction
    fun greet() {
        println("== TEST OBFUSCATION ======================================")
        println("Modules (${moduleMap.size})")

        moduleMap.forEach { entry ->
            var key = ""
            signingReportTask().runCommand { _, result ->
                key = result.extractFingerprint(entry.value.name!!, variant ?: defaultVariant, debug ?: defaultDebug)
            }
            println("fingerprint: $key")
            println("variant: ${variant ?: "debug"}")
            val filesToObfuscate = backupFiles(absoluteProjectPath, entry.value)
            filesToObfuscate.forEach { file ->
                val originalEntities = parseXML(file.file)
                println("============================")
                println("path: ${file.file.absolutePath}")
                originalEntities.forEach { entity ->
                    entity.attributes.forEach { attribute ->
                        println("\"${attribute.name}\": \"${attribute.value}\"")
                    }
                    println("\"value\": \"${entity.value}\"")
                    println("============================")
                }
                println("")
                println("=== content ================")
                println(file.file.getContent())
                println("============================")
                modifyXML(file.file, module!!, key, true)
                println("=== content obfuscated ================")
                println(file.file.getContent())
                println("============================")
            }
            restoreFiles(absoluteProjectPath, entry.value.name!!)
        }
        println("== END OBFUSCATION ==================================")


    }
}
