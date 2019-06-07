package components

import models.Configuration
import java.io.File

fun locateFiles(projectPath: String, configuration: Configuration): List<File> = File(projectPath).walkTopDown()
    .filterIndexed { _, file ->
        file.validForConfiguration(configuration)
    }.toList()
