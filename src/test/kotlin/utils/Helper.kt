package utils

import components.extensionName
import components.getContent
import java.io.File
import java.io.FileWriter

fun modifyForTest(projectPath: String) {
    val file = File("$projectPath${File.separator}build.gradle")
    val content = file.getContent().replace(
        "classpath \"com.stringcare:plugin:\$stringcare_version\"",
        "\nclasspath files(\"..${File.separator}build${File.separator}libs${File.separator}plugin-2.3-SNAPSHOT.jar\")\n"
    )
    FileWriter(file.absolutePath).use { it.write(content) }

}