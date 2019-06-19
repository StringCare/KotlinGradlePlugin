package task

import StringCare.Companion.absoluteProjectPath
import StringCare.Companion.moduleMap
import components.getContent
import components.locateFiles
import components.parseXML
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SCPreview : DefaultTask() {
    @TaskAction
    fun greet() {
        println("== REPORT ======================================")
        println("Modules (${moduleMap.size})")

        moduleMap.forEach { entry ->
            val files = locateFiles(absoluteProjectPath, entry.value)
            println("Located files(${files.size}) for obfuscating")
            files.forEach { file ->
                println("- ${file.file.name}")
                println("\t${file.module}${File.separator}${file.sourceFolder}${File.separator}")
                val entities = parseXML(file.file)
                println("path: ${file.file.absolutePath}")
                println("")
                println("============================")
                entities.forEach { entity ->
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

            }
        }
        println("== END REPORT ==================================")


    }
}
