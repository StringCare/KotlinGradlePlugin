package components

import StringCare.*
import models.AssetsFile
import java.io.File

fun locateAssetsFiles(projectPath: String, configuration: Configuration): List<AssetsFile> {
    if (configuration.debug) {
        println("== ASSETS FILES FOUND ======================================")
    }
    return File(projectPath).walkTopDown()
        .filterIndexed { _, file ->
            file.validForAssetsConfiguration(configuration.normalize())
        }.map {
            it.assetsFile(configuration.normalize())!!
        }.toList()
}
