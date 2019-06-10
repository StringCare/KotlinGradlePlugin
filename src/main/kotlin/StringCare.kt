import components.*
import models.Configuration
import models.Extension
import org.gradle.api.Plugin
import org.gradle.api.Project

class StringCare : Plugin<Project> {

    private lateinit var absoluteProjectPath: String
    private lateinit var project: Project
    private lateinit var extension: Extension
    private val moduleMap: MutableMap<String, Configuration> = mutableMapOf()


    override fun apply(target: Project) {
        this@StringCare.project = target

        extension = project.createExtension()
        absoluteProjectPath = project.absolutePath()

        this.project.afterEvaluate {
            extension.modules.forEach { module ->
                when {
                    module.stringFiles.isNotEmpty() && module.srcFolders.isNotEmpty() -> {
                        moduleMap[module.name!!] = Configuration(module.name).apply {
                            stringFiles.addAll(module.stringFiles)
                            srcFolders.addAll(module.srcFolders)
                        }
                    }
                    module.srcFolders.isNotEmpty() -> {
                        moduleMap[module.name!!] = Configuration(module.name).apply {
                            stringFiles.addAll(defaultConfig().stringFiles)
                            srcFolders.addAll(module.srcFolders)
                        }
                    }
                    module.stringFiles.isNotEmpty() -> {
                        moduleMap[module.name!!] = Configuration(module.name).apply {
                            stringFiles.addAll(module.stringFiles)
                            srcFolders.addAll(defaultConfig().srcFolders)
                        }
                    }
                }
            }
        }
        this.project.gradle.addBuildListener(ExecutionListener(
            debug = extension.debug,
            dataFound = { _, _ ->
                // nothing to do here
            }, mergeResourcesStart = { module, variant ->
                fingerPrint(module, variant, extension.debug) { key ->
                    if ("none" == key) {
                        return@fingerPrint
                    }
                    when {
                        moduleMap.containsKey(module) -> {
                            PrintUtils.print(module, "$variant:$key")
                            PrintUtils.print(module, backupStringRes)
                            moduleMap[module]?.let { configuration ->
                                backupFiles(absoluteProjectPath, configuration)
                            }
                            moduleMap[module]?.let { configuration ->
                                val files = locateFiles(absoluteProjectPath, configuration)
                                files.forEach { file ->
                                    modifyXML(file.file, extension.main_module, key, extension.debug)
                                }
                            }
                            PrintUtils.print(module, obfuscateStringRes)
                        }
                        else -> {
                            val defaultConfiguration = defaultConfig().apply {
                                name = module
                            }
                            PrintUtils.print(module, "$variant:$key")
                            PrintUtils.print(module, backupStringRes)
                            backupFiles(absoluteProjectPath, defaultConfiguration)
                            PrintUtils.print(module, obfuscateStringRes)
                            val files = locateFiles(absoluteProjectPath, defaultConfiguration)
                            files.forEach { file ->
                                modifyXML(file.file, extension.main_module, key, extension.debug)
                            }
                        }
                    }
                }

            }, mergeResourcesFinish = { module, _ ->
                restoreFiles(absoluteProjectPath, module)
            }
        ))
    }

}
